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

package org.wso2.carbon.apimgt.keymgt.model;

import org.wso2.carbon.apimgt.impl.config.InMemorySubscriptionStoreConfig;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;

/**
 * A Facade for obtaining Subscription related Data.
 */
public interface InMemorySubscriptionStore {

    /**
     * Gets an {@link Application} by Id
     * @param appId Id of the Application
     * @return {@link Application} with the appId
     */
    Application getApplicationById(int appId);

    /**
     * Gets the {@link ApplicationKeyMapping} entry by consumerKey
     * @param consumerKey Consumer Key of the Application
     * @return {@link ApplicationKeyMapping} entry
     */
    ApplicationKeyMapping getKeyMappingByConsumerKey(String consumerKey);

    /**
     * Get API by Context and Version
     * @param context Context of the API
     * @param version Version of the API
     * @return {@link API} entry represented by Context and Version.
     */
    API getApiByContextAndVersion(String context, String version);

    /**
     * Gets Subscription by API and by Application
     * @param application Application associated with the Subscription
     * @param api API for which subscription is created
     * @return {@link Subscription}
     */
    Subscription getSubscriptionByApiAndApplication(Application application, API api);

    /**
     * Gets Policy by the name and Tenant Id
     * @param policyName Name of the Policy
     * @param tenantId TenantId of the policy owner
     * @return {@link Policy}
     */
    Policy getPolicyByName(String policyName, int tenantId);
}
