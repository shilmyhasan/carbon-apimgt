package org.wso2.carbon.apimgt.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.UsageRecord;
import com.stripe.net.RequestOptions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.Monetization;
import org.wso2.carbon.apimgt.api.model.MonetizedSubscription;
import org.wso2.carbon.apimgt.impl.caching.MonetizationConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class MonetizationImpl implements Monetization {

    private static final Log log = LogFactory.getLog(MonetizationImpl.class);
    private static int publishIntervalInHours = 24 ;
    private static Long hoursToMilliseconds = 60*60*1000L;
    Boolean retry = true;

    @Override
    public Response publishMonetizationUsageRecord() {
        String apiName = null;
        String apiVersion = null;
        String tenantDomain = null;
        int applicationId;
        String apiProvider = null;
        Long requestCount = 0L;
        String ConnectId = "acct_1EQF7PCxKhMnrBL5";
        String lastPublishedTimeStamp;
        Long currentTimestamp;
        int flag = 0;
        int counter = 0;

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Timestamp current = new Timestamp(System.currentTimeMillis());
        currentTimestamp = getLocalTimestamp(current.toString());

        try{
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry();
            Resource resource = registry.get(MonetizationConstants.UsagePublisher.LAST_PUBSLISHH_TIME_REG_LOCATION);
            if(resource.getContent()!= null) {
                lastPublishedTimeStamp = RegistryUtils.decodeBytes((byte[]) resource.getContent());
            } else{
                //TODO find a proper way to get the lastupdated time
                Timestamp lastupdated = new Timestamp(System.currentTimeMillis() -
                        (publishIntervalInHours*hoursToMilliseconds));
                lastPublishedTimeStamp = lastupdated.toString();
            }
            PrivilegedCarbonContext.endTenantFlow();
        }catch(RegistryException ex){
            String msg = "Could not derive last published time , Error while obtaining registry objects";
            log.error(msg, ex);

        }

        StringBuilder query = new StringBuilder(
                "from " + MonetizationConstants.UsagePublisher.USAGE_RECORD_AGGREGATION
                        + " within " + lastPublishedTimeStamp
                        + "L, " + currentTimestamp + "L per '" + MonetizationConstants.UsagePublisher.GRANULARITY
                        + "' select "
                        + MonetizationConstants.API_NAME + ", "
                        + MonetizationConstants.API_VERSION + ", "
                        + MonetizationConstants.API_PROVIDER + ", "
                        + MonetizationConstants.API_PROVIDER_TENANT_DOMAIN + ", "
                        + MonetizationConstants.APPLICATION_ID + ", "
                        + "sum (requestCount) as requestCount "
                        + "group by "
                        + MonetizationConstants.API_NAME + ", "
                        + MonetizationConstants.API_VERSION + ", "
                        + MonetizationConstants.API_PROVIDER + ", "
                        + MonetizationConstants.API_PROVIDER_TENANT_DOMAIN + ", "
                        + MonetizationConstants.APPLICATION_ID
        );

        log.info("Usage record publisher is running");
        SubscriptionItem subscriptionItem = null;
        try {
            JSONObject jsonObj = APIUtil.executeQueryOnStreamProcessor(
                    MonetizationConstants.UsagePublisher.USAGE_RECORD_SIDDHI_APP,
                    query.toString());
            if (jsonObj != null) {
                JSONArray jArray = (JSONArray) jsonObj.get(MonetizationConstants.UsagePublisher.RECORDS_DELIMITER);
                for (Object record : jArray) {
                    JSONArray recordArray = (JSONArray) record;
                    if (recordArray.size() == 6) {
                        apiName = (String) recordArray.get(0);
                        apiVersion = (String) recordArray.get(1);
                        apiProvider = (String) recordArray.get(2);
                        tenantDomain = (String) recordArray.get(3);
                        applicationId = Integer.parseInt((String)recordArray.get(4));
                        requestCount = (Long) recordArray.get(5);
                        MonetizedSubscription subscription = apiMgtDAO.getMonetizedSubscription(apiName, apiVersion,
                                apiProvider, applicationId, tenantDomain);

                        Stripe.apiKey=getPlatformAccountKey(tenantDomain);
                        if(subscription.getSubscriptionId() != null) {

                            RequestOptions subRequestOptions = RequestOptions.builder().
                                    setStripeAccount(ConnectId).build();
                            Subscription sub = Subscription.retrieve(subscription.getSubscriptionId(),
                                    subRequestOptions);
                            subscriptionItem = sub.getItems().getData().get(0);

                            //check whether the billing plan is Usage Based.
                            if (subscriptionItem.getPlan().getUsageType().equals(
                                    MonetizationConstants.Stripe.METERED_PLAN)) {
                                flag++;
                                Map<String, Object> usageRecordParams = new HashMap<String, Object>();
                                usageRecordParams.put(MonetizationConstants.QUANTITY, requestCount);
                                usageRecordParams.put(MonetizationConstants.TIMESTAMP,
                                        getLocalTimestamp(current.toString()) / 1000);
                                usageRecordParams.put(MonetizationConstants.Stripe.ACTION,
                                        MonetizationConstants.Stripe.INCREMENT);
                                RequestOptions usageRequestOptions = RequestOptions.builder().
                                        setStripeAccount(ConnectId).setIdempotencyKey(subscriptionItem.getId() +
                                        lastPublishedTimeStamp.toString()).build();
                                UsageRecord usageRecord = UsageRecord.createOnSubscriptionItem(
                                        subscriptionItem.getId(), usageRecordParams, usageRequestOptions);
                                if (usageRecord.getId() != null) {
                                    counter++;
                                    if (log.isDebugEnabled()) {
                                        String msg = "Usage for "+ apiName+ " by Application with ID " + applicationId +
                                           "is successfully published to Stripe";
                                        log.info(msg);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (APIManagementException ex) {
            String msg = "Unable to Publish usage Record";
            log.error(msg);

        } catch (StripeException ex) {
            String msg = "Unable to Publish usage Record";
            log.error(msg);
        }

        if(flag == counter){
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
                Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry();
                Resource resource = registry.newResource();
                resource.setContent(currentTimestamp.toString());
                registry.put(MonetizationConstants.UsagePublisher.LAST_PUBSLISHH_TIME_REG_LOCATION, resource);
                PrivilegedCarbonContext.endTenantFlow();
            }catch(RegistryException ex){
                String msg= "Could not update last published time , Registry Objects could not be found";
                log.error(msg,ex);
            }
        } else if(counter > 0 && counter < flag){

        }
        return Response.ok().entity("log").build();
    }

  /*  public String getPlatformAccountStripeKey(String tenantDomain) throws APIManagementException{
        String stripePlatformAccountKey = null;
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try{
            Registry configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry(
                    tenantId);
            if (configRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                Resource resource = configRegistry.get(APIConstants.API_TENANT_CONF_LOCATION);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());

                if (StringUtils.isBlank(content)) {
                    String errorMessage = "Tenant configuration cannot be empty when configuring monetization.";
                    throw new APIManagementException(errorMessage);
                }
                //get the stripe key of patform account from tenant conf file
                JSONObject tenantConfig = (JSONObject) new JSONParser().parse(content);
                JSONObject monetizationInfo = (JSONObject) tenantConfig.get("MonetizationInfo");
                stripePlatformAccountKey = monetizationInfo.get("PlatformAccountStripeKey").toString();

                if (StringUtils.isBlank(stripePlatformAccountKey)) {
                    throw new APIManagementException("stripePlatformAccountKey is empty!!!");
                }
            }
        }catch (RegistryException ex) {
            throw new APIManagementException("Could not get all registry objects : "+ex.getMessage());
        }catch (org.json.simple.parser.ParseException ex) {
            throw new APIManagementException("Could not get Stripe Platform key : "+ex.getMessage());
        }
        return stripePlatformAccountKey;
    }*/

    /**
     * This method is used to get stripe platform account key for a given tenant
     *
     * @param tenantDomain tenant domain
     * @return stripe platform account key for the given tenant
     * @throws APIManagementException if it fails to get stripe platform account key for the given tenant
     */
    private String getPlatformAccountKey(String tenantDomain) throws APIManagementException {

        try {
            int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            Registry configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getConfigSystemRegistry(tenantId);

            if (configRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                Resource resource = configRegistry.get(APIConstants.API_TENANT_CONF_LOCATION);
                String tenantConfContent = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                if (StringUtils.isBlank(tenantConfContent)) {
                    String errorMessage = "Tenant configuration for tenant " + tenantDomain +
                            " cannot be empty when configuring monetization.";
                    APIUtil.handleException(errorMessage);
                }

                //get the stripe key of platform account from  tenant conf json file
                JSONObject tenantConfig = (JSONObject) new JSONParser().parse(tenantConfContent);
                JSONObject monetizationInfo = (JSONObject) tenantConfig.get(MonetizationConstants.MONETIZATION_INFO);
                String stripePlatformAccountKey = monetizationInfo.get
                        (MonetizationConstants.Stripe.PLATFORM_ACCOUNT_STRIPE_KEY).toString();

                if (StringUtils.isBlank(stripePlatformAccountKey)) {
                    String errorMessage = "Stripe platform account key is empty for tenant : " + tenantDomain;
                    APIUtil.handleException(errorMessage);
                }
                return stripePlatformAccountKey;
            }
        } catch (RegistryException e) {
            String errorMessage = "Failed to get the configuration registry for tenant :  " + tenantDomain;
            APIUtil.handleException(errorMessage, e);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    private long getTimestamp(String date){
        SimpleDateFormat formatter = new SimpleDateFormat(MonetizationConstants.TIMESTAMP_PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = 0;
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(date);
            time = parsedDate.getTime();
        } catch (ParseException e) {
            log.error("Error while parsing the date ", e);
        }
        return time;
    }

    private static long getLocalTimestamp(String date){
        SimpleDateFormat formatter = new SimpleDateFormat(MonetizationConstants.TIMESTAMP_PATTERN);
        formatter.setTimeZone(TimeZone.getDefault());
        long time = 0;
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(date);
            time = parsedDate.getTime();
        } catch (ParseException e) {

        }
        return time;
    }
}
