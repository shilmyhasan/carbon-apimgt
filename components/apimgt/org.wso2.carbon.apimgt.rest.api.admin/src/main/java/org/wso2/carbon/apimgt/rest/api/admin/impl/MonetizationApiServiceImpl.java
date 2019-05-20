package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.Monetization;
import org.wso2.carbon.apimgt.impl.MonetizationImpl;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;


import org.wso2.carbon.apimgt.rest.api.admin.dto.PublishStatusDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class MonetizationApiServiceImpl extends MonetizationApiService {
    @Override
    public Response monetizationPublishUsagePost(){
        /*MonetizationImpl monetizationImpl = new MonetizationImpl();
        Response response = monetizationImpl.publishMonetizationUsageRecord();*/
        PublishStatusDTO publishStatusDTO = new PublishStatusDTO();
        return Response.ok().entity(publishStatusDTO).build();
    }
}
