/*
 * Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.common.gateway.util;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

/**
 * Util class for certificate related utilities.
 */
public class CertUtils {

    /**
     * Converts a javax.security.cert.X509Certificate array to java.security.cert.Certificate array.
     *
     * @param x509Certificates certificate array to be converted
     * @return a certificate array of java.security.cert type
     * @throws CertificateException thrown if an error occurs when converting
     * @throws CertificateEncodingException thrown if an error occurs when converting
     */
    public static Certificate[] convertCerts(X509Certificate[] x509Certificates)
            throws CertificateException, CertificateEncodingException {

        List<java.security.cert.X509Certificate> x509List = new ArrayList<>();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        for (javax.security.cert.X509Certificate cert : x509Certificates) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cert.getEncoded());
            java.security.cert.X509Certificate x509Certificate
                    = (java.security.cert.X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
            if (x509Certificate != null) {
                x509List.add(x509Certificate);
            }
        }
        return x509List.toArray(new java.security.cert.X509Certificate[]{});
    }
}
