package org.wso2.carbon.apimgt.impl.caching;

public final class MonetizationConstants {

    public static final String customer = "customer";
    public static final String plan = "plan";

    public static final String API_NAME = "apiName";
    public static final String API_VERSION = "apiVersion";
    public static final String APPLICATION_ID = "applicationId";
    public static final String API_PROVIDER_TENANT_DOMAIN = "apiCreatorTenantDomain";
    public static final String API_PROVIDER = "apiCreator";
    public static final String TIMESTAMP_PATTERN="yyyy-MM-dd HH:mm:ss";

    public static class StripeCustomer{
        public static final String description = "description";
        public static final String email = "email";
        public static final String source = "source";
    }

    public static class UsagePublisher{
        public static final String USAGE_RECORD_SIDDHI_APP = "APIM_MONETIZATION_SUMMARY";
        public static final String USAGE_RECORD_AGGREGATION = "MonetizationAgg";
        public static final String GRANULARITY = "minutes";
        public static final String RECORDS_DELIMITER = "records";
        public static final String LAST_PUBSLISHH_TIME_REG_LOCATION =
                "repository/components/org.wso2.carbon.registry/monetization/lastpublishtime";
    }
}
