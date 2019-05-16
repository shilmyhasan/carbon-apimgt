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
import org.wso2.carbon.apimgt.api.model.Usage;
import org.wso2.carbon.apimgt.impl.caching.MonetizationConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class MonetizationUsageRecordPublisher extends TimerTask implements Monetization  {

    private static final Log log = LogFactory.getLog(MonetizationUsageRecordPublisher.class);
    private static int publishIntervalInHours = 24 ;
    private static Long hoursToMilliseconds = 60*60*1000L;
    Boolean retry = true;

    @Override
    public void run(){

        try {
            this.publishMonetizationUsageRecord();
        }catch (APIManagementException ex)
        {
            log.error("Error in publishing monetization usage data : " + ex.getLocalizedMessage());
        }
    }

    public void startMonetizationUsageRecordPublisher()
    {
        String numberOfHoursString;
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        if((numberOfHoursString = configuration.getFirstProperty("Monetization.UsagePublishIntervalInHours"))
                != null){
            publishIntervalInHours = Integer.parseInt(numberOfHoursString);
        }

        Long period = publishIntervalInHours*hoursToMilliseconds;
        Long delay = period;
        new Timer().schedule(this , delay, period);
    }

    @Override
    public void publishMonetizationUsageRecord() throws APIManagementException {
        String apiName = null;
        String apiVersion = null;
        String tenantDomain = null;
        int applicationId;
        String apiProvider = null;
        Long requestCount = 0L;
        boolean transactionCommitted = false;
        String ConnectId = "acct_1EQF7PCxKhMnrBL5";
        String lastUpdatedTimeStamp;
        Long currentTimestamp;

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");

        Timestamp current = new Timestamp(System.currentTimeMillis());
        currentTimestamp = getLocalTimestamp(current.toString());

        try{
           Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry();
           Resource resource = registry.get(MonetizationConstants.UsagePublisher.LAST_PUBSLISHH_TIME_REG_LOCATION);
           if(resource.getContent()!= null) {
               lastUpdatedTimeStamp = RegistryUtils.decodeBytes((byte[]) resource.getContent());
           } else{
               Timestamp lastupdated = new Timestamp(System.currentTimeMillis() -
                       (publishIntervalInHours*hoursToMilliseconds));
               lastUpdatedTimeStamp = lastupdated.toString();
           }
        }catch(RegistryException ex){
            String msg = "Error while obtaining registry objects";
            log.error(msg, ex);
            throw new APIManagementException(msg, ex);
        }

         StringBuilder query = new StringBuilder(
                "from " + MonetizationConstants.UsagePublisher.USAGE_RECORD_AGGREGATION
                        + " within " + lastUpdatedTimeStamp
                        + "L, " + currentTimestamp + "L per '" + MonetizationConstants.UsagePublisher.GRANULARITY
                        + "' select "
                        + MonetizationConstants.API_NAME + ", "
                        + MonetizationConstants.API_VERSION + ", "
                        + MonetizationConstants.API_PROVIDER + ", "
                        + MonetizationConstants.API_CREATOR_TENANT_DOMAIN + ", "
                        + MonetizationConstants.APPLICATION_ID + ", "
                        + "sum (requestCount) as requestCount "
                        + "group by "
                        + MonetizationConstants.API_NAME + ", "
                        + MonetizationConstants.API_VERSION + ", "
                        + MonetizationConstants.API_PROVIDER + ", "
                        + MonetizationConstants.API_CREATOR_TENANT_DOMAIN + ", "
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
                        StripeSubscription subscription = apiMgtDAO.getStripeSubscription(apiName, apiVersion,
                                apiProvider, applicationId, tenantDomain);

                        Stripe.apiKey=getPlatformAccountStripeKey(tenantDomain);

                        if(subscription.getSubscriptionId() != null) {
                            RequestOptions subRequestOptions = RequestOptions.builder().
                                    setStripeAccount(ConnectId).build();
                            Subscription sub = Subscription.retrieve(subscription.getSubscriptionId(),
                                    subRequestOptions);
                            subscriptionItem = sub.getItems().getData().get(0);
                            Map<String, Object> usageRecordParams = new HashMap<String, Object>();
                            usageRecordParams.put("quantity", requestCount);
                            usageRecordParams.put("timestamp", getLocalTimestamp(current.toString()) / 1000);
                            usageRecordParams.put("action", "increment");
                            RequestOptions usageRequestOptions = RequestOptions.builder().setStripeAccount(ConnectId).
                                    setIdempotencyKey(subscriptionItem.getId()+lastUpdatedTimeStamp.toString()).build();
                            UsageRecord usageRecord = UsageRecord.createOnSubscriptionItem(
                                    subscriptionItem.getId(), usageRecordParams, usageRequestOptions);
                        }
                    }
                }
            }
        } catch (APIManagementException ex) {
            String msg = "Error in publishing usage data to Stripe";
            throw new APIManagementException(ex.getMessage());
        } catch (StripeException ex) {
            log.error("STRIPE ERROR : " + ex.getMessage());
            throw new APIManagementException(ex.getMessage());
        }

        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry();
            Resource resource = registry.newResource();
            resource.setContent(currentTimestamp.toString());
            registry.put(MonetizationConstants.UsagePublisher.LAST_PUBSLISHH_TIME_REG_LOCATION, resource);
        }catch(RegistryException ex){
            String msg= "Registry Objects could not be found";
            handleException(msg,ex);
        }
        PrivilegedCarbonContext.endTenantFlow();
    }

    public String getPlatformAccountStripeKey(String tenantDomain) throws APIManagementException{
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
