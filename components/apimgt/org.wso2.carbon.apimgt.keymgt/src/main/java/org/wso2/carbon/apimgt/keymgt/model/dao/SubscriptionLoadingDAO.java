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

package org.wso2.carbon.apimgt.keymgt.model.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class SubscriptionLoadingDAO {


    private static Log log = LogFactory.getLog(SubscriptionLoadingDAO.class);
    private static SubscriptionLoadingDAO subscriptionLoadingDao = null;

    /**
     * Private constructor
     */
    private SubscriptionLoadingDAO() {

    }

    /**
     * Returns an instance of CertificateMgtDao.
     */
    public static synchronized SubscriptionLoadingDAO getInstance() {

        if (subscriptionLoadingDao == null) {
            subscriptionLoadingDao = new SubscriptionLoadingDAO();
        }
        return subscriptionLoadingDao;
    }


    public List<Subscription> getAllSubscriptions() throws DataLoadingException {

        ArrayList<Subscription> subscriptions = null;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(SubscriptionConstants.SUBSCRIPTION_LOAD_SQL);
             ResultSet resultSet = ps.executeQuery();) {

            subscriptions = new ArrayList<>();

            while (resultSet.next()) {
                Subscription subscription = new Subscription();
                subscription.setSubscriptionId(resultSet.getInt("SUB_ID"));
                subscription.setTierName(resultSet.getString("TIER"));
                subscription.setApiId(resultSet.getInt("API_ID"));
                subscription.setAppId(resultSet.getInt("APP_ID"));
                subscription.setSubscriptionState(resultSet.getString("STATUS"));
                subscription.setSubscriptionWfState(resultSet.getString("WF_STATUS"));
                subscriptions.add(subscription);
            }

        } catch (SQLException e) {
            handleException("Error in loading Subscription : " + e.getMessage(), e);
        }

        return subscriptions;
    }

    public List<Application> getAllApplications() throws DataLoadingException {

        ArrayList<Application> applications = null;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.APPLICATION_LOAD_SQL);
             ResultSet resultSet = ps.executeQuery();
        ) {
            applications = new ArrayList<>();

            while (resultSet.next()) {
                Application application = new Application();
                application.setAppId(resultSet.getInt("APP_ID"));
                application.setAppName(resultSet.getString("NAME"));
                application.setAppTier(resultSet.getString("TIER"));
                application.setAppStatus(resultSet.getString("STATUS"));
                application.setSubName(resultSet.getString("SUB_NAME"));
                application.setCreatedBy(resultSet.getString("STATUS"));
                application.setAppId(resultSet.getInt("SUB_ID"));
                application.setTenantId(resultSet.getInt("TENANT_ID"));
                applications.add(application);
            }

        } catch (SQLException e) {
            handleException("Error in loading Applications : " + e.getMessage(), e);
        }

        return applications;
    }

    public List<ApplicationKeyMapping> getAllApplicationKeyMappings() throws DataLoadingException {

        ArrayList<ApplicationKeyMapping> keyMappings = null;
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.AM_KEY_MAPPING);
                ResultSet resultSet = ps.executeQuery();
        ) {

            keyMappings = new ArrayList<>();

            while (resultSet.next()) {
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                keyMapping.setWfState(resultSet.getString("STATE"));
                keyMappings.add(keyMapping);
            }

        } catch (SQLException e) {
            handleException("Error in loading Applications : " + e.getMessage(), e);
        }

        return keyMappings;
    }


    public Map<Integer, API> getAllApis() throws DataLoadingException {

        Map<Integer, API> apiMap = null;
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.API_LOAD_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            apiMap = new HashMap<>();

            while (resultSet.next()) {
                API api = new API();
                api.setApiId(resultSet.getInt("API_ID"));
                api.setApiProvider(resultSet.getString("API_PROVIDER"));
                api.setApiName(resultSet.getString("API_NAME"));
                api.setApiTier(resultSet.getString("API_TIER"));
                api.setApiVersion(resultSet.getString("API_VERSION"));
                api.setContext(resultSet.getString("CONTEXT"));
                apiMap.put(api.getApiId(), api);
            }

        } catch (SQLException e) {
            handleException("Error in loading Applications : " + e.getMessage(), e);
        }

        return apiMap;
    }

    public List<SubscriptionPolicy> getAllSubscriptionPolicies() throws DataLoadingException {

        ArrayList<SubscriptionPolicy> policies = null;
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.SUB_POLICY_LOAD_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {

            policies = new ArrayList<>();

            while (resultSet.next()) {
                SubscriptionPolicy policy = new SubscriptionPolicy();
                policy.setPolicyId(resultSet.getInt("POLICY_ID"));
                policy.setTierName(resultSet.getString("NAME"));
                policy.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                policy.setRateLimitCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                policy.setRateLimitTimeUnit(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));
                policies.add(policy);
            }

        } catch (SQLException e) {
            handleException("Error in loading Subscriptions : " + e.getMessage(), e);
        }

        return policies;
    }

    public List<ApplicationPolicy> getAllApplicationPolicies() throws DataLoadingException {

        ArrayList<ApplicationPolicy> policies = null;
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.APP_POLICY_LOAD_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            policies = new ArrayList<>();

            while (resultSet.next()) {
                ApplicationPolicy policy = new ApplicationPolicy();
                policy.setPolicyId(resultSet.getInt("POLICY_ID"));
                policy.setTierName(resultSet.getString("NAME"));
                policy.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policies.add(policy);
            }

        } catch (SQLException e) {
            handleException("Error in loading Subscriptions : " + e.getMessage(), e);
        }

        return policies;
    }

    public Map<Integer, Set<APIPolicyConditionGroup>> getApiPolicyConditionGroups() throws DataLoadingException {

        Map<Integer, Set<APIPolicyConditionGroup>> policyMap = null;

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.API_POLICY_CONDITION_LOAD_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            policyMap = new HashMap<>();

            while (resultSet.next()) {
                APIPolicyConditionGroup policyConditionGroup = new APIPolicyConditionGroup();
                policyConditionGroup.setConditionGroupId(resultSet.getInt("CONDITION_GROUP_ID"));
                policyConditionGroup.setPolicyId(resultSet.getInt("POLICY_ID"));
                policyConditionGroup.setQuotaType(resultSet.getString("QUOTA_TYPE"));
                Set<APIPolicyConditionGroup> conditionGroups =
                        policyMap.get(policyConditionGroup.getPolicyId());
                ConditionGroupDTO groupDTO =
                        ApiMgtDAO.getInstance().createConditionGroupDTO(policyConditionGroup.getConditionGroupId());
                policyConditionGroup.
                        setConditionDTOS(new HashSet<>(Arrays.asList(groupDTO.getConditions())));
                if (conditionGroups == null) {
                    conditionGroups = new HashSet<>();
                    conditionGroups.add(policyConditionGroup);
                    policyMap.put(policyConditionGroup.getPolicyId(), conditionGroups);
                } else {
                    conditionGroups.add(policyConditionGroup);
                }
            }

        } catch (SQLException e) {
            handleException("Error in loading Subscriptions : " + e.getMessage(), e);
        } catch (APIManagementException e) {
            handleException("Error while creating ConditionGroups : " + e.getMessage(), e);
        }
        return policyMap;
    }

    public Map<String, Resource> getApiUrlMappings() throws DataLoadingException {

        Map<String, Resource> resourceMap = null;

        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.API_URL_MAPPING_LOAD_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            resourceMap = new HashMap<>();

            while (resultSet.next()) {
                String urlMapping = resultSet.getString("URL_PATTERN");
                int apiId = resultSet.getInt("API_ID");
                Resource resource = new Resource(apiId, urlMapping);
                Resource cachedResource = resourceMap.get(resource.getCacheKey());
                if (cachedResource == null) {
                    resourceMap.put(resource.getCacheKey(), resource);
                    cachedResource = resource;
                }
                Verb verb = new Verb();
                verb.setVerbId(resultSet.getInt("URL_MAPPING_ID"));
                verb.setAuthType(resultSet.getString("AUTH_SCHEME"));
                verb.setHttpVerb(resultSet.getString("HTTP_METHOD"));
                verb.setThrottlingTier(resultSet.getString("THROTTLING_TIER"));

                InputStream mediationScriptBlob = resultSet.getBinaryStream("MEDIATION_SCRIPT");
                if (mediationScriptBlob != null) {
                    verb.setScript(APIMgtDBUtil.getStringFromInputStream(mediationScriptBlob));
                }
                cachedResource.addVerb(verb);
            }

        } catch (SQLException e) {
            handleException("Error in loading Subscriptions : " + e.getMessage(), e);
        }
        return resourceMap;
    }

    public Map<Integer, APIPolicy> getAllApiPolicies() throws DataLoadingException {

        Map<Integer, APIPolicy> policyMap = null;
        try (
                Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(SubscriptionConstants.API_POLICY_LOAD_SQL);
                ResultSet resultSet = ps.executeQuery();
        ) {
            policyMap = new HashMap<>();

            while (resultSet.next()) {
                APIPolicy policy = new APIPolicy();
                policy.setPolicyId(resultSet.getInt("POLICY_ID"));
                policy.setTierName(resultSet.getString("NAME"));
                policy.setQuotaType(resultSet.getString("DEFAULT_QUOTA_TYPE"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policyMap.put(policy.getPolicyId(), policy);
            }

        } catch (SQLException e) {
            handleException("Error in loading Subscriptions : " + e.getMessage(), e);
        }

        return policyMap;
    }

    public static void handleException(String msg, Throwable t) throws DataLoadingException {
        log.error(msg, t);
        throw new DataLoadingException(msg, t);
    }

}
