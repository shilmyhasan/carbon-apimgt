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

import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import TableHead from '@material-ui/core/TableHead';
import { withStyles } from '@material-ui/core/styles';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';

import EndpointTableRows from './EndpointTableRows';

const styles = theme => ({
    root: {
        width: '100%',
        marginTop: theme.spacing.unit * 3,
        overflowX: 'auto',
    },
    table: {
        minWidth: 700,
    },
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginRight: 50,
    },
    addButton: {
        display: 'inline-block',
        marginBottom: 20,
        zIndex: 1,
    },
    popperClose: {
        pointerEvents: 'none',
    },
});

/**
 * Listing component for global endpoints
 * @class EndpointsListing
 * @extends {Component}
 */
class EndpointsListing extends Component {
    /**
     * Creates an instance of EndpointsListing.
     * @param {any} props @inheritDoc
     * @memberof EndpointsListing
     */
    constructor(props) {
        super(props);
        this.state = {
            endpoints: null,
        };
        this.handleEndpointDelete = this.handleEndpointDelete.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof EndpointsListing
     */
    componentDidMount() {
        const api = new API();
        const promisedEndpoints = api.getEndpoints();
        /* TODO: Handle catch case , auth errors and ect ~tmkb */
        promisedEndpoints.then((response) => {
            this.setState({ endpoints: response.obj.list });
        });
    }

    /**
     * Handle Delete global endpoint
     * @param {String} endpointUuid UUID of the global endpoint
     * @param {String} name Name of the endpoint
     * @memberof EndpointsListing
     */
    handleEndpointDelete(endpointUuid, name) {
        const api = new API();
        const promisedDelete = api.deleteEndpoint(endpointUuid);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.info('Something went wrong while deleting the ' + name + ' Endpoint!');
                return;
            }
            Alert.info(name + ' Endpoint deleted successfully!');
            const { endpoints } = this.state;
            for (const endpointIndex in endpoints) {
                if (
                    Object.prototype.hasOwnProperty.call(endpoints, endpointIndex) &&
                    endpoints[endpointIndex].id === endpointUuid
                ) {
                    endpoints.splice(endpointIndex, 1);
                    break;
                }
            }
        });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Endpoint listing component
     * @memberof EndpointsListing
     */
    render() {
        return (
            <Grid item xs={12}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Service URL</TableCell>
                            <TableCell>Max TPS</TableCell>
                            <TableCell>Action</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {this.state.endpoints &&
                            this.state.endpoints.map((endpoint) => {
                                return (
                                    <EndpointTableRows
                                        endpoint={endpoint}
                                        key={endpoint.id}
                                        handleEndpointDelete={this.handleEndpointDelete}
                                    />
                                );
                            })}
                    </TableBody>
                </Table>
            </Grid>
        );
    }
}
EndpointsListing.propTypes = {};

export default withStyles(styles)(EndpointsListing);
