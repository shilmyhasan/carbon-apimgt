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


import axios from 'axios';
import qs from 'qs';
import Utils from './Utils';
import User from './User';
import APIClientFactory from './APIClientFactory';


/**
 * Manage the application authentication and authorization requirements.
 *
 * @class AuthManager
 */
class AuthManager {
    constructor() {
        this.isLogged = false;
        this.username = null;
    }

    static refreshTokenOnExpire() {
        const timestampSkew = 100;
        const currentTimestamp = Math.floor(Date.now() / 1000);
        const tokenTimestamp = localStorage.getItem('expiresIn');
        const rememberMe = localStorage.getItem('rememberMe') === 'true';
        if (rememberMe && tokenTimestamp - currentTimestamp < timestampSkew) {
            const bearerToken = 'Bearer ' + Utils.getCookie('WSO2_AM_REFRESH_TOKEN_1');
            const loginPromise = authManager.refresh(bearerToken);
            loginPromise.then((data, status, xhr) => {
                authManager.setUser(true);
                const expiresIn = data.validityPeriod + Math.floor(Date.now() / 1000);
                window.localStorage.setItem('expiresIn', expiresIn);
            });
            loginPromise.error((error) => {
                const error_data = JSON.parse(error.responseText);
                const message = 'Error while refreshing token' + '<br/> You will be redirect to the login page ...';
                noty({
                    text: message,
                    type: 'error',
                    dismissQueue: true,
                    modal: true,
                    progressBar: true,
                    timeout: 5000,
                    layout: 'top',
                    theme: 'relax',
                    maxVisible: 10,
                    callback: {
                        afterClose() {
                            window.location = loginPageUri;
                        },
                    },
                });
            });
        }
    }

    /**
     * Static method to handle unauthorized user action error catch, It will look for response status code and skip !401 errors
     * @param {object} error_response
     */
    static unauthorizedErrorHandler(error_response) {
        if (error_response.status !== 401) {
            /* Skip unrelated response code to handle in unauthorizedErrorHandler */
            throw error_response;
            /* re throwing the error since we don't handle it here and propagate to downstream error handlers in catch chain */
        }
        const message = 'The session has expired' + '.<br/> You will be redirect to the login page ...';
        if (typeof noty !== 'undefined') {
            noty({
                text: message,
                type: 'error',
                dismissQueue: true,
                modal: true,
                progressBar: true,
                timeout: 5000,
                layout: 'top',
                theme: 'relax',
                maxVisible: 10,
                callback: {
                    afterClose() {
                        window.location = loginPageUri;
                    },
                },
            });
        } else {
            throw error_response;
        }
    }

    /**
     * An user object is return in present of user logged in user info in browser local storage, at the same time checks for partialToken in the cookie as well.
     * This may give a partial indication(passive check not actually check the token validity via an API) of whether the user has logged in or not, The actual API call may get denied
     * if the cookie stored access token is invalid/expired
     * @param {string} environmentName: label of the environment, the user to be retrieved from
     * @returns {User | null} Is any user has logged in or not
     */
    static getUser(environmentName = Utils.getCurrentEnvironment().label) {
        const userData = localStorage.getItem(`${User.CONST.LOCALSTORAGE_USER}_${environmentName}`);
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1, environmentName);
        if (!(userData && partialToken)) {
            return null;
        }

