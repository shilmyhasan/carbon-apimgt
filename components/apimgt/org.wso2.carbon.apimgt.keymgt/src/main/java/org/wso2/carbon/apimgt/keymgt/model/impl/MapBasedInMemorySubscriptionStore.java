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
import org.wso2.carbon.apimgt.keymgt.model.CachableEntity;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigInitializable;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;
import org.wso2.carbon.apimgt.keymgt.model.exception.InitialisationException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * In memory store which keeps data needed to validate subscriptions as Maps. This uses
 * {@link SubscriptionDataLoader} to load related information.
 */
public class MapBasedInMemorySubscriptionStore implements InMemorySubscriptionStore, KeyValidatorConfigInitializable {

    public static final int LOADING_POOL_SIZE = 6;
    private static final Log log = LogFactory.getLog(MapBasedInMemorySubscriptionStore.class);
    private Map<String, ApplicationKeyMapping> applicationKeyMappingMap;
    private Map<Integer, Application> applicationMap;
    private Map<String, API> apiMap;
    private Map<String, Policy> policyMap;
    private Map<String, Subscription> subscriptionMap;
    private SubscriptionDataLoader dataLoader;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(LOADING_POOL_SIZE);
    private MapBasedSubscriptionStoreConfig mapBasedSubscriptionStoreConfig;

    public MapBasedInMemorySubscriptionStore() {
        this.applicationKeyMappingMap = new ConcurrentHashMap<String, ApplicationKeyMapping>();
        this.applicationMap = new ConcurrentHashMap<Integer, Application>();
        this.apiMap = new ConcurrentHashMap<String, API>();
        this.policyMap = new ConcurrentHashMap<String, Policy>();
        this.subscriptionMap = new ConcurrentHashMap<String, Subscription>();
    }

    private void initialiseLoadingTasks() {

        Runnable apiTask = new PeriodicPopulateTask<String, API>(apiMap,
                () -> {
                    try {
                        log.debug("Calling loadAllApis...");
                        return dataLoader.loadAllApis();
                    } catch (APIManagementException e) {
                        log.error("Exception while loading APIs");
                    }
                    return null;
                });

        executorService.scheduleAtFixedRate(apiTask, 0,
                mapBasedSubscriptionStoreConfig.getApiLoadingFrequency(), TimeUnit.SECONDS);


        Runnable subscriptionLoadingTask = new PeriodicPopulateTask<String, Subscription>(subscriptionMap,
                () -> {
                    try {
                        log.debug("Calling loadAllSubscriptions...");
                        return dataLoader.loadAllSubscriptions();
                    } catch (APIManagementException e) {
                        log.error("Exception while loading Subscriptions");
                    }
                    return null;
                });

        executorService.scheduleAtFixedRate(subscriptionLoadingTask, 0,
                mapBasedSubscriptionStoreConfig.getSubLoadingFrequency(), TimeUnit.SECONDS);


        Runnable applicationLoadingTask = new PeriodicPopulateTask<Integer, Application>(applicationMap,
                () -> {
                    try {
                        log.debug("Calling loadAllApplications...");
                        return dataLoader.loadAllApplications();
                    } catch (APIManagementException e) {
                        log.error("Exception while loading Applications");
                    }
                    return null;
                });

        executorService.scheduleAtFixedRate(applicationLoadingTask, 0,
                this.mapBasedSubscriptionStoreConfig.getAppLoadingFrequency(), TimeUnit.SECONDS);

        Runnable keyMappingsTask =
                new PeriodicPopulateTask<String, ApplicationKeyMapping>(applicationKeyMappingMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllKeyMappings...");
                                return dataLoader.loadAllKeyMappings();
                            } catch (APIManagementException e) {
                                log.error("Exception while loading ApplicationKeyMapping");
                            }
                            return null;
                        });

        executorService.scheduleAtFixedRate(keyMappingsTask, 0,
                this.mapBasedSubscriptionStoreConfig.getKeyMappingLoadingFrequency(), TimeUnit.SECONDS);


        Runnable policyLoadingTask =
                new PeriodicPopulateTask<String, Policy>(policyMap,
                        () -> {
                            try {
                                log.debug("Calling loadAllPolicies...");
                                return dataLoader.loadAllPolicies();
                            } catch (APIManagementException e) {
                                log.error("Exception while loading Policies");
                            }
                            return null;
                        });

        executorService.scheduleAtFixedRate(policyLoadingTask, 0,
                this.mapBasedSubscriptionStoreConfig.getPolicyLoadingFrequency(), TimeUnit.SECONDS);

    }

    public Application finApplicationbyConsumerKey(String consumerKey) {
        String applicationKeyMappingKey = consumerKey;
        ApplicationKeyMapping mapping = applicationKeyMappingMap.get(applicationKeyMappingKey);

        //TODO: Check whether key has been approved.
        Application application = mapping == null ? null :
                applicationMap.get(mapping.getApplicationId());

        return application;
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
    public Subscription getSubscriptionByApiAndApplication(Application application, API api) {
        Subscription subKey = new Subscription();
        subKey.setApiId(application.getAppId());
        subKey.setApiId(api.getApiId());
        subscriptionMap.get(subKey.getCacheKey());
        return subscriptionMap.get(subKey);
    }

    @Override
    public Policy getPolicyByName(String policyName, int tenantId) {
        Policy policy = new Policy();
        policy.setTierName(policyName);
        policy.setTenantId(tenantId);
        return policyMap.get(policy.getCacheKey());
    }

    @Override
    public void initialise(KeyValidationHandlerConfig config) throws InitialisationException {
        this.mapBasedSubscriptionStoreConfig =
                (MapBasedSubscriptionStoreConfig) config;
        String subscriptionDataLoader =
                mapBasedSubscriptionStoreConfig.getSubscriptionDataLoaderConfig().getImplementingClass();

        if (subscriptionDataLoader != null) {
            try {
                this.dataLoader =
                        (SubscriptionDataLoader) APIUtil.getClassForName(subscriptionDataLoader.trim()).getDeclaredConstructor().newInstance();
            } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("Error occurred while instantiating " + subscriptionDataLoader, e);
                throw new InitialisationException(e);
            }
        }

        this.initialiseLoadingTasks();
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

            if (list != null) {
                for (V v : list) {
                    entityMap.put(v.getCacheKey(), v);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Adding entry Key : %s Value : %s", v.getCacheKey()
                                , v));
                    }
                }

            }
        }
    }

}
