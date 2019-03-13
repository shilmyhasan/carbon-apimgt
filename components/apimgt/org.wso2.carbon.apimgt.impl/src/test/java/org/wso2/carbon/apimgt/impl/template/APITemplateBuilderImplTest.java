/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIConfigContext.class, ServiceReferenceHolder.class, ResourceFactory.class})
public class APITemplateBuilderImplTest {
    private API sampleAPI = createSampleAPI();

    @Before
    public void setup() {
        System.setProperty("carbon.home", "CARBON_HOME");
    }

    @Test
    public void getConfigStringForTemplateWhenVElocityLogPathIsNull() throws Exception {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(sampleAPI);

        Environment environment = Mockito.mock(Environment.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigurationService);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apimConfiguration);

        PowerMockito.mockStatic(ResourceFactory.class);
        ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
        Template resource = Mockito.mock(Template.class);
        Mockito.when(resourceFactory.getResource(Mockito.anyString(), Mockito.anyInt())).thenReturn(resource);
        Object obj = Mockito.mock(Object.class);
        Mockito.when(resource.getData()).thenReturn(obj);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resource.getResourceLoader()).thenReturn(resourceLoader);

        apiTemplateBuilder.getConfigStringForTemplate(environment);

    }

    @Test
    public void getConfigStringForTemplateWhenVElocityLogPathIsSet() throws Exception {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(sampleAPI);

        Environment environment = Mockito.mock(Environment.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigurationService);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apimConfiguration);

        String velocityLogPath = "repository/resources/api_templates/velocity_template.xml";
        Field velocityLogPathField = APITemplateBuilderImpl.class.getDeclaredField("velocityLogPath");
        velocityLogPathField.setAccessible(true);
        velocityLogPathField.set(apiTemplateBuilder, velocityLogPath);

        PowerMockito.mockStatic(ResourceFactory.class);
        ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
        Template resource = Mockito.mock(Template.class);
        Mockito.when(resourceFactory.getResource(Mockito.anyString(), Mockito.anyInt())).thenReturn(resource);
        Object resourceData = Mockito.mock(Object.class);
        Mockito.when(resource.getData()).thenReturn(resourceData);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resource.getResourceLoader()).thenReturn(resourceLoader);

        apiTemplateBuilder.getConfigStringForTemplate(environment);

        //velocity error
        Mockito.when(resource.getData()).thenReturn(null);
        try {
            apiTemplateBuilder.getConfigStringForTemplate(environment);
            Assert.fail("APITemplateException was not thrown as expected.");
        } catch (APITemplateException e) {
            Assert.assertTrue("Velocity Error".equals(e.getMessage()));
        }

    }

    @Test
    public void testGetConfigStringForPrototypeScriptAPIWhenVElocityLogPathIsNull() throws Exception {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(sampleAPI);

        Environment environment = Mockito.mock(Environment.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigurationService);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apimConfiguration);
        PowerMockito.mockStatic(ResourceFactory.class);
        ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
        Template resource = Mockito.mock(Template.class);
        Mockito.when(resourceFactory.getResource(Mockito.anyString(), Mockito.anyInt())).thenReturn(resource);
        Object obj = Mockito.mock(Object.class);
        Mockito.when(resource.getData()).thenReturn(obj);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resource.getResourceLoader()).thenReturn(resourceLoader);

        apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment);
    }

    @Test
    public void getGetConfigStringForPrototypeScriptAPIWhenVElocityLogPathIsSet() throws Exception {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(sampleAPI);

        Environment environment = Mockito.mock(Environment.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigurationService);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apimConfiguration);

        String velocityLogPath = "repository/resources/api_templates/prototype_template.xml";
        Field velocityLogPathField = APITemplateBuilderImpl.class.getDeclaredField("velocityLogPath");
        velocityLogPathField.setAccessible(true);
        velocityLogPathField.set(apiTemplateBuilder, velocityLogPath);

        PowerMockito.mockStatic(ResourceFactory.class);
        ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
        Template resource = Mockito.mock(Template.class);
        Mockito.when(resourceFactory.getResource(Mockito.anyString(), Mockito.anyInt())).thenReturn(resource);
        Object resourceData = Mockito.mock(Object.class);
        Mockito.when(resource.getData()).thenReturn(resourceData);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resource.getResourceLoader()).thenReturn(resourceLoader);

        apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment);

        //velocity error
        Mockito.when(resource.getData()).thenReturn(null);
        try {
            apiTemplateBuilder.getConfigStringForPrototypeScriptAPI(environment);
            Assert.fail("APITemplateException was not thrown as expected.");
        } catch (APITemplateException e) {
            Assert.assertTrue("Velocity Error".equals(e.getMessage()));
        }

    }

    @Test
    public void testGetConfigStringForDefaultAPITemplateWhenVElocityLogPathIsNull() throws Exception {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(sampleAPI);

        //Environment environment = Mockito.mock(Environment.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigurationService);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apimConfiguration);

        PowerMockito.mockStatic(ResourceFactory.class);
        ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
        Template resource = Mockito.mock(Template.class);
        Mockito.when(resourceFactory.getResource(Mockito.anyString(), Mockito.anyInt())).thenReturn(resource);
        Object obj = Mockito.mock(Object.class);
        Mockito.when(resource.getData()).thenReturn(obj);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resource.getResourceLoader()).thenReturn(resourceLoader);

        apiTemplateBuilder.getConfigStringForDefaultAPITemplate("1.0.0");
    }

    @Test
    public void getGetConfigStringForDefaultAPITemplateWhenVElocityLogPathIsSet() throws Exception {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(sampleAPI);

        //Environment environment = Mockito.mock(Environment.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigurationService);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apimConfiguration);

        String velocityLogPath = "repository/resources/api_templates/default_api_template.xml";
        Field velocityLogPathField = APITemplateBuilderImpl.class.getDeclaredField("velocityLogPath");
        velocityLogPathField.setAccessible(true);
        velocityLogPathField.set(apiTemplateBuilder, velocityLogPath);

        PowerMockito.mockStatic(ResourceFactory.class);
        ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
        Template resource = Mockito.mock(Template.class);
        Mockito.when(resourceFactory.getResource(Mockito.anyString(), Mockito.anyInt())).thenReturn(resource);
        Object resourceData = Mockito.mock(Object.class);
        Mockito.when(resource.getData()).thenReturn(resourceData);
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        Mockito.when(resource.getResourceLoader()).thenReturn(resourceLoader);

        apiTemplateBuilder.getConfigStringForDefaultAPITemplate("1.0.0");

        //velocity error
        Mockito.when(resource.getData()).thenReturn(null);
        try {
            apiTemplateBuilder.getConfigStringForDefaultAPITemplate("1.0.0");
            Assert.fail("APITemplateException was not thrown as expected.");
        } catch (APITemplateException e) {
            Assert.assertTrue("Velocity Error".equals(e.getMessage()));
        }
    }

    private URITemplate getUriTemplate(String httpVerb,String authType,String uriTemplateString) {
        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setAuthTypes(authType);
        uriTemplate.setAuthType(authType);
        uriTemplate.setHTTPVerb(httpVerb);
        uriTemplate.setHttpVerbs(httpVerb);
        uriTemplate.setUriTemplate(uriTemplateString);
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setThrottlingTiers("Unlimited");
        uriTemplate.setScope(null);
        uriTemplate.setScopes(null);
        return uriTemplate;
    }

    private API createSampleAPI() {
        String apiProviderName = "admin";
        String apiName = "testAPI";
        String version = "1.0.0";
        APIIdentifier apiIdentifier = new APIIdentifier(apiProviderName, apiName, version);
        API api = new API(apiIdentifier);
        api.setStatus(APIConstants.PUBLISHED);
        api.setTransports("http,https");
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        uriTemplates.add(getUriTemplate("GET", "Any", "/*"));
        api.setUriTemplates(uriTemplates);
        api.setContext("/context");
        api.setContextTemplate("/{version}/context");
        return api;
    }
}