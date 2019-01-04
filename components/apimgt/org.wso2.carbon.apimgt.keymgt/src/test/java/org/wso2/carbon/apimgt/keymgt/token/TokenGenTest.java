/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.keymgt.token;

import junit.framework.TestCase;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.token.JWTGenerator;
import org.wso2.carbon.caching.impl.Util;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.security.cert.X509Certificate;
//import org.wso2.carbon.apimgt.impl.utils.TokenGenUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, org.wso2.carbon.caching.impl.Util.class, CarbonContext.class, OAuthServerConfiguration.class,
        AuthorizationGrantCache.class, APIUtil.class, KeyStoreManager.class, UserCoreUtil.class})
public class TokenGenTest extends TestCase {
    private static final Log log = LogFactory.getLog(TokenGenTest.class);

    @Override
    protected void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(config));
        System.setProperty("carbon.home", "");
        PowerMockito.mockStatic(Util.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(KeyStoreManager.class);
        PowerMockito.mockStatic(UserCoreUtil.class);
        PowerMockito.mockStatic(AuthorizationGrantCache.class);
        OAuthServerConfiguration authServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        AuthorizationGrantCache authorizationGrantCache = Mockito.mock(AuthorizationGrantCache.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        X509Certificate publicCert = Mockito.mock(X509Certificate.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        KeyStoreManager keyStoreManager = Mockito.mock(KeyStoreManager.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(authServerConfiguration);
        PowerMockito.when(Util.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        PowerMockito.when(AuthorizationGrantCache.getInstance()).thenReturn(authorizationGrantCache);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(KeyStoreManager.getInstance(-1234)).thenReturn(keyStoreManager);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(keyStoreManager.getDefaultPrimaryCertificate()).thenReturn(publicCert);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
    }

    public void testAbstractJWTGenerator() throws Exception {
        JWTGenerator jwtGen = new JWTGenerator();
        APIKeyValidationInfoDTO dto=new APIKeyValidationInfoDTO();

        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setValidationInfoDTO(dto);
        validationContext.setContext("testAPI");
        validationContext.setVersion("1.5.0");
        validationContext.setAccessToken("DUMMY_TOKEN_STRING");

        dto.setSubscriber("sanjeewa");
        dto.setApplicationName("sanjeewa-app");
        dto.setApplicationId("1");
        dto.setApplicationTier("UNLIMITED");
        dto.setEndUserName("malalgoda");
        dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
        PowerMockito.when(APIUtil.getUserNameWithTenantSuffix("malalgoda")).thenReturn("malalgoda@carbon.super");
        PowerMockito.when(UserCoreUtil.removeDomainFromName("malalgoda@carbon.super")).thenReturn("malalgoda");

        //Here we will call generate token method with 4 argument.
        String token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        String header = token.split("\\.")[0];
        String decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        String body = token.split("\\.")[1];
        String decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);
        // With end user name not included
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);
        dto.setUserType(APIConstants.SUBSCRIPTION_USER_TYPE);
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);

        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);
    }
    //    TODO: Have to convert to work with new JWT generation and signing
    public void testJWTGeneration() throws Exception {
        JWTGenerator jwtGen = new JWTGenerator();
        APIKeyValidationInfoDTO dto=new APIKeyValidationInfoDTO();
        dto.setSubscriber("sastry");
        dto.setApplicationName("hubapp");
        dto.setApplicationId("1");
        dto.setApplicationTier("UNLIMITED");
        dto.setEndUserName("denis");
        dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setValidationInfoDTO(dto);
        validationContext.setContext("cricScore");
        validationContext.setVersion("1.9.0");
        String token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        String header = token.split("\\.")[0];
        String decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        String body = token.split("\\.")[1];
        String decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);


        // With end user name not included
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);


        dto.setUserType(APIConstants.SUBSCRIPTION_USER_TYPE);
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);

        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);


        //we can not do assert eaquals because body includes expiration time.
        
        /*String expectedHeader = "{\"typ\":\"JWT\"}";
        String expectedBody = "{\"iss\":\"wso2.org/products/am\", \"exp\":1349270811075, " +
                              "\"http://wso2.org/claims/subscriber\":\"sastry\", " +
                              "\"http://wso2.org/claims/applicationname\":\"hubapp\", " +
                              "\"http://wso2.org/claims/apicontext\":\"cricScore\", " +
                              "\"http://wso2.org/claims/version\":\"1.9.0\", " +
                              "\"http://wso2.org/claims/tier\":\"Bronze\", " +
                              "\"http://wso2.org/claims/enduser\":\"denis\"}";

        Assert.assertEquals(expectedHeader, decodedHeader);
        Assert.assertEquals(expectedBody, decodedBody);*/
        //String decodedToken = new String(Base64Utils.decode(token));
        //log.info(decodedToken);
        //assertNotNull(decodedToken);


    }
}
