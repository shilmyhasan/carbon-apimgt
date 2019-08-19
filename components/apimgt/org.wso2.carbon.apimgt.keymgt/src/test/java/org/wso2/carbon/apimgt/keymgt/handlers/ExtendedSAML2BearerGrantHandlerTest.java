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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OauthTokenIssuer;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthServerConfiguration.class, ScopesIssuer.class})
public class ExtendedSAML2BearerGrantHandlerTest {
    @Test
    public void testValidateScope() throws Exception {
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(ScopesIssuer.class);

        OAuthTokenReqMessageContext tokenReqMessageContext = Mockito.mock(OAuthTokenReqMessageContext.class);
        OAuthServerConfiguration authServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        OauthTokenIssuer tokenIssuer = Mockito.mock(OauthTokenIssuer.class);
        ScopesIssuer scopesIssuer = Mockito.mock(ScopesIssuer.class);
        AuthenticatedUser authenticatedUser = Mockito.mock(AuthenticatedUser.class);

        Mockito.when(authServerConfiguration.getInstance()).thenReturn(authServerConfiguration);
        Mockito.when(authServerConfiguration.getIdentityOauthTokenIssuer()).thenReturn(tokenIssuer);
        Mockito.when(scopesIssuer.getInstance()).thenReturn(scopesIssuer);
        Mockito.doReturn(true).when(scopesIssuer).setScopes((OAuthTokenReqMessageContext)Mockito.anyObject());

        ExtendedSAML2BearerGrantHandler saml2BearerGrantHandler = new ExtendedSAML2BearerGrantHandler();

        //when CHECK_ROLES_FROM_SAML_ASSERTION is false
        saml2BearerGrantHandler.validateScope(tokenReqMessageContext);

        //when CHECK_ROLES_FROM_SAML_ASSERTION is true
        System.setProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION, "true");
        Mockito.when(tokenReqMessageContext.getAuthorizedUser()).thenReturn(authenticatedUser);
        saml2BearerGrantHandler.validateScope(tokenReqMessageContext);
    }

}