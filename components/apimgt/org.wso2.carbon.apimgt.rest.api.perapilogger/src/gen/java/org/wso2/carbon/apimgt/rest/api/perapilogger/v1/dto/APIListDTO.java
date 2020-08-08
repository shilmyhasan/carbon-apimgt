package org.wso2.carbon.apimgt.rest.api.perapilogger.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.perapilogger.v1.dto.APIDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIListDTO   {
  
    private List<APIDTO> apis = new ArrayList<>();

  /**
   **/
  public APIListDTO apis(List<APIDTO> apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apis")
  public List<APIDTO> getApis() {
    return apis;
  }
  public void setApis(List<APIDTO> apis) {
    this.apis = apis;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIListDTO apIList = (APIListDTO) o;
    return Objects.equals(apis, apIList.apis);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apis);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIListDTO {\n");
    
    sb.append("    apis: ").append(toIndentedString(apis)).append("\n");
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

