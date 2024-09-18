/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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


package org.wso2.carbon.apimgt.impl.dao.constants;

/**
 * This class will hold MSSQL constants.
 */
public class SQLConstantsMSSQL extends SQLConstants{

    public static final String GET_ALL_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.UUID AS APP_UUID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.NAME AS APS_NAME," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME," +
                    "   ATTRIBUTES.NAME AS ATTRIBUTE_NAME," +
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE" +
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP" +
                    "   LEFT OUTER JOIN AM_APPLICATION_ATTRIBUTES ATTRIBUTES  " +
                    "ON APP.APPLICATION_ID = ATTRIBUTES.APPLICATION_ID" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID ";

    public static final String GET_TENANT_APPLICATIONS_SQL =
            " SELECT " +
                    "   APP.UUID AS APP_UUID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS APS_NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME," +
                    "   ATTRIBUTES.NAME AS ATTRIBUTE_NAME," +
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE"+
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP" +
                    "   LEFT OUTER JOIN AM_APPLICATION_ATTRIBUTES ATTRIBUTES" +
                    "  ON APP.APPLICATION_ID = ATTRIBUTES.APPLICATION_ID" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   SUB.TENANT_ID = ? ";

    public static final String GET_APPLICATION_BY_ID_SQL =
            " SELECT " +
                    "   APP.UUID AS APP_UUID," +
                    "   APP.APPLICATION_ID AS APP_ID," +
                    "   APP.NAME AS APS_NAME," +
                    "   APP.APPLICATION_TIER AS TIER," +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE," +
                    "   SUB.USER_ID AS SUB_NAME," +
                    "   ATTRIBUTES.NAME AS ATTRIBUTE_NAME," +
                    "   ATTRIBUTES.VALUE AS ATTRIBUTE_VALUE"+
                    " FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP" +
                    "   LEFT OUTER JOIN AM_APPLICATION_ATTRIBUTES ATTRIBUTES  " +
                    "ON APP.APPLICATION_ID = ATTRIBUTES.APPLICATION_ID" +
                    " WHERE " +
                    "   APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID AND" +
                    "   APP.APPLICATION_ID = ? ";

    public static final String ADD_APPLICATION_ATTRIBUTES_SQL =
            " INSERT INTO AM_APPLICATION_ATTRIBUTES (APPLICATION_ID, NAME, VALUE, TENANT_ID) VALUES (?,?,?,?)";

    public static final String REMOVE_APPLICATION_ATTRIBUTES_SQL =
            " DELETE FROM " +
                    "   AM_APPLICATION_ATTRIBUTES" +
                    " WHERE" +
                    "   APPLICATION_ID = ?";

    public static final String REMOVE_APPLICATION_ATTRIBUTES_BY_ATTRIBUTE_NAME_SQL =
            " DELETE FROM " +
                    "   AM_APPLICATION_ATTRIBUTES" +
                    " WHERE" +
                    "   NAME = ? AND APPLICATION_ID = ?";

    public static final String GET_APPLICATION_ATTRIBUTES_BY_APPLICATION_ID =
            " SELECT " +
                    "   APP.APPLICATION_ID," +
                    "   APP.NAME," +
                    "   APP.VALUE" +
                    " FROM " +
                    "   AM_APPLICATION_ATTRIBUTES APP WHERE APPLICATION_ID = ?";

    public static final String ADD_BLOCK_CONDITIONS_SQL =
            "INSERT INTO AM_BLOCK_CONDITIONS (TYPE, VALUE,ENABLED,DOMAIN,UUID) VALUES (?,?,?,?,?)";
    public static final String GET_BLOCK_CONDITIONS_SQL =
            "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE DOMAIN =?";
    public static final String GET_BLOCK_CONDITIONS_BY_TYPE_AND_VALUE_SQL =
            "SELECT CONDITION_ID, TYPE, VALUE, ENABLED, DOMAIN, UUID FROM AM_BLOCK_CONDITIONS WHERE " +
                    "(TYPE = ? OR ? IS NULL) AND (VALUE LIKE '%' + ? + '%' OR ? IS NULL) AND DOMAIN = ?";
    public static final String GET_BLOCK_CONDITIONS_BY_TYPE_AND_EXACT_VALUE_SQL =
            "SELECT CONDITION_ID, TYPE, VALUE, ENABLED, DOMAIN, UUID FROM AM_BLOCK_CONDITIONS WHERE " +
                    "(TYPE = ? OR ? IS NULL) AND VALUE = ? AND DOMAIN = ?";
    public static final String GET_BLOCK_CONDITION_SQL =
            "SELECT TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE CONDITION_ID =?";
    public static final String GET_BLOCK_CONDITION_BY_UUID_SQL =
            "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE UUID =?";
    public static final String UPDATE_BLOCK_CONDITION_STATE_SQL =
            "UPDATE AM_BLOCK_CONDITIONS SET ENABLED = ? WHERE CONDITION_ID = ?";
    public static final String UPDATE_BLOCK_CONDITION_STATE_BY_UUID_SQL =
            "UPDATE AM_BLOCK_CONDITIONS SET ENABLED = ? WHERE UUID = ?";
    public static final String DELETE_BLOCK_CONDITION_SQL =
            "DELETE FROM AM_BLOCK_CONDITIONS WHERE CONDITION_ID=?";
    public static final String DELETE_BLOCK_CONDITION_BY_UUID_SQL =
            "DELETE FROM AM_BLOCK_CONDITIONS WHERE UUID=?";
    public static final String BLOCK_CONDITION_EXIST_SQL =
            "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE DOMAIN =? "
                    + "AND TYPE =? AND VALUE =?";
    public static final String GET_SUBSCRIPTION_BLOCK_CONDITION_BY_VALUE_AND_DOMAIN_SQL =
            "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,DOMAIN,UUID FROM AM_BLOCK_CONDITIONS WHERE "
                    + "VALUE = ? AND DOMAIN = ? ";

