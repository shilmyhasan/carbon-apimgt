/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping APIM core tier related objects into REST API Tier related DTOs
 */
public class ThrottlingPolicyMappingUtil {

    /**
     * Converts a List object of Tiers into a DTO
     *
     * @param tiers  a list of Tier objects
     * @param limit  max number of objects returned
     * @param offset starting index
     * @return ThrottlingPolicyListDTO object containing ThrottlingPolicyDTOs
     */
    public static ThrottlingPolicyListDTO fromTierListToDTO(List<Tier> tiers, String tierLevel, int limit, int offset) {
        ThrottlingPolicyListDTO throttlingPolicyListDTO = new ThrottlingPolicyListDTO();
        List<ThrottlingPolicyDTO> ThrottlingPolicyDTOs = throttlingPolicyListDTO.getList();
        if (ThrottlingPolicyDTOs == null) {
            ThrottlingPolicyDTOs = new ArrayList<>();
            throttlingPolicyListDTO.setList(ThrottlingPolicyDTOs);
        }

        //identifying the proper start and end indexes
        int size = tiers.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        for (int i = start; i <= end; i++) {
            Tier tier = tiers.get(i);
            ThrottlingPolicyDTOs.add(fromTierToDTO(tier, tierLevel));
        }
        throttlingPolicyListDTO.setCount(ThrottlingPolicyDTOs.size());
        return throttlingPolicyListDTO;
    }

    /**
     * Sets pagination urls for a ThrottlingPolicyListDTO object given pagination parameters and url parameters
     *
     * @param throttlingPolicyListDTO a ThrottlingPolicyListDTO object
     * @param tierLevel   tier level (api/application or resource)
     * @param limit       max number of objects returned
     * @param offset      starting index
     * @param size        max offset
     */
    public static void setPaginationParams(ThrottlingPolicyListDTO throttlingPolicyListDTO, String tierLevel, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }


        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        throttlingPolicyListDTO.setPagination(paginationDTO);
    }

    /**
     * Converts a Tier object into ThrottlingPolicyDTO
     *
     * @param tier Tier object
     * @param tierLevel tier level (api/application or resource)
     * @return ThrottlingPolicyDTO corresponds to Tier object
     */
    public static ThrottlingPolicyDTO   fromTierToDTO(Tier tier, String tierLevel) {
        ThrottlingPolicyDTO dto = new ThrottlingPolicyDTO();
        dto.setName(tier.getName());
        dto.setDescription(tier.getDescription());
        if (StringUtils.isEmpty(tier.getDisplayName())) {
            dto.setDisplayName(tier.getName());
        } else {
            dto.setDisplayName(tier.getDisplayName());
        }
        dto.setRequestCount(tier.getRequestCount());
        dto.setUnitTime(tier.getUnitTime());
        dto.setStopOnQuotaReach(tier.isStopOnQuotaReached());
        dto.setPolicyLevel((ThrottlingPolicyDTO.PolicyLevelEnum.fromValue(tierLevel)));
        dto.setTimeUnit(tier.getTimeUnit());
        if (tier.getTierPlan() != null) {
            dto.setTierPlan(ThrottlingPolicyDTO.TierPlanEnum.fromValue(tier.getTierPlan()));
        }
        if (tier.getTierAttributes() != null) {
            Map<String, String> additionalProperties = new HashMap<>();
            for (String key : tier.getTierAttributes().keySet()) {
                additionalProperties.put(key, tier.getTierAttributes().get(key).toString());
            }
            dto.setAttributes(additionalProperties);
        }
        return dto;
    }
}
