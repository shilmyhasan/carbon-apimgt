
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
import org.wso2.carbon.apimgt.keymgt.internal.RegistrationHolder;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigInitializable;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;
import org.wso2.carbon.apimgt.keymgt.model.exception.InitializationException;
import org.wso2.carbon.apimgt.keymgt.model.impl.MapBasedInMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A validation handler that validates subscription by referring to in-memory data.
 */
public class InMemorySubscriptionValidationHandler extends DefaultKeyValidationHandler
        implements KeyValidatorConfigInitializable {

    private static final Log log = LogFactory.getLog(InMemorySubscriptionValidationHandler.class);
    public static final String API_LEVEL_THROTTLING_KEY = "api_level_throttling_key";
    private InMemorySubscriptionStore inMemoryStore = null;

    @Override
    public void initialize(KeyValidationHandlerConfig config) throws InitializationException {

        InMemorySubscriptionValidationHandlerConfig inMemoryConfig =
                (InMemorySubscriptionValidationHandlerConfig) config;
        String subscriptionStoreClass =
                inMemoryConfig.getSubscriptionStoreConfig().getImplementingClass();
        try {
            this.inMemoryStore = (MapBasedInMemorySubscriptionStore)
                    APIUtil.getClassForName(subscriptionStoreClass.trim()).getDeclaredConstructor().newInstance();

            RegistrationHolder.getInstance().registerInstance(InMemorySubscriptionStore.class.getName(),
                    this.inMemoryStore);

            if (this.inMemoryStore instanceof KeyValidatorConfigInitializable) {
                ((KeyValidatorConfigInitializable) this.inMemoryStore)
                        .initialize(inMemoryConfig.getSubscriptionStoreConfig());

            }
        } catch (InitializationException e) {
            log.error("Error occurred while instantiating in MemoryStore", e);
            throw new InitializationException(e);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException |
                IllegalAccessException | InvocationTargetException e) {
            log.error("Error occurred while instantiating " + subscriptionStoreClass, e);
            throw new InitializationException(e);
        }
    }

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException {

        log.debug("Inside validateSubscription");

        boolean state = validateResourceAuthenticationScheme(validationContext);

        if (!state) {
            return false;
        }

        APIKeyValidationInfoDTO dto = validationContext.getValidationInfoDTO();

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

        Subscription subscription = inMemoryStore.getSubscriptionByAPIAndApplication(application,
                api);

        if (subscription == null) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        String subscriptionStatus = subscription.getSubscriptionState();
        String keyType = mapping.getKeyType();

        state = validateAndSetSubscriptionStatus(subscriptionStatus, keyType, dto);

        if (!state) {
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
            state = validateAndSetAdvancedThrottlingTiers(validationContext.getMatchingResource(),
                    validationContext.getHttpVerb(),
                    dto, api, subscription, application);
        }

        if (!state) {
            setForNonExistentSubscription(validationContext);
            return false;
        }

        return true;
    }

    private boolean validateResourceAuthenticationScheme(TokenValidationContext validationContext) {

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

                // TODO: Replace this with a constant
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
        return true;
    }

    /**
     * Method to set errors if the subscription is not existing.
     *
     * @param validationContext
     */
    private void setForNonExistentSubscription(TokenValidationContext validationContext) {

        validationContext.getValidationInfoDTO().setAuthorized(false);
        validationContext.getValidationInfoDTO()
                .setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
    }

    /**
     * When advanced Throttling is used, this method goes through the Throttling policies and
     * populate validation Info Object with necessary values.
     */
    private boolean validateAndSetAdvancedThrottlingTiers(String matchingResource, String httpVerb,
                                                          APIKeyValidationInfoDTO dto, API api,
                                                          Subscription subscription, Application application) {

        String apiTier = api.getApiTier();
        if (apiTier == null) {
            Resource resource = api.getResource(matchingResource);
            if (resource != null) {
                Verb resourceVerb = resource.getVerb(httpVerb);
                apiTier = resourceVerb.getThrottlingTier();
            }
        }

        String subscriberUserId = application.getSubName();

        String subscriberTenant = MultitenantUtils.getTenantDomain(subscriberUserId);
        SubscriptionPolicy subscriptionPolicy =
                inMemoryStore.getSubscriptionPolicyByName(subscription.getTierName(),
                        MultitenantConstants.SUPER_TENANT_ID);

        ApplicationPolicy applicationPolicy =
                inMemoryStore
                        .getApplicationPolicyByName(application.getAppTier(), MultitenantConstants.SUPER_TENANT_ID);

        APIPolicy apiPolicy = inMemoryStore.getApiPolicyByName(apiTier, MultitenantConstants.SUPER_TENANT_ID);

        // If any of the Policies are null, that means in memory store hasn't been updated in a
        // while.
        if (subscriptionPolicy == null || applicationPolicy == null || apiPolicy == null) {
            log.error("Throttling policy not found in the in-memory Store");
            return false;
        }

        // Checking if any of Subscription, Api or Application Throttling policies are
        // ContentAware
        // TODO: Check what happens when Unlimited tier is absent
        boolean isContentAware =
                subscriptionPolicy.isContentAware() || applicationPolicy.isContentAware() || apiPolicy.isContentAware();

        dto.setContentAware(isContentAware);

        int spikeArrest = subscriptionPolicy.getRateLimitCount();

        String spikeArrestUnit = subscriptionPolicy.getRateLimitTimeUnit();

        boolean stopOnQuotaReach = subscriptionPolicy.isStopOnQuotaReach();
        List<String> list = new ArrayList<>();
        list.add(API_LEVEL_THROTTLING_KEY);
        dto.setSpikeArrestLimit(spikeArrest);
        dto.setSpikeArrestUnit(spikeArrestUnit);
        dto.setStopOnQuotaReach(stopOnQuotaReach);
        dto.setSubscriberTenantDomain(subscriberTenant);
        if (apiTier != null && apiTier.trim().length() > 0) {
            dto.setApiTier(apiTier);
        }

        dto.setThrottlingDataList(list);

        return true;
    }

    /**
     * Validates Subscription status and set the relevant error values in
     * {@link APIKeyValidationInfoDTO} object.
     */
    private boolean validateAndSetSubscriptionStatus(String subscriptionStatus, String keyType,
                                                     APIKeyValidationInfoDTO dto) {

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

        return true;
    }

    @Override
    public boolean validateScopes(TokenValidationContext validationContext) throws APIKeyMgtException {

        return true;
    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext validationContext) throws APIKeyMgtException {

        return true;
    }
}
