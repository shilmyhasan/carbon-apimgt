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

public class Application implements CachableEntity<Integer> {

    private int appId;
    private String appName;
    private int subId;
    private String subName;
    private String appTier;
    private String appStatus;
    private String createdBy;
    private int tenantId;

    public int getSubId() {

        return subId;
    }

    public void setSubId(int subId) {

        this.subId = subId;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public int getAppId() {

        return appId;
    }

    public void setAppId(int appId) {

        this.appId = appId;
    }

    public String getAppName() {

        return appName;
    }

    public void setAppName(String appName) {

        this.appName = appName;
    }

    public String getSubName() {

        return subName;
    }

    public void setSubName(String subName) {

        this.subName = subName;
    }

    public String getAppTier() {

        return appTier;
    }

    public void setAppTier(String appTier) {

        this.appTier = appTier;
    }

    public String getAppStatus() {

        return appStatus;
    }

    public void setAppStatus(String appStatus) {

        this.appStatus = appStatus;
    }

    public String getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(String createdBy) {

        this.createdBy = createdBy;
    }

    public Integer getCacheKey() {

        return getAppId();
    }
}
