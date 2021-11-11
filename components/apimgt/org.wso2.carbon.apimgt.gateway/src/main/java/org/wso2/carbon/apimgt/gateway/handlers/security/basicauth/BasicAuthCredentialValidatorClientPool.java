/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

public class BasicAuthCredentialValidatorClientPool {

    private static final Log log = LogFactory.getLog(BasicAuthCredentialValidatorClientPool.class);

    private static final BasicAuthCredentialValidatorClientPool instance = new BasicAuthCredentialValidatorClientPool();

    private final ObjectPool clientPool;
    private static int maxIdle;

    private BasicAuthCredentialValidatorClientPool() {
        String maxIdleClients = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getFirstProperty
                ("APIKeyValidator.ConnectionPool.MaxIdle");
        String initIdleCapacity = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getFirstProperty
                ("APIKeyValidator.ConnectionPool.InitIdleCapacity");
        int initIdleCapSize;
        if (StringUtils.isNotEmpty(maxIdleClients)) {
            maxIdle = Integer.parseInt(maxIdleClients);
        } else {
            maxIdle = 50;
        }
        if (StringUtils.isNotEmpty(initIdleCapacity)) {
            initIdleCapSize = Integer.parseInt(initIdleCapacity);
        } else {
            initIdleCapSize = 20;
        }
        log.debug("Initializing basic auth credential validator client pool");
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                log.debug("Initializing new BasicAuthCredentialValidatorClient instance");
                return new BasicAuthCredentialValidatorClient();
            }
        }, maxIdle, initIdleCapSize);
    }

    public static BasicAuthCredentialValidatorClientPool getInstance() {
        return instance;
    }

    public BasicAuthCredentialValidatorClient get() throws Exception {
        if (log.isTraceEnabled()) {
            int active = clientPool.getNumActive();
            if (active >= maxIdle) {
                log.trace("Basic auth credential validator pool size is :" + active);
            }
        }
        return (BasicAuthCredentialValidatorClient) clientPool.borrowObject();
    }

    public void release(BasicAuthCredentialValidatorClient client) throws Exception {
        clientPool.returnObject(client);
    }

    public void cleanup() {
        try {
            clientPool.close();
        } catch (Exception e) {
            log.warn("Error while cleaning up the basic auth credential validator pool", e);
        }
    }

}