    public static final String GET_APPLICATIONS_PREFIX_CASESENSITVE_WITHGROUPID =
            "select distinct x.*,bl.ENABLED from (" +
            "SELECT * FROM (" +
            "   SELECT " +
            "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row ," +
            "   APPLICATION_ID, " +
            "   cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME," +
            "   APPLICATION_TIER," +
            "   APP.SUBSCRIBER_ID,  " +
            "   CALLBACK_URL,  " +
            "   DESCRIPTION, " +
            "   APPLICATION_STATUS, " +
            "   USER_ID, " +
            "   GROUP_ID, " +
            "   UUID, " +
            "   APP.CREATED_BY AS CREATED_BY, " +
            "   APP.TOKEN_TYPE AS TOKEN_TYPE " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND " +
            "   (GROUP_ID= ?  OR  (GROUP_ID='' AND SUB.USER_ID COLLATE Latin1_General_CS_AS =?))" +
            " And " +
            "    NAME like ?" +
            " ) a " +
            " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.NAME)" +
            " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";




    public static final String GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE_WITHGROUPID =
            "select distinct x.*,bl.ENABLED from (" +
            "SELECT * FROM (" +
            "   SELECT " +
            "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row," +
            "   APPLICATION_ID, " +
            "   cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME," +
            "   APPLICATION_TIER," +
            "   APP.SUBSCRIBER_ID,  " +
            "   CALLBACK_URL,  " +
            "   DESCRIPTION, " +
            "   APPLICATION_STATUS, " +
            "   USER_ID, " +
            "   GROUP_ID, " +
            "   UUID, " +
            "   APP.CREATED_BY AS CREATED_BY, " +
            "   APP.TOKEN_TYPE AS TOKEN_TYPE " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND " +
            "   (GROUP_ID= ?  OR (GROUP_ID='' AND LOWER (SUB.USER_ID) = LOWER(?)))"+
            " And "+
            "    NAME like ?"+
            " ) a WHERE a.row > ? and a.row <= a.row + ?"+
            " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.NAME)"+
            " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";


