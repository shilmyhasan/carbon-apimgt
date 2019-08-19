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

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import com.google.common.net.HttpHeaders;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.metrics.manager.Timer;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OAuthAuthenticatorTest {

    private Timer timer;
    private APIKeyValidator apiKeyValidator;

    @Before
    public void init() {
        timer = Mockito.mock(Timer.class);
        Mockito.when(timer.start()).thenReturn(Mockito.mock(Timer.Context.class));
    }

    @Test
    public void initOAuthParams() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        OAuthAuthenticator oauthAuthenticator = new OauthAuthenticatorWrapper(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn("true");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_HEADER))
                .thenReturn("true");
        oauthAuthenticator.initOAuthParams();
    }

    @Test
    public void initOAuthParamsWhileConfigIsCommented() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        OAuthAuthenticator oauthAuthenticator = new OauthAuthenticatorWrapper(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn("false");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_HEADER))
                .thenReturn(null);
        oauthAuthenticator.initOAuthParams();
    }

    @Test
    public void extractCustomerKeyFromAuthHeader() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        OAuthAuthenticator oauthAuthenticator = new OauthAuthenticatorWrapper(apiManagerConfiguration);
        Map map = new HashMap();
        Assert.assertNull("Assertion failure due to not null", oauthAuthenticator.extractCustomerKeyFromAuthHeader
                (map));
        map.put(HttpHeaders.AUTHORIZATION, "Bearer abcde-fghij");
        Assert.assertNotNull(oauthAuthenticator.extractCustomerKeyFromAuthHeader(map), "Assertion failure due to null");
    }

    @Test
    public void authenticate() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        OAuthAuthenticator oauthAuthenticator = new OauthAuthenticatorWrapper(apiManagerConfiguration, timer,
                createAPIKeyValidator(true, "None"));
        org.apache.synapse.MessageContext messageContext = TestUtils.getMessageContextWithOutAuthContext
                ("testAPI", "1.0.0");
        Assert.assertTrue(oauthAuthenticator.authenticate(messageContext));

        OAuthAuthenticator oauthAuthenticatorNoMatchingAuth = new OauthAuthenticatorWrapper(apiManagerConfiguration,
                timer, createAPIKeyValidator(true, "noMatchedAuthScheme"));
        try {
            oauthAuthenticatorNoMatchingAuth.authenticate(messageContext);
            Assert.assertFalse(true);
        } catch (APISecurityException ex) {
            Assert.assertTrue(true);
        }

        org.apache.synapse.MessageContext messageContextNew = TestUtils.getMessageContextWithOutAuthContext
                ("testAPI1", "1.2.0");
        OAuthAuthenticator oauthAuthenticatorApplicationAuth = new OauthAuthenticatorWrapper(apiManagerConfiguration,
                timer, createAPIKeyValidator(true, "Application"));
        Assert.assertEquals(oauthAuthenticatorApplicationAuth.authenticate(messageContextNew), true);


        org.apache.synapse.MessageContext messageContextWithNull = TestUtils.getMessageContextWithOutAuthContext
                ("testAPI1", null);
        OAuthAuthenticator oauthAuthenticatorApplicationAuthForNull = new OauthAuthenticatorWrapper(apiManagerConfiguration,
                timer, createAPIKeyValidator(true, ""));
        Assert.assertEquals(oauthAuthenticatorApplicationAuthForNull.authenticate(messageContextWithNull), true);
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(null);
    }

    @Test
    public void getRequestOrigin() throws Exception {
    }

    @Test
    public void getSecurityHeader() throws Exception {
    }

    @Test
    public void setSecurityHeader() throws Exception {
    }

    @Test
    public void getDefaultAPIHeader() throws Exception {
    }

    @Test
    public void setDefaultAPIHeader() throws Exception {
    }

    @Test
    public void getConsumerKeyHeaderSegment() throws Exception {
    }

    @Test
    public void setConsumerKeyHeaderSegment() throws Exception {
    }

    @Test
    public void getOauthHeaderSplitter() throws Exception {
    }

    @Test
    public void setOauthHeaderSplitter() throws Exception {
    }

    @Test
    public void getConsumerKeySegmentDelimiter() throws Exception {
    }

    @Test
    public void setConsumerKeySegmentDelimiter() throws Exception {
    }

    @Test
    public void getSecurityContextHeader() throws Exception {
    }

    @Test
    public void setSecurityContextHeader() throws Exception {
    }

    @Test
    public void isRemoveOAuthHeadersFromOutMessage() throws Exception {
    }

    @Test
    public void setRemoveOAuthHeadersFromOutMessage() throws Exception {
    }

    @Test
    public void getClientDomainHeader() throws Exception {
    }

    @Test
    public void setClientDomainHeader() throws Exception {
    }

    @Test
    public void isRemoveDefaultAPIHeaderFromOutMessage() throws Exception {
    }

    @Test
    public void setRemoveDefaultAPIHeaderFromOutMessage() throws Exception {
    }

    @Test
    public void setRequestOrigin() throws Exception {
    }

    private APIKeyValidator createAPIKeyValidator(final boolean isWithEmptyCache, final String authScheme) {
        AxisConfiguration axisConfig = Mockito.mock(AxisConfiguration.class);
        return new APIKeyValidator(axisConfig) {
            @Override
            protected String getKeyValidatorClientType() {
                return "thriftClient";
            }

            @Override
            protected APIManagerConfiguration getApiManagerConfiguration() {
                APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
                Mockito.when(configuration.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY)).thenReturn("900");
                Mockito.when(configuration.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED)).
                        thenReturn("true");
                return configuration;
            }

            @Override
            protected Cache getCache(String cacheManagerName, String cacheName, long modifiedExp, long accessExp) {
                return Mockito.mock(Cache.class);
            }

            @Override
            protected long getDefaultCacheTimeout() {
                return 900L;
            }

            @Override
            protected Cache getCacheFromCacheManager(String cacheName) {
                if (isWithEmptyCache) {
                    return Mockito.mock(Cache.class);
                } else {
                    Cache cache = Mockito.mock(Cache.class);
                    VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                    verbInfoDTO.setHttpVerb("get");
                    verbInfoDTO.setAuthType("None");
                    if (cacheName.equals("resourceCache")) {
                        Mockito.when(cache.get("abc")).thenReturn(verbInfoDTO);
                    } else if (cacheName.equals("GATEWAY_TOKEN_CACHE")) {
                        Mockito.when(cache.get("abc")).thenReturn("token");
                    }
                    return cache;
                }

            }

            @Override
            protected ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion)
                    throws APISecurityException {
                ArrayList<URITemplate> urlTemplates = new ArrayList<>();
                URITemplate template = new URITemplate();
                template.setUriTemplate("/*");
                template.setHTTPVerb("https");
                urlTemplates.add(template);
                return urlTemplates;
            }

            @Override
            protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, String apiKey,
                                                                     String authenticationScheme, String clientDomain,
                                                                     String matchingResource, String httpVerb)
                    throws APISecurityException {
                APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
                info.setAuthorized(true);
                info.setEndUserName("admin");
                info.setApplicationName("test-app");
                info.setApiPublisher("admin");
                return info;
            }

            @Override
            protected String getTenantDomain() {
                return "carbon.super";
            }

            @Override
            public String getResourceAuthenticationScheme(MessageContext synCtx) throws APISecurityException {
                if (authScheme.equalsIgnoreCase("noMatchedAuthScheme")) {
                    return "noMatchedAuthScheme";
                } else if(authScheme.equalsIgnoreCase("Application")) {
                    return "Application";
                }
                else {
                    return "None";
                }
            }
        };
    }

}