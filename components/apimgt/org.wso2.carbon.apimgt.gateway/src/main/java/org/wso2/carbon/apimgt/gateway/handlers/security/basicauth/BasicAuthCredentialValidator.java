/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This class will validate the basic auth credentials.
 */
public class BasicAuthCredentialValidator {

    private boolean gatewayKeyCacheEnabled;
    private static boolean gatewayUsernameCacheInit = false;
    private static boolean gatewayBasicAuthResourceCacheInit = false;

    protected Log log = LogFactory.getLog(getClass());
    private AuthenticationAdminStub authAdminStub;
    private RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;
    private String host;

    /**
     * Initialize the validator with the synapse environment.
     *
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    BasicAuthCredentialValidator() throws APISecurityException {
        this.gatewayKeyCacheEnabled = isGatewayTokenCacheEnabled();
        this.getGatewayUsernameCache();

        ConfigurationContext configurationContext = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        if (url == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "API key manager URL unspecified");
        }

        try {
            authAdminStub = new AuthenticationAdminStub(configurationContext, url +
                    "AuthenticationAdmin");
        } catch (AxisFault axisFault) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, axisFault.getMessage(), axisFault);
        }

        try {
            remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(configurationContext, url +
                    "RemoteUserStoreManagerService");
        } catch (AxisFault axisFault) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, axisFault.getMessage(), axisFault);
        }
        ServiceClient svcClient = remoteUserStoreManagerServiceStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders(config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME),
                config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD), svcClient);

        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Return the resource authentication scheme of the API resource.
     *
     * @param swagger swagger of the API
     * @param synCtx  The message to be authenticated
     * @return the resource authentication scheme
     */
    public String getResourceAuthenticationScheme(Swagger swagger, MessageContext synCtx) {
        String authType = null;
        Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);
        if (vendorExtensions != null) {
            authType = (String) vendorExtensions.get(APIConstants.SWAGGER_X_AUTH_TYPE);
        }

