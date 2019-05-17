package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;


import org.wso2.carbon.apimgt.rest.api.admin.dto.PublishStatusDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class MonetizationApiServiceImpl extends MonetizationApiService {
    @Override
    public Response monetizationPublishUsagePost(){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
