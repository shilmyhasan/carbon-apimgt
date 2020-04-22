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

import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.keymgt.model.CachableEntity;

/**
 * Top level entity for representating a Throttling Policy.
 */
public class Policy implements CachableEntity<String> {

    private int policyId;
    private int tenantId;
    private String tierName;
    private String quotaType;

    public int getPolicyId() {

        return policyId;
    }

    public void setPolicyId(int policyId) {

        this.policyId = policyId;
    }

    public String getQuotaType() {

        return quotaType;
    }

    public void setQuotaType(String quotaType) {

        this.quotaType = quotaType;
    }

    public boolean isContentAware() {

        return PolicyConstants.BANDWIDTH_TYPE.equals(quotaType);
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public String getTierName() {

        return tierName;
    }

    public void setTierName(String tierName) {

        this.tierName = tierName;
    }

    @Override
    public String getCacheKey() {

        return getTierName() + "." + getTenantId();
    }
}
