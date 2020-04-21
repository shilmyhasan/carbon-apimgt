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

import org.wso2.carbon.apimgt.impl.config.KeyValidationHandlerConfig;
import org.wso2.carbon.apimgt.keymgt.model.exception.InitializationException;

/**
 * Interface to be extended if need to initialise a class by supplying a
 * {@link KeyValidationHandlerConfig}.
 */
public interface KeyValidatorConfigInitializable {

    /**
     * Initialises the instance using and instance of {@link KeyValidationHandlerConfig}
     *
     * @param config Subclass of {@link KeyValidationHandlerConfig}
     * @throws InitializationException when an error occurs while instantiation.
     */
    void initialize(KeyValidationHandlerConfig config) throws InitializationException;
}
