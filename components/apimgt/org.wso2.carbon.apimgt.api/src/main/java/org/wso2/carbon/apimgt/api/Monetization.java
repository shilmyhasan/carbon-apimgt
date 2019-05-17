package org.wso2.carbon.apimgt.api;

import javax.ws.rs.core.Response;

public interface Monetization {
    public Response publishMonetizationUsageRecord() throws APIManagementException;
}