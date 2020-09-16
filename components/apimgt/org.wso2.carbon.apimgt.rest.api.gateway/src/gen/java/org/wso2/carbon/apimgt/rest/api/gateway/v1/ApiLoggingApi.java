package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.impl.ApiLoggingApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

@Path("/api-logging")

@Api(description = "the api-logging API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApiLoggingApi  {

  @Context MessageContext securityContext;

ApiLoggingApiService delegate = new ApiLoggingApiServiceImpl();


    @DELETE
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "IInternal server error while deleting API details", response = ErrorDTO.class) })
    public Response apiLoggingDelete( @ApiParam(value = "")  @QueryParam("context") String context) throws APIManagementException{
        return delegate.apiLoggingDelete(context, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = APIListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "", response = APIListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving API data to be logged", response = ErrorDTO.class) })
    public Response apiLoggingGet( @ApiParam(value = "")  @QueryParam("context") String context) throws APIManagementException{
        return delegate.apiLoggingGet(context, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while configuring API to be logged", response = ErrorDTO.class) })
    public Response apiLoggingPost(@ApiParam(value = "Request Body" ) APIListDTO payload,  @ApiParam(value = "")  @QueryParam("context") String context,  @ApiParam(value = "", allowableValues="all, headers, body")  @QueryParam("logLevel") String logLevel) throws APIManagementException{
        return delegate.apiLoggingPost(payload, context, logLevel, securityContext);
    }
}
