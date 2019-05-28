package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ThrottlingPolicy;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import java.util.Set;

import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ThrottlingPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer.log;

public class ThrottlingPoliciesApiServiceImpl implements ThrottlingPoliciesApiService {

    @Override
    public Response throttlingPoliciesPolicyLevelGet(
            String policyLevel, Integer limit, Integer offset, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset if they are null
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        List<ThrottlingPolicy> throttlingPolicyList = getThrottlingPolicyList(policyLevel, xWSO2Tenant);
        ThrottlingPolicyListDTO tierListDTO = ThrottlingPolicyMappingUtil.fromTierListToDTO(throttlingPolicyList,
                policyLevel, limit, offset);
        ThrottlingPolicyMappingUtil.setPaginationParams(tierListDTO, policyLevel, limit, offset,
                throttlingPolicyList.size());
        return Response.ok().entity(tierListDTO).build();
    }

    @Override
    public Response throttlingPoliciesPolicyLevelPolicyIdGet(String tierId, String tierLevel, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    public List<ThrottlingPolicy> getThrottlingPolicyList(String policyLevel,String xWSO2Tenant) {
        List<ThrottlingPolicy> throttlingPolicyList = new ArrayList<>();
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);

        try {
            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }

            if (StringUtils.isBlank(policyLevel)) {
                RestApiUtil.handleBadRequest("tierLevel cannot be empty", log);
            }

            //retrieves the tier based on the given tier-level
            if (ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString().equals(policyLevel)) {
                Map<String, ThrottlingPolicy> apiTierMap = APIUtil.getThrottlingPolicies(
                        APIConstants.TIER_API_TYPE, requestedTenantDomain);
                if (apiTierMap != null) {
                    String username = RestApiUtil.getLoggedInUsername();
                    APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

                    Set<TierPermission> tierPermissions = apiConsumer.getTierPermissions();
                    for (TierPermission tierPermission : tierPermissions) {
                        ThrottlingPolicy tier = apiTierMap.get(tierPermission.getTierName());
                        tier.setThrottlingPolicyPermission(tierPermission);
                        apiTierMap.put(tierPermission.getTierName(), tier);
                    }

                    // Removing denied Tiers
                    Set<String> deniedTiers = apiConsumer.getDeniedTiers();
                    for (String tierName : deniedTiers) {
                        apiTierMap.remove(tierName);
                    }

                    throttlingPolicyList.addAll(apiTierMap.values());
                }
            } else if (ThrottlingPolicyDTO.PolicyLevelEnum.APPLICATION.toString().equals(policyLevel)) {
                Map<String, ThrottlingPolicy> appTierMap =
                        APIUtil.getThrottlingPolicies(APIConstants.TIER_APPLICATION_TYPE, requestedTenantDomain);
                if (appTierMap != null) {
                    throttlingPolicyList.addAll(appTierMap.values());
                }
            } else {
                RestApiUtil.handleResourceNotFoundError("tierLevel should be one of " +
                        Arrays.toString(ThrottlingPolicyDTO.PolicyLevelEnum.values()), log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return throttlingPolicyList;
    }
}
