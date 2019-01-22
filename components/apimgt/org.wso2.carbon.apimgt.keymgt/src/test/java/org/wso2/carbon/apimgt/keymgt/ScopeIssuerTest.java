/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.keymgt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiMgtDAO.class, APIKeyMgtDataHolder.class, Caching.class, AbstractUserStoreManager.class})
public class ScopeIssuerTest {

    @Test
    public void testGetScopesOfRolesWithSpacesAndCases() throws Exception {
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        DefaultRealm defaultRealm = Mockito.mock(DefaultRealm.class);
        AbstractUserStoreManager abstractUserStoreManager = Mockito.mock(AbstractUserStoreManager.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
//        mock ApiMgtDAO
        PowerMockito.mockStatic(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        /* mock APIKeyMgtDataHolder to get realm service, tenant manager, tenant id, tenant user realm and
        user store manager fo user realm */
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        Mockito.when(APIKeyMgtDataHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(-1234);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(defaultRealm);
        Mockito.when(defaultRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);
//        mock AbstractUserStoreManager to return user roles
        String[] roles = { "CaseRole", "space role" };
        PowerMockito.whenNew(AbstractUserStoreManager.class).withAnyArguments().thenReturn(abstractUserStoreManager);
        Mockito.when(abstractUserStoreManager.getRoleListOfUser("ADMIN.USER.STORE.DOMAIN/caseuser"))
                .thenReturn(roles);
//        mock Caching to set restAPIScopes for tenant domain
        PowerMockito.mockStatic(Caching.class);
        Mockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(cache.get(Mockito.anyString())).thenReturn(new HashMap<String, String>());
//        create access token request DTO with user details
        OAuth2AccessTokenReqDTO tokenDTO = new OAuth2AccessTokenReqDTO();
        tokenDTO.setClientId("clientId");
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenDTO);
        tokReqMsgCtx.setScope(new String[] { "caseScope1", "caseScope2", "spaceScope" });
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("caseuser");
        authenticatedUser.setUserStoreDomain("admin.user.store.domain");
        tokReqMsgCtx.setAuthorizedUser(authenticatedUser);
//        set application scopes
        Map<String, String> appScopes = new HashMap<String, String>();
        appScopes.put("caseScope1", "caserole");
        appScopes.put("caseScope2", "CaseRole");
        appScopes.put("spaceScope", "space role");
        Mockito.when(apiMgtDAO.getScopeRolesOfApplication("clientId")).thenReturn(appScopes);
//        create scopeIssuer instance with empty whitelist/scopeSkipList
        ScopesIssuer.loadInstance(new ArrayList<String>());
        ScopesIssuer scopesIssuer = ScopesIssuer.getInstance();
//        test scopes for case insensitive
        boolean setScopes = scopesIssuer.setScopes(tokReqMsgCtx);
        Assert.assertTrue(setScopes);
        Assert.assertEquals(3, tokReqMsgCtx.getScope().length);
//        test scopes for case sensitive
        System.setProperty("preservedCaseSensitive", "true");
        setScopes = scopesIssuer.setScopes(tokReqMsgCtx);
        Assert.assertTrue(setScopes);
        Assert.assertEquals(2, tokReqMsgCtx.getScope().length);
    }
}
