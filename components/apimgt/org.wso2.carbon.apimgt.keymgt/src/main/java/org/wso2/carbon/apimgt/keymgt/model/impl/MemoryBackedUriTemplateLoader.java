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

package org.wso2.carbon.apimgt.keymgt.model.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.RegistrationHolder;
import org.wso2.carbon.apimgt.keymgt.model.InMemorySubscriptionStore;
import org.wso2.carbon.apimgt.keymgt.model.UriTemplateLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.Api;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicyConditionGroup;
import org.wso2.carbon.apimgt.keymgt.model.entity.Resource;
import org.wso2.carbon.apimgt.keymgt.model.entity.Verb;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation loading {@link URITemplate}s using Subscriptions stored in memory. This uses
 * the same {@link InMemorySubscriptionStore} instance used by
 * {@link org.wso2.carbon.apimgt.keymgt.handlers.KeyValidationHandler} implementation.
 */
public class MemoryBackedUriTemplateLoader implements UriTemplateLoader {

    private static final Log log = LogFactory.getLog(MemoryBackedUriTemplateLoader.class);
    private InMemorySubscriptionStore memorySubscriptionStore;

    public MemoryBackedUriTemplateLoader() {

        this.memorySubscriptionStore =
                (InMemorySubscriptionStore) RegistrationHolder.getInstance().
                        getReference(InMemorySubscriptionStore.class.getName());
    }

    @Override
    public List<URITemplate> getAllURITemplates(String context, String version) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Starting getAllURITemplates for context : %s and version : " +
                    "%s ", context, version));
        }

        List<URITemplate> uriTemplates = new ArrayList<>();

        Api api = memorySubscriptionStore.getApiByContextAndVersion(context, version);
        if (api == null) {
            return uriTemplates;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("API found for context : %s and version : %s", context, version));
        }

        List<Resource> resourceList = api.getAllResources();
        for (Resource resource : resourceList) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Traversing resource %s found for context : %s and " +
                        "version : %s", resource.getUrlPattern(), context, version));
            }

            List<Verb> httpVerbs = resource.getAllVerbs();

            for (Verb verb : httpVerbs) {
                URITemplate uriTemplate = new URITemplate();
                uriTemplate.setThrottlingTier(verb.getThrottlingTier());
                uriTemplate.setAuthType(verb.getAuthType());
                uriTemplate.setHTTPVerb(verb.getHttpVerb());
                uriTemplate.setUriTemplate(resource.getUrlPattern());
                uriTemplate.setMediationScript(verb.getScript());
                uriTemplates.add(uriTemplate);

                // Works only for Super Tenant Mode.

                if (APIUtil.isAdvanceThrottlingEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Api Throttling Policy found for  resource : %s ," +
                                        " context : %s and version : %s", resource.getUrlPattern(), context
                                , version));
                    }
                    setAdvancedThrottlingPolicies(uriTemplate);
                }

            }

        }

        return uriTemplates;

    }

    private void setAdvancedThrottlingPolicies(URITemplate uriTemplate) {

        ApiPolicy policy =
                memorySubscriptionStore.getApiPolicyByName(uriTemplate.getThrottlingTier(),
                        MultitenantConstants.SUPER_TENANT_ID);

        Set<ApiPolicyConditionGroup> conditionGroup = policy.getConditionGroups();
        Set<ConditionGroupDTO> conditionGroupDTOS = new HashSet<>();
        ConditionGroupDTO defaultGroup = new ConditionGroupDTO();
        defaultGroup.setConditionGroupId(APIConstants.THROTTLE_POLICY_DEFAULT);
        uriTemplate.getThrottlingConditions().add(APIConstants.THROTTLE_POLICY_DEFAULT);
        conditionGroupDTOS.add(defaultGroup);

        if (conditionGroup != null) {
            for (ApiPolicyConditionGroup policyConditionGroup : conditionGroup) {
                ConditionGroupDTO groupDTO = new ConditionGroupDTO();
                groupDTO.setConditionGroupId("_condition_" + policyConditionGroup.getConditionGroupId());
                groupDTO.setConditions(policyConditionGroup.getConditionDTOS().toArray(new ConditionDTO[]{}));
                conditionGroupDTOS.add(groupDTO);
            }
        }
        uriTemplate.setConditionGroups(conditionGroupDTOS.toArray(new ConditionGroupDTO[]{}));
    }

}
