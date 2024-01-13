/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.OAuthTokenGenerator;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.TokenCache;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.OAuthClient;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.conf.OAuthEndpoint;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuthClient.class, OAuthTokenGenerator.class, TokenCache.class, ServiceReferenceHolder.class})
public class OAuthTokenGeneratorTest {

    private TokenResponse mockTokenResponse;
    private TokenCache tokenCache;
    private CountDownLatch latch;
    private OAuthEndpoint oAuthEndpoint;

    @Before
    public void setup() throws ParseException, IOException, APIManagementException {

        PowerMockito.spy(TokenCache.class);
        tokenCache = TokenCache.getInstance();
        PowerMockito.when(TokenCache.getInstance()).thenReturn(tokenCache);
        PowerMockito.mockStatic(OAuthClient.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        latch = new CountDownLatch(1);
        // Initialize mock token response.
        mockTokenResponse = new TokenResponse();
        mockTokenResponse.setAccessToken("testAccessToken");
        mockTokenResponse.setTokenType("Bearer");

        // Initialize properties of oAuthEndpoint object having common values.
        oAuthEndpoint = new OAuthEndpoint();
        oAuthEndpoint.setTokenApiUrl("testTokenURL");
        oAuthEndpoint.setClientId("testClientID");
        oAuthEndpoint.setClientSecret("decryptedClientSecret");
        JSONParser parser = new JSONParser();
        oAuthEndpoint.setCustomParameters((JSONObject) parser.parse("{}"));
    }
}
