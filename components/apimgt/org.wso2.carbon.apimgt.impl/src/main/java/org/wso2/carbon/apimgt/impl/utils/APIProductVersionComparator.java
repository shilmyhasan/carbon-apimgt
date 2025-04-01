/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.APIProduct;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <p>Compares APIs by their versions. This comparator supports following version string
 * format.</p>
 * <ul>
 *     <li>VersionString := VersionToken+</li>
 *     <li>VersionToken  := VersionNumber | VersionSuffix | VersionNumber VersionSuffix</li>
 *     <li>VersionNumber := [0-9]+</li>
 *     <li>VersionSuffix := ~(0-9) AnyChar*</li>
 * </ul>
 * <p>Some example version strings supported by the comparator are given below.</p>
 * <ul>
 *     <li>1.5</li>
 *     <li>2.1.1</li>
 *     <li>2.1.2b</li>
 *     <li>1.3-SNAPSHOT</li>
 *     <li>2.0.0.wso2v4</li>
 * </ul>
 * <p>Version matching is carried out by comparing the version strings token by token. Version
 * numbers are compared in the conventional manner and the suffixes are compared
 * lexicographically.</p>
 */
public class APIProductVersionComparator implements Comparator<APIProduct>,Serializable {

    private APIVersionStringComparator stringComparator = new APIVersionStringComparator();

    @Override
    public int compare(APIProduct apiProduct1, APIProduct apiProduct2) {
        // In tenant mode, we could have same api published by two tenants to public store. So we need to check the
        // provider as well.
        // However, in the same tenant we could have 2 APIs with same API name and different providers.
        if (apiProduct1.getId().getName().equals(apiProduct2.getId().getName())) {
            if (apiProduct1.getId().getProviderName().equals(apiProduct2.getId().getProviderName()) ||
                    (apiProduct1.getOrganization() != null
                            && apiProduct1.getOrganization().equals(apiProduct2.getOrganization()))) {
                return stringComparator.compare(apiProduct1.getId().getVersion(), apiProduct2.getId().getVersion());
            }
        }
        return new APIProductNameComparator().compare(apiProduct1, apiProduct2);
    }
}
