/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Collections;
import java.util.UUID;

public class SubscriptionBlockingUtil {

    /**
     * Add a subscription block condition
     *
     * @param conditionType  type of the condition
     * @param conditionValue value of the condition
     * @throws APIManagementException if failed to add subscription block condition
     */
    public static void addSubscriptionBlockCondition(String conditionType, String conditionValue,
                                                     String tenantDomain) throws
            APIManagementException {

        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setConditionType(conditionType);
        blockConditionsDTO.setConditionValue(conditionValue);
        blockConditionsDTO.setTenantDomain(tenantDomain);
        blockConditionsDTO.setEnabled(true);
        blockConditionsDTO.setUUID(UUID.randomUUID().toString());
        String[] conditionsArray = conditionValue.split(":");
        BlockConditionsDTO createdBlockConditionsDto;
        if (conditionsArray.length > 0) {
            createdBlockConditionsDto = ApiMgtDAO.getInstance().insertBlockCondition(blockConditionsDTO);
        } else {
            throw new APIManagementException(
                    "Invalid subscription block condition with insufficient data : " + conditionValue);
        }
        if (createdBlockConditionsDto != null) {
            publishSubscriptionBlockingEvent(createdBlockConditionsDto, "true", tenantDomain);
        }
    }

    /**
     * Publishes the changes on blocking conditions.
     *
     * @param blockConditionsDTO Block condition Dto event
     * @param tenantDomain       The tenant domain
     */
    public static void publishSubscriptionBlockingEvent(BlockConditionsDTO blockConditionsDTO, String state,
                                                        String tenantDomain) {

        Object[] objects = new Object[]{blockConditionsDTO.getConditionId(), blockConditionsDTO.getConditionType(),
                blockConditionsDTO.getConditionValue(), state, tenantDomain};
        Event blockingMessage = new Event(APIConstants.BLOCKING_CONDITIONS_STREAM_ID, System.currentTimeMillis(),
                null, null, objects);
        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().getThrottleProperties();

        if (throttleProperties.getDataPublisher() != null && throttleProperties.getDataPublisher().isEnabled()) {
            APIUtil.publishEvent(APIConstants.BLOCKING_EVENT_PUBLISHER, Collections.EMPTY_MAP, blockingMessage);
        }
    }
}
