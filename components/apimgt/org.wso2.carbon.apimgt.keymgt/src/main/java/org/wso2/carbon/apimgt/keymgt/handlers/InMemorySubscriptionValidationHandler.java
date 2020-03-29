
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
import org.wso2.carbon.apimgt.impl.config.InMemorySubscriptionValidationHandlerConfig;
import org.wso2.carbon.apimgt.impl.config.KeyValidationHandlerConfig;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigInitializable;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;
import org.wso2.carbon.apimgt.keymgt.model.exception.InitialisationException;
import org.wso2.carbon.apimgt.keymgt.model.impl.MapBasedInMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A validation handler that validates subscription by referring to in-memory data.
 */
public class InMemorySubscriptionValidationHandler extends DefaultKeyValidationHandler implements KeyValidatorConfigInitializable {

    private static final Log log = LogFactory.getLog(InMemorySubscriptionValidationHandler.class);
    private InMemorySubscriptionStore inMemoryStore = null;

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException {
        log.debug("Inside validateSubscription");


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

        if (mapping == null) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        Application application =
                inMemoryStore.getApplicationById(mapping.getApplicationId());

        if (application == null) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        API api = inMemoryStore.getApiByContextAndVersion(validationContext.getContext(),
                validationContext.getVersion());

        if (api == null) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        Subscription subscription = inMemoryStore.getSubscriptionByApiAndApplication(application,
                api);

        if (api == null) {
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


        if (APIUtil.isAdvanceThrottlingEnabled()) {
            String apiTier = api.getApiTier();
            String subscriberUserId = application.getSubName();
            int apiId = api.getApiId();
            int subscriberTenantId = APIUtil.getTenantId(subscriberUserId);
            //TODO:Check if this works without the DB
            int apiTenantId = APIUtil.getTenantId(api.getApiProvider());

            String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
            Policy subscriptionPolicy = inMemoryStore.getPolicyByName(subscription.getTierName(), apiTenantId);

            //TODO isContentAware
//            boolean isContentAware = isAnyPolicyContentAware(conn, apiTier, appTier, subTier, subscriberTenantId, apiTenantId, apiId);
//            infoDTO.setContentAware(isContentAware);

            //TODO this must implement as a part of throttling implementation.
            int spikeArrest = subscriptionPolicy.getCount();
            String apiLevelThrottlingKey = "api_level_throttling_key";

            String spikeArrestUnit = subscriptionPolicy.getUnitTime();
            ;

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

    /**
     * Method to set errors if the subscription is not existing.
     * @param validationContext
     */
    private void setForNonExistentSubscription(TokenValidationContext validationContext) {
        validationContext.getValidationInfoDTO().setAuthorized(false);
        validationContext.getValidationInfoDTO().setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
    }

    @Override
    public void initialise(KeyValidationHandlerConfig config) throws InitialisationException {
        InMemorySubscriptionValidationHandlerConfig inMemoryConfig = (InMemorySubscriptionValidationHandlerConfig) config;
        String subscriptionStoreClass =
                inMemoryConfig.getSubscriptionStoreConfig().getImplementingClass();
        try {
            this.inMemoryStore = (MapBasedInMemorySubscriptionStore)
                    APIUtil.getClassForName(subscriptionStoreClass.trim()).getDeclaredConstructor().newInstance();

            if (this.inMemoryStore instanceof KeyValidatorConfigInitializable) {
                ((KeyValidatorConfigInitializable) this.inMemoryStore).initialise(inMemoryConfig.getSubscriptionStoreConfig());

            }
        } catch (InitialisationException e) {
            log.error("Error occurred while instantiating in MemoryStore", e);
            throw new InitialisationException(e);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException |
                IllegalAccessException | InvocationTargetException e) {
            log.error("Error occurred while instantiating " + subscriptionStoreClass, e);
            throw new InitialisationException(e);
        }
    }

    @Override
    public boolean validateScopes(TokenValidationContext validationContext) throws APIKeyMgtException {
        return true;
    }
}