    public static final String GET_APPLICATIONS_PREFIX_CASESENSITVE_WITH_MULTIGROUPID =
            "select distinct x.*,bl.ENABLED from (" +
                    "SELECT * FROM (" +
                    "   SELECT " +
                    "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row ," +
                    "   APPLICATION_ID, " +
                    "   cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME," +
                    "   APPLICATION_TIER," +
                    "   APP.SUBSCRIBER_ID,  " +
                    "   CALLBACK_URL,  " +
                    "   DESCRIPTION, " +
                    "   APPLICATION_STATUS, " +
                    "   USER_ID, " +
                    "   GROUP_ID, " +
                    "   UUID, " +
                    "   APP.CREATED_BY AS CREATED_BY, " +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE " +
                    " FROM" +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIBER SUB  " +
                    " WHERE " +
                    "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    " AND (" +
                    "    (APPLICATION_ID IN ( SELECT APPLICATION_ID FROM AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID IN ($params) AND TENANT = ?)) " +
                    "           OR " +
                    "    SUB.USER_ID COLLATE Latin1_General_CS_AS =?" +
                    "           OR " +
                    "    (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ?))" +
                    " )" +
                    " And "+
                    "    NAME like ? ) a " +
                    " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.NAME)" +
                    " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";




    public static final String GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE_WITH_MULTIGROUPID =
            "select distinct x.*,bl.ENABLED from (" +
                    "SELECT * FROM (" +
                    "   SELECT " +
                    "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row," +
                    "   APPLICATION_ID, " +
                    "   cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME," +
                    "   APPLICATION_TIER," +
                    "   APP.SUBSCRIBER_ID,  " +
                    "   CALLBACK_URL,  " +
                    "   DESCRIPTION, " +
                    "   APPLICATION_STATUS, " +
                    "   USER_ID, " +
                    "   GROUP_ID, " +
                    "   UUID, " +
                    "   APP.CREATED_BY AS CREATED_BY, " +
                    "   APP.TOKEN_TYPE AS TOKEN_TYPE " +
                    " FROM" +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIBER SUB  " +
                    " WHERE " +
                    "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    " AND (" +
                    "    (APPLICATION_ID IN ( SELECT APPLICATION_ID FROM AM_APPLICATION_GROUP_MAPPING WHERE GROUP_ID " +
                    " COLLATE Latin1_General_CS_AS IN ($params) AND TENANT = ? ))" +
                    "           OR " +
                    "    (LOWER (SUB.USER_ID) = LOWER(?))" +
                    "           OR " +
                    "    (APP.APPLICATION_ID IN (SELECT APPLICATION_ID FROM AM_APPLICATION WHERE GROUP_ID = ? COLLATE Latin1_General_CS_AS))" +
                    " )" +
                    " And " +
                    "    NAME like ?"+
                    " ) a " +
                    " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.NAME)" +
                    " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";


    public static final String GET_APPLICATIONS_PREFIX_CASESENSITVE =
            "select distinct x.*,bl.ENABLED from (" +
            "SELECT * FROM (" +
            "   SELECT " +
            "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row," +
            "   APPLICATION_ID, " +
            "   cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME," +
            "   APPLICATION_TIER," +
            "   APP.SUBSCRIBER_ID,  " +
            "   CALLBACK_URL,  " +
            "   DESCRIPTION, " +
            "   APPLICATION_STATUS, " +
            "   USER_ID, " +
            "   GROUP_ID, " +
            "   UUID, " +
            "   APP.CREATED_BY AS CREATED_BY, " +
            "   APP.TOKEN_TYPE AS TOKEN_TYPE " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND " +
            "    SUB.USER_ID COLLATE Latin1_General_CS_AS =?"+
            " And "+
            "    NAME like ?"+
            " )a " +
            " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.NAME)" +
            " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";


    public static final String GET_APPLICATIONS_PREFIX_NONE_CASESENSITVE =
            "select distinct x.*,bl.ENABLED from (" +
            "SELECT * FROM (" +
            "   SELECT " +
            "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row," +
            "   APPLICATION_ID, " +
            "   cast(NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME," +
            "   APPLICATION_TIER," +
            "   APP.SUBSCRIBER_ID,  " +
            "   CALLBACK_URL,  " +
            "   DESCRIPTION, " +
            "   APPLICATION_STATUS, " +
            "   USER_ID, " +
            "   GROUP_ID, " +
            "   UUID, " +
            "   APP.CREATED_BY AS CREATED_BY, " +
            "   APP.TOKEN_TYPE AS TOKEN_TYPE " +
            " FROM" +
            "   AM_APPLICATION APP, " +
            "   AM_SUBSCRIBER SUB  " +
            " WHERE " +
            "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
            " AND " +
            "    LOWER(SUB.USER_ID) = LOWER(?)" +
            " And "+
            "    NAME like ?"+
            " ) a " +
            " )x left join AM_BLOCK_CONDITIONS bl on  ( bl.TYPE = 'APPLICATION' AND bl.VALUE = (x.USER_ID + ':') + x.NAME)" +
            " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GET_APPLICATIONS_BY_TENANT_ID =
            "select distinct x.* from (" +
                    "SELECT * FROM (" +
                    "   SELECT " +
                    "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row," +
                    "   APP.APPLICATION_ID as APPLICATION_ID, " +
                    "   SUB.CREATED_BY AS CREATED_BY, " +
                    "   APP.GROUP_ID AS GROUP_ID, " +
                    "   SUB.TENANT_ID AS TENANT_ID, " +
                    "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID, " +
                    "   APP.UUID AS UUID," +
                    "   cast(APP.NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME, " +
                    "   APP.APPLICATION_STATUS as APPLICATION_STATUS" +
                    " FROM" +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIBER SUB  " +
                    " WHERE " +
                    "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    " AND " +
                    "    SUB.TENANT_ID = ?"+
                    " And "+
                    "    ( SUB.CREATED_BY like ?"+
                    " OR APP.NAME like ?"+
                    " )) a " +
                    " )x" +
                    " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GET_APPLICATIONS_BY_NAME =
            "select distinct x.* from (" +
                    "SELECT * FROM (" +
                    "   SELECT " +
                    "   ROW_NUMBER() OVER (ORDER BY APPLICATION_ID) as row," +
                    "   APP.APPLICATION_ID as APPLICATION_ID, " +
                    "   SUB.CREATED_BY AS CREATED_BY, " +
                    "   APP.GROUP_ID AS GROUP_ID, " +
                    "   SUB.TENANT_ID AS TENANT_ID, " +
                    "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID, " +
                    "   APP.UUID AS UUID," +
                    "   cast(APP.NAME as varchar(100)) collate SQL_Latin1_General_CP1_CI_AS as NAME, " +
                    "   APP.APPLICATION_STATUS as APPLICATION_STATUS" +
                    " FROM" +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIBER SUB  " +
                    " WHERE " +
                    "   SUB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    " AND " +
                    "    SUB.TENANT_ID = ?"+
                    " And "+
                    "    ( APP.NAME like ? )) a " +
                    " )x" +
                    " ORDER BY $1 $2 OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

}
