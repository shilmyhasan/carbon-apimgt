/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {APIUtil.class})
public class APIDefinitionFromSwagger20Test {
    @Test
    public void getURITemplates() throws Exception {
        Resource resource = Mockito.mock(Resource.class);
        Registry registry = Mockito.mock(Registry.class);
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        uriTemplates.add(getUriTemplate("POST", APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN, "/*"));
        uriTemplates.add(getUriTemplate("GET", APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN, "/*"));
        uriTemplates.add(getUriTemplate("PUT", APIConstants.AUTH_APPLICATION_LEVEL_TOKEN, "/*"));
        uriTemplates.add(getUriTemplate("DELETE", APIConstants.AUTH_APPLICATION_LEVEL_TOKEN, "/*"));
        uriTemplates.add(getUriTemplate("GET", APIConstants.AUTH_APPLICATION_LEVEL_TOKEN, "/abc"));
        API api = new API(new APIIdentifier("admin", "PhoneVerification", "1.0.0"));
        api.setUriTemplates(uriTemplates);
        Set<Scope> scopeSet = new HashSet<Scope>();
        Scope scope = new Scope();
        scope.setName("read");
        scope.setRoles("admin");
        scope.setDescription("read");
        scope.setKey("read");
        scopeSet.add(scope);
        api.setScopes(scopeSet);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        Environment environment = new Environment();
        environment.setApiGatewayEndpoint("http://localhost,https://localhost");
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        environmentMap.put("Production", environment);
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);
        String swagger = apiDefinitionFromSwagger20.generateAPIDefinition(api);
        apiDefinitionFromSwagger20.getURITemplates(api, swagger);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(false).thenReturn(true).thenReturn
                (true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false)
                .thenReturn(true);
        Mockito.when(registry.get(Mockito.anyString())).thenReturn(resource).thenThrow(RegistryException.class)
                .thenReturn(resource).thenReturn(resource).thenThrow(RegistryException.class).thenReturn(resource)
                .thenThrow(RegistryException.class);
        Mockito.when(resource.getContent()).thenReturn(org.apache.commons.io.IOUtils.toByteArray(swagger));
        Mockito.when(resource.getLastModified()).thenReturn(new Date(System.currentTimeMillis()));
        Mockito.when(registry.newResource()).thenReturn(resource);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doNothing().when(APIUtil.class, "setResourcePermissions", Mockito.anyString(), Mockito.anyString
                (), Mockito.any(), Mockito.anyString());
        Mockito.when(registry.put(Mockito.anyString(), Mockito.any(Resource.class))).thenReturn("").thenReturn("")
                .thenThrow(RegistryException.class);
        apiDefinitionFromSwagger20.saveAPIDefinition(api, swagger, registry);
        apiDefinitionFromSwagger20.saveAPIDefinition(api, swagger, registry);
        try {
            apiDefinitionFromSwagger20.saveAPIDefinition(api, swagger, registry);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while adding Swagger Definition for "));
        }
        apiDefinitionFromSwagger20.getAPIDefinition(api.getId(), registry);
        apiDefinitionFromSwagger20.getAPIDefinition(api.getId(), registry);
        try {
            apiDefinitionFromSwagger20.getAPIDefinition(api.getId(), registry);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while retrieving Swagger v2.0 Definition for "));
        }
        apiDefinitionFromSwagger20.getAPISwaggerDefinitionTimeStamps(api.getId(), registry);
        apiDefinitionFromSwagger20.getAPISwaggerDefinitionTimeStamps(api.getId(), registry);
        try {
            apiDefinitionFromSwagger20.getAPISwaggerDefinitionTimeStamps(api.getId(), registry);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while retrieving Swagger v2.0 updated time for"));
        }
    }

    @Test
    public void testProcessingGlobalSwaggerParams() throws Exception {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();

        API api = Mockito.mock(API.class);

        String filePath = "src" + File.separator + "test" + File.separator + "resources" + File
                .separator + "swagger" + File.separator + "global-params.json";
        File file = Paths.get(filePath).toFile();
        String swaggerJson = IOUtils.toString(new FileInputStream(file));

        Set<URITemplate> uriTemplates = apiDefinitionFromSwagger20.getURITemplates(api, swaggerJson);

        Assert.assertTrue(uriTemplates.size() > 0);
    }

    protected URITemplate getUriTemplate(String httpVerb, String authType, String uriTemplateString) {
        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setAuthTypes(authType);
        uriTemplate.setAuthType(authType);
        uriTemplate.setHTTPVerb(httpVerb);
        uriTemplate.setHttpVerbs(httpVerb);
        uriTemplate.setUriTemplate(uriTemplateString);
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setThrottlingTiers("Unlimited");
        Scope scope = new Scope();
        scope.setName("read");
        scope.setRoles("admin");
        scope.setDescription("read");
        scope.setKey("read");
        uriTemplate.setScope(scope);
        uriTemplate.setScopes(scope);
        return uriTemplate;
    }
}