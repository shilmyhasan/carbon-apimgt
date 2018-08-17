/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthServerConfiguration.class, String.class, ScopesIssuer.class})
public class ExtendedClientCredentialsGrantHandlerTest {

    @Test
    public void testAuthorizeAccessDelegation() throws Exception {
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = Mockito.mock(OAuth2AccessTokenReqDTO.class);
        PowerMockito.when(oAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);

        RequestParameter parameter1 = new RequestParameter("validity_period", "3600");
        RequestParameter[] requestParameters = {parameter1};
        OAuthTokenReqMessageContext authTokenReqMessageContext1 = new OAuthTokenReqMessageContext(oAuth2AccessTokenReqDTO);
        Mockito.when(oAuth2AccessTokenReqDTO.getRequestParameters()).thenReturn(requestParameters);
        ExtendedClientCredentialsGrantHandler eccGrantHandler = new ExtendedClientCredentialsGrantHandler();
        Assert.assertTrue(eccGrantHandler.authorizeAccessDelegation(authTokenReqMessageContext1));
        Assert.assertEquals(3600L, authTokenReqMessageContext1.getValidityPeriod());

        OAuthTokenReqMessageContext authTokenReqMessageContext2 = new OAuthTokenReqMessageContext(oAuth2AccessTokenReqDTO);
        Mockito.when(oAuth2AccessTokenReqDTO.getRequestParameters()).thenReturn(null);
        Assert.assertTrue(eccGrantHandler.authorizeAccessDelegation(authTokenReqMessageContext2));
        Assert.assertEquals(-1L, authTokenReqMessageContext2.getValidityPeriod());
    }

    @Test
    public void testValidateGrant() throws Exception {
        PowerMockito.mockStatic(OAuthServerConfiguration.class);

        OAuthTokenReqMessageContext oAuthTokenReqMessageContext = Mockito.mock(OAuthTokenReqMessageContext.class);
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = Mockito.mock(OAuth2AccessTokenReqDTO.class);
        AuthenticatedUser authenticatedUser = Mockito.mock(AuthenticatedUser.class);

        String[] scopes = {"api_view", "api_update"};
        Mockito.when(oAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        Mockito.doNothing().when(oAuthTokenReqMessageContext).setScope(scopes);
        Mockito.when(oAuthTokenReqMessageContext.getOauth2AccessTokenReqDTO()).thenReturn(oAuth2AccessTokenReqDTO);
        Mockito.when(oAuth2AccessTokenReqDTO.getScope()).thenReturn(scopes);
        Mockito.when(oAuthTokenReqMessageContext.getAuthorizedUser()).thenReturn(authenticatedUser);
        Mockito.when(authenticatedUser.getUserName()).thenReturn("abcd");

        Assert.assertTrue("abcd".equals(authenticatedUser.getUserName()));
        ExtendedClientCredentialsGrantHandler eccGrantHandler = new ExtendedClientCredentialsGrantHandler();
        eccGrantHandler.validateGrant(oAuthTokenReqMessageContext);
    }

    @Test
    public void testValidateScope() throws Exception {
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(ScopesIssuer.class);

        String[] scopes = {"api_view", "api_update"};

        OAuthTokenReqMessageContext oAuthTokenReqMessageContext = Mockito.mock(OAuthTokenReqMessageContext.class);
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        ScopesIssuer scopesIssuer = Mockito.mock(ScopesIssuer.class);

        Mockito.when(oAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        Mockito.when(scopesIssuer.getInstance()).thenReturn(scopesIssuer);
        Mockito.when(scopesIssuer.setScopes(oAuthTokenReqMessageContext)).thenReturn(true);
        Mockito.when(oAuthTokenReqMessageContext.getScope()).thenReturn(scopes);

        ExtendedClientCredentialsGrantHandler eccgHandler = new ExtendedClientCredentialsGrantHandler();
        Assert.assertTrue(eccgHandler.validateScope(oAuthTokenReqMessageContext));
    }

}