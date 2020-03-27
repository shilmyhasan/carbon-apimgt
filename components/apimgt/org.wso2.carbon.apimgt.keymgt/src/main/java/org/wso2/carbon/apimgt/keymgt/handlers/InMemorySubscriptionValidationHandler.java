
/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.config.InMemorySubscriptionStoreConfig;
import org.wso2.carbon.apimgt.impl.config.InMemorySubscriptionValidationHandlerConfig;
import org.wso2.carbon.apimgt.impl.config.KeyValidationHandlerConfig;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigLoadable;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;
import org.wso2.carbon.apimgt.keymgt.model.impl.MapBasedInMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class InMemorySubscriptionValidationHandler extends DefaultKeyValidationHandler implements KeyValidatorConfigLoadable {

    private static final Log log = LogFactory.getLog(InMemorySubscriptionValidationHandler.class);
    private InMemorySubscriptionStore inMemoryStore = null;

    public InMemorySubscriptionValidationHandler(){
        APIManagerConfiguration configuration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        //configuration.
    }

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException {
        log.info("Inside validateSubscription");

        /*
        *
        *         Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(true);
            ps = conn.prepareStatement(sql);
            ps.setString(1, context);
            ps.setString(2, consumerKey);
            if (!defaultVersionInvoked) {
                ps.setString(3, version);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                String subscriptionStatus = rs.getString("SUB_STATUS");
                String type = rs.getString("KEY_TYPE");
                if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setAuthorized(false);
                    return false;
                } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) || APIConstants
                        .SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
                    infoDTO.setAuthorized(false);
                    return false;
                } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus) &&
                        !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
                    infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
                    infoDTO.setType(type);
                    infoDTO.setAuthorized(false);
                    return false;
                }

                String apiProvider = rs.getString("API_PROVIDER");
                String subTier = rs.getString("TIER_ID");
                String appTier = rs.getString("APPLICATION_TIER");
                infoDTO.setTier(subTier);
                infoDTO.setSubscriber(rs.getString("USER_ID"));
                infoDTO.setApplicationId(rs.getString("APPLICATION_ID"));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setApiPublisher(apiProvider);
                infoDTO.setApplicationName(rs.getString("NAME"));
                infoDTO.setApplicationTier(appTier);
                infoDTO.setType(type);

                //Advanced Level Throttling Related Properties
                if (APIUtil.isAdvanceThrottlingEnabled()) {
                    String apiTier = rs.getString("API_TIER");
                    String subscriberUserId = rs.getString("USER_ID");
                    String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
                    int apiId = rs.getInt("API_ID");
                    int subscriberTenantId = APIUtil.getTenantId(subscriberUserId);
                    int apiTenantId = APIUtil.getTenantId(apiProvider);
                    //TODO isContentAware
                    boolean isContentAware = isAnyPolicyContentAware(conn, apiTier, appTier, subTier, subscriberTenantId, apiTenantId, apiId);
                    infoDTO.setContentAware(isContentAware);

                    //TODO this must implement as a part of throttling implementation.
                    int spikeArrest = 0;
                    String apiLevelThrottlingKey = "api_level_throttling_key";
                    if (rs.getInt("RATE_LIMIT_COUNT") > 0) {
                        spikeArrest = rs.getInt("RATE_LIMIT_COUNT");
                    }

                    String spikeArrestUnit = null;
                    if (rs.getString("RATE_LIMIT_TIME_UNIT") != null) {
                        spikeArrestUnit = rs.getString("RATE_LIMIT_TIME_UNIT");
                    }
                    boolean stopOnQuotaReach = rs.getBoolean("STOP_ON_QUOTA_REACH");
                    List<String> list = new ArrayList<String>();
                    list.add(apiLevelThrottlingKey);
                    infoDTO.setSpikeArrestLimit(spikeArrest);
                    infoDTO.setSpikeArrestUnit(spikeArrestUnit);
                    infoDTO.setStopOnQuotaReach(stopOnQuotaReach);
                    infoDTO.setSubscriberTenantDomain(subscriberTenant);
                    if (apiTier != null && apiTier.trim().length() > 0) {
                        infoDTO.setApiTier(apiTier);
                    }
                    //We also need to set throttling data list associated with given API. This need to have policy id and
                    // condition id list for all throttling tiers associated with this API.
                    infoDTO.setThrottlingDataList(list);
                }
                return true;
            }
            infoDTO.setAuthorized(false);
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        } catch (SQLException e) {
            handleException("Exception occurred while validating Subscription.", e);
        } finally {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {

            }
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return false;
        * */

        if (validationContext == null || validationContext.getValidationInfoDTO() == null) {
            return false;
        }

        if (validationContext.isCacheHit()) {
            return true;
        }

        APIKeyValidationInfoDTO dto = validationContext.getValidationInfoDTO();


        if (validationContext.getTokenInfo() != null) {
            if (validationContext.getTokenInfo().isApplicationToken()) {
                dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            } else {
                dto.setUserType("APPLICATION_USER");
            }

            AccessTokenInfo tokenInfo = validationContext.getTokenInfo();

            // This block checks if a Token of Application Type is trying to access a resource protected with
            // Application Token
            if (!hasTokenRequiredAuthLevel(validationContext.getRequiredAuthenticationLevel(), tokenInfo)) {
                dto.setAuthorized(false);
                dto.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                return false;
            }
        }

        boolean state = false;

        ApplicationKeyMapping mapping =
                inMemoryStore.getKeyMappingByConsumerKey(validationContext.getTokenInfo().getConsumerKey());

        if(mapping == null){
            setForNonExistentSubscription(validationContext);
            return false;
        }

        Application application =
                inMemoryStore.getApplicationById(mapping.getApplicationId());

        if(application == null){
            setForNonExistentSubscription(validationContext);
            return false;
        }

        API api = inMemoryStore.findApiByContextAndVersion(validationContext.getContext(),
                validationContext.getVersion());

        if(api == null) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        Subscription subscription = inMemoryStore.findSubscriptionByApiAndApplication(application,
                api);

        if(api == null) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        String subscriptionStatus = subscription.getSubscriptionState();
        String keyType = mapping.getKeyType();

        if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
            dto.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            dto.setAuthorized(false);
            return false;
        } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) || APIConstants
                .SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
            dto.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
            dto.setAuthorized(false);
            return false;
        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus) &&
                !APIConstants.API_KEY_TYPE_SANDBOX.equals(keyType)) {
            dto.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            dto.setType(keyType);
            dto.setAuthorized(false);
            return false;
        }

        dto.setTier(subscription.getTierName());
        dto.setSubscriber(application.getSubName());
        dto.setApplicationId(Integer.toString(application.getAppId()));
        dto.setApiName(api.getApiName());
        dto.setApiPublisher(api.getApiProvider());
        dto.setApplicationName(application.getAppName());
        dto.setApplicationTier(application.getAppTier());
        dto.setType(keyType);

