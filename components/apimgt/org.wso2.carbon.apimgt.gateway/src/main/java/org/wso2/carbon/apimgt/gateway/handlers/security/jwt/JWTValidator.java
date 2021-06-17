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

package org.wso2.carbon.apimgt.gateway.handlers.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.util.DateUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.cache.Cache;
import javax.cache.Caching;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Validator class to validate JWT tokens in an API request.
 */
public class JWTValidator {

    private static final Log log = LogFactory.getLog(JWTValidator.class);

    private String apiLevelPolicy;
    private boolean isGatewayTokenCacheEnabled;
    private APIKeyValidator apiKeyValidator;
    private Map<String, TokenIssuerDto> tokenIssuerDtoMap;

    public JWTValidator(String apiLevelPolicy, APIKeyValidator apiKeyValidator) {
        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
        this.apiKeyValidator = apiKeyValidator;
        tokenIssuerDtoMap =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getTokenIssuerDtoMap();
    }

    /**
     * Authenticates the given request with a JWT token to see if an API consumer is allowed to access
     * a particular API or not.
     *
     * @param jwtToken The JWT token sent with the API request
     * @param synCtx   The message to be authenticated
     * @param openAPI  The OpenAPI object of the invoked API
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticate(String jwtToken, MessageContext synCtx, OpenAPI openAPI)
            throws APISecurityException {

        String[] splitToken = jwtToken.split("\\.");

        JWSHeader header;
        JWTClaimsSet payload = null;
        boolean isVerified = false;
        String tokenIdentifier = "";

        String tokenSignature = splitToken[2];

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        SignedJWT parsedJWTToken = null;
        try {
            parsedJWTToken = (SignedJWT) JWTParser.parse(jwtToken);
        } catch (ParseException e) {
            log.error("Invalid JWT token. Failed to decode the token.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token. Failed to decode the token.", e);
        }
        String jti = getJWTID(parsedJWTToken);
        if (StringUtils.isNotEmpty(jti)) {
            tokenIdentifier = jti;
        } else {
            tokenIdentifier = tokenSignature;
        }
        String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenIdentifier, apiContext, apiVersion, matchingResource, httpMethod);
        String tenantDomain = GatewayUtils.getTenantDomain();
        JWTTokenPayloadInfo payloadInfo = null;
        // Validate from cache
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayTokenCache().get(tokenIdentifier);
            if (cacheToken != null) {
                log.debug("Token retrieved from the token cache.");
                if (getGatewayKeyCache().get(cacheKey) != null) {
                    // Token is found in the key cache
                    payloadInfo = (JWTTokenPayloadInfo) getGatewayKeyCache().get(cacheKey);
                    String rawPayload = payloadInfo.getRawPayload();
                    if (!rawPayload.equals(splitToken[1])) {
                        isVerified = false;
                    } else {
                        isVerified = true;
                    }
                }
            } else if (getInvalidTokenCache().get(tokenIdentifier) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the invalid token cache. Token: " + GatewayUtils
                            .getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
            // Check revoked map.
            else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(jti) ||
                    RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        } else {
            if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(jti) ||
                    RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        if (!isVerified) {
            log.debug("Token not found in the caches and revoked jwt token map.");
            try {
                header = parsedJWTToken.getHeader();
                payload = parsedJWTToken.getJWTClaimsSet();
            } catch (JSONException | IllegalArgumentException | ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid JWT token. Token: " + GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. Failed to decode the token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token. Failed to decode the token.", e);
            }
            log.debug("Verifying signature of JWT");
            String certificateAlias = APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS;
            if (header.getKeyID() != null) {
                certificateAlias = header.getKeyID();
            }

            isVerified = verifyTokenSignature(parsedJWTToken, certificateAlias);
            if (isGatewayTokenCacheEnabled) {
                // Add token to tenant token cache
                if (isVerified) {
                    getGatewayTokenCache().put(tokenIdentifier, tenantDomain);
                } else {
                    getInvalidTokenCache().put(tokenIdentifier, tenantDomain);
                }

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //Add the tenant domain as a reference to the super tenant cache so we know from which tenant cache
                    //to remove the entry when the need occurs to clear this particular cache entry.
                    try {
                        // Start super tenant flow
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        // Add token to super tenant token cache
                        if (isVerified) {
                            getGatewayTokenCache().put(tokenIdentifier, tenantDomain);
                        } else {
                            getInvalidTokenCache().put(tokenIdentifier, tenantDomain);
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }
            }
        }

        // If token signature is verified
        if (isVerified) {
            log.debug("Token signature is verified.");
            if (isGatewayTokenCacheEnabled && payloadInfo != null) {
                // Token is found in the key cache
                payload = payloadInfo.getPayload();
                checkTokenExpiration(tokenIdentifier, payload, tenantDomain);
                synCtx.setProperty(APIMgtGatewayConstants.SCOPES, payloadInfo.getScopes());
            } else {
                // Retrieve payload from token
                log.debug("Token payload not found in the cache.");
                if (payload == null) {
                    try {
                        parsedJWTToken = (SignedJWT) JWTParser.parse(jwtToken);
                        payload = parsedJWTToken.getJWTClaimsSet();
                    } catch (JSONException | IllegalArgumentException | ParseException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Token decryption failure when retrieving payload. Token: "
                                    + GatewayUtils.getMaskedToken(splitToken[0]), e);
                        }
                        log.error("Invalid JWT token. Failed to decode the token");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                }
                checkTokenExpiration(tokenIdentifier, payload, tenantDomain);
                try {
                    validateScopes(synCtx, openAPI, payload);
                } catch (ParseException e) {
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                }
                //setting scopes
                Set<String> scopeSet = new HashSet<>();
                if (payload.getClaim(APIConstants.JwtTokenConstants.SCOPE) instanceof String) {
                    String[] tokenScopes = String.valueOf(payload.getClaim(APIConstants.JwtTokenConstants.SCOPE))
                            .split(APIConstants.JwtTokenConstants.SCOPE_DELIMITER);
                    scopeSet = new HashSet<>(Arrays.asList(tokenScopes));
                    synCtx.setProperty(APIMgtGatewayConstants.SCOPES, scopeSet.toString());
                }

                if (isGatewayTokenCacheEnabled) {
                    JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                    jwtTokenPayloadInfo.setPayload(payload);
                    jwtTokenPayloadInfo.setRawPayload(splitToken[1]);
                    jwtTokenPayloadInfo.setScopes(scopeSet.toString());
                    getGatewayKeyCache().put(cacheKey, jwtTokenPayloadInfo);
                }
            }

            net.minidev.json.JSONObject api =
                    GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, true);

            /*
             * Set api.ut.apiPublisher of the subscribed api to the message context.
             * This is necessary for the functionality of Publisher alerts.
             * Set API_NAME to the message context
             * */
            if (api != null) {
                synCtx.setProperty(APIMgtGatewayConstants.API_PUBLISHER, api.get("publisher"));
                synCtx.setProperty(APIMgtGatewayConstants.API_NAME, api.get("name"));
            } else {
                boolean validateSubscriptionViaKM = Boolean.parseBoolean(
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                                .getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION)
                );
                if (validateSubscriptionViaKM) {
                    log.debug("Begin subscription validation via Key Manager");
                    APIKeyValidationInfoDTO apiKeyValidationInfoDTO =
                            validateSubscriptionUsingKeyManager(synCtx, payload);

                    if (log.isDebugEnabled()) {
                        log.debug("Subscription validation via Key Manager. Status: " +
                                apiKeyValidationInfoDTO.isAuthorized());
                    }
                    if (apiKeyValidationInfoDTO.isAuthorized()) {
                        synCtx.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiKeyValidationInfoDTO.getApiPublisher());
                        log.debug("JWT authentication successful.");
                        try {
                            return GatewayUtils.generateAuthenticationContext(tokenIdentifier, payload, null,
                                    apiKeyValidationInfoDTO, getApiLevelPolicy(), true);
                        } catch (ParseException e) {
                            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                        }
                    } else {
                        log.debug("User is NOT authorized to access the Resource. API Subscription validation failed.");
                        throw new APISecurityException(apiKeyValidationInfoDTO.getValidationStatus(),
                                "User is NOT authorized to access the Resource. API Subscription validation failed.");
                    }
                }
                log.debug("Ignored subscription validation");
            }

            log.debug("JWT authentication successful.");
            try {
                return GatewayUtils.generateAuthenticationContext(tokenIdentifier, payload, api, null,
                        getApiLevelPolicy(), true);
            } catch (ParseException e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token signature verification failure. Token: " + GatewayUtils.getMaskedToken(splitToken[0]));
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                "Invalid JWT token. Signature verification failed.");
    }

    private APIKeyValidationInfoDTO validateSubscriptionUsingKeyManager(MessageContext synCtx, JWTClaimsSet payload)
            throws APISecurityException {
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        try {
            String consumerkey = null;
            if (payload.getClaim(APIConstants.JwtTokenConstants.CONSUMER_KEY) != null) {
                consumerkey = payload.getStringClaim(APIConstants.JwtTokenConstants.CONSUMER_KEY);
            } else if (payload.getClaim(APIConstants.JwtTokenConstants.AUTHORIZED_PARTY) != null) {
                consumerkey = payload.getStringClaim(APIConstants.JwtTokenConstants.AUTHORIZED_PARTY);
            }
            if (consumerkey != null) {
                return apiKeyValidator.validateSubscription(apiContext, apiVersion, consumerkey);
            }
            log.debug("Cannot call Key Manager to validate subscription. " +
                    "Payload of the token does not contain the Authorized party - the party to which the ID Token was issued");
            throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                    APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
        } catch (ParseException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
        }
    }

    /**
     * Authenticates the given WebSocket handshake request with a JWT token to see if an API consumer is allowed to
     * access a particular API or not.
     *
     * @param jwtToken   The JWT token sent with the API request
     * @param apiContext The context of the invoked API
     * @param apiVersion The version of the invoked API
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticateForWebSocket(String jwtToken, String apiContext, String apiVersion)
            throws APISecurityException {

        String[] splitToken = jwtToken.split("\\.");

        JWTClaimsSet payload = null;
        boolean isVerified = false;
        String tokenIdentifier = "";

        String tokenSignature = splitToken[2];
        String tenantDomain = GatewayUtils.getTenantDomain();
        JWTTokenPayloadInfo payloadInfo = null;
        SignedJWT parsedJWTToken = null;
        try {
            parsedJWTToken = (SignedJWT) JWTParser.parse(jwtToken);
        } catch (ParseException e) {
            log.error("Invalid JWT token. Failed to decode the token.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token. Failed to decode the token.", e);
        }

        String jti = getJWTID(parsedJWTToken);

        if (StringUtils.isNotEmpty(jti)) {
            tokenIdentifier = jti;
        } else {
            tokenIdentifier = tokenSignature;
        }
        String cacheKey = WebsocketUtil.getAccessTokenCacheKey(tokenIdentifier, apiContext);

        // Validate from cache
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayTokenCache().get(tokenIdentifier);
            if (cacheToken != null) {
                log.debug("Token retrieved from the token cache.");
                if (getGatewayKeyCache().get(cacheKey) != null) {
                    // Token is found in the key cache
                    payloadInfo = (JWTTokenPayloadInfo) getGatewayKeyCache().get(cacheKey);
                    String rawPayload = payloadInfo.getRawPayload();
                    if (!rawPayload.equals(splitToken[1])) {
                        isVerified = false;
                    } else {
                        isVerified = true;
                    }
                }
            } else if (getInvalidTokenCache().get(tokenIdentifier) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the invalid token cache. Token: " + GatewayUtils
                            .getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
            // Check revoked map.
            else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(jti) ||
                    RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        } else {
            if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(jti) ||
                    RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        if (!isVerified) {
            log.debug("Token not found in the caches and revoked jwt token map.");
            try {
                payload = parsedJWTToken.getJWTClaimsSet();
            } catch (JSONException | IllegalArgumentException | ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid JWT token. Token: " + GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid JWT token. Failed to decode the token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token. Failed to decode the token.", e);
            }
            log.debug("Verifying signature of JWT");
            isVerified = verifyTokenSignature(parsedJWTToken, APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS);
            if (isGatewayTokenCacheEnabled) {
                // Add token to tenant token cache
                if (isVerified) {
                    getGatewayTokenCache().put(tokenIdentifier, tenantDomain);
                } else {
                    getInvalidTokenCache().put(tokenIdentifier, tenantDomain);
                }

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //Add the tenant domain as a reference to the super tenant cache so we know from which tenant cache
                    //to remove the entry when the need occurs to clear this particular cache entry.
                    try {
                        // Start super tenant flow
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        // Add token to super tenant token cache
                        if (isVerified) {
                            getGatewayTokenCache().put(tokenIdentifier, tenantDomain);
                        } else {
                            getInvalidTokenCache().put(tokenIdentifier, tenantDomain);
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }
            }
        }


        // If token signature is verified
        if (isVerified) {
            log.debug("Token signature is verified.");
            if (isGatewayTokenCacheEnabled && payloadInfo != null) {
                // Token is found in the key cache
                payload = payloadInfo.getPayload();
                checkTokenExpiration(tokenIdentifier, payload, tenantDomain);
            } else {
                // Retrieve payload from token
                log.debug("Token payload not found in the cache.");
                if (payload == null) {
                    try {
                        payload = parsedJWTToken.getJWTClaimsSet();
                    } catch (JSONException | IllegalArgumentException | ParseException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Token decryption failure when retrieving payload. Token: "
                                    + GatewayUtils.getMaskedToken(splitToken[0]), e);
                        }
                        log.error("Invalid JWT token. Failed to decode the token");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                }
                checkTokenExpiration(tokenIdentifier, payload, tenantDomain);

                if (isGatewayTokenCacheEnabled) {
                    JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                    jwtTokenPayloadInfo.setPayload(payload);
                    jwtTokenPayloadInfo.setRawPayload(splitToken[1]);
                    getGatewayKeyCache().put(cacheKey, jwtTokenPayloadInfo);
                }
            }

            net.minidev.json.JSONObject api =
                    GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, true);

            log.debug("JWT authentication successful.");
            try {
                return GatewayUtils.generateAuthenticationContext(tokenIdentifier, payload, api, null, getApiLevelPolicy(), true);
            } catch (ParseException e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token signature verification failure. Token: " + GatewayUtils.getMaskedToken(splitToken[0]));
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                "Invalid JWT token. Signature verification failed.");
    }

    private String getJWTID(SignedJWT parsedJWTToken) throws APISecurityException {
        if (parsedJWTToken == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token is provided.");
        }
        try {
            return parsedJWTToken.getJWTClaimsSet().getJWTID();
        } catch (ParseException e) {
            log.error("Invalid JWT token. Failed to decode the token.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token. Failed to decode the token.", e);
        }
    }
    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes specified
     * in the JWT token payload.
     *
     * @param synCtx  The message to be authenticated
     * @param openAPI The OpenAPI object of the invoked API
     * @param payload The payload of the JWT token
     * @throws APISecurityException in case of scope validation failure
     */
    private void validateScopes(MessageContext synCtx, OpenAPI openAPI, JWTClaimsSet payload)
            throws APISecurityException, ParseException {
        if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
            HashMap<String, String>  operationScopeMappingList =
                    (HashMap<String, String>) synCtx.getProperty(APIConstants.SCOPE_OPERATION_MAPPING);
            String[] operationList = ((String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).split(",");
            for (String operation: operationList) {
                String operationScope = operationScopeMappingList.get(operation);
                checkTokenWithTheScope(operation, operationScope, payload);
            }
        } else {
            String resource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            String resourceScope = OpenAPIUtils.getScopesOfResource(openAPI, synCtx);
            checkTokenWithTheScope(resource, resourceScope, payload);
        }
    }

    private void checkTokenWithTheScope(String resource, String resourceScope, JWTClaimsSet payload)
            throws APISecurityException, ParseException {
        if (StringUtils.isNotBlank(resourceScope)) {
            if (payload.getClaim(APIConstants.JwtTokenConstants.SCOPE) == null) {
                log.error("Scopes not found in the token.");
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
            String[] tokenScopes = new String[0];
            if (payload.getClaim(APIConstants.JwtTokenConstants.SCOPE) instanceof String) {
                tokenScopes = String.valueOf(payload.getClaim(APIConstants.JwtTokenConstants.SCOPE))
                        .split(APIConstants.JwtTokenConstants.SCOPE_DELIMITER);
            } else if (payload.getClaim(APIConstants.JwtTokenConstants.SCOPE) instanceof List) {
                tokenScopes = (String[]) ((List) payload.getClaim(APIConstants.JwtTokenConstants.SCOPE)).toArray();
            }

            boolean scopeFound = false;

            for (String scope : tokenScopes) {
                if (scope.trim().equals(resourceScope)) {
                    scopeFound = true;
                    break;
                }
            }
            if (!scopeFound) {
                if (log.isDebugEnabled()) {
                    log.debug("Scope validation failed. User: " + payload.getSubject());
                }
                log.error("Scope validation failed.");
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
            if (log.isDebugEnabled()) {
                log.debug("Scope validation successful for the resource: " + resource + ", Resource Scope: " + resourceScope
                        + ", User: " + payload.getSubject());
            }
        }
        log.debug("No scopes assigned to the resource: " + resource);
    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param tokenIdentifier The jti/signature of the JWT token
     * @param payload        The payload of the JWT token
     * @param tenantDomain   The tenant domain from which the token cache is retrieved
     * @throws APISecurityException if the token is expired
     */
    private void checkTokenExpiration(String tokenIdentifier, JWTClaimsSet payload, String tenantDomain)
            throws APISecurityException {
        // Check whether the token is expired or not.
        long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();

        Date now = new Date();
        Date exp = payload.getExpirationTime();
        if (exp != null && !DateUtils.isAfter(exp, now, timestampSkew)) {
            if (isGatewayTokenCacheEnabled) {
                getGatewayTokenCache().remove(tokenIdentifier);
                getInvalidTokenCache().put(tokenIdentifier, tenantDomain);
            }
            log.error("JWT token is expired :" + GatewayUtils.getMaskedToken(tokenIdentifier));
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        }
    }

    private boolean verifyTokenSignature(SignedJWT parsedJWTToken, String certificateAlias)
            throws APISecurityException {

        try {
            String issuer = parsedJWTToken.getJWTClaimsSet().getIssuer();
            if (StringUtils.isNotEmpty(issuer)) {
                TokenIssuerDto tokenIssuerDto = tokenIssuerDtoMap.get(issuer);
                if (tokenIssuerDto != null) {
                    if (tokenIssuerDto.getJwksConfigurationDTO().isEnabled() &&
                            StringUtils.isNotEmpty(tokenIssuerDto.getJwksConfigurationDTO().getUrl())) {
                        // Check JWKSet Available in Cache
                        Object jwks = getJWKSCache().get(tokenIssuerDto.getIssuer());
                        JWKSet jwkSet;
                        if (jwks != null) {
                            jwkSet = (JWKSet) jwks;
                        } else {
                            String jwksInfo = GatewayUtils
                                    .retrieveJWKSConfiguration(tokenIssuerDto.getJwksConfigurationDTO().getUrl());
                            jwkSet = JWKSet.parse(jwksInfo);
                            getJWKSCache().put(tokenIssuerDto.getIssuer(), jwkSet);
                        }

                        if (jwkSet.getKeyByKeyId(certificateAlias) instanceof RSAKey) {
                            RSAKey keyByKeyId = (RSAKey) jwkSet.getKeyByKeyId(certificateAlias);
                            RSAPublicKey rsaPublicKey = keyByKeyId.toRSAPublicKey();
                            if (rsaPublicKey != null) {
                                return GatewayUtils.verifyTokenSignature(parsedJWTToken, rsaPublicKey);
                            }
                        } else {
                            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                    "Key Algorithm not supported");
                        }
                    }
                }
            }
        } catch (ParseException | JOSEException | IOException e) {
            log.error("Error while parsing JWT", e);
        }
        return GatewayUtils.verifyTokenSignature(parsedJWTToken, certificateAlias);
    }
    private Cache getJWKSCache(){
        return CacheProvider.getJWKSCache();
    }

    private Cache getGatewayTokenCache() {
        return CacheProvider.getGatewayTokenCache();
    }

    private Cache getInvalidTokenCache() {
        return CacheProvider.getInvalidTokenCache();
    }

    private Cache getGatewayKeyCache() {
        return CacheProvider.getGatewayKeyCache();
    }

    private Cache getCacheFromCacheManager(String cacheName) {
        return Caching.getCacheManager(
                APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName);
    }

    private String getApiLevelPolicy() {
        return apiLevelPolicy;
    }
}
