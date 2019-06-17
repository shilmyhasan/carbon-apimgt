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
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import Paper from '@material-ui/core/Paper';
import InputAdornment from '@material-ui/core/InputAdornment';
import User from '@material-ui/icons/AccountCircle';
import Lock from '@material-ui/icons/Lock';
import Person from '@material-ui/icons/Person';
import Mail from '@material-ui/icons/Mail';
import { Link } from 'react-router-dom';
import Checkbox from '@material-ui/core/Checkbox';
import AuthManager from '../../data/AuthManager';
import Utils from '../../data/Utils';
import ConfigManager from '../../data/ConfigManager';
import LoadingAnimation from '../Base/Loading/Loading';
import API from '../../data/api';
import Alert from '../Shared/Alert';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    buttonsWrapper: {
        marginTop: theme.spacing.unit,
        marginLeft: theme.spacing.unit,
    },
    buttonAlignment: {
        marginLeft: theme.spacing.unit * 3,
    },
    linkDisplay: {
        textDecoration: 'none',
    },
    gridAlignment: {
        height: theme.spacing.unit * 150,
    },
});
/**
 *
 *
 * @class SignUp
 * @extends {React.Component}
 */
class SignUp extends React.Component {
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            environments: [],
            environmentId: 0,
            username: '',
            password: '',
            firstName: '',
            lastName: '',
            email: '',
            error: false,
            errorMessage: '',
            validation: false,
            validationError: '',
            policy: false,
            privacyPolicyUrl: '',
            cookiePolicyUrl: '',
            isExternal: false,
        };
    }

    /**
     *
     *
     * @memberof SignUp
     */
    componentDidMount() {
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const environments = response.data.environments;
                let environmentId = Utils.getEnvironmentID(environments);
                if (environmentId === -1) {
                    environmentId = 0;
                }
                this.setState({
                    environments,
                    environmentId,
                });
                const environment = environments[environmentId];
                Utils.setEnvironment(environment);
            })
            .catch(() => {
                console.error('Error while receiving environment configurations');
            });
        ConfigManager.getConfigs()
            .policyRoutes.then((response) => {
                this.setState({
                    privacyPolicyUrl: response.data.privacyPolicyUrl,
                    cookiePolicyUrl: response.data.cookiePolicyUrl,
                });
                if (this.state.privacyPolicyUrl !== '/policy/privacy-policy' || this.state.cookiePolicyUrl !== '/policy/cookie-policy') {
                    this.setState({ isExternal: true });
                }
            })
            .catch(() => {
                console.error('Error while receiving policy routes configurations');
            });
    }

    /**
     *
     *
     * @memberof SignUp
     */
    handleClick = () => {
        this.handleAuthentication()
            .then(() => this.handleSignUp())
            .catch(() => console.log('Error occurred during authentication'));
    };

    /**
     *
     *
     * @memberof SignUp
     */
    handleSignUp = () => {
        const {
            username, password, firstName, lastName, email, error,
        } = this.state;
        if (!username || !password || !firstName || !lastName || !email || error) {
            if (error) {
                Alert.warning('Please re-check password');
            } else {
                Alert.warning('Please fill all required fields');
            }
        } else {
            const user_data = {
                username: username.toLowerCase(),
                password,
                firstName,
                lastName,
                email,
            };
            const api = new API();
            const promise = api.createUser(user_data);
            promise
                .then(() => {
                    console.log('User created successfully.');
                    this.authManager.logout();
                    Alert.info('User added successfully. You can now sign into the API store.');
                    const redirect_url = '/login';
                    this.props.history.push(redirect_url);
                })
                .catch(() => {
                    console.log('Error while creating user');
                });
        }
    };

    /**
     *
     *
     * @memberof SignUp
     */
    handleAuthentication = () => {
        const { environments, environmentId } = this.state;
        return this.authManager.registerUser(environments[environmentId]);
    };

    /**
     *
     *
     * @memberof SignUp
     */
    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    /**
     *
     *
     * @memberof SignUp
     */
    handlePasswordChange = () => (event) => {
        if (event.target.value !== this.state.password) {
            this.setState({
                error: true,
                errorMessage: 'Password does not match',
            });
        } else {
            this.setState({
                error: false,
                errorMessage: '',
            });
        }
    };

    /**
     *
     *
     * @memberof SignUp
     */
    handlePasswordValidation = name => (event) => {
        const password = event.target.value;
        const regex = new RegExp('^(?=.*[A-Z])(?=.*[0-9])(?=.{8,})');
        if (!regex.test(password)) {
            this.setState({
                validation: true,
                validationError: 'Password must contain minimum 8 characters, at least one upper case letter and one number',
            });
        } else {
            this.setState({
                validation: false,
                validationError: '',
                [name]: event.target.value,
            });
        }
    };

    /**
     *
     *
     * @memberof SignUp
     */
    handlePolicyChange = (event) => {
        if (event.target.checked) {
            this.setState({ policy: true });
        } else {
            this.setState({ policy: false });
        }
    };

    /**
     *
     *
     * @returns
     * @memberof SignUp
     */
    render() {
        const { classes } = this.props;
        const {
            environments, error, errorMessage, policy, environmentId, validation, validationError, privacyPolicyUrl, cookiePolicyUrl, isExternal,
        } = this.state;
        if (!environments[environmentId]) {
            return <LoadingAnimation />;
        }
        return (
            <div className='login-flex-container'>
                <Grid container justify='center' alignItems='center' spacing={0} className={classes.gridAlignment}>
                    <Grid item lg={6} md={8} xs={10}>
                        <Grid container>
                            <Grid item sm={3} xs={12}>
                                <Grid container direction='column'>
                                    <Grid item>
                                        <img className='brand' src='/store-new/site/public/images/logo.svg' alt='wso2-logo' />
                                    </Grid>
                                    <Grid item>
                                        <Typography type='subheading' align='right' gutterBottom>
                                            API STORE
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Grid>

                            {/* Sign-up Form */}
                            <Grid item sm={9} xs={12}>
                                <div className='login-main-content'>
                                    <Paper elevation={1} square className='login-paper'>
                                        <form className='login-form'>
                                            <Typography type='body1' gutterBottom>
                                                Create your account
                                            </Typography>
                                            <span>
                                                <FormControl>
                                                    <TextField
                                                        required
                                                        id='username'
                                                        label='Username'
                                                        type='text'
                                                        autoComplete='username'
                                                        margin='normal'
                                                        onChange={this.handleChange('username')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position='start'>
                                                                    <User />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        id='password'
                                                        label='Password'
                                                        type='password'
                                                        autoComplete='current-password'
                                                        margin='normal'
                                                        onChange={this.handlePasswordValidation('password')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position='start'>
                                                                    <Lock />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                        error={validation}
                                                        helperText={validationError}
                                                    />
                                                    <TextField
                                                        required
                                                        error={error}
                                                        id='rePassword'
                                                        label='Re-type Password'
                                                        type='password'
                                                        autoComplete='current-password'
                                                        margin='normal'
                                                        onChange={this.handlePasswordChange('rePassword')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position='start'>
                                                                    <Lock />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                        helperText={errorMessage}
                                                    />
                                                    <TextField
                                                        required
                                                        id='firstName'
                                                        label='First Name'
                                                        type='text'
                                                        margin='normal'
                                                        onChange={this.handleChange('firstName')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position='start'>
                                                                    <Person />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        id='lastName'
                                                        label='Last Name'
                                                        type='text'
                                                        margin='normal'
                                                        onChange={this.handleChange('lastName')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position='start'>
                                                                    <Person />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <TextField
                                                        required
                                                        id='email'
                                                        label='E mail'
                                                        type='email'
                                                        margin='normal'
                                                        onChange={this.handleChange('email')}
                                                        InputProps={{
                                                            startAdornment: (
                                                                <InputAdornment position='start'>
                                                                    <Mail />
                                                                </InputAdornment>
                                                            ),
                                                        }}
                                                    />
                                                    <FormControl>
                                                        <Typography>
                                                            After successfully signing in, a cookie is placed in your browser to track your session. See our
                                                            {' '}
                                                            {isExternal ? (
                                                                <a href={cookiePolicyUrl} target='_blank'>
                                                                    Cookie Policy
                                                                </a>
                                                            ) : (
                                                                <Link to={cookiePolicyUrl} target='_blank'>
                                                                    Cookie Policy
                                                                </Link>
                                                            )}
                                                            {' '}
                                                            for more details.
                                                        </Typography>
                                                    </FormControl>
                                                    <FormControlLabel
                                                        control={<Checkbox onChange={this.handlePolicyChange} />}
                                                        label={(
                                                            <p>
                                                                <strong>
                                                                    I hereby confirm that I have read and understood the
                                                                    {' '}
                                                                    {''}
                                                                    {isExternal ? (
                                                                        <a href={privacyPolicyUrl} target='_blank' className={classes.linkDisplay}>
                                                                            Privacy Policy.
                                                                        </a>
                                                                    ) : (
                                                                        <Link to='/policy/privacy-policy' target='_blank' className={classes.linkDisplay}>
                                                                            Privacy Policy.
                                                                        </Link>
                                                                    )}
                                                                </strong>
                                                            </p>
                                                        )}
                                                    />
                                                </FormControl>
                                            </span>
                                            <div className={classes.buttonsWrapper}>
                                                <Button variant='raised' color='primary' onClick={this.handleClick.bind(this)} disabled={!policy}>
                                                    Sign up
                                                </Button>
                                                <Link to='/' className={classes.linkDisplay}>
                                                    <Button variant='raised' className={classes.buttonAlignment}>
                                                        Back to Store
                                                    </Button>
                                                </Link>
                                            </div>
                                        </form>
                                    </Paper>
                                </div>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

SignUp.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SignUp);
