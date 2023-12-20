package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
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
public class OAS3ParserTest extends OASTestBase {
    private OAS3Parser oas3Parser = new OAS3Parser();

    @Test
    public void testGetURITemplates() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_scopes.json";
        String oas3Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetURITemplatesByParser(oas3Parser, oas3Scope);
    }

    @Test
    public void testGetScopes() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_scopes.json";
        String oas3Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetScopesByParser(oas3Parser, oas3Scope);
    }

    @Test
    public void testGenerateAPIDefinition() throws Exception {
        testGenerateAPIDefinitionByParser(oas3Parser);
    }

    @Test
    public void testUpdateAPIDefinition() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");

        OASParserEvaluator evaluator = (definition -> {
            OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
            SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(definition, null, null);
            OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
            Assert.assertNotNull(openAPI);
            Assert.assertEquals(1, openAPI.getPaths().size());
            Assert.assertFalse(openAPI.getPaths().containsKey("/noresource/{resid}"));
        });
        testGenerateAPIDefinition2(oas3Parser, oas2Resources, evaluator);
    }

    @Test
    public void testUpdateAPIDefinitionWithExtensions() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3Resources.json";
        String oas3Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();

        // check remove vendor extensions
        String definition = testGenerateAPIDefinitionWithExtension(oas3Parser, oas3Resources);
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(definition, null, null);
        OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
        boolean isExtensionNotFound = openAPI.getExtensions() == null || !openAPI.getExtensions()
                .containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY);
        Assert.assertTrue(isExtensionNotFound);
        Assert.assertEquals(2, openAPI.getPaths().size());

        Iterator<Map.Entry<String, PathItem>> itr = openAPI.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, PathItem> pathEntry = itr.next();
            PathItem path = pathEntry.getValue();
            for (Operation operation : path.readOperations()) {
                Assert.assertFalse(operation.getExtensions().containsKey(APIConstants.SWAGGER_X_SCOPE));
            }
        }

        // check updated scopes in security definition
        Operation itemGet = openAPI.getPaths().get("/items").getGet();
        Assert.assertTrue(itemGet.getSecurity().get(0).get("default").contains("newScope"));

        // check available scopes in security definition
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("default");
        OAuthFlow implicityOauth = securityScheme.getFlows().getImplicit();
        Assert.assertTrue(implicityOauth.getScopes().containsKey("newScope"));
        Assert.assertEquals("newScopeDescription", implicityOauth.getScopes().get("newScope"));

        Assert.assertTrue(implicityOauth.getExtensions().containsKey(APIConstants.SWAGGER_X_SCOPES_BINDINGS));
        Map<String, String> scopeBinding =
                (Map<String, String>) implicityOauth.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
        Assert.assertTrue(scopeBinding.containsKey("newScope"));
        Assert.assertEquals("admin", scopeBinding.get("newScope"));
    }

    @Test
    public void testGetURITemplatesOfOpenAPI300Spec() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_uri_template.json";
        String openAPISpec300 =
                IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        uriTemplates.add(getUriTemplate("POST", "Application User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Any", "/abc"));
        Set<URITemplate> uriTemplateSet = oas3Parser.getURITemplates(openAPISpec300);
        Assert.assertEquals(uriTemplateSet, uriTemplates);

    }

    @Test
    public void testOpenApi3WithNonHttpVerbElementInPathItem() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_non_httpverb.json";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> expectedTemplates = new LinkedHashSet<>();
        expectedTemplates.add(getUriTemplate("GET", "Application", "/item"));
        Set<URITemplate> actualTemplates = oas3Parser.getURITemplates(openApi);
        Assert.assertEquals(actualTemplates, expectedTemplates);
    }

    @Test
    public void testValidateAPIDefinition() throws Exception {

        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_missing_info.json";
        String swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        API api = new API(new APIIdentifier("admin", "API", "1.0.0"));
        SwaggerData swaggerData = new SwaggerData(api);
        Mockito.when(apiManagerConfiguration.isAdvancedSwaggerValidationEnabled()).thenReturn(false);
        String validatedSwagger = oas3Parser.validateAPIDefinition(swagger, swaggerData);
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        OpenAPI validatedSwaggerObj = openAPIV3Parser.readContents(validatedSwagger, null, null).getOpenAPI();
        Assert.assertNotNull(validatedSwaggerObj.getInfo());
        Assert.assertEquals("API", validatedSwaggerObj.getInfo().getTitle());
        Assert.assertEquals("1.0.0", validatedSwaggerObj.getInfo().getVersion());
        relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_missing_version_title.json";
        swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        api = new API(new APIIdentifier("admin", "API2", "2.0.0"));
        validatedSwagger = oas3Parser.validateAPIDefinition(swagger, new SwaggerData(api));
        validatedSwaggerObj = openAPIV3Parser.readContents(validatedSwagger, null, null).getOpenAPI();
        Assert.assertNotNull(validatedSwaggerObj.getInfo());
        Assert.assertEquals("API2", validatedSwaggerObj.getInfo().getTitle());
        Assert.assertEquals("2.0.0", validatedSwaggerObj.getInfo().getVersion());
        PowerMockito.mockStatic(OASParserUtil.class);
        APIManagementException apiManagementException = new APIManagementException("Dummy exception");
        PowerMockito.when(OASParserUtil.class, "verifyAPIDefinitionFromParser",swagger, oas3Parser, swaggerData)
                .thenThrow(apiManagementException);
        try {
            validatedSwagger = oas3Parser.validateAPIDefinition(swagger, swaggerData);
        } catch (APIManagementException e) {
            Assert.assertEquals("Dummy exception", e.getMessage());
        }
    }
}