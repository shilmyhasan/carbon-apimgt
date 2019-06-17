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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import { FileCopy } from '@material-ui/icons';
import FormHelperText from '@material-ui/core/FormHelperText';
import InlineMessage from '../../Shared/InlineMessage';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    epWrapper: {
        display: 'flex',
    },
    bootstrapRoot: {
        padding: 0,
        'label + &': {
            marginTop: theme.spacing.unit * 3,
        },
    },
    bootstrapInput: {
        borderRadius: 4,
        backgroundColor: theme.palette.common.white,
        border: '1px solid #ced4da',
        padding: '5px 12px',
        width: 350,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        fontFamily: ['-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', '"Helvetica Neue"', 'Arial', 'sans-serif', '"Apple Color Emoji"', '"Segoe UI Emoji"', '"Segoe UI Symbol"'].join(','),
        '&:focus': {
            borderColor: '#80bdff',
            boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
        },
    },
    epWrapper: {
        display: 'flex',
        marginTop: 20,
    },
    prodLabel: {
        lineHeight: '30px',
        marginRight: 10,
        width: 100,
    },
    contentWrapper: {
        width: theme.custom.contentAreaWidth - theme.custom.leftMenuWidth,
    },
    root: {
        marginTop: 20,
    },
});
/**
 *
 *
 * @class ViewToken
 * @extends {React.Component}
 */
class ViewToken extends React.Component {
    constructor(props) {
        super(props);
    }

    state = {
        tokenCopied: false,
    };

    /**
     *
     *
     * @memberof ViewToken
     */
    onCopy = name => (event) => {
        this.setState({
            [name]: true,
        });
        const that = this;
        const elementName = name;
        const caller = function () {
            that.setState({
                [elementName]: false,
            });
        };
        setTimeout(caller, 4000);
    };

    /**
     * Generate a comma separate string of token scopes
     * @param {string} tokenScopes token scopes
     * @returns {String} scopeString comma separated string of token scopes
     * @memberof ViewToken
     */
    getTokeScopesString(tokenScopes) {
        let scopeString = tokenScopes.splice(0,1);
        tokenScopes.map(scope => scopeString +=  ", " + scope);
        return scopeString;
    }

    /**
     *
     *
     * @returns
     * @memberof ViewToken
     */
    render() {
        const { classes, token } = this.props;
        return (
            <div className={classes.root}>
                <InlineMessage type='warn'>
                    <Typography variant='headline' component='h3'>
                        Please Copy the Access Token
                    </Typography>
                    <Typography component='p'>Please copy this generated token value as it will be displayed only for the current browser session. ( After a page refresh, the token is not visible in the UI )</Typography>
                </InlineMessage>
                <div className={classes.epWrapper}>
                    <Typography className={classes.prodLabel}>Access Token</Typography>
                    <TextField
                        defaultValue={token.accessToken}
                        id='bootstrap-input'
                        InputProps={{
                            disableUnderline: true,
                            classes: {
                                root: classes.bootstrapRoot,
                                input: classes.bootstrapInput,
                            },
                        }}
                        InputLabelProps={{
                            shrink: true,
                            className: classes.bootstrapFormLabel,
                        }}
                    />
                    <Tooltip title={this.state.tokenCopied ? 'Copied' : 'Copy to clipboard'} placement='right'>
                        <CopyToClipboard text={token.accessToken} onCopy={this.onCopy('tokenCopied')}>
                            <FileCopy color='secondary' />
                        </CopyToClipboard>
                    </Tooltip>
                </div>
                <FormHelperText>
                    Above token has a validity period of
                    {' '}
                    {token.validityTime}
                    {' '}
seconds. And the token has (
                    {' '}
                    {this.getTokeScopesString(token.tokenScopes)}
                    {' '}
) scopes.
                </FormHelperText>
            </div>
        );
    }
}

ViewToken.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ViewToken);
