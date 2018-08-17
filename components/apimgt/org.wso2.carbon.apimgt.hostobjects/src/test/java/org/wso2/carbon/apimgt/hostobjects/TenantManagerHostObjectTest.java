/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hostobjects;

import org.jaggeryjs.hostobjects.file.FileHostObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TenantManagerHostObject.class})
public class TenantManagerHostObjectTest {
    private TenantManagerHostObject tmhostObject = new TenantManagerHostObject();

    @Test
    public void testGetStoreTenantThemesPath() throws Exception {
        Assert.assertEquals(TenantManagerHostObject.getStoreTenantThemesPath(), "repository/deployment/server/" +
                "jaggeryapps/store/site/tenant_themes/");
    }

    @Test
    public void testGetClassName() throws Exception {
        Assert.assertEquals(tmhostObject.getClassName(), "APIManager");
    }

    @Test
    public void testJsFunction_addTenantTheme() throws Exception {
        FileHostObject fileHostObject = Mockito.mock(FileHostObject.class);
        FileInputStream inputStream = Mockito.mock(FileInputStream.class);
        ZipInputStream zipInputStream = Mockito.mock(ZipInputStream.class);
        Object args[] = {fileHostObject, "b"};
        Mockito.when(fileHostObject.getInputStream()).thenReturn(inputStream);
        PowerMockito.whenNew(ZipInputStream.class).withAnyArguments().thenReturn(zipInputStream);

        Assert.assertTrue(tmhostObject.jsFunction_addTenantTheme(null, null, args, null));
    }

    @Test
    public void testJsFunction_addTenantThemeForClassCasting() throws Exception {
        Object args[] = {"test", "b"};
        try {
            tmhostObject.jsFunction_addTenantTheme(null, null, args, null);
            Assert.fail("APIManagementException exception not thrown for the error scenario");
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid input parameters for addTenantTheme"));
        }
    }

    @Test
    public void testJsFunction_addTenantThemeWhenArgsAreNull() throws Exception {
        try {
            tmhostObject.jsFunction_addTenantTheme(null, null, null, null);
            Assert.fail("APIManagementException exception not thrown for the error scenario");
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid input parameters for addTenantTheme"));
        }
    }

    @Test
    public void testJsFunction_addTenantThemeWhenArgsNotNull() throws Exception {
        Object args[] = {"test"};
        try {
            tmhostObject.jsFunction_addTenantTheme(null, null, args, null);
            Assert.fail("APIManagementException exception not thrown for the error scenario");
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid input parameters for addTenantTheme"));
        }
    }
}