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
import org.wso2.carbon.apimgt.impl.config.KeyValidationHandlerConfig;
import org.wso2.carbon.apimgt.keymgt.model.KeyValidatorConfigInitializable;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.dao.SubscriptionLoadingDao;
import org.wso2.carbon.apimgt.keymgt.model.entity.Api;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicyConditionGroup;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Resource;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.exception.InitializationException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Subscription Data Loader that loads data from DB
 */
public class DbDataLoader implements SubscriptionDataLoader, KeyValidatorConfigInitializable {

    private static final Log log = LogFactory.getLog(DbDataLoader.class);

    @Override
    public List<Subscription> loadAllSubscriptions() throws DataLoadingException {

        return SubscriptionLoadingDao.getInstance().getAllSubscriptions();
    }

    @Override
    public List<Application> loadAllApplications() throws DataLoadingException {

        return SubscriptionLoadingDao.getInstance().getAllApplications();
    }

    @Override
    public List<ApplicationKeyMapping> loadAllKeyMappings() throws DataLoadingException {

        return SubscriptionLoadingDao.getInstance().getAllApplicationKeyMappings();
    }

    @Override
    public List<Api> loadAllApis() throws DataLoadingException {

        Map<Integer, Api> apiMap = SubscriptionLoadingDao.getInstance().getAllApis();
        Map<String, Resource> resourceMap = SubscriptionLoadingDao.getInstance().getApiUrlMappings();
        for (Resource resource : resourceMap.values()) {
            Api api = apiMap.get(resource.getApiId());
            if (api != null) {
                api.addResource(resource);
            } else {
                log.error("Api not found for Id : " + resource.getApiId());
            }
        }
        return Arrays.asList(apiMap.values().toArray(new Api[]{}));
    }

    @Override
    public List<SubscriptionPolicy> loadAllSubscriptionPolicies() throws DataLoadingException {

        return SubscriptionLoadingDao.getInstance().getAllSubscriptionPolicies();
    }

    @Override
    public List<ApiPolicy> loadAllApiPolicies() throws DataLoadingException {

        Map<Integer, ApiPolicy> apiPolicyMap =
                SubscriptionLoadingDao.getInstance().getAllApiPolicies();
        Map<Integer, Set<ApiPolicyConditionGroup>> conditionGroups =
                SubscriptionLoadingDao.getInstance().getApiPolicyConditionGroups();

        for (Map.Entry<Integer, Set<ApiPolicyConditionGroup>> conditionGroupEntry :
                conditionGroups.entrySet()) {
            ApiPolicy policy = apiPolicyMap.get(conditionGroupEntry.getKey());
            policy.setConditionGroups(conditionGroupEntry.getValue());
        }

        return Arrays.asList(apiPolicyMap.values().toArray(new ApiPolicy[]{}));
    }

    @Override
    public List<ApplicationPolicy> loadAllAppPolicies() throws DataLoadingException {

        return SubscriptionLoadingDao.getInstance().getAllApplicationPolicies();
    }

    @Override
    public void initialize(KeyValidationHandlerConfig config) throws InitializationException {
        // Currently this class doesn't have any configs.
    }
}
