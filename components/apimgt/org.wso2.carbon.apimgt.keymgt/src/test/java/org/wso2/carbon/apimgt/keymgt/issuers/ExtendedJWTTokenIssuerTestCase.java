package org.wso2.carbon.apimgt.keymgt.issuers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationGrantHandler;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CarbonUtils.class, OAuthServerConfiguration.class,
        AuthorizationGrantHandler.class })

public class ExtendedJWTTokenIssuerTestCase {
    private final String GRANT_TYPE = "grant_type";
    private final String CONSUMER_KEY = "a4FwHlq0iCIKVs2MPIIDnepZnYMa";
    private final String CARBON_HOME = "carbon.home";
    private OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = Mockito.mock(OAuth2AccessTokenReqDTO.class);

    @Before
    public void setUp() {
        String identityConfigPath = System.getProperty("IdentityConfigurationPath");
        IdentityConfigParser.getInstance(identityConfigPath);
    }

    @Test
    public void testAuthorizationCodeGetAccessTokenLifeTimeInMillis() throws Exception {
        String carbonHome = System.getProperty(CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        OAuthTokenReqMessageContext tokenReqMessageContext = new OAuthTokenReqMessageContext(oAuth2AccessTokenReqDTO);
        tokenReqMessageContext.setValidityPeriod(7200);
        PowerMockito.when(tokenReqMessageContext.getOauth2AccessTokenReqDTO().getGrantType()).thenReturn(GRANT_TYPE);
        OAuthTokenReqMessageContext defaultTokenReqMessageContext = new OAuthTokenReqMessageContext(
                oAuth2AccessTokenReqDTO);
        defaultTokenReqMessageContext.setValidityPeriod(-1);
        PowerMockito.when(defaultTokenReqMessageContext.getOauth2AccessTokenReqDTO().getGrantType())
                .thenReturn(GRANT_TYPE);

        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.mockStatic((AuthorizationGrantHandler.class));
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        AuthorizationGrantHandler authorizationGrantHandler = Mockito.mock(AuthorizationGrantHandler.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        PowerMockito.when(oAuthServerConfiguration.getSignatureAlgorithm()).thenReturn("SHA256withRSA");
        Map map = Mockito.mock(Map.class);
        PowerMockito.when(oAuthServerConfiguration.getSupportedGrantTypes()).thenReturn(map);
        PowerMockito.when(oAuthServerConfiguration.getSupportedGrantTypes().get(GRANT_TYPE))
                .thenReturn(authorizationGrantHandler);
        PowerMockito.when(authorizationGrantHandler.isOfTypeApplicationUser()).thenReturn(true);

        OAuthAppDO oAuthAppDO = new OAuthAppDO();
        oAuthAppDO.setUserAccessTokenExpiryTime(3600);

        ExtendedJWTTokenIssuer extendedJWTTokenIssuer = new ExtendedJWTTokenIssuer();
        long defaultLifetime = extendedJWTTokenIssuer
                .getAccessTokenLifeTimeInMillis(defaultTokenReqMessageContext, oAuthAppDO, CONSUMER_KEY);

        Assert.assertEquals(3600000, defaultLifetime);
    }

    @Test
    public void testClientCredentialsGetAccessTokenLifeTimeInMillis() throws Exception {
        String carbonHome = System.getProperty(CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        OAuthTokenReqMessageContext tokenReqMessageContext = new OAuthTokenReqMessageContext(oAuth2AccessTokenReqDTO);
        tokenReqMessageContext.setValidityPeriod(7200);
        PowerMockito.when(tokenReqMessageContext.getOauth2AccessTokenReqDTO().getGrantType()).thenReturn(GRANT_TYPE);
        OAuthTokenReqMessageContext defaultTokenReqMessageContext = new OAuthTokenReqMessageContext(
                oAuth2AccessTokenReqDTO);
        defaultTokenReqMessageContext.setValidityPeriod(-1);
        PowerMockito.when(defaultTokenReqMessageContext.getOauth2AccessTokenReqDTO().getGrantType())
                .thenReturn(GRANT_TYPE);

        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.mockStatic((AuthorizationGrantHandler.class));
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        AuthorizationGrantHandler authorizationGrantHandler = Mockito.mock(AuthorizationGrantHandler.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        PowerMockito.when(oAuthServerConfiguration.getSignatureAlgorithm()).thenReturn("SHA256withRSA");
        Map map = Mockito.mock(Map.class);
        PowerMockito.when(oAuthServerConfiguration.getSupportedGrantTypes()).thenReturn(map);
        PowerMockito.when(oAuthServerConfiguration.getSupportedGrantTypes().get(GRANT_TYPE))
                .thenReturn(authorizationGrantHandler);
        PowerMockito.when(authorizationGrantHandler.isOfTypeApplicationUser()).thenReturn(false);

        OAuthAppDO oAuthAppDO = new OAuthAppDO();
        oAuthAppDO.setApplicationAccessTokenExpiryTime(3600);

        ExtendedJWTTokenIssuer extendedJWTTokenIssuer = new ExtendedJWTTokenIssuer();
        long lifetime = extendedJWTTokenIssuer
                .getAccessTokenLifeTimeInMillis(tokenReqMessageContext, oAuthAppDO, CONSUMER_KEY);
        long defaultLifetime = extendedJWTTokenIssuer
                .getAccessTokenLifeTimeInMillis(defaultTokenReqMessageContext, oAuthAppDO, CONSUMER_KEY);

        Assert.assertEquals(7200000, lifetime);
        Assert.assertEquals(3600000, defaultLifetime);
    }
}