//        state = dao.validateSubscriptionDetails(validationContext.getContext(), validationContext.getVersion(),
//                dto.getConsumerKey(), dto);

        if (APIUtil.isAdvanceThrottlingEnabled()) {
            String apiTier = api.getApiTier();
            String subscriberUserId = application.getSubName();
            int apiId = api.getApiId();
            int subscriberTenantId = APIUtil.getTenantId(subscriberUserId);
            //TODO:Check if this works without the DB
            int apiTenantId = APIUtil.getTenantId(api.getApiProvider());

            String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
            Policy subscriptionPolicy = inMemoryStore.getPolicyByName(subscription.getTierName(),apiTenantId);

            //TODO isContentAware
//            boolean isContentAware = isAnyPolicyContentAware(conn, apiTier, appTier, subTier, subscriberTenantId, apiTenantId, apiId);
//            infoDTO.setContentAware(isContentAware);

            //TODO this must implement as a part of throttling implementation.
            int spikeArrest = subscriptionPolicy.getCount();
            String apiLevelThrottlingKey = "api_level_throttling_key";

            String spikeArrestUnit = subscriptionPolicy.getUnitTime();;

            boolean stopOnQuotaReach = subscriptionPolicy.isStopOnQuotaReach();
            List<String> list = new ArrayList<String>();
            list.add(apiLevelThrottlingKey);
            dto.setSpikeArrestLimit(spikeArrest);
            dto.setSpikeArrestUnit(spikeArrestUnit);
            dto.setStopOnQuotaReach(stopOnQuotaReach);
            dto.setSubscriberTenantDomain(subscriberTenant);
            if (apiTier != null && apiTier.trim().length() > 0) {
                dto.setApiTier(apiTier);
            }
            //We also need to set throttling data list associated with given API. This need to have policy id and
            // condition id list for all throttling tiers associated with this API.
            dto.setThrottlingDataList(list);
        }

        return true;
    }


    private void setForNonExistentSubscription(TokenValidationContext validationContext) {
        validationContext.getValidationInfoDTO().setAuthorized(false);
        validationContext.getValidationInfoDTO().setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
    }

    @Override
    public void initialise(KeyValidationHandlerConfig config) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        InMemorySubscriptionValidationHandlerConfig inMemoryConfig = (InMemorySubscriptionValidationHandlerConfig) config;
        String subscriptionStoreClass =
                inMemoryConfig.getSubscriptionStoreConfig().getImplementingClass();
        this.inMemoryStore =
                (MapBasedInMemorySubscriptionStore) APIUtil.getClassForName(subscriptionStoreClass.trim()).getDeclaredConstructor().newInstance();
        if(this.inMemoryStore instanceof KeyValidatorConfigLoadable) {
            ((KeyValidatorConfigLoadable)this.inMemoryStore).initialise(inMemoryConfig.getSubscriptionStoreConfig());
        }
    }
}