        if (StringUtils.isNotBlank(authType)) {
            return authType;
        }
        return APIConstants.NO_MATCHING_AUTH_SCHEME;
    }

    /**
     * Validates the given username and password against the users in the user store.
     *
     * @param username given username
     * @param password given password
     * @return true if the validation passed
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    @MethodStats
    public boolean validate(String username, String password) throws APISecurityException {
        String providedPasswordHash = null;
        if (gatewayKeyCacheEnabled) {
            providedPasswordHash = hashString(password);
            String cachedPasswordHash = (String) getGatewayUsernameCache().get(username);
            if (cachedPasswordHash != null && cachedPasswordHash.equals(providedPasswordHash)) {
                log.debug("Basic Authentication: <Valid Username Cache> Username & password authenticated");
                return true; //If (username->password) is in the valid cache
            } else {
                String invalidCachedPasswordHash = (String) getInvalidUsernameCache().get(username);
                if (invalidCachedPasswordHash != null && invalidCachedPasswordHash.equals(providedPasswordHash)) {
                    log.debug("Basic Authentication: <Invalid Username Cache> Username & password authentication failed");
                    return false; //If (username->password) is in the invalid cache
                }
            }
        }

        boolean authenticated;
        try {
            authenticated = authAdminStub.login(username, password, host);
        } catch (RemoteException | LoginAuthenticationExceptionException e) {
            log.debug("Basic Authentication: Username and Password authentication failure");
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }

        if (gatewayKeyCacheEnabled) {
            if (authenticated) {
                // put (username->password) into the valid cache
                getGatewayUsernameCache().put(username, providedPasswordHash);
            } else {
                // put (username->password) into the invalid cache
                getInvalidUsernameCache().put(username, providedPasswordHash);
            }
        }

        return authenticated;
    }

    /**
     * Validates the roles of the given user against the roles of the scopes of the API resource.
     *
     * @param username given username
     * @param swagger  swagger of the API
     * @param synCtx   The message to be authenticated
     * @return true if the validation passed
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    @MethodStats
    public boolean validateScopes(String username, Swagger swagger, MessageContext synCtx) throws APISecurityException {
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String apiElectedResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
        if (swagger != null) {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            String httpMethod = (String) axis2MessageContext.getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD);
            String resourceKey = apiElectedResource + ":" + httpMethod;
            String resourceCacheKey = resourceKey + ":" + username;
            if (gatewayKeyCacheEnabled && getGatewayBasicAuthResourceCache().get(resourceCacheKey) != null) {
                return true;
            } else {
                // retrieve the user roles related to the scope of the API resource
                String resourceRoles = null;
                Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);
                if (vendorExtensions != null) {
                    resourceRoles = (String) vendorExtensions.get(APIConstants.SWAGGER_X_ROLES);
                }

                if (StringUtils.isNotBlank(resourceRoles)) {
                    String[] userRoles;

                    String tenantDomain = MultitenantUtils.getTenantDomain(username);
                    if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        try {
                            userRoles = remoteUserStoreManagerServiceStub
                                    .getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
                        } catch (RemoteException | RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage());
                        }
                    } else {
                        try {
                            int tenantId = ServiceReferenceHolder.getInstance()
                                    .getRealmService().getTenantManager().getTenantId(tenantDomain);

                            UserStoreManager manager = ServiceReferenceHolder
                                    .getInstance().getRealmService()
                                    .getTenantUserRealm(tenantId).getUserStoreManager();

                            userRoles = manager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
                        } catch (UserStoreException e) {
                            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage());
                        }
                    }
                    // check if the roles related to the API resource contains any of the role of the user
                    for (String role : userRoles) {
                        if (resourceRoles.contains(role)) {
                            if (gatewayKeyCacheEnabled) {
                                getGatewayBasicAuthResourceCache().put(resourceCacheKey, resourceKey);
                            }
                            return true;
                        }
                    }
                } else {
                    // No scopes for the requested resource
                    if (gatewayKeyCacheEnabled) {
                        getGatewayBasicAuthResourceCache().put(resourceCacheKey, resourceKey);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Basic Authentication: No scopes for the API resource: ".concat(resourceKey));
                    }
                    return true;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: No swagger found in the gateway for the API: ".concat(apiContext)
                        .concat(":").concat(apiVersion));
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Basic Authentication: Scope validation failed for the API resource: ".concat(apiElectedResource));
        }
        throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
    }

    /**
     * Return the throttling tier of the API resource.
     *
     * @param swagger swagger of the API
     * @param synCtx  The message to be authenticated
     * @return the resource throttling tier
     */
    public String getResourceThrottlingTier(Swagger swagger, MessageContext synCtx) {
        String throttlingTier = null;
        Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);
        if (vendorExtensions != null) {
            throttlingTier = (String) vendorExtensions.get(APIConstants.SWAGGER_X_THROTTLING_TIER);
        }
        if (StringUtils.isNotBlank(throttlingTier)) {
            return throttlingTier;
        }
        return APIConstants.UNLIMITED_TIER;
    }

    private Map<String, Object> getVendorExtensions(MessageContext synCtx, Swagger swagger) {
        if (swagger != null) {
            String apiElectedResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            String httpMethod = (String) axis2MessageContext.getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD);
            Path path = swagger.getPath(apiElectedResource);
            if (path != null) {
                switch (httpMethod) {
                    case APIConstants.HTTP_GET:
                        return path.getGet().getVendorExtensions();
                    case APIConstants.HTTP_POST:
                        return path.getPost().getVendorExtensions();
                    case APIConstants.HTTP_PUT:
                        return path.getPut().getVendorExtensions();
                    case APIConstants.HTTP_DELETE:
                        return path.getDelete().getVendorExtensions();
                    case APIConstants.HTTP_HEAD:
                        return path.getHead().getVendorExtensions();
                    case APIConstants.HTTP_OPTIONS:
                        return path.getOptions().getVendorExtensions();
                    case APIConstants.HTTP_PATCH:
                        return path.getPatch().getVendorExtensions();
                }
            }
        }
        return null;
    }

    /**
     * Returns the md5 hash of a given string.
     *
     * @param str the string input to be hashed
     * @return hashed string
     */
    private String hashString(String str) {
        String generatedHash = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add str bytes to digest
            md.update(str.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed str in hex format
            generatedHash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        return generatedHash;
    }

    /**
     * Returns the basic authenticated resource request cache.
     *
     * @return the resource cache
     */
    private Cache getGatewayBasicAuthResourceCache() {
        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        if (!gatewayBasicAuthResourceCacheInit) {
            gatewayBasicAuthResourceCacheInit = true;
            if (apimGWCacheExpiry != null) {
                return createCache(APIConstants.GATEWAY_BASIC_AUTH_RESOURCE_CACHE_NAME,
                        Long.parseLong(apimGWCacheExpiry), Long.parseLong(apimGWCacheExpiry));
            } else {
                long defaultCacheTimeout =
                        getDefaultCacheTimeout();
                return createCache(APIConstants.GATEWAY_BASIC_AUTH_RESOURCE_CACHE_NAME,
                        defaultCacheTimeout, defaultCacheTimeout);
            }
        }
        return getCacheFromCacheManager(APIConstants.GATEWAY_BASIC_AUTH_RESOURCE_CACHE_NAME);
    }

    /**
     * Returns the valid username cache.
     *
     * @return the valid username cache
     */
    private Cache getGatewayUsernameCache() {
        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        if (!gatewayUsernameCacheInit) {
            gatewayUsernameCacheInit = true;
            if (apimGWCacheExpiry != null) {
                return createCache(APIConstants.GATEWAY_USERNAME_CACHE_NAME,
                        Long.parseLong(apimGWCacheExpiry), Long.parseLong(apimGWCacheExpiry));
            } else {
                long defaultCacheTimeout =
                        getDefaultCacheTimeout();
                return createCache(APIConstants.GATEWAY_USERNAME_CACHE_NAME,
                        defaultCacheTimeout, defaultCacheTimeout);
            }
        }
        return getCacheFromCacheManager(APIConstants.GATEWAY_USERNAME_CACHE_NAME);
    }

    /**
     * Returns the invalid username cache.
     *
     * @return the invalid username cache
     */
    private Cache getInvalidUsernameCache() {
        String apimGWCacheExpiry = getApiManagerConfiguration().
                getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);

        if (!gatewayUsernameCacheInit) {
            gatewayUsernameCacheInit = true;
            if (apimGWCacheExpiry != null) {
                return createCache(APIConstants.GATEWAY_INVALID_USERNAME_CACHE_NAME,
                        Long.parseLong(apimGWCacheExpiry), Long.parseLong(apimGWCacheExpiry));
            } else {
                long defaultCacheTimeout = getDefaultCacheTimeout();
                return createCache(APIConstants.GATEWAY_INVALID_USERNAME_CACHE_NAME,
                        defaultCacheTimeout, defaultCacheTimeout);
            }
        }
        return getCacheFromCacheManager(APIConstants.GATEWAY_INVALID_USERNAME_CACHE_NAME);
    }

    /**
     * Create the Cache object from the given parameters.
     *
     * @param cacheName   name of the Cache
     * @param modifiedExp value of the modified expiry type
     * @param accessExp   value of the accessed expiry type
     * @return the cache object
     */
    private Cache createCache(final String cacheName, final long modifiedExp,
                              long accessExp) {
        return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER, cacheName, modifiedExp, accessExp);
    }

    /**
     * Returns the API Manager Configuration.
     *
     * @return the API Manager Configuration
     */
    private APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    /**
     * Returns the Cache object of the given name.
     *
     * @param cacheName name of the Cache
     * @return the cache object
     */
    private Cache getCacheFromCacheManager(String cacheName) {
        return Caching.getCacheManager(
                APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName);
    }

    /**
     * Returns the default cache timeout.
     *
     * @return the default cache timeout
     */
    private long getDefaultCacheTimeout() {
        return Long.valueOf(ServerConfiguration.getInstance().getFirstProperty(APIConstants.DEFAULT_CACHE_TIMEOUT))
                * 60;
    }

    /**
     * Returns whether the gateway token cache is enabled.
     *
     * @return true if the gateway token cache is enabled
     */
    private boolean isGatewayTokenCacheEnabled() {
        try {
            APIManagerConfiguration config = getApiManagerConfiguration();
            String cacheEnabled = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            return Boolean.parseBoolean(cacheEnabled);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration." +
                    " Use default configuration " + e, e);
        }
        return true;
    }
}