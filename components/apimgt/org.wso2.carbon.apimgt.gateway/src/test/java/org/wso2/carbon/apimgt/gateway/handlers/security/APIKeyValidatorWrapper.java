package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.cache.Cache;

public class APIKeyValidatorWrapper extends APIKeyValidator {
    private Cache invalidTokenCache;
    private Cache tokenCache;
    private Cache keyCache;
    private Cache resourceCache;
    private APIKeyDataStore apiKeyDataStore;

    public APIKeyValidatorWrapper(AxisConfiguration axisConfig, Cache invalidTokenCache, Cache tokenCache, Cache
            keyCache, Cache resourceCache) {
        super(axisConfig);
        this.invalidTokenCache = invalidTokenCache;
        this.tokenCache = tokenCache;
        this.keyCache = keyCache;
        this.resourceCache = resourceCache;
    }

    @Override
    protected Cache getGatewayKeyCache() {
        return keyCache;
    }

    @Override
    protected Cache getGatewayTokenCache() {
        return tokenCache;
    }

    @Override
    protected Cache getInvalidTokenCache() {
        return invalidTokenCache;
    }

    @Override
    protected Cache getResourceCache() {
        return resourceCache;
    }



    @Override
    public boolean isGatewayTokenCacheEnabled() {
        return true;
    }

    @Override
    public boolean isAPIResourceValidationEnabled() {
        return true;
    }

    @Override
    protected APIKeyDataStore getDataStore() {
        return apiKeyDataStore;
    }
}
