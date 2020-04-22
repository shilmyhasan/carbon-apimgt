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

package org.wso2.carbon.apimgt.impl.config;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;

/**
 * Config element for MapBasedSubscriptionStore
 */
public class MapBasedSubscriptionStoreConfig extends InMemorySubscriptionStoreConfig {

    private SubscriptionDataLoaderConfig subscriptionDataLoaderConfig;

    public static final String SUBSCRIPTION_DATA_LOADER = "SubscriptionDataLoader";
    public static final String API_LOADING_FREQUENCY = "ApiLoadingFrequency";
    public static final String APP_LOADING_FREQUENCY = "ApplicationLoadingFrequency";
    public static final String SUB_LOADING_FREQUENCY = "SubscriptionLoadingFrequency";
    public static final String KEYMAPPING_LOADING_FREQUENCY = "KeyMappingLoadingFrequency";
    public static final String POLICY_LOADING_FREQUENCY = "PolicyLoadingFrequency";

    private int apiLoadingFrequency;
    private int appLoadingFrequency;
    private int subLoadingFrequency;
    private int keyMappingLoadingFrequency;
    private int policyLoadingFrequency;

    public int getApiLoadingFrequency() {

        return apiLoadingFrequency;
    }

    public void setApiLoadingFrequency(int apiLoadingFrequency) {

        this.apiLoadingFrequency = apiLoadingFrequency;
    }

    public int getAppLoadingFrequency() {

        return appLoadingFrequency;
    }

    public void setAppLoadingFrequency(int appLoadingFrequency) {

        this.appLoadingFrequency = appLoadingFrequency;
    }

    public int getSubLoadingFrequency() {

        return subLoadingFrequency;
    }

    public void setSubLoadingFrequency(int subLoadingFrequency) {

        this.subLoadingFrequency = subLoadingFrequency;
    }

    public int getKeyMappingLoadingFrequency() {

        return keyMappingLoadingFrequency;
    }

    public void setKeyMappingLoadingFrequency(int keyMappingLoadingFrequency) {

        this.keyMappingLoadingFrequency = keyMappingLoadingFrequency;
    }

    public int getPolicyLoadingFrequency() {

        return policyLoadingFrequency;
    }

    public void setPolicyLoadingFrequency(int policyLoadingFrequency) {

        this.policyLoadingFrequency = policyLoadingFrequency;
    }

    public SubscriptionDataLoaderConfig getSubscriptionDataLoaderConfig() {

        return subscriptionDataLoaderConfig;
    }

    @Override
    public void loadFromNode(OMElement node) {

        super.loadFromNode(node);
        OMElement configElement =
                node.getFirstChildWithName(new QName(APIConstants.ApiKeyValidator.CONFIGURATION_ELEMENT));

        if (configElement != null) {
            OMElement apiElement =
                    configElement.getFirstChildWithName(new QName(API_LOADING_FREQUENCY));
            this.apiLoadingFrequency = apiElement != null ?
                    Integer.parseInt(apiElement.getText()) : -1;

            OMElement appElement = configElement.getFirstChildWithName(new QName(APP_LOADING_FREQUENCY));
            this.appLoadingFrequency = appElement != null ?
                    Integer.parseInt(appElement.getText()) : -1;

            OMElement kmElement =
                    configElement.getFirstChildWithName(new QName(KEYMAPPING_LOADING_FREQUENCY));
            this.keyMappingLoadingFrequency = kmElement != null ?
                    Integer.parseInt(kmElement.getText()) : -1;

            OMElement policyElement =
                    configElement.getFirstChildWithName(new QName(POLICY_LOADING_FREQUENCY));
            this.policyLoadingFrequency = policyElement != null ?
                    Integer.parseInt(policyElement.getText()) : -1;

            OMElement subElement =
                    configElement.getFirstChildWithName(new QName(SUB_LOADING_FREQUENCY));
            this.subLoadingFrequency = subElement != null ?
                    Integer.parseInt(subElement.getText()) : -1;

            this.subscriptionDataLoaderConfig = new DbLoaderConfig();

            OMElement dataLoaderElement =
                    configElement.getFirstChildWithName(new QName(SUBSCRIPTION_DATA_LOADER));

            this.subscriptionDataLoaderConfig.loadFromNode(dataLoaderElement);
        }

    }

}
