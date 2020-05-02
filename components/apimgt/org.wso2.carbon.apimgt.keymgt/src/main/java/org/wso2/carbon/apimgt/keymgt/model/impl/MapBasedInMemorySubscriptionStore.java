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

package org.wso2.carbon.apimgt.keymgt.model.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.config.KeyValidationHandlerConfig;
import org.wso2.carbon.apimgt.impl.config.MapBasedSubscriptionStoreConfig;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.RegistrationHolder;
import org.wso2.carbon.apimgt.keymgt.model.CachableEntity;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigInitializable;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.APIPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Policy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.InitializationException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In memory store which keeps data needed to validate subscriptions as Maps. This uses
 * {@link SubscriptionDataLoader} to load related information.
 */
public class MapBasedInMemorySubscriptionStore implements InMemorySubscriptionStore, KeyValidatorConfigInitializable {

    public static final int LOADING_POOL_SIZE = 6;
    private static final Log log = LogFactory.getLog(MapBasedInMemorySubscriptionStore.class);

    // Maps for keeping Subscription related details.
    private Map<String, ApplicationKeyMapping> applicationKeyMappingMap;
    private Map<Integer, Application> applicationMap;
    private Map<String, API> apiMap;
    private Map<String, Policy> policyMap;
    private Map<String, APIPolicy> apiPolicyMap;
    private Map<String, SubscriptionPolicy> subPolicyMap;
    private Map<String, ApplicationPolicy> appPolicyMap;
    private Map<String, Subscription> subscriptionMap;

    // DataLoader responsible for loading Data from underlying storage.
    private SubscriptionDataLoader dataLoader;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(LOADING_POOL_SIZE);
    private MapBasedSubscriptionStoreConfig mapBasedSubscriptionStoreConfig;

    public MapBasedInMemorySubscriptionStore() {

        this.applicationKeyMappingMap = new ConcurrentHashMap<String, ApplicationKeyMapping>();
        this.applicationMap = new ConcurrentHashMap<Integer, Application>();
        this.apiMap = new ConcurrentHashMap<String, API>();
        this.policyMap = new ConcurrentHashMap<String, Policy>();
        this.subPolicyMap = new ConcurrentHashMap<String, SubscriptionPolicy>();
        this.appPolicyMap = new ConcurrentHashMap<String, ApplicationPolicy>();
        this.apiPolicyMap = new ConcurrentHashMap<String, APIPolicy>();
        this.subscriptionMap = new ConcurrentHashMap<String, Subscription>();

    }

    @Override
    public Application getApplicationById(int appId) {

        return applicationMap.get(appId);
    }

    @Override
    public ApplicationKeyMapping getKeyMappingByConsumerKey(String consumerKey) {

        return applicationKeyMappingMap.get(consumerKey);
    }

    @Override
    public API getApiByContextAndVersion(String context, String version) {

        API api = new API();
        api.setContext(context);
        api.setApiVersion(version);
        return apiMap.get(api.getCacheKey());
    }

