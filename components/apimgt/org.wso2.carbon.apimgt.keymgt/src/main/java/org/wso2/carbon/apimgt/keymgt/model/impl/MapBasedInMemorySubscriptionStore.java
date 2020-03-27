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
import org.wso2.carbon.apimgt.impl.config.InMemorySubscriptionStoreConfig;
import org.wso2.carbon.apimgt.impl.config.KeyValidationHandlerConfig;
import org.wso2.carbon.apimgt.impl.config.MapBasedSubscriptionStoreConfig;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigLoadable;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class MapBasedInMemorySubscriptionStore implements InMemorySubscriptionStore, KeyValidatorConfigLoadable {

    private static final Log log = LogFactory.getLog(MapBasedInMemorySubscriptionStore.class);
    private Map<String, ApplicationKeyMapping> applicationKeyMappingMap;
    private Map<Integer, Application> applicationMap;
    private Map<String, API> apiMap = new ConcurrentHashMap<String, API>();
    private Map<String, Policy> policyMap;
    private Map<String, Subscription> subscriptionMap;
    private SubscriptionDataLoader dataLoader = new DbDataLoader();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);
    private MapBasedSubscriptionStoreConfig mapBasedSubscriptionStoreConfig;

    public MapBasedInMemorySubscriptionStore(){
        try {

            Runnable apiLoadingTask = new PeriodicPopulateTask<String,API>(apiMap, () -> {
                try {
                    log.info("Started running Periodic Task for Loading APIs...");
                    List<API> apis = dataLoader.loadAllApis();
                    Map<String, API> internalMap = new HashMap<String, API>();
                    for (API api : apis) {
                        internalMap.put(api.getContext()+"."+api.getApiVersion(),api);
                        log.info("Adding API : "+api.getContext() + " , Version : "+ api.getApiVersion());
                    }
                    return internalMap;
                } catch (APIManagementException e) {
                    log.error("Exception while loading APIs");
                }
                return null;
            });

            executorService.scheduleAtFixedRate(apiLoadingTask,100,60, TimeUnit.SECONDS);
            List<Subscription> subscriptionList =  dataLoader.loadAllSubscriptions();
            subscriptionMap = new HashMap<String, Subscription>();
            for (Subscription subscription : subscriptionList) {
                subscriptionMap.put(Integer.toString(subscription.getAppId()) + "." + Integer.toString(subscription.getApiId()),subscription);
                log.info("Adding Subscription : "+subscription.getSubscriptionId() + ", Tier : "+ subscription.getTierName());
            }

            List<Application> applicationList = dataLoader.loadAllApplications();
            applicationMap = new HashMap<Integer, Application>();
            for (Application application : applicationList) {
                applicationMap.put(application.getAppId(),application);
                log.info("Adding Application : "+application.getAppName() + ", Tier : "+ application.getAppTier());
            }

            List<ApplicationKeyMapping> keyMappings = dataLoader.loadAllKeyMappings();
            applicationKeyMappingMap = new HashMap<String, ApplicationKeyMapping>();
            for (ApplicationKeyMapping keyMapping : keyMappings) {
                applicationKeyMappingMap.put(keyMapping.getConsumerKey(),keyMapping);
                log.info("Adding KeyMappingEntry : App Id "+keyMapping.getApplicationId() + " " +
                        "Consumer Key : "+keyMapping.getConsumerKey());
            }

            List<Policy> policies = dataLoader.loadAllPolicies();
            policyMap = new HashMap<String, Policy>();
            for (Policy policy : policies) {
                policyMap.put(policy.getTierName()+"."+Integer.toString(policy.getTenantId()),
                        policy);
                log.info("Adding Policy : "+policy.getTierName() + " , TID : "+ policy.getTenantId());
            }

        } catch (APIManagementException e) {
            log.error("Error occurred while fetching Subscriptions");
        }
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
    public API findApiByContextAndVersion(String context, String version) {
        String apiKey = context +"."+version;
        return apiMap.get(apiKey);
    }

    @Override
    public Subscription findSubscriptionByApiAndApplication(Application application, API api) {
        String subKey =
                Integer.toString(application.getAppId()) + "." + Integer.toString(api.getApiId());
        subscriptionMap.get(subKey);
        return subscriptionMap.get(subKey);
    }

    @Override
    public Policy getPolicyByName(String policyName, int tenantId) {
        String policyKey = policyName+"."+tenantId;
        return policyMap.get(policyKey);
    }

    @Override
    public void initialise(KeyValidationHandlerConfig config) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        this.mapBasedSubscriptionStoreConfig =
                (MapBasedSubscriptionStoreConfig) config;
        String subscriptionDataLoader =
                mapBasedSubscriptionStoreConfig.getSubscriptionDataLoaderConfig().getImplementingClass();

        if(subscriptionDataLoader != null){
            this.dataLoader =
                    (SubscriptionDataLoader) APIUtil.getClassForName(subscriptionDataLoader.trim()).getDeclaredConstructor().newInstance();
        }
    }

    private static class PeriodicPopulateTask<K,V> implements Runnable {

        private Map<K,V> entityMap;
        private Supplier<Map<K,V>> supplier;

        PeriodicPopulateTask(Map<K,V> entityMap, Supplier<Map<K,V>> supplier){
            this.entityMap = entityMap;
            this.supplier = supplier;
        }

        public void run() {
            Map<K,V> map = supplier.get();
            // entityMap.clear() and putALl could have been used, but would render some elements
            // null for a longer period. Hence followed this approach.
            if(map != null) {
                for (Map.Entry<K, V> entry : map.entrySet()) {
                    entityMap.put(entry.getKey(),entry.getValue());
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Adding entry Key : %s Value : %s",entry.getKey()
                                ,entry.getValue()));
                    }
                }
            }
        }
    }

}
