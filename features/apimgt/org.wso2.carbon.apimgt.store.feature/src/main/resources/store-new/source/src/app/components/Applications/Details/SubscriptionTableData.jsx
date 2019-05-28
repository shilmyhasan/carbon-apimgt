/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
"use strict";
import React from 'react';
import { Link } from 'react-router-dom';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import IconButton from '@material-ui/core/IconButton';
import Delete from '@material-ui/icons/Delete';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import Slide from '@material-ui/core/Slide';
import Button from '@material-ui/core/Button';
/**
 *
 *
 * @class SubscriptionTableData
 * @extends {React.Component}
 */
class SubscriptionTableData extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            openMenu: false,
        };
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleRequestOpen = this.handleRequestOpen.bind(this);
    }
/**
 *
 *
 * @memberof SubscriptionTableData
 */
handleRequestClose() {
        this.setState({ openMenu: false });
    }
/**
 *
 *
 * @memberof SubscriptionTableData
 */
handleRequestOpen() {
        this.setState({ openMenu: true });
    }
/**
 *
 *
 * @returns
 * @memberof SubscriptionTableData
 */
render() {
        const {
            apiName, lifeCycleStatus, policy, subscriptionId, apiIdentifier
        } = this.props.subscription;
        return (
            <TableRow hover>
                <TableCell style={{paddingLeft: 0}}>
                    <Link to={'/apis/' + apiIdentifier}>{apiName}</Link>
                </TableCell>
                <TableCell>{policy}</TableCell>
                <TableCell>{lifeCycleStatus}</TableCell>

                <TableCell>
                    <div>
                        {/* Scope validation should be implemente here */}
                        <IconButton aria-label='Delete' onClick={this.handleRequestOpen}>
                            <Delete />
                        </IconButton>

                        <Dialog open={this.state.openMenu} transition={Slide}>
                            <DialogTitle>Confirm</DialogTitle>
                            <DialogContent>
                                <DialogContentText>Are you sure you want to delete the Subscription?</DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button dense color='primary' onClick={this.handleRequestClose}>
                                    Cancel
                                </Button>
                                <Button dense color='primary' onClick={() =>
                                    this.props.handleSubscriptionDelete(subscriptionId)}>
                                    Delete
                                </Button>
                            </DialogActions>
                        </Dialog>
                    </div>
                </TableCell>
            </TableRow>
        );
    }
};

export default SubscriptionTableData;

