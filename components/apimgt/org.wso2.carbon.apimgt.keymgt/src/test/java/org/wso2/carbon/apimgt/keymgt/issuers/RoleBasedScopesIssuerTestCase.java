/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.keymgt.handlers.ResourceConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth.common.GrantType;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OAuth2Util.class, RoleBasedScopesIssuer.class, FrameworkUtils.class})
@SuppressStaticInitializationFor("org.wso2.carbon.identity.oauth2.util.OAuth2Util")
public class RoleBasedScopesIssuerTestCase {

    private ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
    private RealmService realmService = Mockito.mock(RealmService.class);
    private CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private TenantManager tenantManager = Mockito.mock(TenantManager.class);
    private Cache cache = Mockito.mock(Cache.class);
    private DefaultRealm defaultRealm = Mockito.mock(DefaultRealm.class);
    private AbstractUserStoreManager abstractUserStoreManager = Mockito.mock(AbstractUserStoreManager.class);

    @Before
    public void init() throws Exception  {
        PowerMockito.mockStatic(OAuth2Util.class);
        PowerMockito.mockStatic(FrameworkUtils.class);
        PowerMockito.when(OAuth2Util.getServiceProvider("clientId", "carbon.super"))
                .thenReturn(new ServiceProvider());
    }

    @Test
    public void testGetPrefix() throws Exception {

        String ISSUER_PREFIX = "default";
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        Assert.assertEquals(ISSUER_PREFIX, roleBasedScopesIssuer.getPrefix());
    }

    @Test
    public void testGetAllowedScopes() throws Exception {

        ArrayList<String> scopeSkipList = new ArrayList<String>();
        ArrayList<String> requestedScopes = new ArrayList<String>();
        scopeSkipList.add("scope 1");
        scopeSkipList.add("scope 2");
        requestedScopes.add("scope 1");
        requestedScopes.add("scope 3");
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        List<String> authorizedScopes = roleBasedScopesIssuer.getAllowedScopes(scopeSkipList, requestedScopes);

        Assert.assertEquals(1, authorizedScopes.size());
        Assert.assertEquals("scope 1", authorizedScopes.get(0));
    }

    @Test
    public void testGetAllowedScopesWhenAuthorizedScopesEmpty() throws Exception {

        ArrayList<String> scopeSkipList = new ArrayList<String>();
        ArrayList<String> requestedScopes = new ArrayList<String>();
        scopeSkipList.add("scope 1");
        scopeSkipList.add("scope 2");
        requestedScopes.add("scope 3");
        requestedScopes.add("scope 4");
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuer();
        List<String> authorizedScopes = roleBasedScopesIssuer.getAllowedScopes(scopeSkipList, requestedScopes);

        Assert.assertEquals(1, authorizedScopes.size());
        Assert.assertEquals("default", authorizedScopes.get(0));
    }

