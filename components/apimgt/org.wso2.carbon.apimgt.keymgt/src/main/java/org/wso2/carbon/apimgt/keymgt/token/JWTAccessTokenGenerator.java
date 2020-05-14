package org.wso2.carbon.apimgt.keymgt.token;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;

public interface JWTAccessTokenGenerator {
    String generateJWT(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException;
}
