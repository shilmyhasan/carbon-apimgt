package org.wso2.carbon.apimgt.gateway.handlers.common;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.util.JWTUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for JWKS endpoint.
 */
public class JwksHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(JwksHandler.class);
    private static final String KEY_USE = "sig";
    private static final String SECURITY_KEY_STORE_LOCATION = "Security.KeyStore.Location";
    private static final String SECURITY_KEY_STORE_PW = "Security.KeyStore.Password";
    private static final String KEYS = "keys";
    private Map<String, Certificate> certificatesWithAliases = new HashMap<>();

    public boolean handleRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        try {
            String payload = getJwksEndpointResponse();
            JsonUtil.removeJsonPayload(axis2MsgContext);
            JsonUtil.getNewJsonPayload(axis2MsgContext, payload, true, true);
            axis2MsgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MsgContext.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MsgContext.removeProperty(APIConstants.NO_ENTITY_BODY);
        } catch (ParseException | IdentityOAuth2Exception e) {
            log.error("Error while generating payload " + axis2MsgContext.getLogIDString(), e);
        } catch (AxisFault axisFault) {
            log.error("Error while setting payload " + axis2MsgContext.getLogIDString(), axisFault);
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    /**
     * This method is used to get the response for the JWKS endpoint using the certificates in the keystore.
     *
     * @return JWKS response
     */
    public String getJwksEndpointResponse() throws IdentityOAuth2Exception, ParseException {
        if (certificatesWithAliases.isEmpty()) {
            String tenantDomain = getTenantDomain();
            String keyStorePath = CarbonUtils.getServerConfiguration()
                    .getFirstProperty(SECURITY_KEY_STORE_LOCATION);
            String keyStorePassword = CarbonUtils.getServerConfiguration()
                    .getFirstProperty(SECURITY_KEY_STORE_PW);

            try (FileInputStream file = new FileInputStream(keyStorePath)) {
                final KeyStore keystore;
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
            } catch (Exception e) {
                String errorMessage = "Error while generating the keyset for tenant domain: " + tenantDomain;
                return logAndReturnError(errorMessage, e);
            }
        }
        return buildResponse(certificatesWithAliases);
    }

    /**
     * JWKS response is formed by considering the map of certificates provided
     *
     * @param certificates Map of certificates
     * @return JWKS response as a JSON string
     */
    private String buildResponse(Map<String, Certificate> certificates) throws IdentityOAuth2Exception, ParseException {

        JSONArray jwksArray = new JSONArray();
        JSONObject jwksJson = new JSONObject();
        OAuthServerConfiguration config = OAuthServerConfiguration.getInstance();
        JWSAlgorithm accessTokenSignAlgorithm =
                OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(config.getSignatureAlgorithm());
        List<JWSAlgorithm> diffAlgorithms = findDifferentAlgorithms(accessTokenSignAlgorithm, config);

        for (Map.Entry certificateWithAlias : certificates.entrySet()) {
            for (JWSAlgorithm algorithm : diffAlgorithms) {
                Certificate cert = (Certificate) certificateWithAlias.getValue();
                RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
                RSAKey.Builder jwk = new RSAKey.Builder(publicKey);

                X509Certificate x509Certificate = (X509Certificate) cert;
                jwk.keyID(JWTUtil.getKID(x509Certificate));
                jwk.algorithm(algorithm);
                jwk.keyUse(KeyUse.parse(KEY_USE));
                jwksArray.put(jwk.build().toJSONObject());
            }
        }

        jwksJson.put(KEYS, jwksArray);
        return jwksJson.toString();
    }

    /**
     * This method read identity.xml and find different signing algorithms
     *
     * @param accessTokenSignAlgorithm Access token signing algorithm
     * @param config                   OAuthServerConfiguration object
     * @return List of different signing algorithms
     * @throws IdentityOAuth2Exception exception
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

    /**
     * Method to get the tenant domain from the thread local properties
     *
     * @return tenant domain
     */
    private String getTenantDomain() {

        Object tenantObj = IdentityUtil.threadLocalProperties.get().get(OAuthConstants.TENANT_NAME_FROM_CONTEXT);
        if (tenantObj != null && StringUtils.isNotBlank((String) tenantObj)) {
            return (String) tenantObj;
        }
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    /**
     * This method logs the error and returns the error message
     *
     * @param errorMessage error message
     * @param e exception
     * @return error message that was logged
     */
    private String logAndReturnError(String errorMessage, Exception e) {

        if (e != null) {
            log.error(errorMessage, e);
        } else {
            log.error(errorMessage);
        }
        return errorMessage;
    }

    /**
     * This method generates the key store file name from the Domain Name
     *
     * @param tenantDomain tenant domain
     * @return key store file name
     */
    private String generateKSNameFromDomainName(String tenantDomain) {

        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + ".jks");
    }
}
