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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.impl.APIConstants;

public class GatewayUtils {

    /**
     * Constructs and returns the alias for the OAuth client secret based on the API name, version, and endpoint type.
     *
     * @param name    The name of the API.
     * @param version The version of the API.
     * @param type    The type of the endpoint security.
     * @return A concatenated string representing the OAuth client secret alias.
     */
    public static String retrieveOauthClientSecretAlias(String name, String version, String type) {

        return name.concat("--v").concat(version).concat("--")
                .concat(APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH).concat("--")
                .concat(APIConstants.ENDPOINT_SECURITY_CLIENT_SECRET).concat("--").concat(type);
    }

    /**
     * Constructs and returns the alias for the OAuth password based on the API name, version, and endpoint type.
     *
     * @param name    The name of the API.
     * @param version The version of the API.
     * @param type    The type of the endpoint security.
     * @return A concatenated string representing the OAuth password alias.
     */
    public static String retrieveOAuthPasswordAlias(String name, String version, String type) {

        return name.concat("--v").concat(version).concat("--")
                .concat(APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH).concat("--")
                .concat(APIConstants.ENDPOINT_SECURITY_PASSWORD).concat("--").concat(type);
    }
}
