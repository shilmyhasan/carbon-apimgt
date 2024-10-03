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

package org.wso2.carbon.apimgt.impl.template;

import com.google.gson.Gson;
import org.apache.velocity.VelocityContext;
import org.apache.commons.lang3.StringEscapeUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.util.Map;

/**
 * This is a utility class with a bunch of methods to help in Template
 */
public class TemplateUtilContext extends ConfigContextDecorator {

    public TemplateUtilContext(ConfigContext context) {
        super(context);
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context =  super.getContext();

        context.put("util",this);

        return context;
    }

    public String escapeXml(String url){
        return StringEscapeUtils.escapeXml(StringEscapeUtils.unescapeXml(url)).trim();
    }

    /**
     * This function converts a JSON string to a Map and this is used when rendering the templates.
     *
     * @param jsonString the JSON string to be converted
     * @return a Map representation of the JSON string
     */
    public Map jsonStringToMap(String jsonString) {
        return new Gson().fromJson(jsonString, Map.class);
    }

    /**
     * Decrypts the provided Base64 encoded ciphertext using the default cryptographic utility when rendering the
     * templates.
     *
     * @param cipherText the Base64 encoded ciphertext to be decrypted
     * @return the decrypted plaintext as a String
     * @throws CryptoException if an error occurs during decryption
     */
    public String decrypt(String cipherText) throws CryptoException {
        return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(cipherText));
    }
}
