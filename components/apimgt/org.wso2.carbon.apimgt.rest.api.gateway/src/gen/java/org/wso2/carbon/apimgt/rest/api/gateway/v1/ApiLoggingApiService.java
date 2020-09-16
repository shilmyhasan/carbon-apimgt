package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.APIListDTO;

import javax.ws.rs.core.Response;

public interface ApiLoggingApiService {
      public Response apiLoggingDelete(String context, MessageContext messageContext) throws APIManagementException;
      public Response apiLoggingGet(String context, MessageContext messageContext) throws APIManagementException;
      public Response apiLoggingPost(APIListDTO payload, String context, String logLevel, MessageContext messageContext) throws APIManagementException;
}
