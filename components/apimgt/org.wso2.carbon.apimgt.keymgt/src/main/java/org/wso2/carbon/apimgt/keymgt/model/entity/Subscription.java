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

package org.wso2.carbon.apimgt.keymgt.model.entity;

import org.wso2.carbon.apimgt.keymgt.model.CachableEntity;

/**
 * Entity for representing a Subscription in APIM
 */
public class Subscription implements CachableEntity<String> {

    private int subscriptionId = -1;
    private String tierName;
    private int apiId;
    private int appId;
    private String subscriptionState;
    private String subscriptionWfState;
    private long createdTime;

    public int getSubscriptionId() {

        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {

        this.subscriptionId = subscriptionId;
    }

    public String getTierName() {

        return tierName;
    }

    public void setTierName(String tierName) {

        this.tierName = tierName;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public int getAppId() {

        return appId;
    }

    public void setAppId(int appId) {

        this.appId = appId;
    }

    public String getSubscriptionState() {

        return subscriptionState;
    }

    public void setSubscriptionState(String subscriptionState) {

        this.subscriptionState = subscriptionState;
    }

    public String getSubscriptionWfState() {

        return subscriptionWfState;
    }

    public void setSubscriptionWfState(String subscriptionWfState) {

        this.subscriptionWfState = subscriptionWfState;
    }

    public long getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(long createdTime) {

        this.createdTime = createdTime;
    }

    public String getCacheKey() {

        return getAppId() + "." + getApiId();
    }
}
