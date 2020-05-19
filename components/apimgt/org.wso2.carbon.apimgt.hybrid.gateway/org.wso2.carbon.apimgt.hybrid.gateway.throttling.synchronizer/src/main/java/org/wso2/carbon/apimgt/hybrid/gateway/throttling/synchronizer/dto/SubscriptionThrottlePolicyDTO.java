/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "")
public class SubscriptionThrottlePolicyDTO extends ThrottlePolicyDTO {

  private ThrottleLimitDTO defaultLimit = null;

  private MonetizationInfoDTO monetization = null;

  private Integer rateLimitCount = null;

  private String rateLimitTimeUnit = null;

  private List<CustomAttributeDTO> customAttributes = new ArrayList<CustomAttributeDTO>();

  private Boolean stopOnQuotaReach = false;

  private String billingPlan = null;

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("monetization")
  public MonetizationInfoDTO getMonetization() {
    return monetization;
  }
  public void setMonetization(MonetizationInfoDTO monetization) {
    this.monetization = monetization;
  }

  /**
   * Burst control request count
   **/
  @ApiModelProperty(value = "Burst control request count")
  @JsonProperty("rateLimitCount")
  public Integer getRateLimitCount() {
    return rateLimitCount;
  }
  public void setRateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
  }

  /**
   * Burst control time unit
   **/
  @ApiModelProperty(value = "Burst control time unit")
  @JsonProperty("rateLimitTimeUnit")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }
  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  /**
   * Custom attributes added to the Subscription Throttling Policy\n
   **/
  @ApiModelProperty(value = "Custom attributes added to the Subscription Throttling Policy\n")
  @JsonProperty("customAttributes")
  public List<CustomAttributeDTO> getCustomAttributes() {
    return customAttributes;
  }
  public void setCustomAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
  }

  /**
   * This indicates the action to be taken when a user goes beyond the allocated quota. If checked, the user's requests will be dropped. If unchecked, the requests will be allowed to pass through.\n
   **/
  @ApiModelProperty(value = "This indicates the action to be taken when a user goes beyond the allocated quota. If checked, the user's requests will be dropped. If unchecked, the requests will be allowed to pass through.\n")
  @JsonProperty("stopOnQuotaReach")
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  /**
   * define whether this is Paid or a Free plan. Allowed values are FREE or COMMERCIAL.\n
   **/
  @ApiModelProperty(value = "define whether this is Paid or a Free plan. Allowed values are FREE or COMMERCIAL.\n")
  @JsonProperty("billingPlan")
  public String getBillingPlan() {
    return billingPlan;
  }
  public void setBillingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  defaultLimit: ").append(defaultLimit).append("\n");
    sb.append("  monetization: ").append(monetization).append("\n");
    sb.append("  rateLimitCount: ").append(rateLimitCount).append("\n");
    sb.append("  rateLimitTimeUnit: ").append(rateLimitTimeUnit).append("\n");
    sb.append("  customAttributes: ").append(customAttributes).append("\n");
    sb.append("  stopOnQuotaReach: ").append(stopOnQuotaReach).append("\n");
    sb.append("  billingPlan: ").append(billingPlan).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