    @Test
    public void testGetScopes() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Map<String, String> appScopes = new HashMap<String, String>();
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication("clientId")).thenReturn(appScopes);
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenRestAPIScopesOfCurrentTenantIsNotNull() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenAppScopesIsEmpty() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenTenantISZero() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(0);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesWhenTenantISMinusOne() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForGrantType() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        tokenDTO.setGrantType(GrantType.SAML20_BEARER.toString());
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);
        System.setProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION, "true");

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForGrantTypeWhenSAML2NotEnabled() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(anyString())).thenReturn(new String[]{});
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        tokenDTO.setGrantType(GrantType.SAML20_BEARER.toString());
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);
        System.setProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION, "false");

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForUserStoreException() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.doThrow(UserStoreException.class).when(tenantManager).getTenantId(anyString());
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> defaultScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, defaultScopes.size());
        Assert.assertEquals("default", defaultScopes.get(0));
    }

    @Test
    public void testGetScopesForRoles() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> scopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("api_view", scopes.get(0));
    }

    @Test
    public void testGetScopesForRolesWhenRestAPIScopesNotNull() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("", "");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> scopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("", scopes.get(0));
    }

    @Test
    public void testGetScopesForAppScopes() throws Exception {

        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito. when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "api_view"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 1");
        whiteListedScopes.add("scope 2");

        List<String> scopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(2, scopes.size());
    }

    @Test
    public void testGetScopesHandleException() throws Exception {

        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getScopeRolesOfApplication(anyString());
        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{"scope 1", "scope 2"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService
                , apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");
        Assert.assertEquals(null, roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes));
    }

    @Test
    public void testGetScopesOfRolesWithSpacesAndCases() throws Exception {
        AbstractUserStoreManager abstractUserStoreManager = Mockito.mock(AbstractUserStoreManager.class);
        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        String[] roles = { "CaseRole", "space role" };
        Mockito.when(abstractUserStoreManager.getRoleListOfUser("caseuser@ADMIN.USER.STORE.DOMAIN")).thenReturn(roles);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[] { "caseScope1", "caseScope2", "spaceScope" });
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("caseuser");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Map<String, String> appScopes = new HashMap<String, String>();
        appScopes.put("caseScope1", "caserole");
        appScopes.put("caseScope2", "CaseRole");
        appScopes.put("spaceScope", "space role");
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication("clientId")).thenReturn(appScopes);
        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService,
                apiMgtDAO);
        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("scope 3");
        whiteListedScopes.add("scope 4");

        List<String> returnedScopes = roleBasedScopesIssuer.getScopes(tokReqMsgCtx, whiteListedScopes);
        Assert.assertEquals(3, returnedScopes.size());
    }

    @Test
    public void testGetScopesForRolesWithOpenIDScope() throws Exception {

        final String tenantDomain = "carbon.super";
        final String clientId = "clientId";
        final String scope = "scope";
        Mockito.when(cacheManager.getCache(anyString())).thenReturn(cache);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt()))
                .thenReturn(defaultRealm);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser(anyString())).thenReturn
                (new String[]{"api_view"});
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
        Map<String, String> restAPIScopes = new HashMap<String, String>();
        restAPIScopes.put("api_view", "api_view");
        Mockito.when(cache.get(anyString())).thenReturn(restAPIScopes);

        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId(clientId);
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[]{scope, "openid"});
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain(tenantDomain);
        authenticatedUser.setUserName("admin");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        authenticatedUser.setFederatedUser(true);

        Map<ClaimMapping, String> userAttributes = new HashMap<ClaimMapping, String>();
        userAttributes.put(buildClaimMapping(), "localRole");

        authenticatedUser.setUserAttributes(userAttributes);
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);

        OAuthCallback oAuthCallback = new OAuthCallback(authenticatedUser, "admin", OAuthCallback.
                OAuthCallbackType.SCOPE_VALIDATION_AUTHZ);
        oAuthCallback.setRequestedScope(new String[]{"openid", scope});

        RoleBasedScopesIssuer roleBasedScopesIssuer = new RoleBasedScopesIssuerWrapper(cacheManager, realmService,
                apiMgtDAO);

        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Map<String, String> appScopes = new HashMap<String, String>();
        appScopes.put(scope, "localRole");
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication(clientId)).thenReturn(appScopes);

        RoleBasedScopesIssuer spy = PowerMockito.spy(roleBasedScopesIssuer);
        PowerMockito.doReturn("role").when(spy, "getOIDCMappedLocalClaimURI", anyString());
        PowerMockito.doReturn(appScopes).when(spy, "getAppScopes", anyString(), any(AuthenticatedUser.class));

        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(MULTI_ATTRIBUTE_SEPARATOR_DEFAULT);

        ArrayList<String> whiteListedScopes = new ArrayList<String>();
        whiteListedScopes.add("openid");

        List<String> scopes = spy.getScopes(oAuthCallback, whiteListedScopes);
        Assert.assertEquals(2, scopes.size());
        Assert.assertTrue(scopes.contains(scope));
    }

    private ClaimMapping buildClaimMapping() {

        ClaimMapping claimMapping = new ClaimMapping();
        Claim claim = new Claim();
        claim.setClaimUri("role");
        claimMapping.setRemoteClaim(claim);
        claimMapping.setLocalClaim(claim);
        return claimMapping;
    }
}
