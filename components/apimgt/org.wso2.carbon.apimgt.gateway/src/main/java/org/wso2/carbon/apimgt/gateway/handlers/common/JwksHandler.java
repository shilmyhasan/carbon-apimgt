package org.wso2.carbon.apimgt.gateway.handlers.common;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.util.JWTUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

/**
 * Implementation for JWKS endpoint.
 */
public class JwksHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(JwksHandler.class);
    private static final String KEY_USE = "sig";
    private static final String SECURITY_KEY_STORE_LOCATION = "Security.KeyStore.Location";
    private static final String SECURITY_KEY_STORE_PW = "Security.KeyStore.Password";
    private static final String KEYS = "keys";
    public JWTConfigurationDto jwtConfigurationDto;

    public boolean handleRequest(MessageContext messageContext) {
//        log.info("Hello");
//        log.info(getJwksEndpointResponse());

        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        // Set the content type as application/json
        headers.put(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);

        // Set the response payload to the message context
        // Retrieve the response body
        String payload = getJwksEndpointResponse();
        axis2MsgContext.getEnvelope().getBody().setText(payload);
        //body.setText(payload);

        // Get the JWKS endpoint response that is to be sent as the response body and create a new OMElement
        // representing the new response body
//        String payload = getJwksEndpointResponse();
//        OMElement newBody = OMAbstractFactory.getOMFactory().createOMElement(payload, null);
//
//        // Replace the existing response body with the new response body
//        body.getFirstElement().detach();
//        body.addChild(newBody);
//        body = axis2MsgContext.getEnvelope().getOMFactory().createOMElement(payload, null);
//        axis2MsgContext.getEnvelope().getBody().addChild(body);

//        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
//        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
//        axis2MC.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
//        JsonUtil.removeJsonPayload(axis2MC);
//        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("ContentType",APIConstants.APPLICATION_JSON_MEDIA_TYPE);
//        try {
//            JsonUtil.getNewJsonPayload(axis2MC, payload, true, true);
//        } catch (AxisFault e) {
//            throw new RuntimeException(e);
//        }
        return true;
    }
