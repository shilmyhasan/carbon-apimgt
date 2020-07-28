/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.jwt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import org.apache.axis2.util.URL;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Singleton which stores the revoked JWT map
 */
public class RevokedJWTDataHolder {

    private static final Log log = LogFactory.getLog(RevokedJWTDataHolder.class);
    private static Map<String, Long> revokedJWTMap = new ConcurrentHashMap<>();
    private static RevokedJWTDataHolder instance = new RevokedJWTDataHolder();

    /**
     * Adds a given key,value pair to the revoke map.
     * @param key key to be added.
     * @param value value to be added.
     */
    public void addRevokedJWTToMap(String key, Long value) {
        if (key != null && value != null) {
            log.debug("Adding revoked JWT key, value pair to the revoked map :" + key + " , " + value);
            revokedJWTMap.put(key, value);
        }
    }

    /**
     * Checks whether a given signature is in the map.
     * @param jwtSignature signature to be checked.
     * @return true if it exists and false otherwise.
     */
    public static boolean isJWTTokenSignatureExistsInRevokedMap(String jwtSignature) {
        return revokedJWTMap.containsKey(jwtSignature);
    }

    private RevokedJWTDataHolder() {

    }

    /**
     * Fetches the revoke map.
     * @return
     */
    Map<String, Long> getRevokedJWTMap() {
        return revokedJWTMap;
    }

    /**
     * This method can be used to get the singleton instance of this class.
     * @return the singleton instance.
     */
    public static RevokedJWTDataHolder getInstance() {
        return instance;
    }

    /**
     * This method can be used to revoke token without using client secret
     *
     * @param accessToken JWT Access Token
     * @param consumerKey Consumer Key
     */
    public void revokeJWTAccessToken(String accessToken, String consumerKey) {

        KeyManagerConfiguration configuration;
        RealmConfiguration realmConfig;

        try {
            realmConfig = new RealmConfigXMLProcessor().buildRealmConfigurationFromFile();
            configuration = KeyManagerHolder.getKeyManagerInstance().getKeyManagerConfiguration();

            String revokeEndpoint = configuration.getParameter(APIConstants.REVOKE_URL);
            URL keyMgtURL = new URL(revokeEndpoint);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();

            HttpPost httpRevokePost = new HttpPost(revokeEndpoint);
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);

            List<BasicNameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(APIConstants.TOKEN_KEY, accessToken));
            urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
            urlParameters.add(new BasicNameValuePair("username", realmConfig.getAdminUserName()));
            urlParameters.add(new BasicNameValuePair("password", realmConfig.getAdminPassword()));
            urlParameters.add(new BasicNameValuePair("token_type_hint", "access_token"));

            httpRevokePost.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
            httpRevokePost.setHeader(APIConstants.CONTENT_TYPE_HEADER, APIConstants.CONTENT_TYPE_APPLICATION_FORM);

            HttpResponse httpResponse = httpClient.execute(httpRevokePost);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully revoked the token");
                }
            } else {
                String responseBody = EntityUtils.toString(httpResponse.getEntity());
                log.error("Error occurred when revoking the Access token. Server responded with "
                        + httpResponse.getStatusLine().getStatusCode() + ". Reason " + responseBody);
            }

        } catch (APIManagementException | IOException | UserStoreException e) {
            log.error("Error occurred when revoking the One Time Access Token", e);
        }
    }
}