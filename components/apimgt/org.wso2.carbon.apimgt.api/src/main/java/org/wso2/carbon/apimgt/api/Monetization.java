package org.wso2.carbon.apimgt.api;

import javax.ws.rs.core.Response;

public interface Monetization {

    /**
     * This method publishes the usage record to the Billing Engine when usage based billing model is used
     * @return Response returns the HTTP response for Rest API Implementation
     */
    public Response publishMonetizationUsageRecord();
}