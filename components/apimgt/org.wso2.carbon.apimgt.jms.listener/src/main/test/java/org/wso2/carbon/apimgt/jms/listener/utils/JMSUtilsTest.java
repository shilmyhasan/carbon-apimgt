package org.wso2.carbon.apimgt.jms.listener.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Hashtable;

public class JMSUtilsTest {

    @Test
    public void testMaskAxis2ConfigSensitiveParameters() {
        Hashtable<String, String> sensitiveParamsTable = new Hashtable<String, String>();
        sensitiveParamsTable.put("connectionfactory.TopicConnectionFactory",
                "amqp://admin:admin@clientid/carbon?brokerlist='tcp://localhost:5672'");

        Hashtable<String, String> maskedParamTable = JMSUtils.maskAxis2ConfigSensitiveParameters(sensitiveParamsTable);
        Assert.assertEquals("amqp://***:***@clientid/carbon?brokerlist='tcp://localhost:5672'",
                maskedParamTable.get("connectionfactory.TopicConnectionFactory"));

        sensitiveParamsTable = new Hashtable<>();
        sensitiveParamsTable.put("connectionfactory.TopicConnectionFactory",
                "amqp://admin:%23admin@clientid/carbon?brokerlist='tcp://localhost:5672'");
        maskedParamTable = JMSUtils.maskAxis2ConfigSensitiveParameters(sensitiveParamsTable);
        Assert.assertEquals("amqp://***:***@clientid/carbon?brokerlist='tcp://localhost:5672'",
                maskedParamTable.get("connectionfactory.TopicConnectionFactory"));
    }
}