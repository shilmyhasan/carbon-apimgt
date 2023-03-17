/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ImportUtils.class, APIConstants.class })
public class ImportUtilTest {

    private final JsonObject endpointConfigObject = new JsonObject();
    private final JsonObject config = new JsonObject();

    @Before
    public void init() {
        PowerMockito.mockStatic(APIConstants.class);
        endpointConfigObject.add(APIConstants.ENDPOINT_SPECIFIC_CONFIG, config);
    }

    @Test
    public void testGetUpdatedEndpointConfig() throws Exception {
        String activeDuration = "200";
        config.addProperty(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION, activeDuration);
        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpointConfigObject)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();
        String actualDuration = actualConfig.get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION).getAsString();
        Assert.assertEquals(actualDuration, activeDuration);
    }

    @Test
    public void testGetUpdatedEndpointConfigWithEmptyActionDuration() throws Exception {
        String emptyActiveDuration = "";
        config.addProperty(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION, emptyActiveDuration);
        JsonObject actualConfig = ImportUtils.getUpdatedEndpointConfig(endpointConfigObject)
                .get(APIConstants.ENDPOINT_SPECIFIC_CONFIG).getAsJsonObject();
        Assert.assertNull(actualConfig.get(APIConstants.ENDPOINT_CONFIG_ACTION_DURATION));
    }
}
