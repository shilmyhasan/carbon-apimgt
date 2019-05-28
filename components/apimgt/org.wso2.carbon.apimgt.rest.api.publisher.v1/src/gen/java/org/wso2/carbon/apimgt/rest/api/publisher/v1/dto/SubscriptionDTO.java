package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApplicationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class SubscriptionDTO   {
  
    private String subscriptionId = null;
    private ApplicationDTO applicationInfo = null;
    private String policy = null;

@XmlType(name="SubscriptionStatusEnum")
@XmlEnum(String.class)
public enum SubscriptionStatusEnum {

    @XmlEnumValue("BLOCKED") BLOCKED(String.valueOf("BLOCKED")), @XmlEnumValue("PROD_ONLY_BLOCKED") PROD_ONLY_BLOCKED(String.valueOf("PROD_ONLY_BLOCKED")), @XmlEnumValue("UNBLOCKED") UNBLOCKED(String.valueOf("UNBLOCKED")), @XmlEnumValue("ON_HOLD") ON_HOLD(String.valueOf("ON_HOLD")), @XmlEnumValue("REJECTED") REJECTED(String.valueOf("REJECTED"));


    private String value;

    SubscriptionStatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SubscriptionStatusEnum fromValue(String v) {
        for (SubscriptionStatusEnum b : SubscriptionStatusEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private SubscriptionStatusEnum subscriptionStatus = null;

  /**
   **/
  public SubscriptionDTO subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "")
  @JsonProperty("subscriptionId")
  @NotNull
  public String getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  /**
   **/
  public SubscriptionDTO applicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("applicationInfo")
  @NotNull
  public ApplicationDTO getApplicationInfo() {
    return applicationInfo;
  }
  public void setApplicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
  }

  /**
   **/
  public SubscriptionDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  @JsonProperty("policy")
  @NotNull
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   **/
  public SubscriptionDTO subscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
    return this;
  }

  
  @ApiModelProperty(example = "BLOCKED", required = true, value = "")
  @JsonProperty("subscriptionStatus")
  @NotNull
  public SubscriptionStatusEnum getSubscriptionStatus() {
    return subscriptionStatus;
  }
  public void setSubscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionDTO subscription = (SubscriptionDTO) o;
    return Objects.equals(subscriptionId, subscription.subscriptionId) &&
        Objects.equals(applicationInfo, subscription.applicationInfo) &&
        Objects.equals(policy, subscription.policy) &&
        Objects.equals(subscriptionStatus, subscription.subscriptionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationInfo, policy, subscriptionStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationInfo: ").append(toIndentedString(applicationInfo)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    subscriptionStatus: ").append(toIndentedString(subscriptionStatus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

