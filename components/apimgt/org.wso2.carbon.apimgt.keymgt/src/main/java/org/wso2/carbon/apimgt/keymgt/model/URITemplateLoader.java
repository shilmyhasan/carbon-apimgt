/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.model;

import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.List;

/**
 * Abstraction for loading {@link URITemplate}. When making Subscription data memory resident,
 * URITemplates too should be loaded from the in-memory store. Hence this abstraction is needed.
 */
public interface URITemplateLoader {

    /**
     * Returns the list of {@link URITemplate}s associated with an API
     *
     * @param context context of the API
     * @param version version of the API
     * @return List of {@link URITemplate}s
     */
    public List<URITemplate> getAllURITemplates(String context, String version);
}
