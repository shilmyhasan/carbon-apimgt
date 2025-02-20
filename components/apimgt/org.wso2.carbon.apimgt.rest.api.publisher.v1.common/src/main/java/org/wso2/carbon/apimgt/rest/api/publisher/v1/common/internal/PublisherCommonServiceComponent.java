/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * OSGi component responsible for managing and configuring the API Manager Configuration Service in the
 * API Publisher common component. This class activates the OSGi service and binds/unbinds the necessary services.
 */
@Component(
        name = "api.publisher.common.component",
        immediate = true)
public class PublisherCommonServiceComponent {

    private static final Log log = LogFactory.getLog(PublisherCommonServiceComponent.class);

    /**
     * Activates the component.
     *
     * @param context The component context.
     */
    @Activate
    protected void activate(org.osgi.service.component.ComponentContext context) {
    }

    /**
     * Sets the API Manager Configuration Service. This method is called  when the API Manager Configuration Service
     * becomes available.
     *
     * @param configurationService The API Manager Configuration Service instance.
     */
    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY,
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    /**
     * Unsets the API Manager Configuration Service.
     *
     * @param configurationService The API Manager Configuration Service instance.
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }
}
