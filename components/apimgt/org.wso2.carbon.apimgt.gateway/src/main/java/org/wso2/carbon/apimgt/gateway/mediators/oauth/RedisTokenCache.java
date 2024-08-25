/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators.oauth;

import org.apache.synapse.endpoints.auth.oauth.TokenCacheProvider;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.redis.RedisCacheUtils;

/**
 * Singleton class implementing TokenCacheProvider for caching OAuth tokens using Redis.
 * This class allows storing, retrieving, and removing tokens from Redis via RedisCacheUtils
 * and is utilized by OAuthHandler in Synapse for OAuth token caching.
 */
public class RedisTokenCache implements TokenCacheProvider {

    // Singleton instance of RedisTokenCache
    private static final RedisTokenCache instance = new RedisTokenCache();

    private RedisTokenCache() {
    }

    /**
     * Provides the singleton instance of RedisTokenCache.
     * If no instance exists, a new one is created.
     *
     * @return the singleton instance of RedisTokenCache
     */
    public static RedisTokenCache getInstance() {
        return instance;
    }

    /**
     * Stores a token in the Redis cache with the given identifier as the key.
     *
     * @param id    the identifier (key) for the token in the cache
     * @param token the token to be cached
     */
    @Override
    public void putToken(String id, String token) {
        ServiceReferenceHolder.getInstance().getRedisCacheUtils().setValue(id, token);
    }

    /**
     * Retrieves a token from the Redis cache for the given identifier.
     *
     * @param id the identifier (key) of the token in the cache
     * @return the token associated with the given identifier, or null if not found
     */
    @Override
    public String getToken(String id) {
        return ServiceReferenceHolder.getInstance().getRedisCacheUtils().getValue(id);
    }

    /**
     * Removes the token associated with the given identifier from the Redis cache.
     *
     * @param id the identifier (key) of the token to be removed
     */
    @Override
    public void removeToken(String id) {
        if (ServiceReferenceHolder.getInstance().getRedisCacheUtils().isRedisCacheSessionActive()) {
            ServiceReferenceHolder.getInstance().getRedisCacheUtils().deleteKey(id);
        }
    }
}