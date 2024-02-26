/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.parser.SwaggerParser;
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
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, OASParserUtil.class})
public class OAS2ParserTest extends OASTestBase {
    private OAS2Parser oas2Parser = new OAS2Parser();

    @Test
    public void testGetURITemplates() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_scopes.json";
        String oas2Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetURITemplatesByParser(oas2Parser, oas2Scope);
    }

    @Test
    public void testGetScopes() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_scopes.json";
        String oas2Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetScopesByParser(oas2Parser, oas2Scope);
    }

    @Test
    public void testGenerateAPIDefinition() throws Exception {
        testGenerateAPIDefinitionByParser(oas2Parser);
    }

    @Test
    public void testUpdateAPIDefinition() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        OASParserEvaluator evaluator = (definition -> {
            SwaggerParser swaggerParser = new SwaggerParser();
            Swagger swagger = swaggerParser.parse(definition);
            Assert.assertNotNull(swagger);
            Assert.assertEquals(1, swagger.getPaths().size());
            Assert.assertFalse(swagger.getPaths().containsKey("/noresource/{resid}"));
        });
        testGenerateAPIDefinition2(oas2Parser, oas2Resources, evaluator);
    }

    @Test
    public void testUpdateAPIDefinitionWithExtensions() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        SwaggerParser swaggerParser = new SwaggerParser();

        // check remove vendor extensions
        String definition = testGenerateAPIDefinitionWithExtension(oas2Parser, oas2Resources);
        Swagger swaggerObj = swaggerParser.parse(definition);
        boolean isExtensionNotFound =
                swaggerObj.getVendorExtensions() == null || swaggerObj.getVendorExtensions().isEmpty();
        Assert.assertTrue(isExtensionNotFound);
        Assert.assertEquals(2, swaggerObj.getPaths().size());

        Iterator<Map.Entry<String, Path>> itr = swaggerObj.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Path> pathEntry = itr.next();
            Path path = pathEntry.getValue();
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();
                Assert.assertFalse(operation.getVendorExtensions().containsKey(APIConstants.SWAGGER_X_SCOPE));
            }
        }

        // check updated scopes in security definition
        Operation itemGet = swaggerObj.getPath("/items").getGet();
        Assert.assertTrue(itemGet.getSecurity().get(0).get("default").contains("newScope"));

        // check available scopes in security definition
        OAuth2Definition oAuth2Definition = (OAuth2Definition) swaggerObj.getSecurityDefinitions().get("default");
        Assert.assertTrue(oAuth2Definition.getScopes().containsKey("newScope"));
        Assert.assertEquals("newScopeDescription", oAuth2Definition.getScopes().get("newScope"));

        Assert.assertTrue(oAuth2Definition.getVendorExtensions().containsKey(APIConstants.SWAGGER_X_SCOPES_BINDINGS));
        Map<String, String> scopeBinding = (Map<String, String>) oAuth2Definition.getVendorExtensions()
                .get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
        Assert.assertTrue(scopeBinding.containsKey("newScope"));
        Assert.assertEquals("admin", scopeBinding.get("newScope"));
    }

    @Test
    public void testGetURITemplatesOfOpenAPI20Spec() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_uri_template.json";
        String swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        uriTemplates.add(getUriTemplate("POST", "Application User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application & Application User", "/abc"));
        Set<URITemplate> uriTemplateSet = oas2Parser.getURITemplates(swagger);
        Assert.assertEquals(uriTemplateSet, uriTemplates);
    }

    /**
     * This test is used to test the behaviour of validateAPIDefinition method
     * @throws Exception If test run fails
     */
    @Test
    public void testValidateAPIDefinition() throws Exception {

        // If the 'info' section is absent in the definition, it must be added to the definition during validation from SwaggerData
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_missing_info.json";
        String swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        API api = new API(new APIIdentifier("admin", "API", "1.0.0"));
        SwaggerData swaggerData = new SwaggerData(api);
        SwaggerParser swaggerParser = new SwaggerParser();
        Mockito.when(apiManagerConfiguration.isAdvancedSwaggerValidationEnabled()).thenReturn(false);
        String validatedSwagger = oas2Parser.validateAPIDefinition(swagger, swaggerData);
        Swagger  validatedSwaggerObj = swaggerParser.parse(validatedSwagger);
        Assert.assertNotNull(validatedSwaggerObj.getInfo());
        Assert.assertEquals("API", validatedSwaggerObj.getInfo().getTitle());
        Assert.assertEquals("1.0.0", validatedSwaggerObj.getInfo().getVersion());

        // If definition includes a title/version, those should not be modified during validation
        relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_with_info.json";
        swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        api = new API(new APIIdentifier("admin", "API2", "2.0.0"));
        validatedSwagger = oas2Parser.validateAPIDefinition(swagger, new SwaggerData(api));
        validatedSwaggerObj = swaggerParser.parse(validatedSwagger);
        Assert.assertNotNull(validatedSwaggerObj.getInfo());
        Assert.assertEquals("SampleAPI", validatedSwaggerObj.getInfo().getTitle());
        Assert.assertEquals("1.0.1", validatedSwaggerObj.getInfo().getVersion());

        // When validation fails from parser, exception should be thrown from method
        PowerMockito.mockStatic(OASParserUtil.class);
        APIManagementException apiManagementException = new APIManagementException("Dummy exception");
        PowerMockito.when(OASParserUtil.class, "verifyAPIDefinitionFromParser",swagger, oas2Parser, swaggerData)
                .thenThrow(apiManagementException);
        try {
            validatedSwagger = oas2Parser.validateAPIDefinition(swagger, swaggerData);
        } catch (APIManagementException e) {
            Assert.assertEquals("Dummy exception", e.getMessage());
        }
    }

    /**
     * This test is used to test the behaviour of validateAPIDefinitionForNewVersion method
     * @throws Exception If test run fails
     */
    @Test
    public void testValidateAPIDefinitionForNewVersion() throws Exception {

        // If the 'info' section is absent in the definition, it must be added to the definition during validation from SwaggerData
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_missing_info.json";
        String swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        API api = new API(new APIIdentifier("admin", "API", "1.0.0"));
        SwaggerData swaggerData = new SwaggerData(api);
        SwaggerParser swaggerParser = new SwaggerParser();
        Mockito.when(apiManagerConfiguration.isAdvancedSwaggerValidationEnabled()).thenReturn(false);
        String validatedSwagger = oas2Parser.validateAPIDefinitionForNewVersion(swagger, swaggerData);
        Swagger  validatedSwaggerObj = swaggerParser.parse(validatedSwagger);
        Assert.assertNotNull(validatedSwaggerObj.getInfo());
        Assert.assertEquals("API", validatedSwaggerObj.getInfo().getTitle());
        Assert.assertEquals("1.0.0", validatedSwaggerObj.getInfo().getVersion());

        // When "title" and "version" not specified, those values must be added to the definition during validation from SwaggerData
        relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_missing_version_title.json";
        swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        api = new API(new APIIdentifier("admin", "API2", "2.0.0"));
        validatedSwagger = oas2Parser.validateAPIDefinitionForNewVersion(swagger, new SwaggerData(api));
        validatedSwaggerObj = swaggerParser.parse(validatedSwagger);
        Assert.assertNotNull(validatedSwaggerObj.getInfo());
        Assert.assertEquals("API2", validatedSwaggerObj.getInfo().getTitle());
        Assert.assertEquals("2.0.0", validatedSwaggerObj.getInfo().getVersion());

        // When validation fails from parser, exception should be thrown from method
        PowerMockito.mockStatic(OASParserUtil.class);
        APIManagementException apiManagementException = new APIManagementException("Dummy exception");
        PowerMockito.when(OASParserUtil.class, "verifyAPIDefinitionFromParser",swagger, oas2Parser, swaggerData)
                .thenThrow(apiManagementException);
        try {
            validatedSwagger = oas2Parser.validateAPIDefinitionForNewVersion(swagger, swaggerData);
        } catch (APIManagementException e) {
            Assert.assertEquals("Dummy exception", e.getMessage());
        }
    }
}
