/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import { Switch, Route } from 'react-router-dom';
import PageContainer from 'AppComponents/Base/container/';
import { PageNotFound } from 'AppComponents/Base/Errors';

import EndpointsListing from './Listing';
import EndpointDetails from './Details';
import EndpointCreate from './Create';
import EndpointsDiscover from './Discover';
import EndpointsTopMenu from './components/EndpointsTopMenu';
import EndpointsNavigation from './EndpointsNavigation';

const Endpoints = () => {
    return (
        <PageContainer pageTopMenu={<EndpointsTopMenu />} pageNav={<EndpointsNavigation />}>
            <Switch>
                <Route exact path='/endpoints' component={EndpointsListing} />
                <Route path='/endpoints/create' component={EndpointCreate} />
                <Route path='/endpoints/discover' component={EndpointsDiscover} />
                <Route path='/endpoints/:endpointUUID/' component={EndpointDetails} />
                <Route component={PageNotFound} />
            </Switch>
        </PageContainer>
    );
};

export default Endpoints;
