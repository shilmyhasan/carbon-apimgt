/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;

public final class TrustStoreUtils {
    private static final Log log = LogFactory.getLog(TrustStoreUtils.class);
    private static int maximumBackOffTime = APIUtil.getMaximumBackOffTime();
    private static int maximumRetryCounts = APIUtil.getMaximumRetryCounts();
    private static int waitTimeBeforeLockRelease = APIUtil.getWaitTimeBeforeLockRelease();

    public static synchronized void loadCerts(KeyStore trustStore, String keyStorePath, char[] password )
            throws CertificateException, NoSuchAlgorithmException, IOException {
        FileInputStream localTrustStoreStream = new FileInputStream(keyStorePath);
        InputStream dest = IOUtils.toBufferedInputStream(localTrustStoreStream);
        localTrustStoreStream.close();
        trustStore.load(dest, password);
        dest.close();
    }

    public static synchronized boolean acquireLockWithRetries(String lockFilePath) throws InterruptedException {
        for (int attempt = 1; attempt <= maximumRetryCounts; attempt++) {
            try {
                // check if file exists
                Path path = Paths.get(lockFilePath);
                if (Files.exists(path)) {
                    // check the file created time
                    File file = new File(lockFilePath);
                    long currentTime = System.currentTimeMillis();
                    long fileCreatedTime = file.lastModified();
                    if (currentTime - fileCreatedTime > waitTimeBeforeLockRelease) {
                        Files.delete(path);
                    } else {
                        int backOff = generateRandomBackOff();
                        if (log.isDebugEnabled()){
                            log.debug("Attempt " + attempt + " failed. Retrying after " + backOff + "ms");
                        }
                        Thread.sleep(backOff);
                    }
                }
                Files.write(path, "locked".getBytes(), StandardOpenOption.CREATE_NEW);
                return true;
            } catch (IOException e) {
                int backOff = generateRandomBackOff();
                if (log.isDebugEnabled()) {
                    log.debug("Attempt " + attempt + " failed. Retrying after " + backOff + "ms");
                }
                Thread.sleep(backOff);
            }
        }
        return false;
    }

    private static int generateRandomBackOff() {
        return new Random().nextInt(maximumBackOffTime);
    }

    public static synchronized void releaseLock(String lockFilePath) {
        try {
            final Path path = Paths.get(lockFilePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
