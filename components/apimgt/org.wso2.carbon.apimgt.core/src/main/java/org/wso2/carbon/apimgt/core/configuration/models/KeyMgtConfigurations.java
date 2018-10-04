/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Class to hold key manager configurations
 */
@Configuration(description = "Key Management Configurations")
public class KeyMgtConfigurations {

    @Element(description = "Key Manager Implementation class")
    private String keyManagerImplClass = "org.wso2.carbon.apimgt.core.impl.DefaultKeyManagerImpl";
    @Element(description = "DCR Endpoint URL")
    private String dcrEndpoint = "https://localhost:9443/api/identity/oauth2/dcr/v1.0/register";
    @Element(description = "Token Endpoint URL")
    private String tokenEndpoint = "https://localhost:9443/api/auth/oauth2/v1.0/token";
    @Element(description = "Revoke Endpoint URL")
    private String revokeEndpoint = "https://localhost:9443/oauth2/revoke";
    @Element(description = "Introspect Endpoint URL")
    private String introspectEndpoint = "https://localhost:9443/api/identity/oauth2/introspect/v1.0/introspect";
    @Element(description = "Scope Registration Endpoint URL")
    private String scopeRegistrationEndpoint = "https://localhost:9443/api/auth/scope-registration/v1.0/scopes";
    @Element(description = "UserInfo Endpoint URL")
    private String userInfoEndpoint = "https://localhost:9443/api/auth/oidc/v1.0/userinfo";
    @Element(description = "Key manager Credentials")
    private CredentialConfigurations keyManagerCredentials = new CredentialConfigurations();
    @Element(description = "Alias of Key Manager Certificate in Client Trust Store")
    private String keyManagerCertAlias = "wso2carbon";
    @Element(description = "OAuth app validity period")
    private long defaultTokenValidityPeriod = 3600L;
    @Element(description = "OpenId Connect Userinfo Response JWT Signing Algorithm")
    private String oidcUserinfoJWTSigningAlgo = "SHA256withRSA";
    @Element(description = "Type of scope binding")
    private String scopeBindingType = "role";
    @Element(description = "Type of scope binding")
    private String productRestApiScopesKeyWord = "apim:";

    public String getKeyManagerImplClass() {
        return keyManagerImplClass;
    }

    public void setKeyManagerImplClass(String keyManagerImplClass) {
        this.keyManagerImplClass = keyManagerImplClass;
    }

    public String getDcrEndpoint() {
        return dcrEndpoint;
    }

    public void setDcrEndpoint(String dcrEndpoint) {
        this.dcrEndpoint = dcrEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getRevokeEndpoint() {
        return revokeEndpoint;
    }

    public void setRevokeEndpoint(String revokeEndpoint) {
        this.revokeEndpoint = revokeEndpoint;
    }

    public String getIntrospectEndpoint() {
        return introspectEndpoint;
    }

    public void setIntrospectEndpoint(String introspectEndpoint) {
        this.introspectEndpoint = introspectEndpoint;
    }

    public CredentialConfigurations getKeyManagerCredentials() {
        return keyManagerCredentials;
    }

    public void setKeyManagerCredentials(CredentialConfigurations keyManagerCredentials) {
        this.keyManagerCredentials = keyManagerCredentials;
    }

    public String getKeyManagerCertAlias() {
        return keyManagerCertAlias;
    }

    public void setKeyManagerCertAlias(String keyManagerCertAlias) {
        this.keyManagerCertAlias = keyManagerCertAlias;
    }

    public long getDefaultTokenValidityPeriod() {
        return defaultTokenValidityPeriod;
    }

    public void setDefaultTokenValidityPeriod(long defaultTokenValidityPeriod) {
        this.defaultTokenValidityPeriod = defaultTokenValidityPeriod;
    }

    public String getOidcUserinfoJWTSigningAlgo() {
        return oidcUserinfoJWTSigningAlgo;
    }

    public void setOidcUserinfoJWTSigningAlgo(String oidcUserinfoJWTSigningAlgo) {
        this.oidcUserinfoJWTSigningAlgo = oidcUserinfoJWTSigningAlgo;
    }

    public String getScopeRegistrationEndpoint() {
        return scopeRegistrationEndpoint;
    }

    public void setScopeRegistrationEndpoint(String scopeRegistrationEndpoint) {
        this.scopeRegistrationEndpoint = scopeRegistrationEndpoint;
    }

    public String getScopeBindingType() {
        return scopeBindingType;
    }

    public String getProductRestApiScopesKeyWord() {
        return productRestApiScopesKeyWord;
    }

    public void setProductRestApiScopesKeyWord(String productRestApiScopesKeyWord) {
        this.productRestApiScopesKeyWord = productRestApiScopesKeyWord;
    }

    public String getUserInfoEndpoint() {

        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {

        this.userInfoEndpoint = userInfoEndpoint;
    }
}
