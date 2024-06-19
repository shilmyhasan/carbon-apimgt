package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
//    private static final int MAX_RETRY_COUNT = 100;
//    private static final int MAX_BACKOFF = 1000;
//    private static final int WAIT_TIME_BEFORE_LOCK_RELEASE = 10000;

    public static synchronized void loadCerts(KeyStore trustStore, String keyStorePath, char[] password )
            throws CertificateException, NoSuchAlgorithmException, IOException {
        FileInputStream localTrustStoreStream = new FileInputStream(keyStorePath);
        InputStream dest = IOUtils.toBufferedInputStream(localTrustStoreStream);
        localTrustStoreStream.close();
        trustStore.load(dest, password);
        dest.close();
    }

    public static synchronized boolean acquireLockWithRetries(String lockFilePath) throws InterruptedException {
        int MAX_RETRY_COUNT = System.getProperty("maxRetryCount") != null ?
                Integer.parseInt(System.getProperty("maxRetryCount")) : 100;
        int WAIT_TIME_BEFORE_LOCK_RELEASE = System.getProperty("waitTimeBeforeLockRelease") != null ?
                Integer.parseInt(System.getProperty("waitTimeBeforeLockRelease")) : 10000;
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                // check if file exists
                Path path = Paths.get(lockFilePath);
                if (Files.exists(path)) {
                    // check the file created time
                    File file = new File(lockFilePath);
                    long currentTime = System.currentTimeMillis();
                    long fileCreatedTime = file.lastModified();
                    if (currentTime - fileCreatedTime > WAIT_TIME_BEFORE_LOCK_RELEASE) {
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
        int MAX_BACKOFF = System.getProperty("maxBackoff") != null ?
                Integer.parseInt(System.getProperty("maxBackoff")) : 2000;
        return new Random().nextInt(MAX_BACKOFF);
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
