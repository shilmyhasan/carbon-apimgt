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
import qs from 'qs';

import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableSortLabel from '@material-ui/core/TableSortLabel';

import IconButton from '@material-ui/core/IconButton';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import List from '@material-ui/icons/List';
import GridIcon from '@material-ui/icons/GridOn';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';
import API from '../../../data/api';
import APiTableRow from './ApiTableRow';
import ApiThumb from './ApiThumb';
import CustomIcon from '../../Shared/CustomIcon';
import ApiTableView from './ApiTableView';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
        width: theme.custom.contentAreaWidth,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
        paddingRight: 20,
    },
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {
        flexGrow: 1,
    },
    content: {
        flexGrow: 1,
    },
});
/**
 *
 *
 * @param {*} order
 * @param {*} orderBy
 * @returns
 */
function getSorting(order, orderBy) {
    return order === 'desc' ? (a, b) => (b[orderBy] < a[orderBy] ? -1 : 1) : (a, b) => (a[orderBy] < b[orderBy] ? -1 : 1);
}
/**
 *
 *
 * @class EnhancedAPITableHead
 * @extends {React.Component}
 */
class EnhancedAPITableHead extends React.Component {
    static propTypes = {
        onRequestSort: PropTypes.func.isRequired,
        order: PropTypes.string.isRequired,
        orderBy: PropTypes.string.isRequired,
    };

    /**
     *
     *
     * @memberof EnhancedAPITableHead
     */
    createSortHandler = property => (event) => {
        this.props.onRequestSort(event, property);
    };

    /**
     *
     *
     * @returns
     * @memberof EnhancedAPITableHead
     */
    render() {
        const columnData = [{
            id: 'name', numeric: false, disablePadding: true, label: 'Name',
        }, {
            id: 'version', numeric: false, disablePadding: false, label: 'Version',
        }, {
            id: 'context', numeric: false, disablePadding: false, label: 'Context',
        }, {
            id: 'rating', numeric: false, disablePadding: false, label: 'Rating',
        }];
        const { order, orderBy } = this.props;

        return (
            <TableHead>
                <TableRow>
                    {columnData.map((column) => {
                        return (
                            <TableCell key={column.id} numeric={column.numeric} sortDirection={orderBy === column.id ? order : false}>
                                <TableSortLabel active={orderBy === column.id} direction={order} onClick={this.createSortHandler(column.id)}>
                                    {column.label}
                                </TableSortLabel>
                            </TableCell>
                        );
                    }, this)}
                </TableRow>
            </TableHead>
        );
    }
}
/**
 *
 *
 * @class Listing
 * @extends {React.Component}
 */
class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apis: null,
            value: 1,
            order: 'asc',
            orderBy: 'name',
        };
        this.state.listType = this.props.theme.custom.defaultApiView;
    }

    /**
     *
     *
     * @memberof Listing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };

    /**
     *
     *
     * @memberof Listing
     */
    componentDidMount() {
        const api = new API();
        const promised_apis = api.getAllAPIs();
        promised_apis
            .then((response) => {
                this.setState({ apis: response.obj });
            })
            .catch((error) => {
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                    const params = qs.stringify({ reference: this.props.location.pathname });
                    this.props.history.push({ pathname: '/login', search: params });
                }
            });
    }

    /**
     *
     *
     * @memberof Listing
     */
    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';

        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }

        this.setState({ order, orderBy });
    };

    /**
     *
     *
     * @returns
     * @memberof Listing
     */
    render() {
        if (this.state.notFound) {
            return <ResourceNotFound />;
        }

        const { order, orderBy, apis } = this.state;
        const { theme, classes } = this.props;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);

        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='display1' className={classes.mainTitle}>
                            APIs
                        </Typography>
                        {this.state.apis && (
                            <Typography variant='caption' gutterBottom align='left'>
                                Displaying
                                {' '}
                                {this.state.apis.count}
                                {' '}
API
                            </Typography>
                        )}
                    </div>
                    <div className={classes.buttonRight}>
                        <IconButton className={classes.button} onClick={() => this.setListType('list')}>
                            <List color={this.state.listType === 'list' ? 'primary' : 'default'} />
                        </IconButton>
                        <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                            <GridIcon color={this.state.listType === 'grid' ? 'primary' : 'default'} />
                        </IconButton>
                    </div>
                </div>

                <Grid container spacing={0} justify='center'>
                    <Grid item xs={12}>
                        {this.state.apis ? (
                            this.state.listType === 'grid' ? (
                                <Grid container className={classes.ListingWrapper}>
                                    {this.state.apis.list.map(api => (
                                        <ApiThumb api={api} key={api.id} />
                                    ))}
                                </Grid>
                            ) : (
                                <React.Fragment>
                                    <ApiTableView apis={apis.list} />
                                </React.Fragment>
                            )
                        ) : (
                            <Loading />
                        )}
                    </Grid>
                </Grid>
            </main>
        );
    }
}

Listing.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};
export default withStyles(styles, { withTheme: true })(Listing);
