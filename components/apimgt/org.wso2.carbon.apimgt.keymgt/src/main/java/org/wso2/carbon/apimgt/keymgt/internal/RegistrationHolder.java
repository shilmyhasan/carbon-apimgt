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

package org.wso2.carbon.apimgt.keymgt.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Static register to keep track of different implementations used in KeyValidation Handler.
 */
public class RegistrationHolder {

    private static RegistrationHolder registrationHolder = new RegistrationHolder();
    private Map<String, Object> referenceHolder;

    private RegistrationHolder() {

        referenceHolder = new HashMap<String, Object>();
    }

    public static RegistrationHolder getInstance() {

        return registrationHolder;
    }

    public void registerInstance(String className, Object instance) {

        referenceHolder.put(className, instance);
    }

    public Object getReference(String className) {

        return referenceHolder.get(className);
    }

    public void unregister(String className) {

        referenceHolder.remove(className);
    }
}
