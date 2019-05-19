/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.workflow;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.Token;
import com.stripe.net.RequestOptions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.MonetizationPlatformCustomer;
import org.wso2.carbon.apimgt.api.model.MonetizationSharedCustomer;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.MonetizationConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SubscriptionCreationSimpleWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(SubscriptionCreationSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException{
        return null;
    }

    /**
     * This method executes subscription creation simple workflow and return workflow response back to the caller
     *
     * @param workflowDTO  The WorkflowDTO which contains workflow contextual information related to the workflow

     * @return workflow response back to the caller
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        super.execute(workflowDTO);
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        WorkflowResponse workflowResponse = complete(workflowDTO);
        super.publishEvents(workflowDTO);

        return new GeneralWorkflowResponse();

    }

    /**
     * This method executes all the subscriptions related to monetizationand return workflow response back to the caller
     * @param workflowDTO  The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response back to the caller
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    @Override
    public WorkflowResponse monetizeSubscription(WorkflowDTO workflowDTO) throws WorkflowException{

        boolean isMonetizationEnabled = false;
        SubscriptionWorkflowDTO subWorkFlowDTO = null;
        String stripePlatformAccountKey = null;
        String ConnectId="acct_1EQF7PCxKhMnrBL5";
        Subscriber subscriber = null;
        Customer customer = null;
        Customer sharedCustomerBE = null;
        MonetizationPlatformCustomer monetizationPlatformCustomer;
        MonetizationSharedCustomer monetizationSharedCustomer;

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        subWorkFlowDTO = (SubscriptionWorkflowDTO) workflowDTO;

        Stripe.apiKey = getPlatformAccountStripeKey(subWorkFlowDTO.getTenantId());
        RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(ConnectId).build();

        try {
            subscriber = apiMgtDAO.getSubscriber(subWorkFlowDTO.getSubscriber());
            // check whether the application is already registered as a customer under the particular
            // APIprovider/Connected Account in Stripe
            monetizationSharedCustomer = apiMgtDAO.getSharedCustomer(subWorkFlowDTO.getApplicationId(),
                        subWorkFlowDTO.getApiProvider(),subWorkFlowDTO.getTenantId());
            if(monetizationSharedCustomer.getSharedCustomerId() == null) {
                // checks whether the subscriber is already registered as a customer Under the
                // tenant/Platform account in Stripe
                monetizationPlatformCustomer = apiMgtDAO.getPlatformCustomer(subscriber.getId(),
                        subscriber.getTenantId());
                if (monetizationPlatformCustomer.getCustomerId() == null) {
                    //throw new WorkflowException("Subscriber is not registered as a customer");
                    monetizationPlatformCustomer = createMonetizationPlatformCutomer(subscriber);
                }
                monetizationSharedCustomer = createSharedCustomer(subscriber.getEmail(), monetizationPlatformCustomer,
                        requestOptions, subWorkFlowDTO);
            }
            //creating Subscriptions
            //TODO call the api to get the plan ID
            String planId = "plan_Euc6iAjA3Pevr2";
            createMonetizedSubscriptions(planId, monetizationSharedCustomer, requestOptions, subWorkFlowDTO);
        }catch (APIManagementException e) {
            throw new WorkflowException("Could not complete subscription creation workflow", e);
        }
        return execute(workflowDTO);
    }

    /**
     * Returns the stripe key of the platform/tenant
     * @param tenantId id of the tenant
     * @return the stripe key of the platform/tenant
     * @throws WorkflowException
     */
    public String getPlatformAccountStripeKey(int tenantId) throws WorkflowException{
        String stripePlatformAccountKey = null;
        try{
            Registry configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry(
                    tenantId);
            if (configRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                Resource resource = configRegistry.get(APIConstants.API_TENANT_CONF_LOCATION);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());

                if (StringUtils.isBlank(content)) {
                    String errorMessage = "Tenant configuration cannot be empty when configuring monetization.";
                    throw new WorkflowException(errorMessage);
                }
                //get the stripe key of patform account from tenant conf file
                JSONObject tenantConfig = (JSONObject) new JSONParser().parse(content);
                JSONObject monetizationInfo = (JSONObject) tenantConfig.get("MonetizationInfo");
                stripePlatformAccountKey = monetizationInfo.get("PlatformAccountStripeKey").toString();

                if (StringUtils.isBlank(stripePlatformAccountKey)) {
                    throw new WorkflowException("stripePlatformAccountKey is empty!!!");
                }
            }
        }catch (RegistryException ex) {
            throw new WorkflowException("Could not get all registry objects : ", ex);
        }catch (org.json.simple.parser.ParseException ex) {
            throw new WorkflowException("Could not get Stripe Platform key : ", ex);
        }
        return stripePlatformAccountKey;
    }

    /**
     * Returns the created org.wso2.carbon.apimgt.api.model.SharedCustomer
     * @param email Email of the subscriber
     * @param platformCustomer Monetization customer created under tenant account
     * @param requestOptions object of com.stripe.net.RequestOptions;
     * @param subWorkFlowDTO  object of org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO
     * @return it will return a object of .org.wso2.carbon.apimgt.api.model.SharedCustomer
     * @throws WorkflowException
     */
    public MonetizationSharedCustomer createSharedCustomer(String email, MonetizationPlatformCustomer platformCustomer,
                                                           RequestOptions requestOptions,
                                                           SubscriptionWorkflowDTO subWorkFlowDTO ) throws WorkflowException{
        Customer stripeCustomer;
        MonetizationSharedCustomer monetizationSharedCustomer = new MonetizationSharedCustomer();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Token token = new Token();
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("customer", platformCustomer.getCustomerId());
            token = Token.create(params, requestOptions);
        }catch (StripeException ex){
            String errorMsg = "Error when creating a stripe token for"+platformCustomer.getSubscriberName();
            log.error(errorMsg);
            throw new WorkflowException("Could not complete subscription creation workflow", ex);
        }

        Map<String, Object> sharedCustomerParams = new HashMap<>();
        if(!email.equals("")) {
            sharedCustomerParams.put(MonetizationConstants.StripeCustomer.email,email);
        }
        try {
            sharedCustomerParams.put(MonetizationConstants.StripeCustomer.description, "Shared Customer for "
                    + subWorkFlowDTO.getApplicationName() + " / " + subWorkFlowDTO.getSubscriber());
            sharedCustomerParams.put(MonetizationConstants.StripeCustomer.source, token.getId());
            stripeCustomer = Customer.create(sharedCustomerParams, requestOptions);
            try {
                monetizationSharedCustomer.setApplicationId(subWorkFlowDTO.getApplicationId());
                monetizationSharedCustomer.setApiProvider(subWorkFlowDTO.getApiProvider());
                monetizationSharedCustomer.setTenantId(subWorkFlowDTO.getTenantId());
                monetizationSharedCustomer.setSharedCustomerId(stripeCustomer.getId());
                monetizationSharedCustomer.setParentCustomerId(platformCustomer.getId());

                int id = apiMgtDAO.addMSSharedCustomer(monetizationSharedCustomer);
                monetizationSharedCustomer.setId(id);
            } catch (APIManagementException ex) {
                stripeCustomer.delete(requestOptions);
                String errorMsg = "Error when inserting stripe shared customer details of Application "
                        + subWorkFlowDTO.getApplicationName() + " Database";
                log.error(errorMsg, ex);
                throw new WorkflowException("Could not complete subscription creation workflow", ex);
            }
            if(log.isDebugEnabled()){
                String msg = "A Customer for application "+subWorkFlowDTO.getApplicationName()+ " is created under the "
                        + subWorkFlowDTO.getApiProvider() + "'s Connected Account in STRIPE";
                log.debug(msg);
            }
        }catch (StripeException ex)
        {
            String errorMsg = "Error while creating a shared customer in stripe for Application "
                    +subWorkFlowDTO.getApplicationName();
            log.error(errorMsg);
            throw new WorkflowException("Could not complete subscription creation workflow", ex);
        }
        return monetizationSharedCustomer;
    }

    /**
     * @param planId plan Id of the Stripe monetization plan
     * @param sharedCustomer object of org.wso2.carbon.apimgt.api.model.StripeSharedCustomer contains info about
     *                       the customer created in the provider account of Stripe
     * @param requestOptions object of com.stripe.net.RequestOptions contains providers credential needed for Stripe
     *                       transactions
     * @param subWorkFlowDTO  object of org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO
     * @throws WorkflowException
     */
    public void createMonetizedSubscriptions(String planId, MonetizationSharedCustomer sharedCustomer ,
                                          RequestOptions requestOptions, SubscriptionWorkflowDTO subWorkFlowDTO)
            throws WorkflowException{

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        APIIdentifier identifier = new APIIdentifier(subWorkFlowDTO.getApiProvider(), subWorkFlowDTO.getApiName(),
                subWorkFlowDTO.getApiVersion());
        Subscription subscription = new Subscription();

        try {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put(MonetizationConstants.plan, planId);
            Map<String, Object> items = new HashMap<String, Object>();
            items.put("0", item);
            Map<String, Object> subParams = new HashMap<String, Object>();
            subParams.put(MonetizationConstants.customer, sharedCustomer.getSharedCustomerId());
            subParams.put("items", items);
            List<String> expandList = new LinkedList<String>();
            expandList.add("latest_invoice.payment_intent");

            try {
                //create a subscritpion in stripe under the API Providers Connected Account
                subscription = Subscription.create(subParams, requestOptions);
            } catch (StripeException ex) {
                String errorMsg = "Error when adding a subscription in Stripe for Application " +
                        subWorkFlowDTO.getApplicationName();
                log.error(errorMsg);
                throw new WorkflowException("Could not complete subscription creation workflow", ex);
            }
            try {
                apiMgtDAO.addMSSubscription(identifier, subWorkFlowDTO.getApplicationId(),
                        subWorkFlowDTO.getTenantId(), sharedCustomer.getId(), subscription.getId());
            } catch (APIManagementException e) {
                //delete the subscription in Stripe, if the entry to database fails in API Manager
                subscription.cancel(null, requestOptions);
                String errorMsg = "Error when adding stripe subscription details of Application "
                        + subWorkFlowDTO.getApplicationName() + " to Database";
                log.error(errorMsg);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
            if(log.isDebugEnabled()){
                String msg = "Stripe Subscription for " +subWorkFlowDTO.getApplicationName()+ " is created for"
                        +subWorkFlowDTO.getApiName()+ " API";
                log.debug(msg);
            }
        }catch(StripeException ex)
        {
            throw new WorkflowException("Could not complete subscription creation workflow", ex);
        }
    }

    /**
     * @param subscriber object which contains info about the subscriber
     * @return StripeCustomer object which contains info about the customer created in platform account of stripe
     * @throws WorkflowException
     */
    public MonetizationPlatformCustomer createMonetizationPlatformCutomer(Subscriber subscriber)
            throws WorkflowException
    {
        MonetizationPlatformCustomer monetizationPlatformCustomer = new MonetizationPlatformCustomer();
        Customer customer = null;
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        try {
            Map<String, Object> customerParams = new HashMap<String, Object>();
            if (!subscriber.getEmail().equals("")) {
                customerParams.put(MonetizationConstants.StripeCustomer.email, subscriber.getEmail());
            }
            customerParams.put(MonetizationConstants.StripeCustomer.description, "Customer for "
                    + subscriber.getName());
            customerParams.put(MonetizationConstants.StripeCustomer.source, "tok_visa");
            customer = Customer.create(customerParams);
            monetizationPlatformCustomer.setCustomerId(customer.getId());
            try {
                int id = apiMgtDAO.addMonetizationPlatformCustomer(subscriber.getId(), subscriber.getTenantId(),
                         customer.getId());
                monetizationPlatformCustomer.setId(id);
            } catch (APIManagementException e) {
                if (customer != null) {
                    customer.delete();
                }
                String errorMsg = "Error when inserting stripe customer details of "+subscriber.getName()+" to Database";
                log.error(errorMsg);
                throw new WorkflowException("Could not complete Subscription work flow"+e.getLocalizedMessage());
            }
        } catch(StripeException ex)
        {
            String errorMsg = "Error while creating a customer in Stripe for "+subscriber.getName();
            log.error(errorMsg);
            throw new WorkflowException("Could not complete Subscription work flow due to Stripe Error : "+ex);
        }
        return  monetizationPlatformCustomer;
    }

    /**
     * This method completes subscription creation simple workflow and return workflow response back to the caller
     *
     * @param workflowDTO  The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response back to the caller
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                    APIConstants.SubscriptionStatus.UNBLOCKED);
        } catch (APIManagementException e) {
            log.error("Could not complete subscription creation workflow", e);
            throw new WorkflowException("Could not complete subscription creation workflow", e);
        }
        return new GeneralWorkflowResponse();
    }

}
