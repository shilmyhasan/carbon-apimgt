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

package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.Map;

/**
 * Set the parameters for secured endpoints
 */
public class SecurityConfigContext extends ConfigContextDecorator {

    private API api;
    private APIProduct apiProduct;

    public SecurityConfigContext(ConfigContext context,API api) {
        super(context);
        this.api = api;
    }

    public SecurityConfigContext(ConfigContext context, APIProduct apiProduct) {
        super(context);
        this.apiProduct = apiProduct;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        if (api != null) {
            String alias =  api.getId().getProviderName() + "--" + api.getId().getApiName()
                    + api.getId().getVersion();
            String unpw = api.getEndpointUTUsername() + ":" + api.getEndpointUTPassword();

            boolean isSecureVaultEnabled = Boolean.parseBoolean(getApiManagerConfiguration().
                    getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));

            context.put("isEndpointSecured", api.isEndpointSecured());
            context.put("isEndpointAuthDigest", api.isEndpointAuthDigest());
            context.put("username", api.getEndpointUTUsername());
            context.put("securevault_alias", alias);
            context.put("base64unpw", new String(Base64.encodeBase64(unpw.getBytes())));
            context.put("isSecureVaultEnabled", isSecureVaultEnabled);

        } else if (apiProduct != null) {
            boolean isSecureVaultEnabled = Boolean.parseBoolean(getApiManagerConfiguration().
                    getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
            for (APIProductResource apiProductResource : apiProduct.getProductResources()) {
                Map<String, EndpointSecurity> endpointSecurityMap = apiProductResource.getEndpointSecurityMap();
                String username = endpointSecurityMap.get(APIConstants.ENDPOINT_SECURITY).getUsername();
                String password = endpointSecurityMap.get(APIConstants.ENDPOINT_SECURITY).getPassword();
                String alias =  apiProduct.getId().getProviderName() + "--" + apiProduct.getId().getName()
                        + apiProduct.getId().getVersion();
                String unpw = username + ":" + password;
                boolean isEndpointAuthDigest = false;
                if (endpointSecurityMap.get(APIConstants.ENDPOINT_SECURITY).getType()== APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST.
                        toUpperCase()) {
                    isEndpointAuthDigest = true;
                }

                context.put("isEndpointSecured",  endpointSecurityMap.get(APIConstants.ENDPOINT_SECURITY).getEnabled());
                context.put("isEndpointAuthDigest", isEndpointAuthDigest);
                context.put("username", username);
                context.put("securevault_alias", alias);
                context.put("base64unpw", new String(Base64.encodeBase64(unpw.getBytes())));
                context.put("isSecureVaultEnabled", isSecureVaultEnabled);
            }
        }
        return context;
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }
}
