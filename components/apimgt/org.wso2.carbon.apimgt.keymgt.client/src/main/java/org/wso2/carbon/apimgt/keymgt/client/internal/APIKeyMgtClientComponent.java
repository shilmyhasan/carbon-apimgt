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

package org.wso2.carbon.apimgt.keymgt.client.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;

/**
 * @scr.component name="api.keymgt.client.component" immediate="true"
 */
public class APIKeyMgtClientComponent {

    private static final Log log = LogFactory.getLog(APIKeyMgtClientComponent.class);


    protected void activate(ComponentContext componentContext) {
        try {
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                    (getClientRepoLocation(), getAxis2ClientXmlLocation());
            ServiceReferenceHolder.getInstance().setAxis2ConfigurationContext(ctx);
            if (log.isDebugEnabled()) {
                log.debug("KeyManagement Client component activated");
            }
        } catch (AxisFault axisFault) {
            log.error("Error while initializing the Key Management Client component", axisFault);
        }
    }

    protected String getAxis2ClientXmlLocation() {
        return ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    }

    protected String getClientRepoLocation() {
        return ServerConfiguration.getInstance().getFirstProperty("Axis2Config.ClientRepositoryLocation");
    }
}