//        return "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"MDJlNjIxN2E1OGZlOGVmMGQxOTFlMzBmNmFjZjQ0Y2YwOGY0N2I0YzE4YzZjNjRhYmRmMmQ0ODdiNDhjMGEwMA_RS256\",\"alg\":\"RS256\",\"n\":\"kdgncoCrz655Lq8pTdX07eoVBjdZDCUE6ueBd0D1hpJ0_zE3x3Az6tlvzs98PsPuGzaQOMmuLa4qxNJ-OKxJmutDUlClpuvxuf-jyq4gCV5tEIILWRMBjlBEpJfWm63-VKKU4nvBWNJ7KfhWjl8-DUdNSh2pCDLpUObmb9Kquqc1x4BgttjN4rx_P-3_v-1jETXzIP1L44yHtpQNv0khYf4j_aHjcEri9ykvpz1mtdacbrKK25N4V1HHRwDqZiJzOCCISXDuqB6wguY_v4n0l1XtrEs7iCyfRFwNSKNrLqr23tR1CscmLfbH6ZLg5CYJTD-1uPSx0HMOB4Wv51PbWw\"}]}";

    public boolean handleResponse(MessageContext messageContext) {
//        log.info("World");
//        log.info(getJwksEndpointResponse());
//        String payload = getJwksEndpointResponse();
//        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
//        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
//        axis2MC.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
//        //        JsonUtil.removeJsonPayload(axis2MC);
//        //        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("ContentType",APIConstants.APPLICATION_JSON_MEDIA_TYPE);
//        try {
//            JsonUtil.getNewJsonPayload(axis2MC, payload, true, true);
//        } catch (AxisFault e) {
//            throw new RuntimeException(e);
//        }
        return true;
    }

    public String getJwksEndpointResponse() {
        String tenantDomain = getTenantDomain();
        String keyStorePath = CarbonUtils.getServerConfiguration()
                .getFirstProperty(SECURITY_KEY_STORE_LOCATION);
        String keyStorePassword = CarbonUtils.getServerConfiguration()
                .getFirstProperty(SECURITY_KEY_STORE_PW);

        try (FileInputStream file = new FileInputStream(keyStorePath)) {
            final KeyStore keystore;
            Map<String, Certificate> certificatesWithAliases = new HashMap<>();
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
                keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(file, keyStorePassword.toCharArray());
            } else {
                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    IdentityTenantUtil.initializeRegistry(tenantId);
                    FrameworkUtils.startTenantFlow(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    keystore = keyStoreManager.getKeyStore(generateKSNameFromDomainName(tenantDomain));
                } finally {
                    FrameworkUtils.endTenantFlow();
                }
            }
            Enumeration enumeration = keystore.aliases();
            while (enumeration.hasMoreElements()) {
                String alias = (String) enumeration.nextElement();
                if (keystore.isKeyEntry(alias)) {
                    Certificate cert = keystore.getCertificate(alias);
                    certificatesWithAliases.put(alias, cert);
                }
            }
            return buildResponse(certificatesWithAliases);
        } catch (Exception e) {
            String errorMessage = "Error while generating the keyset for tenant domain: " + tenantDomain;
            return logAndReturnError(errorMessage, e);
        }
    }

    private String buildResponse(Map<String, Certificate> certificates)
            throws IdentityOAuth2Exception, ParseException {

        JSONArray jwksArray = new JSONArray();
        JSONObject jwksJson = new JSONObject();
        OAuthServerConfiguration config = OAuthServerConfiguration.getInstance();
        JWSAlgorithm accessTokenSignAlgorithm =
                OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(config.getSignatureAlgorithm());
        List<JWSAlgorithm> diffAlgorithms = findDifferentAlgorithms(accessTokenSignAlgorithm, config);
        // Create JWKS for different algorithms using new KeyID creation method.
        for (Map.Entry certificateWithAlias : certificates.entrySet()) {
            for (JWSAlgorithm algorithm : diffAlgorithms) {
                Certificate cert = (Certificate) certificateWithAlias.getValue();
                RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
                RSAKey.Builder jwk = new RSAKey.Builder(publicKey);

                try {
                    String base64UrlEncodedThumbPrint = JWTUtil.generateThumbprint("SHA-1", cert, true);
                    jwk.keyID(JWTUtil.getKID(base64UrlEncodedThumbPrint, algorithm.toString()));
                    jwk.algorithm(algorithm);
                    jwk.keyUse(KeyUse.parse(KEY_USE));
                    jwksArray.put(jwk.build().toJSONObject());
                } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
                    String errorMessage = "Error in generating certificate thumbprint";
                    return logAndReturnError(errorMessage, e);
                }
            }
        }
        jwksJson.put(KEYS, jwksArray);
        return jwksJson.toString();
    }

    /**
     * This method read identity.xml and find different signing algorithms
     *
     * @param accessTokenSignAlgorithm
     * @param config
     * @return
     * @throws IdentityOAuth2Exception
     */
    private List<JWSAlgorithm> findDifferentAlgorithms(
            JWSAlgorithm accessTokenSignAlgorithm, OAuthServerConfiguration config) throws IdentityOAuth2Exception {

        List<JWSAlgorithm> diffAlgorithms = new ArrayList<>();
        diffAlgorithms.add(accessTokenSignAlgorithm);
        JWSAlgorithm idTokenSignAlgorithm =
                OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(config.getIdTokenSignatureAlgorithm());
        if (!accessTokenSignAlgorithm.equals(idTokenSignAlgorithm)) {
            diffAlgorithms.add(idTokenSignAlgorithm);
        }
        JWSAlgorithm userInfoSignAlgorithm =
                OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(config.getUserInfoJWTSignatureAlgorithm());
        if (!accessTokenSignAlgorithm.equals(userInfoSignAlgorithm)
                && !idTokenSignAlgorithm.equals(userInfoSignAlgorithm)) {
            diffAlgorithms.add(userInfoSignAlgorithm);
        }
        return diffAlgorithms;
    }

    private String getTenantDomain() {

        Object tenantObj = IdentityUtil.threadLocalProperties.get().get(OAuthConstants.TENANT_NAME_FROM_CONTEXT);
        if (tenantObj != null && StringUtils.isNotBlank((String) tenantObj)) {
            return (String) tenantObj;
        }
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    private String logAndReturnError(String errorMesage, Exception e) {

        if (e != null) {
            log.error(errorMesage, e);
        } else {
            log.error(errorMesage);
        }
        return errorMesage;
    }

    /**
     * This method generates the key store file name from the Domain Name
     *
     * @return key store file name
     */
    private String generateKSNameFromDomainName(String tenantDomain) {

        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + ".jks");
    }
}
