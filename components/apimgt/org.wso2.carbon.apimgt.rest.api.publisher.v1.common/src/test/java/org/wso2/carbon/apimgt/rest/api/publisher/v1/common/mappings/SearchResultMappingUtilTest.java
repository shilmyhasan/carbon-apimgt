/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductSearchResultDTO;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SearchResultMappingUtil.class, APIUtil.class, StringUtils.class })
public class SearchResultMappingUtilTest {

    private static final String uuid = "uuid";
    private static final String name = "testName";
    private static final String version = "1.0";
    private static final String provider = "provider@domain.com";
    private static final String context = "/context/api/v1";
    private static final String contextTemplate = "/context/api/v1";
    private static final String transportType = "http";
    private static final String description = "sample description";
    private static final String status = "PUBLISHED";
    private static final String thumbnailUrl = "/resource/thumbnail.png";

    @Before
    public void init() {
        PowerMockito.mockStatic(APIUtil.class);
        // Call the static method under test using PowerMockito
        Mockito.when(APIUtil.replaceEmailDomainBack(provider)).thenReturn(provider);
    }

    //Test case for the conversation of API to APISearchResultDTO
    @Test
    public void testFromAPIToAPIResultDTO() {
        // Prepare test data
        API apiMock = Mockito.mock(API.class);
        Mockito.when(apiMock.getUuid()).thenReturn(uuid);
        APIIdentifier apiIdMock = Mockito.mock(APIIdentifier.class);
        Mockito.when(apiIdMock.getApiName()).thenReturn(name);
        Mockito.when(apiIdMock.getVersion()).thenReturn(version);
        Mockito.when(apiIdMock.getProviderName()).thenReturn(provider);
        Mockito.when(apiMock.getId()).thenReturn(apiIdMock);
        Mockito.when(apiMock.getContextTemplate()).thenReturn(contextTemplate);
        Mockito.when(apiMock.getType()).thenReturn(transportType);
        Mockito.when(apiMock.getDescription()).thenReturn(description);
        Mockito.when(apiMock.getStatus()).thenReturn(status);
        Mockito.when(apiMock.getThumbnailUrl()).thenReturn(thumbnailUrl);

        // Mock StringUtils.isBlank method
        PowerMockito.mockStatic(StringUtils.class);
        PowerMockito.when(StringUtils.isBlank(apiMock.getThumbnailUrl())).thenReturn(false);

        //Invoke the testing method
        APISearchResultDTO resultDTO = SearchResultMappingUtil.fromAPIToAPIResultDTO(apiMock);

        // Assert the results
        Assert.assertEquals(name, resultDTO.getName());
        Assert.assertEquals(version, resultDTO.getVersion());
        Assert.assertEquals(provider, resultDTO.getProvider());
        Assert.assertEquals(context, resultDTO.getContext());
        Assert.assertEquals(contextTemplate, resultDTO.getContextTemplate());
        Assert.assertEquals(SearchResultDTO.TypeEnum.API, resultDTO.getType());
        Assert.assertEquals(transportType, resultDTO.getTransportType());
        Assert.assertEquals(description, resultDTO.getDescription());
        Assert.assertEquals(status, resultDTO.getStatus());
        Assert.assertEquals(thumbnailUrl, resultDTO.getThumbnailUri());
        Assert.assertFalse(resultDTO.isAdvertiseOnly());
        Assert.assertTrue(resultDTO.isHasThumbnail());
    }

    ////Test case for the conversation of APIProduct to APIProductToAPIResultDTO
    @Test
    public void testFromAPIProductToAPIResultDTO() {
        // Mock the APIProduct object
        APIProduct apiProductMock = Mockito.mock(APIProduct.class);
        Mockito.when(apiProductMock.getUuid()).thenReturn("uuid");
        APIProductIdentifier apiProductIdMock = Mockito.mock(APIProductIdentifier.class);
        Mockito.when(apiProductMock.getId()).thenReturn(apiProductIdMock);
        Mockito.when(apiProductIdMock.getName()).thenReturn(name);
        Mockito.when(apiProductIdMock.getVersion()).thenReturn(version);
        Mockito.when(apiProductIdMock.getProviderName()).thenReturn(provider);
        Mockito.when(apiProductMock.getContextTemplate()).thenReturn(contextTemplate);
        Mockito.when(apiProductMock.getDescription()).thenReturn(description);
        Mockito.when(apiProductMock.getState()).thenReturn(status);
        Mockito.when(apiProductMock.getThumbnailUrl()).thenReturn(thumbnailUrl);

        //Invoke the testing method
        APIProductSearchResultDTO result = SearchResultMappingUtil.fromAPIProductToAPIResultDTO(apiProductMock);

        // Verify the result
        Assert.assertEquals(name, result.getName());
        Assert.assertEquals(version, result.getVersion());
        Assert.assertEquals(provider, result.getProvider());
        Assert.assertEquals(context, result.getContext());
        Assert.assertEquals(SearchResultDTO.TypeEnum.APIPRODUCT, result.getType());
        Assert.assertEquals(description, result.getDescription());
        Assert.assertEquals(status, result.getStatus());
        Assert.assertEquals(thumbnailUrl, result.getThumbnailUri());
        Assert.assertTrue(result.isHasThumbnail());
    }
}