    @Override
    public Subscription getSubscriptionByAPIAndApplication(Application application, API api) {

        Subscription subKey = new Subscription();
        subKey.setAppId(application.getAppId());
        subKey.setApiId(api.getApiId());
        return subscriptionMap.get(subKey.getCacheKey());
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByName(String policyName, int tenantId) {

        return getPolicy(policyName, tenantId, subPolicyMap);
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByName(String policyName, int tenantId) {

        return getPolicy(policyName, tenantId, appPolicyMap);
    }

    @Override
    public APIPolicy getApiPolicyByName(String policyName, int tenantId) {

        return getPolicy(policyName, tenantId, apiPolicyMap);
    }

    @Override
    public void initialize(KeyValidationHandlerConfig config) throws InitializationException {

        this.mapBasedSubscriptionStoreConfig =
                (MapBasedSubscriptionStoreConfig) config;
        String subscriptionDataLoader =
                mapBasedSubscriptionStoreConfig.getSubscriptionDataLoaderConfig().getImplementingClass();

        if (subscriptionDataLoader != null) {
            try {
                this.dataLoader =
                        (SubscriptionDataLoader) APIUtil.getClassForName(subscriptionDataLoader.trim())
                                .getDeclaredConstructor().newInstance();
                RegistrationHolder.getInstance().registerInstance(SubscriptionDataLoader.class.getName(),
                        this.dataLoader);
            } catch (InstantiationException e) {
                log.error("Error occurred while instantiating " + subscriptionDataLoader, e);
                throw new InitializationException(e);
            } catch (ClassNotFoundException e) {
                log.error("Error occurred while instantiating " + subscriptionDataLoader, e);
                throw new InitializationException(e);
            } catch (NoSuchMethodException e) {
                log.error("Error occurred while instantiating " + subscriptionDataLoader, e);
                throw new InitializationException(e);
            } catch (IllegalAccessException e) {
                log.error("Error occurred while instantiating " + subscriptionDataLoader, e);
                throw new InitializationException(e);
            } catch (InvocationTargetException e) {
                log.error("Error occurred while instantiating " + subscriptionDataLoader, e);
                throw new InitializationException(e);
            }
        }
        this.initializeLoadingTasks();
    }

    private void initializeLoadingTasks() {

        Runnable apiTask = new PeriodicPopulateTask<String, API>(apiMap,
                new Supplier<List<API>>() {
                    @Override
                    public List<API> get() {

                        try {
                            log.debug("Calling loadAllApis...");
                            return dataLoader.loadAllApis();
                        } catch (APIManagementException e) {
                            log.error("Exception while loading APIs");
                        }
                        return null;
                    }
                });

        executorService.scheduleAtFixedRate(apiTask, 0,
                mapBasedSubscriptionStoreConfig.getApiLoadingFrequency(), TimeUnit.SECONDS);

        Runnable subscriptionLoadingTask = new PeriodicPopulateTask<String, Subscription>(subscriptionMap,
                new Supplier<List<Subscription>>() {
                    @Override
                    public List<Subscription> get() {

                        try {
                            log.debug("Calling loadAllSubscriptions...");
                            return dataLoader.loadAllSubscriptions();
                        } catch (APIManagementException e) {
                            log.error("Exception while loading Subscriptions");
                        }
                        return null;
                    }
                });

        executorService.scheduleAtFixedRate(subscriptionLoadingTask, 0,
                mapBasedSubscriptionStoreConfig.getSubLoadingFrequency(), TimeUnit.SECONDS);

        Runnable applicationLoadingTask = new PeriodicPopulateTask<Integer, Application>(applicationMap,
                new Supplier<List<Application>>() {
                    @Override
                    public List<Application> get() {

                        try {
                            log.debug("Calling loadAllApplications...");
                            return dataLoader.loadAllApplications();
                        } catch (APIManagementException e) {
                            log.error("Exception while loading Applications");
                        }
                        return null;
                    }
                });

        executorService.scheduleAtFixedRate(applicationLoadingTask, 0,
                this.mapBasedSubscriptionStoreConfig.getAppLoadingFrequency(), TimeUnit.SECONDS);

        Runnable keyMappingsTask =
                new PeriodicPopulateTask<String, ApplicationKeyMapping>(applicationKeyMappingMap,
                        new Supplier<List<ApplicationKeyMapping>>() {
                            @Override
                            public List<ApplicationKeyMapping> get() {

                                try {
                                    log.debug("Calling loadAllKeyMappings...");
                                    return dataLoader.loadAllKeyMappings();
                                } catch (APIManagementException e) {
                                    log.error("Exception while loading ApplicationKeyMapping");
                                }
                                return null;
                            }
                        });

        executorService.scheduleAtFixedRate(keyMappingsTask, 0,
                this.mapBasedSubscriptionStoreConfig.getKeyMappingLoadingFrequency(), TimeUnit.SECONDS);

        Runnable subPolicyLoadingTask =
                new PeriodicPopulateTask<String, SubscriptionPolicy>(subPolicyMap,
                        new Supplier<List<SubscriptionPolicy>>() {
                            @Override
                            public List<SubscriptionPolicy> get() {

                                try {
                                    log.debug("Calling loadAllSubscriptionPolicies...");
                                    return dataLoader.loadAllSubscriptionPolicies();
                                } catch (APIManagementException e) {
                                    log.error("Exception while loading Subscription Policies");
                                }
                                return null;
                            }
                        });

        executorService.scheduleAtFixedRate(subPolicyLoadingTask, 0,
                this.mapBasedSubscriptionStoreConfig.getPolicyLoadingFrequency(), TimeUnit.SECONDS);

        Runnable appPolicyLoadingTask =
                new PeriodicPopulateTask<String, ApplicationPolicy>(appPolicyMap,
                        new Supplier<List<ApplicationPolicy>>() {
                            @Override
                            public List<ApplicationPolicy> get() {

                                try {
                                    log.debug("Calling loadAllAppPolicies...");
                                    return dataLoader.loadAllAppPolicies();
                                } catch (APIManagementException e) {
                                    log.error("Exception while loading Application Policies");
                                }
                                return null;
                            }
                        });

        executorService.scheduleAtFixedRate(appPolicyLoadingTask, 0,
                this.mapBasedSubscriptionStoreConfig.getPolicyLoadingFrequency(), TimeUnit.SECONDS);

        Runnable apiPolicyLoadingTask =
                new PeriodicPopulateTask<String, APIPolicy>(apiPolicyMap,
                        new Supplier<List<APIPolicy>>() {
                            @Override
                            public List<APIPolicy> get() {

                                try {
                                    log.debug("Calling loadAllApiPolicies...");
                                    return dataLoader.loadAllApiPolicies();
                                } catch (APIManagementException e) {
                                    log.error("Exception while loading Api Policies");
                                }
                                return null;
                            }
                        });

        executorService.scheduleAtFixedRate(apiPolicyLoadingTask, 0,
                this.mapBasedSubscriptionStoreConfig.getPolicyLoadingFrequency(), TimeUnit.SECONDS);

    }

    private <T extends Policy> T getPolicy(String policyName, int tenantId,
                                           Map<String, T> policyMap) {

        Policy policy = new Policy();
        policy.setTierName(policyName);
        policy.setTenantId(tenantId);
        return policyMap.get(policy.getCacheKey());
    }

    private class PeriodicPopulateTask<K, V extends CachableEntity<K>> implements Runnable {

        private Map<K, V> entityMap;
        private Supplier<List<V>> supplier;

        PeriodicPopulateTask(Map<K, V> entityMap, Supplier<List<V>> supplier) {

            this.entityMap = entityMap;
            this.supplier = supplier;
        }

        public void run() {

            List<V> list = supplier.get();
            HashMap<K, V> tempMap = new HashMap<K, V>();

            if (list != null) {
                for (V v : list) {
                    tempMap.put(v.getCacheKey(), v);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Adding entry Key : %s Value : %s", v.getCacheKey()
                                , v));
                    }
                }

                if (!tempMap.isEmpty()) {
                    entityMap.clear();
                    entityMap.putAll(tempMap);
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("List is null for " + supplier.getClass());
                }
            }
        }
    }

    // Defining Supplier to minimize changes while porting from java 8
    public interface Supplier<T> {

        T get();
    }

}
