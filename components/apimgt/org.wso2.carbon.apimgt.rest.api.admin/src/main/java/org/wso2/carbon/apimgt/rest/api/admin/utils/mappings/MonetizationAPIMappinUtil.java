package org.wso2.carbon.apimgt.rest.api.admin.utils.mappings;

import org.wso2.carbon.apimgt.rest.api.admin.dto.PublishStatusDTO;

public class MonetizationAPIMappinUtil {

    public static PublishStatusDTO fromStatusToDTO(String status, String msg){
        PublishStatusDTO publishStatusDTO = new PublishStatusDTO();
        publishStatusDTO.setStatus(status);
        publishStatusDTO.setMessage(msg);
        return publishStatusDTO;
    }

}
