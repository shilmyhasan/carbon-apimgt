/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.gateway.handlers.security;


import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.UUID;
import javax.cache.Cache;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class APIKeyValidatorTest {
    String context = "/abc/1.0.0";
    String apiKey = UUID.randomUUID().toString();
    String apiVersion = "1.0.0";
    String authenticationScheme = "ANY";
    String clientDomain = "";
    String matchingResource = "/abc";
    String httpVerb = "GET";
    boolean defaultVersionInvoked = false;

    @Before
    public void setup() {
        System.setProperty("carbon.home", APIKeyValidatorTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(APIUtil.class);
    }

    // Test for first time invocation for valid token
    // Expectation: Token get cached in token cache and @APIKeyValidationInfoDTO cache in key cache
    // Neither invalid token cache get called in put/remove
    @Test
    public void testCheckForValidToken() throws APISecurityException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(true);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(invalidTokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(apiKeyDataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                    clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(1)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(1)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(1)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // Test for first time invocation for valid token for Tenant
    // Expectation : token need to put into token cache at super tenant,tenant and put @APIKeyValidationInfoDTO to cache
    @Test
    public void testCheckForValidTokenForTenant() throws APISecurityException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain("abc.com");
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(true);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(invalidTokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(apiKeyDataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                    clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(2)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(1)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(1)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // Test case for Invalid,expired,revoked tokens when first time invocation
    // Expectation : invalid token need to put into invalid token cache
    @Test
    public void testCheckForInValidToken() throws APISecurityException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(invalidTokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(apiKeyDataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                    clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(1)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // Test case for Invalid,expired,revoked tokens when first time invocation
    // Expectation : invalid token need to put into invalid token cache in tenant and super tenant
    @Test
    public void testCheckForInValidTokenInTenant() throws APISecurityException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain("abc.com");
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(invalidTokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(apiKeyDataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                    clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(2)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(1)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    // Token is valid in cache
    // Expectation : token get from token cache is not null then get from key cache check token is expired then send
    // Token not accessed or insert into invalid token cache
    @Test
    public void testCheckForValidTokenWhileTokenInCache() throws APISecurityException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(true);
            PowerMockito.when(APIUtil.isAccessTokenExpired(apiKeyValidationInfoDTO)).thenReturn(false);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn("carbon.super");
            Mockito.when(keyCache.get(Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(0)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // Token is expired in cache
    // Expectation : token get from token cache then get from key cache check token is expiry
    // remove from key cache remove from token cache put into invalid token cache
    @Test
    public void testCheckForExpiredTokenWhileTokenInCache() throws APISecurityException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(true);
            PowerMockito.when(APIUtil.isAccessTokenExpired(apiKeyValidationInfoDTO)).thenReturn(true);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn("carbon.super");
            Mockito.when(keyCache.get(Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
            Mockito.when(apiKeyDataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                    clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(1)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(1)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(0)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    // Token is expired in cache
    // Expectation : token get from token cache then get from key cache check token is expiry
    // remove from key cache remove from token cache put into invalid token cache
    @Test
    public void testCheckForRevokedTokenWhereAlreadyGetCached() throws APISecurityException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setAuthorized(true);
            PowerMockito.when(APIUtil.isAccessTokenExpired(apiKeyValidationInfoDTO)).thenReturn(true);
            AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
            Cache tokenCache = Mockito.mock(Cache.class);
            Cache keyCache = Mockito.mock(Cache.class);
            Cache resourceCache = Mockito.mock(Cache.class);
            Cache invalidTokenCache = Mockito.mock(Cache.class);
            APIKeyDataStore apiKeyDataStore = Mockito.mock(APIKeyDataStore.class);
            APIKeyValidator apiKeyValidator = new APIKeyValidatorWrapper(axisConfiguration, invalidTokenCache,
                    tokenCache, keyCache, resourceCache);
            apiKeyValidator.dataStore = apiKeyDataStore;
            Mockito.when(tokenCache.get(Mockito.anyString())).thenReturn(null);
            Mockito.when(invalidTokenCache.get(Mockito.anyString())).thenReturn("carbon.super");
            Mockito.when(keyCache.get(Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
            Mockito.when(apiKeyDataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme,
                    clientDomain, matchingResource, httpVerb)).thenReturn(apiKeyValidationInfoDTO);
            apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpVerb, defaultVersionInvoked);
            Mockito.verify(tokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(1)).get(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).get(Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).put(Mockito.any(APIKeyValidationInfoDTO.class), Mockito
                    .anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).put(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(tokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(invalidTokenCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(keyCache, Mockito.times(0)).remove(Mockito.anyString());
            Mockito.verify(apiKeyDataStore, Mockito.times(0)).getAPIKeyData(context, apiVersion, apiKey,
                    authenticationScheme, clientDomain, matchingResource, httpVerb);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


}