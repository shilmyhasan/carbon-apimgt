/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@ApiModel(description = "")
public class MonetizationInfoDTO {

    public enum MonetizationPlanEnum {
        FixedRate, DynamicRate,
    }

    @NotNull
    private MonetizationPlanEnum monetizationPlan = null;

    @NotNull
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Flag to indicate the monetization plan
     **/
    @ApiModelProperty(required = true, value = "Flag to indicate the monetization plan")
    @JsonProperty("monetizationPlan")
    public MonetizationPlanEnum getMonetizationPlan() {
        return monetizationPlan;
    }

    public void setMonetizationPlan(MonetizationPlanEnum monetizationPlan) {
        this.monetizationPlan = monetizationPlan;
    }

    /**
     * Map of custom properties related to each monetization plan
     **/
    @ApiModelProperty(required = true, value = "Map of custom properties related to each monetization plan")
    @JsonProperty("properties")
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MonetizationInfoDTO {\n");
        sb.append("  monetizationPlan: ").append(monetizationPlan).append("\n");
        sb.append("  properties: ").append(properties).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
