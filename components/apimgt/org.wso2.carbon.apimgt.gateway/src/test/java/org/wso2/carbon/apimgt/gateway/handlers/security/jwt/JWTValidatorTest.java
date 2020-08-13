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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;

import java.text.ParseException;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTValidator.class, GatewayUtils.class, ServiceReferenceHolder.class})
public class JWTValidatorTest {
    private JWTValidator jwtValidator;
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgCntxt;
    private JSONObject payload;
    private String validJwtToken;
    private String validJwtTokenWithCookieBindingRef;
    private APIManagerConfiguration amConfig;

    @Before
    public void setup() throws Exception {
        // JSON payload of the token
        payload = new JSONObject(
                "{\n" +
                        "          \"sub\": \"admin@carbon.super\",\n" +
                        "          \"iss\": \"https://localhost:9443/oauth2/token\",\n" +
                        "          \"tierInfo\": {\n" +
                        "            \"Unlimited\": {\n" +
                        "              \"stopOnQuotaReach\": true,\n" +
                        "              \"spikeArrestLimit\": 0,\n" +
                        "              \"spikeArrestUnit\": null\n" +
                        "            }\n" +
                        "          },\n" +
                        "          \"keytype\": \"PRODUCTION\",\n" +
                        "          \"subscribedAPIs\": [\n" +
                        "            {\n" +
                        "              \"subscriberTenantDomain\": \"carbon.super\",\n" +
                        "              \"name\": \"PizzaShackAPI\",\n" +
                        "              \"context\": \"/pizzashack/1.0.0\",\n" +
                        "              \"publisher\": \"admin\",\n" +
                        "              \"version\": \"1.0.0\",\n" +
                        "              \"subscriptionTier\": \"Unlimited\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"aud\": \"http://org.wso2.apimgt/gateway\",\n" +
                        "          \"application\": {\n" +
                        "            \"owner\": \"admin\",\n" +
                        "            \"tier\": \"Unlimited\",\n" +
                        "            \"name\": \"DefaultApplication\",\n" +
                        "            \"id\": 1\n" +
                        "          },\n" +
                        "          \"scope\": \"am_application_scope default OTT\",\n" +
                        "          \"consumerKey\": \"U6Sjm1pawuc6K0mx5Hc9je5PTN8a\",\n" +
                        "          \"exp\": 1563032691,\n" +
                        "          \"iat\": 1563029091,\n" +
                        "          \"jti\": \"3f31a2db-39f9-4a00-a314-f548c8e657e2\"\n" +
                        "        }"
        );

        // Encoded jwt token
        validJwtToken = "eyJ4NXQiOiJOVEF4Wm1NeE5ETXlaRGczTVRVMVpHTTBNekV6T0RKaFpXSTRORE5sWkRVMU9HRmtOakZpTVEiL" +
                "CJraWQiOiJOVEF4Wm1NeE5ETXlaRGczTVRVMVpHTTBNekV6T0RKaFpXSTRORE5sWkRVMU9HRmtOakZpTVEiLCJhbGciO" +
                "iJSUzI1NiJ9" +
                ".ewogICJzdWIiOiAiYWRtaW5AY2FyYm9uLnN1cGVyIiwKICAiaXNzIjogImh0dHBzOi8vbG9j" +
                "YWxob3N0Ojk0NDMvb2F1dGgyL3Rva2VuIiwKICAidGllckluZm8iOiB7CiAgICAiVW5saW1pdGVkIjogewogICAgICAic" +
                "3RvcE9uUXVvdGFSZWFjaCI6IHRydWUsCiAgICAgICJzcGlrZUFycmVzdExpbWl0IjogMCwKICAgICAgInNwaWtlQXJyZX" +
                "N0VW5pdCI6IG51bGwKICAgIH0KICB9LAogICJrZXl0eXBlIjogIlBST0RVQ1RJT04iLAogICJzdWJzY3JpYmVkQVBJcyI" +
                "6IFsKICAgIHsKICAgICAgInN1YnNjcmliZXJUZW5hbnREb21haW4iOiAiY2FyYm9uLnN1cGVyIiwKICAgICAgIm5hbWUi" +
                "OiAiUGl6emFTaGFja0FQSSIsCiAgICAgICJjb250ZXh0IjogIi9waXp6YXNoYWNrLzEuMC4wIiwKICAgICAgInB1Ymxpc" +
                "2hlciI6ICJhZG1pbiIsCiAgICAgICJ2ZXJzaW9uIjogIjEuMC4wIiwKICAgICAgInN1YnNjcmlwdGlvblRpZXIiOiAiVW" +
                "5saW1pdGVkIgogICAgfQogIF0sCiAgImF1ZCI6ICJodHRwOi8vb3JnLndzbzIuYXBpbWd0L2dhdGV3YXkiLAogICJhcHB" +
                "saWNhdGlvbiI6IHsKICAgICJvd25lciI6ICJhZG1pbiIsCiAgICAidGllciI6ICJVbmxpbWl0ZWQiLAogICAgIm5hbWUi" +
                "OiAiRGVmYXVsdEFwcGxpY2F0aW9uIiwKICAgICJpZCI6IDEKICB9LAogICJzY29wZSI6ICJhbV9hcHBsaWNhdGlvbl9zY" +
                "29wZSBkZWZhdWx0IiwKICAiY29uc3VtZXJLZXkiOiAiVTZTam0xcGF3dWM2SzBteDVIYzlqZTVQVE44YSIsCiAgImV4cC" +
                "I6IDE1NjMwMzI2OTEsCiAgImdyYW50VHlwZSI6ICJjbGllbnRfY3JlZGVudGlhbHMiLAogICJpYXQiOiAxNTYzMDI5MDk" +
                "xLAogICJqdGkiOiAiM2YzMWEyZGItMzlmOS00YTAwLWEzMTQtZjU0OGM4ZTY1N2UyIgp9" +
                ".ghi";

        validJwtTokenWithCookieBindingRef = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IlpERTVNRE01TURoaU1EbGlZ" +
                "ekJqWVdNeE56WTNNV05sTlRVNU1tSTVNV1JrT1ROa09URTJPUT09In0.eyJiaW5kaW5nX3R5cGUiOiJjb29raWUiLCJzdWIiO" +
                "iJhZG1pbkBjYXJib24uc3VwZXIiLCJpc3MiOiJodHRwczpcL1wvaXNrbS5jb206OTQ0NFwvb2F1dGgyXC90b2tlbiIsInRpZX" +
                "JJbmZvIjp7IkdvbGQiOnsidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNw" +
                "aWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH0sIlVubGltaXRlZCI6eyJ0aWVyUXVvdGFUeXBlIjoicm" +
                "VxdWVzdENvdW50Iiwic3RvcE9uUXVvdGFSZWFjaCI6dHJ1ZSwic3Bpa2VBcnJlc3RMaW1pdCI6MCwic3Bpa2VBcnJlc3RVbml0" +
                "IjpudWxsfX0sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOlt7InN1YnNjcmliZXJUZW5hbnREb21haW" +
                "4iOiJjYXJib24uc3VwZXIiLCJuYW1lIjoiUGl6emFTaGFja0FQSSIsImNvbnRleHQiOiJcL3Bpenphc2hhY2tcLzEuMC4wIiwi" +
                "cHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoiMS4wLjAiLCJzdWJzY3JpcHRpb25UaWVyIjoiVW5saW1pdGVkIn0seyJzdW" +
                "JzY3JpYmVyVGVuYW50RG9tYWluIjoiY2FyYm9uLnN1cGVyIiwibmFtZSI6IlJlc3RBUEkiLCJjb250ZXh0IjoiXC90ZXN0XC8x" +
                "LjAuMCIsInB1Ymxpc2hlciI6ImFkbWluIiwidmVyc2lvbiI6IjEuMC4wIiwic3Vic2NyaXB0aW9uVGllciI6IkdvbGQifSx7In" +
                "N1YnNjcmliZXJUZW5hbnREb21haW4iOiJjYXJib24uc3VwZXIiLCJuYW1lIjoiUmVzdEFQSTEiLCJjb250ZXh0IjoiXC90ZXN0" +
                "M1wvMS4wLjAiLCJwdWJsaXNoZXIiOiJhZG1pbiIsInZlcnNpb24iOiIxLjAuMCIsInN1YnNjcmlwdGlvblRpZXIiOiJHb2xkIn" +
                "1dLCJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV3YXkiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWlu" +
                "IiwidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F" +
                "0aW9uIiwiaWQiOjEsInV1aWQiOm51bGx9LCJzY29wZSI6Im9wZW5pZCIsImNvbnN1bWVyS2V5IjoicUZYNE5SenlXWGtNZ0RxeW" +
                "puX25sNWh3QnpRYSIsImV4cCI6MTU5NjA5OTUxNSwiYmluZGluZ19yZWYiOiI0ZThmNmY4NjQzYmYxM2VmNWY1ZDIwYWU2ZGQ2O" +
                "TQ3NiIsImlhdCI6MTU5NjA5NTkxNSwianRpIjoiMmVlODhjNmYtOGVlOC00YWEyLTg1MmEtMGNiOGQ0YTNlMmJjIn0.0AK0ksMj" +
                "MXpjL0eEMMzeRl8g1uzw_PZPnd4FcRukFSzUGvSYD7PBrpUyuPfUBB9FyjqUGnVO9fi5rFK4tEqv_LxYjKpsisk8l2MtoAQ9N_9" +
                "ZlwpXmASGxTeHSmyrq0lwt1ix3sHjojVIx4iiVIeV_ms-90o55UK7VpAf3o-OkShTSgi9FedX-YcxwB5Ig3UF-MdNh017jTdpMc" +
                "SJZ2QQZk7OuBUmA_grPLlFqHhM0s9w1wbedSvNd7OwX0Q7kq-7MKvTmANgWdBhSAfifnQoH_uCFF4JEyLoPWbs5ZjuGYfHVV3vl" +
                "Brhb2vA2vvIByfQovOV5c1aA3EdcTZdL6uQQg";

        jwtValidator = PowerMockito.mock(JWTValidator.class);
        PowerMockito.when(jwtValidator, "authenticate",
                Mockito.any(), Mockito.any(), Mockito.any()).thenCallRealMethod();

        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        amConfig = Mockito.mock(APIManagerConfiguration.class);

        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/pizzashack/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/menu");
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("get");

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(amConfig);
    }

