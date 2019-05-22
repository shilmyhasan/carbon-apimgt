package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.Monetization;
import org.wso2.carbon.apimgt.impl.MonetizationImpl;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;


import org.wso2.carbon.apimgt.rest.api.admin.dto.PublishStatusDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.MonetizationAPIMappinUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class MonetizationApiServiceImpl extends MonetizationApiService {

    private static final Log log = LogFactory.getLog(MonetizationApiServiceImpl.class);
    @Override
    public Response monetizationPublishUsagePost(){
        PublishStatusDTO publishStatusDTO;
        MonetizationImpl monetizationImpl = new MonetizationImpl();
        Response response = monetizationImpl.publishMonetizationUsageRecord();
        if(response.getStatus() == 200 )
        {
            String status = "Successfull";
            String msg = "All Usage Records Published Successfully";
            publishStatusDTO = MonetizationAPIMappinUtil.fromStatusToDTO(status, msg);
            return Response.ok().entity(publishStatusDTO).build();
        }else if(response.getStatus() == 200 && response.getEntity().equals("Partially Successfull")) {
            String status = " Partially Successfull";
            String msg = "All Usage Records were not Published Successfully";
            publishStatusDTO = MonetizationAPIMappinUtil.fromStatusToDTO(status, msg);
            return Response.ok().entity(publishStatusDTO).build();
        } else  if(response.getStatus() == 403){
            String status = " UnSuccessfull";
            String msg = response.getEntity().toString();
            publishStatusDTO = MonetizationAPIMappinUtil.fromStatusToDTO(status, msg);
            return Response.serverError().entity(publishStatusDTO).build();
        } else if( response.getStatus() == 500) {
            String status = " UnSuccessfull";
            String msg = response.getEntity().toString();
            publishStatusDTO = MonetizationAPIMappinUtil.fromStatusToDTO(status, msg);
            return Response.serverError().entity(publishStatusDTO).build();
        }
        return null;
    }
}
