package org.wso2.carbon.apimgt.impl.util.test;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.mockito.Mockito.never;

/**
 * Test if default throttling policies are added into the database again if they had already been added once. (Decision
 * on whether default policies are added already once is taken depending on the availability of 'Unlimited' Application
 * Policy in the database)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, ApiMgtDAO.class, ApplicationPolicy.class, SubscriptionPolicy.class, APIPolicy.class})
public class ThrottlingTest extends TestCase {
    public void testDeleteDefaultThrottlingPoliciesOfSuperTenant() throws APIManagementException {
        final int tenantId = -1234;
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_APP, tenantId,
                APIConstants.DEFAULT_APP_POLICY_UNLIMITED)).thenReturn(true);
        APIUtil.addDefaultSuperTenantAdvancedThrottlePolicies();

        Mockito.verify(apiMgtDAO,never()).addApplicationPolicy(Mockito.any(ApplicationPolicy.class));
        Mockito.verify(apiMgtDAO,never()).addSubscriptionPolicy(Mockito.any(SubscriptionPolicy.class));
        Mockito.verify(apiMgtDAO,never()).addAPIPolicy(Mockito.any(APIPolicy.class));
    }

    public void testDeleteDefaultThrottlingPoliciesOfTenant() throws APIManagementException {
        final int tenantId = 1;
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        final String tenantDomain = MultitenantConstants.TENANT_DOMAIN;

        PowerMockito.mockStatic(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_APP, tenantId,
                APIConstants.DEFAULT_APP_POLICY_UNLIMITED)).thenReturn(true);
        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);

        Mockito.verify(apiMgtDAO,never()).addApplicationPolicy(Mockito.any(ApplicationPolicy.class));
        Mockito.verify(apiMgtDAO,never()).addSubscriptionPolicy(Mockito.any(SubscriptionPolicy.class));
        Mockito.verify(apiMgtDAO,never()).addAPIPolicy(Mockito.any(APIPolicy.class));
    }
}
