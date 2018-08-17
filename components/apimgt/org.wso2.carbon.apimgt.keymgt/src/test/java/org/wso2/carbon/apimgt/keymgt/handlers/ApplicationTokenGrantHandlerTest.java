/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.apimgt.keymgt.handlers;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthServerConfiguration.class, String.class})
public class ApplicationTokenGrantHandlerTest {
    @Test
    public void testAuthorizeAccessDelegation() throws Exception {
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        OAuthServerConfiguration authServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        OAuth2AccessTokenReqDTO accessTokenReqDTO = Mockito.mock(OAuth2AccessTokenReqDTO.class);
        RequestParameter parameter1 = new RequestParameter("validity_period", "3600");
        RequestParameter[] requestParameters1 = {parameter1};
        OAuthTokenReqMessageContext authTokenReqMessageContext1 = new OAuthTokenReqMessageContext(accessTokenReqDTO);
        PowerMockito.when(authServerConfiguration.getInstance()).thenReturn(authServerConfiguration);
        Mockito.when(accessTokenReqDTO.getRequestParameters()).thenReturn(requestParameters1);

        ApplicationTokenGrantHandler tokenGrantHandler = new ApplicationTokenGrantHandler();
        Assert.assertTrue(tokenGrantHandler.authorizeAccessDelegation(authTokenReqMessageContext1));
        Assert.assertEquals(3600L, authTokenReqMessageContext1.getValidityPeriod());

        RequestParameter parameter2 = new RequestParameter("validity_period", "0");
        RequestParameter[] requestParameters2 = {parameter2};
        OAuthTokenReqMessageContext authTokenReqMessageContext2 = new OAuthTokenReqMessageContext(accessTokenReqDTO);
        Mockito.when(accessTokenReqDTO.getRequestParameters()).thenReturn(requestParameters2);
        Assert.assertTrue(tokenGrantHandler.authorizeAccessDelegation(authTokenReqMessageContext2));
        Assert.assertEquals(-1L, authTokenReqMessageContext2.getValidityPeriod());

        Mockito.when(accessTokenReqDTO.getRequestParameters()).thenReturn(null);
        OAuthTokenReqMessageContext authTokenReqMessageContext3 = new OAuthTokenReqMessageContext(accessTokenReqDTO);
        Assert.assertTrue(tokenGrantHandler.authorizeAccessDelegation(authTokenReqMessageContext3));
        Assert.assertEquals(-1L, authTokenReqMessageContext3.getValidityPeriod());
    }

}