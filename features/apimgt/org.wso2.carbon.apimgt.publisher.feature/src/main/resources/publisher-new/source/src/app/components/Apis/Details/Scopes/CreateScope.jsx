/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import React from 'react';
import APIPropertyField from 'AppComponents/Apis/Details/Overview/APIPropertyField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import TagsInput from 'react-tagsinput';
import Api from 'AppData/api';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Alert from 'AppComponents/Shared/Alert';

const styles = theme => ({
    buttonSave: {
        marginTop: theme.spacing.unit * 10,
    },
    buttonCancel: {
        marginTop: theme.spacing.unit * 10,
        marginLeft: theme.spacing.unit * 5,
    },
    topics: {
        marginTop: theme.spacing.unit * 10,
    },
    headline: {
        paddingTop: theme.spacing.unit * 1.5,
        paddingLeft: theme.spacing.unit * 2.5,
    },
});

/**
 * Create new scopes for an API
 * @class CreateScope
 * @extends {Component}
 */
class CreateScope extends React.Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            apiScopes: null,
            apiScope: {},
            roles: [],
        };
        this.addScope = this.addScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    /**
     * Add new scope
     * @memberof Scopes
     */
    addScope() {
        const api = new Api();
        const scope = this.state.apiScope;
        scope.bindings = {
            type: 'role',
            values: this.state.roles,
        };
        const promisedScopeAdd = api.addScope(this.props.match.params.api_uuid, scope);
        promisedScopeAdd.then((response) => {
            if (response.status !== 201) {
                Alert.info('Something went wrong while updating the scope');
                return;
            }
            Alert.info('Scope added successfully');
            const { apiScopes } = this.state;
            this.setState({
                apiScopes,
                apiScope: {},
                roles: [],
            });
        });
    }

    /**
     * Handle api scope addition event
     * @param {any} event Button Click event
     * @memberof Scopes
     */
    handleInputs(event) {
        if (Array.isArray(event)) {
            this.setState({
                roles: event,
            });
        } else {
            const input = event.target;
            const { apiScope } = this.state;
            apiScope[input.id] = input.value;
            this.setState({
                apiScope,
            });
        }
    }

    render() {
        const { classes } = this.props;
        const url = `/apis/${this.props.api.id}/scopes`;
        return (
            <Grid container>
                <Typography
                    className={classes.headline}
                    gutterBottom
                    variant='headline'
                    component='h2'
                >
                    <FormattedMessage
                        id='create.new.scopes'
                        defaultMessage='Create New Scope'
                    />
                </Typography>
                <Grid item lg={5} className={classes.topics}>
                    <APIPropertyField name='Name'>
                        <TextField
                            fullWidth
                            id='name'
                            type='text'
                            name='name'
                            margin='normal'
                            value={this.state.apiScope.name || ''}
                            onChange={this.handleInputs}
                        />
                    </APIPropertyField>
                    <APIPropertyField name='Description'>
                        <TextField
                            style={{
                                width: '100%',
                            }}
                            id='description'
                            name='description'
                            helperText={<FormattedMessage
                                id='create.scope.helper.text'
                                defaultMessage='Short description about the scope'
                            />}
                            margin='normal'
                            type='text'
                            onChange={this.handleInputs}
                            value={this.state.apiScope.description || ''}
                        />
                    </APIPropertyField>
                    <APIPropertyField name='Roles'>
                        <TagsInput
                            value={this.state.roles}
                            onChange={this.handleInputs}
                            onlyUnique
                        />
                    </APIPropertyField>
                    <Button
                        variant='contained'
                        color='primary'
                        onClick={this.addScope}
                        className={classes.buttonSave}
                    >
                        <FormattedMessage
                            id='save'
                            defaultMessage='Save'
                        />
                    </Button>
                    <Link to={url}>
                        <Button
                            variant='contained'
                            color='primary'
                            className={classes.buttonCancel}
                        >
                            <FormattedMessage
                                id='cancel.btn'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </Link>
                </Grid>
            </Grid>
        );
    }
}

CreateScope.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

CreateScope.defaultProps = {
    match: { params: {} },
};

export default withRouter(withStyles(styles)(CreateScope));