    @Test
    public void testAuthenticationWithInvalidJwtToken2()   {
        // Invalid token payload
        initMocks();
        String invalidJwtToken = "aasdasd.xxx#.sad";

        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(invalidJwtToken);
            PowerMockito.when(GatewayUtils.verifyTokenSignature(signedJWT, Mockito.anyString())).thenReturn(false);
            jwtValidator.authenticate(invalidJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException | ParseException e) {
            if (e instanceof APISecurityException) {
                Assert.assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        ((APISecurityException) e).getErrorCode());
            }
        }
    }

    @Test
    public void testAuthenticationSignatureVerificationFailure() throws Exception {
        // Token signature verification failure
        initMocks();
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString()))
                .thenReturn(false);

        try {
            jwtValidator.authenticate(validJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationWithExpiredJwtToken() throws Exception {
        // Expired token
        initMocks();
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(jwtValidator, "checkTokenExpiration",
                Mockito.any(), Mockito.any(), Mockito.any()).thenThrow(
                new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "JWT token is expired"));

        try {
            jwtValidator.authenticate(validJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationWithScopeFailure() throws Exception {
        // Token does not have the scopes required to access the resource
        initMocks();
        PowerMockito.when(jwtValidator, "validateScopes",
                Mockito.any(), Mockito.any(), Mockito.any()).thenThrow(new APISecurityException(
                APISecurityConstants.INVALID_SCOPE, "Scope validation failed"));
        PowerMockito.when(jwtValidator, "verifyTokenSignature", Mockito.any(), Mockito.any()).thenReturn(true);

        try {
            jwtValidator.authenticate(validJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.INVALID_SCOPE, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationWithAPISubscriptionFailure() throws Exception {
        // Owner of the token is not subscribed to access the resource
        try {
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(payload.toString());

            GatewayUtils.validateAPISubscription("/unsubscribedPizzashack/1.0.0", "1.0.0", jwtClaimsSet,
                    validJwtToken.split("\\."), true);
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_FORBIDDEN, e.getErrorCode());
        }
    }

    @Test
    public void testCSRFAttackIdentification() throws Exception {
        initMocks();

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cookie" , "commonAuthId=5869ce9e-3d10-4412-ad16-cdba2ee923f6; " +
                "atbv=8424cb1e-65a2-4131-b5f6-34f096105220; opbs=a016b339-b789-4da2-81f3-026c4c560aec; " +
                "pastr-b3e99280-3d46-4174-877f-d912773e4f5b=79bbed9a-3f76-4d14-8e53-417afc8de004");

        JSONObject csrfPayload = payload;
        csrfPayload = csrfPayload.put("binding_type", "cookie");
        csrfPayload = csrfPayload.put("binding_ref", "4e8f6f8643bf13ef5f5d20ae6dd69476");
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(csrfPayload.toString());

        String[] splitToken = validJwtTokenWithCookieBindingRef.split("\\.");
        AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(splitToken[2],
                jwtClaimsSet, null, null, APIConstants.UNLIMITED_TIER, null,true);

        Mockito.when(amConfig.getFirstProperty(APIConstants.CSRF_COOKIE_NAME)).
                thenReturn(APIConstants.DEFAULT_COOKIE_BINDING_NAME);
        Mockito.when(axis2MsgCntxt.getProperty(APIConstants.API_TRANSPORT_HEADERS)).thenReturn(headers);

        PowerMockito.when(jwtValidator, "verifyTokenSignature", Mockito.any(), Mockito.anyString()).
                thenReturn(true);
        PowerMockito.when(jwtValidator, "transformJWTClaims", Mockito.any()).thenReturn(jwtClaimsSet);
        PowerMockito.when(jwtValidator, "transformJWTClaims", Mockito.any()).thenReturn(jwtClaimsSet);
        PowerMockito.when(jwtValidator, "checkCSRF", messageContext, validJwtTokenWithCookieBindingRef,
                jwtClaimsSet).thenCallRealMethod();
        PowerMockito.when(jwtValidator, "getApiManagerConfiguration").thenCallRealMethod();

        AuthenticationContext result = jwtValidator.authenticate(validJwtTokenWithCookieBindingRef, messageContext,
                null);

        Assert.assertEquals(result, authenticationContext);
    }

    @Test
    public void testOneTimeToken() throws Exception {
        initMocks();
        String[] splitToken = validJwtToken.split("\\.");
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(payload.toString());

        AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(splitToken[2],
                jwtClaimsSet, null, null, APIConstants.UNLIMITED_TIER, null,true);

        Mockito.when(amConfig.getFirstProperty(APIConstants.ONE_TIME_TOKEN_SCOPE)).thenReturn("OTT");
        PowerMockito.when(jwtValidator, "verifyTokenSignature", Mockito.any(), Mockito.anyString()).thenReturn(true);
        PowerMockito.when(jwtValidator, "transformJWTClaims", Mockito.any()).thenReturn(jwtClaimsSet);
        PowerMockito.when(jwtValidator, "getApiManagerConfiguration").thenCallRealMethod();
        PowerMockito.when(jwtValidator, "checkOneTimeToken", validJwtToken, jwtClaimsSet).thenCallRealMethod();
        AuthenticationContext result = jwtValidator.authenticate(validJwtTokenWithCookieBindingRef, messageContext, null);
        Assert.assertEquals(result, authenticationContext);
    }

    @Test
    public void testGenerateAuthContext() throws Exception {

        String[] splitToken = validJwtToken.split("\\.");
        net.minidev.json.JSONObject api =
                (net.minidev.json.JSONObject) new net.minidev.json.parser.JSONParser().parse("{\n" +
                        "              \"subscriberTenantDomain\": \"carbon.super\",\n" +
                        "              \"name\": \"PizzaShackAPI\",\n" +
                        "              \"context\": \"/pizzashack/1.0.0\",\n" +
                        "              \"publisher\": \"admin\",\n" +
                        "              \"version\": \"1.0.0\",\n" +
                        "              \"subscriptionTier\": \"Unlimited\"\n" +
                        "            }\n");
        JSONObject tierInfo = new JSONObject("{\n" +
                "              \"stopOnQuotaReach\": true,\n" +
                "              \"spikeArrestLimit\": 0,\n" +
                "              \"spikeArrestUnit\": null\n" +
                "            }\n");

        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(payload.toString());
            AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(splitToken[2],
                    jwtClaimsSet, api, null, APIConstants.UNLIMITED_TIER, null,true);

        Assert.assertTrue(authenticationContext.isAuthenticated());
        Assert.assertEquals(splitToken[2], authenticationContext.getApiKey());
        Assert.assertEquals(payload.getString(APIConstants.JwtTokenConstants.KEY_TYPE),
                authenticationContext.getKeyType());
        Assert.assertEquals(payload.getString(APIConstants.JwtTokenConstants.SUBJECT),
                authenticationContext.getUsername());
        Assert.assertEquals(APIConstants.UNLIMITED_TIER, authenticationContext.getApiTier());

        JSONObject applicationObj = (JSONObject) payload.get(APIConstants.JwtTokenConstants.APPLICATION);
        Assert.assertEquals(String.valueOf(applicationObj.getInt(APIConstants.JwtTokenConstants.APPLICATION_ID)),
                authenticationContext.getApplicationId());
        Assert.assertEquals(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_NAME),
                authenticationContext.getApplicationName());
        Assert.assertEquals(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_TIER),
                authenticationContext.getApplicationTier());

        Assert.assertEquals(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_OWNER),
                authenticationContext.getSubscriber());
        Assert.assertEquals(payload.getString(APIConstants.JwtTokenConstants.CONSUMER_KEY),
                authenticationContext.getConsumerKey());

        Assert.assertEquals(api.getAsString(APIConstants.JwtTokenConstants.SUBSCRIPTION_TIER),
                authenticationContext.getTier());
        Assert.assertEquals(api.getAsString(APIConstants.JwtTokenConstants.SUBSCRIBER_TENANT_DOMAIN),
                authenticationContext.getSubscriberTenantDomain());

        Assert.assertEquals(tierInfo.getBoolean(APIConstants.JwtTokenConstants.STOP_ON_QUOTA_REACH),
                authenticationContext.isStopOnQuotaReach());
        Assert.assertEquals(tierInfo.getInt(APIConstants.JwtTokenConstants.SPIKE_ARREST_LIMIT),
                authenticationContext.getSpikeArrestLimit());
        Assert.assertNull(authenticationContext.getSpikeArrestUnit());
    }

    public void initMocks() {
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(GatewayUtils.isGatewayTokenCacheEnabled()).thenReturn(true);
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }
}
