/*
 *
 *    Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import com.google.gson.Gson;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.andes.client.message.JMSTextMessage;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerDataService;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class GatewayJMSMessageListenerTest {

    private final GatewayJMSMessageListener gatewayJMSMessageListener = new GatewayJMSMessageListener();
    private ServiceReferenceHolder serviceReferenceHolder;

    @Before
    public void setup() {
        assertTrue(gatewayJMSMessageListener instanceof GatewayJMSMessageListener);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    @Test
    public void testSubscriptionPolicyUpdate() throws JMSException {
        String messageBody = "Body:\n" +
                "{\"event\":{\"payloadData\":{\"eventType\":\"POLICY_UPDATE\",\"timestamp\":1670477868131," +
                "\"event\":\"eyJwb2xpY3lJZCI6NSwicG9saWN5TmFtZSI6IlVubGltaXRlZCIsInF1b3RhVHlwZSI6InJlcXVlc3RDb3" +
                "VudCIsInN1YnNjcmliZXJDb3VudCI6MCwicmF0ZUxpbWl0Q291bnQiOjAsInJhdGVMaW1pdFRpbWVVbml0Ijoic2VjIiwic3R" +
                "vcE9uUXVvdGFSZWFjaCI6dHJ1ZSwiZ3JhcGhRTE1heERlcHRoIjowLCJncmFwaFFMTWF4Q29tcGxleGl0eSI6MCwicG9saWN5VHl" +
                "wZSI6IlNVQlNDUklQVElPTiIsImV2ZW50SWQiOiI4NTZjZGMzZC04NDg1LTRjMjAtODZhNC03Mzg1MGQ2NzFlMDYiLCJ0aW1lU3R" +
                "hbXAiOjE2NzA0Nzc4NjgxMzEsInR5cGUiOiJQT0xJQ1lfVVBEQVRFIiwidGVuYW50SWQiOi0xMjM0LCJ0ZW5hbnREb21haW4iOiJ" +
                "jYXJib24uc3VwZXIifQ==\"}}}\n" +
                "JMS Correlation ID: null\n" +
                "JMS timestamp: 1670477868266\n" +
                "JMS expiration: 0\n" +
                "JMS priority: 4\n" +
                "JMS delivery mode: 2\n" +
                "JMS reply to: null\n" +
                "JMS Redelivered: false\n" +
                "JMS Destination: topic://amq.topic/notification/?routingkey=" +
                "'notification'&exclusive='true'&autodelete='true'\n" +
                "JMS Type: null\n" +
                "JMS MessageID: ID:9cf76f89-45a8-3e8d-af58-286a2e4dcc72\n" +
                "JMS Content-Type: text/plain\n" +
                "AMQ message number: 1\n" +
                "Properties:\n" +
                "\tJMS_QPID_DESTTYPE = 2\n";
        String eventJson = "{\"policyId\":5,\"policyName\":\"Unlimited\",\"quotaType\":\"requestCount\"," +
                "\"subscriberCount\":0,\"rateLimitCount\":0,\"rateLimitTimeUnit\":\"sec\",\"stopOnQuotaReach\":true," +
                "\"graphQLMaxDepth\":0,\"graphQLMaxComplexity\":0,\"policyType\":\"SUBSCRIPTION\"," +
                "\"eventId\":\"856cdc3d-8485-4c20-86a4-73850d671e06\",\"timeStamp\":1670477868131," +
                "\"type\":\"POLICY_UPDATE\",\"tenantId\":-1234,\"tenantDomain\":\"carbon.super\"}";
        TextMessage textMessage = Mockito.mock(JMSTextMessage.class);
        PowerMockito.mockStatic(JMSTextMessage.class);
        Mockito.doNothing().when(textMessage).setText(messageBody);
        gatewayJMSMessageListener.onMessage(textMessage);
        SubscriptionPolicyEvent policyEvent = new Gson().fromJson(eventJson, SubscriptionPolicyEvent.class);
        KeyManagerDataService keyManagerDataService = new KeyManagerDataServiceImplWrapper();
        PowerMockito.when(serviceReferenceHolder.getKeyManagerDataService()).thenReturn(keyManagerDataService);
        Mockito.doNothing().when(keyManagerDataService).addOrUpdateSubscriptionPolicy(policyEvent);
        assertTrue(((KeyManagerDataServiceImplWrapper) keyManagerDataService).isSubscriptionPolicyUpdated());
    }

}