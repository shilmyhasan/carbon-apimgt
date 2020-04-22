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

public class SubscriptionConstants {

    public static final String APPLICATION_LOAD_SQL =
            " SELECT " +
                    "   APP.APPLICATION_ID AS APP_ID," +
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


    public static final String SUBSCRIPTION_LOAD_SQL =
            "SELECT " +
                    "   SUBSCRIPTION_ID AS SUB_ID," +
                    "   TIER_ID AS TIER," +
                    "   API_ID AS API_ID," +
                    "   APPLICATION_ID AS APP_ID," +
                    "   SUB_STATUS AS STATUS," +
                    "   SUBS_CREATE_STATE AS WF_STATUS" +
                    " FROM " +
                    "   AM_SUBSCRIPTION";

    public static final String SUB_POLICY_LOAD_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   RATE_LIMIT_COUNT," +
                    "   RATE_LIMIT_TIME_UNIT," +
                    "   QUOTA_TYPE," +
                    "   STOP_ON_QUOTA_REACH, " +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_SUBSCRIPTION";

    public static final String APP_POLICY_LOAD_SQL =
            "SELECT " +
                    "   POLICY_ID," +
                    "   NAME," +
                    "   QUOTA_TYPE," +
                    "   TENANT_ID " +
                    "FROM " +
                    "   AM_POLICY_APPLICATION";


    public static final String API_LOAD_SQL =
            "SELECT " +
                    " API_ID," +
                    " API_PROVIDER," +
                    " API_NAME," +
                    " API_TIER," +
                    " API_VERSION," +
                    " CONTEXT " +
                    " FROM "+
                    "   AM_API";

    public static final String API_URL_MAPPING_LOAD_SQL =
            "SELECT " +
                    " URL_MAPPING_ID," +
                    " API_ID," +
                    " HTTP_METHOD," +
                    " AUTH_SCHEME," +
                    " URL_PATTERN," +
                    " THROTTLING_TIER, " +
                    " MEDIATION_SCRIPT " +
                    " FROM "+
                    "   AM_API_URL_MAPPING";

    public static final String API_POLICY_CONDITION_LOAD_SQL =
            "SELECT " +
                    "CONDITION_GROUP_ID," +
                    "POLICY_ID," +
                    "QUOTA_TYPE " +
                    "   FROM " +
                    "       AM_CONDITION_GROUP;";

    public static final String API_POLICY_LOAD_SQL =
            "SELECT " +
                    "POLICY_ID," +
                    "NAME," +
                    "TENANT_ID," +
                    "DEFAULT_QUOTA_TYPE" +
                    " FROM " +
                    "   AM_API_THROTTLE_POLICY";

    public static final String AM_KEY_MAPPING =
            "SELECT "+
                    "   APPLICATION_ID," +
                    "   CONSUMER_KEY," +
                    "   KEY_TYPE," +
                    "   STATE" +
                    " FROM " +
                    "   AM_APPLICATION_KEY_MAPPING";

}
