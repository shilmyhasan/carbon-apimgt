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
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class SubscriptionLoadingDao {


    private static Log log = LogFactory.getLog(SubscriptionLoadingDao.class);
    private static SubscriptionLoadingDao subscriptionLoadingDao = null;
    private static boolean initialAutoCommit = false;

    /**
     * Private constructor
     */
    private SubscriptionLoadingDao() {

    }

    /**
     * Returns an instance of CertificateMgtDao.
     */
    public static synchronized SubscriptionLoadingDao getInstance() {

        if (subscriptionLoadingDao == null) {
            subscriptionLoadingDao = new SubscriptionLoadingDao();
        }
        return subscriptionLoadingDao;
    }


    public List<Subscription> getAllSubscriptions() throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<Subscription> subscriptions = null;
        try {
            conn = APIMgtDBUtil.getConnection();
//            conn.setAutoCommit(false);

            String query = SubscriptionConstants.SUBSCRIPTION_LOAD_SQL;
/*
            "SELECT " +
                    "   SUB.SUBSCRIPTION_ID AS SUB_ID," +
                    "   SUB.TIER_ID AS TIER," +
                    "   SUB.API_ID AS API_ID," +
                    "   SUB.APPLICATION_ID AS APP_ID," +
                    "   SUB.SUB_STATUS AS STATUS," +
                    "   SUB.SUBS_CREATE_STATE AS WF_STATUS" +
                    " FROM " +
                    "   AM_SUBSCRIPTION AS SUB";
            */
            ps = conn.prepareStatement(query);
            resultSet = ps.executeQuery();
//            conn.commit();
            subscriptions = new ArrayList<Subscription>();

            while (resultSet.next()){
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
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return subscriptions;
    }

    public List<Application> getAllApplications() throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<Application> applications = null;
        try {
            conn = APIMgtDBUtil.getConnection();

            String query = SubscriptionConstants.APPLICATION_LOAD_SQL;

            /*
            *                     "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.APPLICATION_STATUS AS STATUS," +
                    "   SUB.SUBSCRIBER_ID AS SUB_ID," +
                    "   SUB.TENANT_ID AS TENANT_ID," +
                    "   SUB.USER_ID AS SUB_NAME" +
                    " FROM " +
                    "   AM_APPLICATION AS APP," +
                    "   AM_SUBSCRIBER AS SUB" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID ";
            * */

            ps = conn.prepareStatement(query);
            resultSet = ps.executeQuery();
            applications = new ArrayList<Application>();

            while (resultSet.next()){
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
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return applications;
    }

    public List<ApplicationKeyMapping> getAllApplicationKeyMappings() throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<ApplicationKeyMapping> keyMappings = null;
        try {

            conn = APIMgtDBUtil.getConnection();

            String query = SubscriptionConstants.AM_KEY_MAPPING;

            ps = conn.prepareStatement(query);
            resultSet = ps.executeQuery();
            keyMappings = new ArrayList<ApplicationKeyMapping>();

            while (resultSet.next()){
                ApplicationKeyMapping keyMapping = new ApplicationKeyMapping();
                keyMapping.setApplicationId(resultSet.getInt("APPLICATION_ID"));
                keyMapping.setConsumerKey(resultSet.getString("CONSUMER_KEY"));
                keyMapping.setKeyType(resultSet.getString("KEY_TYPE"));
                keyMapping.setWfState(resultSet.getString("STATE"));
                keyMappings.add(keyMapping);
            }

        } catch (SQLException e) {
            handleException("Error in loading Applications : " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return keyMappings;
    }


    public List<API> getAllApis() throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<API> apis = null;
        try {

            conn = APIMgtDBUtil.getConnection();

            String query = SubscriptionConstants.API_LOAD_SQL;

            ps = conn.prepareStatement(query);
            resultSet = ps.executeQuery();
            apis = new ArrayList<API>();

            /*
            * "SELECT " +
                    " API_ID," +
                    " API_PROVIDER," +
                    " API_NAME," +
                    " API_VERSION," +
                    " CONTEXT " +
                    " FROM "+
                    "   API";
            * */

            while (resultSet.next()){
                API api = new API();
                api.setApiId(resultSet.getInt("API_ID"));
                api.setApiProvider(resultSet.getString("API_PROVIDER"));
                api.setApiName(resultSet.getString("API_NAME"));
                api.setApiTier(resultSet.getString("API_TIER"));
                api.setApiVersion(resultSet.getString("API_VERSION"));
                api.setContext(resultSet.getString("CONTEXT"));
                apis.add(api);
            }

        } catch (SQLException e) {
            handleException("Error in loading Applications : " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return apis;
    }


    public List<Policy> getAllPolicies() throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        ArrayList<Policy> policies = null;
        try {

            conn = APIMgtDBUtil.getConnection();

            String query = SubscriptionConstants.POLICY_LOAD_SQL;

            ps = conn.prepareStatement(query);
            resultSet = ps.executeQuery();
            policies = new ArrayList<Policy>();

            while (resultSet.next()){
                Policy policy = new Policy();
                policy.setPolicyId(resultSet.getInt("POLICY_ID"));
                policy.setTierName(resultSet.getString("NAME"));
                policy.setCount(resultSet.getInt("RATE_LIMIT_COUNT"));
                policy.setUnitTime(resultSet.getString("RATE_LIMIT_TIME_UNIT"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setStopOnQuotaReach(resultSet.getBoolean("STOP_ON_QUOTA_REACH"));
                policies.add(policy);
            }

        } catch (SQLException e) {
            handleException("Error in loading Applications : " + e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

        return policies;
    }



}