        return User.fromJson(JSON.parse(userData), environmentName);
    }

    /**
     * Do token introspection and Get the currently logged in user's details
     * When user authentication happens via redirection flow, This method might get used to
     * retrieve the user information
     * after setting the access token parts in cookies, Because access token parts are stored in /publisher-new path ,
     * just making an external request in same path will submit both cookies, allowing the service to build the
     * complete access token and do the introspection.
     * Return a promise resolving to user object iff introspect calls return active user else null
     * @static
     * @returns {Promise} fetch response promise resolving to introspect response JSON or null otherwise
     * @memberof AuthManager
     */
    static getUserFromToken() {
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1);
        if (!partialToken) {
            return new Promise((resolve, reject) => reject(new Error('No partial token found')));
        }
        const promisedResponse = fetch('/store-new/services/auth/introspect', { credentials: 'same-origin' });
        return promisedResponse
            .then(response => response.json())
            .then((data) => {
                let user = null;
                if (data.active) {
                    const currentEnv = Utils.getCurrentEnvironment();
                    user = new User(currentEnv.label, data.username);
                    user.scopes = data.scope.split(' ');
                    AuthManager.setUser(user, currentEnv.label);
                } else {
                    console.warn('User with ' + partialToken + ' is not active!');
                }
                return user;
            });
    }

    /**
     * Persist an user in browser local storage and in-memory, Since only one use can be logged into the application at a time,
     * This method will override any previously persist user data.
     * @param {User} user : An instance of the {User} class
     * @param {string} environmentName: label of the environment to be set the user
     */
    static setUser(user, environmentName) {
        environmentName = environmentName || Utils.getEnvironment().label;
        if (!(user instanceof User)) {
            throw new Error('Invalid user object');
        }

        if (user) {
            localStorage.setItem(`${User.CONST.LOCALSTORAGE_USER}_${environmentName}`, JSON.stringify(user.toJson()));
        }
    }

    /**
     * By given username and password Authenticate the user, Since this REST API has no swagger definition,
     * Can't use swaggerjs to generate client.Hence using Axios to make AJAX calls
     * @param {String} username : Username of the user
     * @param {String} password : Plain text password
     * @param {Object} environment : environment object
     * @returns {AxiosPromise} : Promise object with the login request made
     */
    authenticateUser(username, password, environment) {
        const headers = {
            Authorization: 'Basic deidwe',
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        const data = {
            username,
            password,
            grant_type: 'password',
            validity_period: 3600,
            scopes: 'apim:subscribe apim:signup apim:workflow_approve',
        };
        const promised_response = axios(Utils.getLoginTokenPath(environment), {
            method: 'POST',
            data: qs.stringify(data),
            headers,
            withCredentials: true,
        });
        // Set the environment that user tried to authenticate
        const previous_environment = Utils.getEnvironment();
        Utils.setEnvironment(environment);

        promised_response
            .then((response) => {
                const validityPeriod = response.data.validityPeriod; // In seconds
                const WSO2_AM_TOKEN_1 = response.data.partialToken;
                const user = new User(Utils.getEnvironment().label, response.data.authUser, response.data.idToken);
                user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, Utils.CONST.CONTEXT_PATH);
                user.scopes = response.data.scopes.split(' ');
                AuthManager.setUser(user);
            })
            .catch((error) => {
                console.error('Authentication Error:\n', error);
                Utils.setEnvironment(previous_environment);
            });
        return promised_response;
    }

    /**
     * Revoke the issued OAuth access token for currently logged in user and clear both cookie and localstorage data.
     */
    logout() {
        const authHeader = 'Bearer ' + AuthManager.getUser().getPartialToken();
        // TODO Will have to change the logout end point url to contain the app context(i.e. publisher/store-new, etc.)
        const url = Utils.getAppLogoutURL();
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: authHeader,
        };
        const promisedLogout = axios.post(url, null, { headers });
        return promisedLogout.then((response) => {
            Utils.delete_cookie(User.CONST.WSO2_AM_TOKEN_1, Utils.CONST.CONTEXT_PATH);
            localStorage.removeItem(User.CONST.LOCALSTORAGE_USER);
            new APIClientFactory().destroyAPIClient(Utils.getEnvironment().label); // Single client should be re initialize after log out
        });
    }

    refresh(authzHeader) {
        const params = {
            grant_type: 'refresh_token',
            validity_period: '3600',
            scopes: 'apim:subscribe apim:signup apim:workflow_approve',
        };
        const referrer = document.referrer.indexOf('https') !== -1 ? document.referrer : null;
        const url = Utils.CONST.CONTEXT_PATH + '/auth/apis/login/token';
        /* TODO: Fetch this from configs ~tmkb */
        const headers = {
            Authorization: authzHeader,
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer,
        };
        return axios.post(url, qs.stringify(params), { headers });
    }

    /**
     * Register anonymous user by generating token using client_credentials grant type
     * @param {Object} environment : environment object
     * @returns {AxiosPromise} : Promise object with the request made
     */
    registerUser(environment) {
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        const data = {
            grant_type: 'client_credentials',
            validity_period: 3600,
            scopes: 'apim:self-signup',
        };
        const promised_response = axios(Utils.getSignUpTokenPath(environment), {
            method: 'POST',
            data: qs.stringify(data),
            headers,
            withCredentials: false,
        });

        promised_response
            .then((response) => {
                const validityPeriod = response.data.validityPeriod;
                const WSO2_AM_TOKEN_1 = response.data.partialToken;
                const user = new User(Utils.getEnvironment().label, response.data.authUser, response.data.idToken);
                user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, Utils.CONST.CONTEXT_PATH);
                user.scopes = response.data.scopes;
                AuthManager.setUser(user);
            })
            .catch((error) => {
                console.error('Authentication Error: ', error);
            });
        return promised_response;
    }
}

export default AuthManager;
