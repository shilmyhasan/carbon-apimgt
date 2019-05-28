/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import Loadable from 'react-loadable';
import { Login, Logout } from './app/components';
import SignUp from './app/components/AnonymousView/SignUp';
import PrivacyPolicy from './app/components/Policy/PrivacyPolicy';
import CookiePolicy from './app/components/Policy/CookiePolicy';
import Progress from './app/components/Shared/Progress';

const LoadableProtectedApp = Loadable({
    loader: () => import(// eslint-disable-line function-paren-newline
        /* webpackChunkName: "ProtectedApp" */
        /* webpackPrefetch: true */
        './app/ProtectedApp',
    ),
    loading: Progress,
});


/**
 *Root Store component
 *
 * @class Store
 * @extends {React.Component}
 */
class Store extends React.Component {
    /**
     *Creates an instance of Store.
     * @param {*} props Properties passed from the parent component
     * @memberof Store
     */
    constructor(props) {
        super(props);
        LoadableProtectedApp.preload();
    }

    /**
     *Reners the Store component
     *
     * @returns {JSX} this is the description
     * @memberof Store
     */
    render() {
        return (
            <Router basename='/store-new'>
                <Switch>
                    <Route path={"/login"} render={() => <Login appName={"store-new"} appLabel={"STORE"}/>}/>
                    <Route path='/logout' component={Logout} />
                    <Route path='/sign-up' component={SignUp} />
                    <Route path='/policy/privacy-policy' component={PrivacyPolicy} />
                    <Route path='/policy/cookie-policy' component={CookiePolicy} />
                    <Route component={LoadableProtectedApp} />
                </Switch>
            </Router>
        );
    }
}

export default Store;
