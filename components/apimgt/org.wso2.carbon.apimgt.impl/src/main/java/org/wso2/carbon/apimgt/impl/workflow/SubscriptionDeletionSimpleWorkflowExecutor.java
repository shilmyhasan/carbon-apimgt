/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pdfbox.pdmodel.graphics.predictor.Sub;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.StripeSubscription;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple workflow executor for subscription delete action
 */
public class SubscriptionDeletionSimpleWorkflowExecutor extends WorkflowExecutor {
    private static final Log log = LogFactory.getLog(SubscriptionDeletionSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {

        // implemetation is not provided in this version
        return null;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workflowDTO);
        super.publishEvents(workflowDTO);
        return new GeneralWorkflowResponse();
    }

    public  WorkflowResponse deleteMonetizedSubscription(WorkflowDTO workflowDTO) throws WorkflowException {

        SubscriptionWorkflowDTO subWorkflowDTO;
        StripeSubscription stripeSubscription;
        Stripe.apiKey = "sk_test_1Y8cd8EgnY1KYtBcs1vObHUF00020Je2H4";
        String ConnectId="acct_1EQF7PCxKhMnrBL5";
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        subWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;

        RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(ConnectId).build();
        try {
            stripeSubscription = apiMgtDAO.getStripeSubscription(subWorkflowDTO.getApiName(),
                    subWorkflowDTO.getApiVersion(), subWorkflowDTO.getApiProvider(), subWorkflowDTO.getApplicationId(),
                    subWorkflowDTO.getTenantDomain());
        }catch (APIManagementException ex){
            throw new WorkflowException(""+ex);
        }

        if(stripeSubscription.getSubscriptionId() != null){
            try {
                Subscription subscription = Subscription.retrieve(stripeSubscription.getSubscriptionId(), requestOptions);
                Map<String, Object> params = new HashMap<>();
                params.put("invoice_now", true);
                subscription = subscription.cancel(params,requestOptions);
                if(subscription.getStatus().equals("canceled")) {
                    apiMgtDAO.removeStripeSubscription(stripeSubscription.getId());
                }
            }catch (StripeException ex)
            {
                log.error("Stripe Error : "+ ex.getMessage());
                throw new WorkflowException("Could not complete subscription deletion workflow for "
                        + subWorkflowDTO.getApiName(), ex);
            } catch (APIManagementException ex){
                throw new WorkflowException("Could not complete subscription deletion workflow for "
                        + subWorkflowDTO.getApiName(), ex);
            }
        }

        return  new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        SubscriptionWorkflowDTO subWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String errorMsg = null;

        try {
            APIIdentifier identifier = new APIIdentifier(subWorkflowDTO.getApiProvider(),
                    subWorkflowDTO.getApiName(), subWorkflowDTO.getApiVersion());

            apiMgtDAO.removeSubscription(identifier, ((SubscriptionWorkflowDTO) workflowDTO).getApplicationId());
        } catch (APIManagementException e) {
            errorMsg = "Could not complete subscription deletion workflow for api: " + subWorkflowDTO.getApiName();
            throw new WorkflowException(errorMsg, e);
        }
        return new GeneralWorkflowResponse();
    }
}
