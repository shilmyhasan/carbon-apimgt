package org.wso2.carbon.apimgt.impl.caching;

public final class MonetizationConstants {

    public static final String API_NAME = "apiName";
    public static final String API_VERSION = "apiVersion";
    public static final String APPLICATION_ID = "applicationId";
    public static final String API_PROVIDER_TENANT_DOMAIN = "apiCreatorTenantDomain";
    public static final String API_PROVIDER = "apiCreator";
    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String QUANTITY = "quantity";
    public static final String TIMESTAMP = "timestamp";
    public static final String MONETIZATION_INFO = "MonetizationInfo";


    public static class Stripe{
        public static final String PLATFORM_ACCOUNT_STRIPE_KEY = "PlatformAccountStripeKey";
        public static final String CUSTOMER = "customer";
        public static final String PLAN = "plan";
        public static final String METERED_PLAN = "metered";
        public static final String CUSTOMER_DESCRIPTION = "description";
        public static final String CUSTOMER_EMAIL = "email";
        public static final String CUSTOMER_SOURCE = "source";
        public static final String ITEMS = "items";
        public static final String ACTION = "action";
        public static final String INCREMENT = "increment";
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
