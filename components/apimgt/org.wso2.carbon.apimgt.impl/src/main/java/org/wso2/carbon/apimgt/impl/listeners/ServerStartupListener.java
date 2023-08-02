/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.listeners;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.loader.KeyManagerConfigurationDataRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for performing operations on initial server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {

        copyToExtensions();

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String enableKeyManagerRetrieval =
                    apiManagerConfiguration.getFirstProperty(APIConstants.ENABLE_KEY_MANAGER_RETRIVAL);
            if (JavaUtils.isTrueExplicitly(enableKeyManagerRetrieval)) {
                startConfigureKeyManagerConfigurations();
            }
            if (APIUtil.isCrossTenantSubscriptionsEnabled() && APIUtil.isGlobalKMEnabled()) {
                startConfigureGlobalKeyManagerConfigurations();
            }
            Map<String, TokenIssuerDto> tokenIssuerDtoMap =
                    apiManagerConfiguration.getJwtConfigurationDto().getTokenIssuerDtoMap();
            tokenIssuerDtoMap.forEach((issuer, tokenIssuer) -> KeyManagerHolder.addGlobalJWTValidators(tokenIssuer));
        }
    }

    /**
     * Method for copying identity component jsp pages to webapp extensions upon initial server startup
     */
    private static void copyToExtensions() {
        String repositoryDir = "repository";
        String resourcesDir = "resources";
        String extensionsDir = "extensions";
        String customAssetsDir = "customAssets";
        String webappDir = "webapps";
        String authenticationEndpointDir = "authenticationendpoint";
        String accountRecoveryEndpointDir = "accountrecoveryendpoint";
        String headerJspFile = "header.jsp";
        String footerJspFile = "product-footer.jsp";
        String titleJspFile = "product-title.jsp";
        String cookiePolicyContentJspFile = "cookie-policy-content.jsp";
        String privacyPolicyContentJspFile = "privacy-policy-content.jsp";
        try {
            String resourceExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                            + File.separator + extensionsDir;
            String customAssetsExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                    + File.separator + extensionsDir + File.separator + customAssetsDir;
            String authenticationEndpointWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + authenticationEndpointDir;
            String authenticationEndpointWebAppExtPath =
                    authenticationEndpointWebAppPath + File.separator + extensionsDir;
            String accountRecoveryWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + accountRecoveryEndpointDir;
            String accountRecoveryWebAppExtPath = accountRecoveryWebAppPath + File.separator + extensionsDir;
            if (new File(resourceExtDirectoryPath).exists()) {
                // delete extensions directory from the webapp folders if they exist
                FileUtils.deleteDirectory(new File(authenticationEndpointWebAppExtPath));
                FileUtils.deleteDirectory(new File(accountRecoveryWebAppExtPath));
                log.info("Starting to copy identity page extensions...");
                String headerJsp = resourceExtDirectoryPath + File.separator + headerJspFile;
                String footerJsp = resourceExtDirectoryPath + File.separator + footerJspFile;
                String titleJsp = resourceExtDirectoryPath + File.separator + titleJspFile;
                String cookiePolicyContentJsp = resourceExtDirectoryPath + File.separator + cookiePolicyContentJspFile;
                String privacyPolicyContentJsp =
                        resourceExtDirectoryPath + File.separator + privacyPolicyContentJspFile;
                if (new File(headerJsp).exists()) {
                    copyFileToDirectory(headerJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(headerJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(footerJsp).exists()) {
                    copyFileToDirectory(footerJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(footerJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(titleJsp).exists()) {
                    copyFileToDirectory(titleJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(titleJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(cookiePolicyContentJsp).exists()) {
                    copyFileToDirectory(cookiePolicyContentJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                }
                if (new File(privacyPolicyContentJsp).exists()) {
                    copyFileToDirectory(privacyPolicyContentJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                }
                // copy custom asset files to the webapp directories
                if (new File(customAssetsExtDirectoryPath).exists()) {
                    FileUtils.copyDirectory(new File(customAssetsExtDirectoryPath),
                            new File(authenticationEndpointWebAppExtPath + File.separator + customAssetsDir));
                    FileUtils.copyDirectory(new File(customAssetsExtDirectoryPath),
                            new File(accountRecoveryWebAppExtPath + File.separator + customAssetsDir));
                }
                log.info("Successfully completed copying identity page extensions");
            }
        } catch (IOException ex) {
            log.error("An error occurred while copying extension files to web apps", ex);
        }
    }

    private static void copyFileToDirectory(String filePath, String directoryPath, String parentDir)
            throws IOException {
        try {
            if (new File(parentDir).exists()) {
                FileUtils.copyFileToDirectory(new File(filePath), new File(directoryPath));
            }
        } catch (IOException ex) {
            log.error("An error occurred while copying file to directory", ex);
            throw new IOException("An error occurred while copying file to directory", ex);
        }
    }

    private void startConfigureKeyManagerConfigurations() {

        KeyManagerConfigurationDataRetriever keyManagerConfigurationDataRetriever =
                new KeyManagerConfigurationDataRetriever(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        keyManagerConfigurationDataRetriever.startLoadKeyManagerConfigurations();
    }

    private void startConfigureGlobalKeyManagerConfigurations() {

        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO oldKeyManagerConfigurationDTO =
                    apiAdmin.getKeyManagerConfigurationByName(APIUtil.getGlobalKMTenantDomain(), APIUtil.getGlobalKMName());
            KeyManagerConfigurationDTO keyManagerConfigurationDTO = loadGlobalKeyManagerConfigurations();
            if (oldKeyManagerConfigurationDTO == null) {
                apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            } else {
                if (!oldKeyManagerConfigurationDTO.getName().equals(keyManagerConfigurationDTO.getName())) {
                    throw new APIManagementException("Cannot change global key manager name");
                }
                keyManagerConfigurationDTO.setUuid(oldKeyManagerConfigurationDTO.getUuid());
                apiAdmin.updateKeyManagerConfiguration(keyManagerConfigurationDTO);
            }
        } catch (APIManagementException e) {
            if (!e.getMessage().contains("Key manager Already Exist by Name")) {
                log.error("Error while initializing global key manager configurations", e);
            }
        }
    }

    private KeyManagerConfigurationDTO loadGlobalKeyManagerConfigurations() {
        KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        keyManagerConfigurationDTO.setName(apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.NAME));
        keyManagerConfigurationDTO.setDisplayName(apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.DISPLAY_NAME));
        keyManagerConfigurationDTO.setDescription(apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.DESCRIPTION));
        keyManagerConfigurationDTO.setEnabled(APIUtil.isGlobalKMEnabled());
        keyManagerConfigurationDTO.setType(apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.TYPE));
        keyManagerConfigurationDTO.setTenantDomain(APIUtil.getGlobalKMTenantDomain());
        String dcrEndpoint = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.CLIENT_REGISTRATION_ENDPOINT);
        String introspectionEndpoint = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.INTROSPECTION_ENDPOINT);
        String issuer = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.ISSUER);
        String userinfoEndpoint = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.USERINFO_ENDPOINT);
        String tokenEndpoint = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.TOKEN_ENDPOINT);
        String revokeEndpoint = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.REVOKE_ENDPOINT);
        String scopeEndpoint = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.SCOPE_ENDPOINT);
        String grantTypes = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.GRANT_TYPES);
        String enableOAuthAppCreation = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.ENABLE_OAUTH_APP_CREATION);
        String enableTokenGeneration = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.ENABLE_TOKEN_GENERATION);
        String selfValidateJWT = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.SELF_VALIDATE_JWT);
        String certificateType = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.CERTIFICATE_TYPE);
        String certificateValue = apiManagerConfiguration.getFirstProperty(APIConstants.GlobalKMConstants.CERTIFICATE_VALUE);
        List<String> grantTypesList = Arrays.asList(grantTypes.split(","));
        Map<String,Object> additionalProperties = new HashMap<>();
        if (StringUtils.isNotEmpty(dcrEndpoint)) {
            additionalProperties.put(APIConstants.KeyManager.CLIENT_REGISTRATION_ENDPOINT, dcrEndpoint);
        }
        if (StringUtils.isNotEmpty(introspectionEndpoint)) {
            additionalProperties.put(APIConstants.KeyManager.INTROSPECTION_ENDPOINT, introspectionEndpoint);
        }
        if (StringUtils.isNotEmpty(tokenEndpoint)) {
            additionalProperties.put(APIConstants.KeyManager.TOKEN_ENDPOINT, tokenEndpoint);
        }
        if (StringUtils.isNotEmpty(userinfoEndpoint)) {
            additionalProperties.put(APIConstants.KeyManager.USERINFO_ENDPOINT, userinfoEndpoint);
        }
        if (StringUtils.isNotEmpty(scopeEndpoint)) {
            additionalProperties.put(APIConstants.KeyManager.SCOPE_MANAGEMENT_ENDPOINT, scopeEndpoint);
        }
        if (StringUtils.isNotEmpty(revokeEndpoint)) {
            additionalProperties.put(APIConstants.KeyManager.REVOKE_ENDPOINT, revokeEndpoint);
        }
        if (StringUtils.isNotEmpty(issuer)) {
            additionalProperties.put(APIConstants.KeyManager.ISSUER, issuer);
        }
        if (!grantTypesList.isEmpty()) {
            additionalProperties.put(APIConstants.KeyManager.AVAILABLE_GRANT_TYPE, grantTypesList);
        }
        if (StringUtils.isNotEmpty(enableOAuthAppCreation)) {
            additionalProperties
                    .put(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION, Boolean.parseBoolean(enableOAuthAppCreation));
        }
        if (StringUtils.isNotEmpty(enableTokenGeneration)) {
            additionalProperties
                    .put(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION, Boolean.parseBoolean(enableTokenGeneration));
        }
        if (StringUtils.isNotEmpty(selfValidateJWT)) {
            additionalProperties
                    .put(APIConstants.KeyManager.SELF_VALIDATE_JWT, Boolean.parseBoolean(selfValidateJWT));
        }
        if (StringUtils.isNotEmpty(certificateType)) {
            if (APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT.equals(certificateType)) {
                additionalProperties.put(APIConstants.KeyManager.CERTIFICATE_TYPE,
                        APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT);
            } else if (APIConstants.KeyManager.CERTIFICATE_TYPE_PEM_FILE.equals(certificateType)) {
                additionalProperties.put(APIConstants.KeyManager.CERTIFICATE_TYPE,
                        APIConstants.KeyManager.CERTIFICATE_TYPE_PEM_FILE);
            }
        }
        if (StringUtils.isNotEmpty(certificateValue)) {
            additionalProperties
                    .put(APIConstants.KeyManager.CERTIFICATE_VALUE, certificateValue);
        }
        keyManagerConfigurationDTO.setAdditionalProperties(additionalProperties);
        return keyManagerConfigurationDTO;
    }

    @Override
    public void completingServerStartup() {
    }
}
