/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useEffect, useState } from 'react';
import API from 'AppData/api';
import { useIntl, FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import Delete from 'AppComponents/KeyManagers/DeleteKeyManager';
import { Link as RouterLink } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import Switch from '@material-ui/core/Switch';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import Toolbar from '@material-ui/core/Toolbar';
import Grid from '@material-ui/core/Grid';
import SearchIcon from '@material-ui/icons/Search';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import RefreshIcon from '@material-ui/icons/Refresh';
import AppBar from '@material-ui/core/AppBar';
import { makeStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import EditIcon from '@material-ui/icons/Edit';
import InlineProgress from 'AppComponents/AdminPages/Addons/InlineProgress';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';

const useStyles = makeStyles((theme) => ({
    searchBar: {
        borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
    },
    searchInput: {
        fontSize: theme.typography.fontSize,
    },
    block: {
        display: 'block',
    },
    contentWrapper: {
        margin: '40px 16px',
    },
    button: {
        borderColor: 'rgba(255, 255, 255, 0.7)',
    },
    tableCellWrapper: {
        '& td': {
            'word-break': 'break-all',
            'white-space': 'normal',
        },
    },
}));

/**
 * API call to get microgateway labels
 * @returns {Promise}.
 */
function apiCall() {
    const restApi = new API();
    return restApi
        .getKeyManagersList()
        .then((result) => {
            return result.body.list;
        })
        .catch((error) => {
            throw error;
        });
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListKeyManagers() {
    const { isSuperTenant, user: { _scopes } } = useAppContext();
    const isSuperAdmin = isSuperTenant && _scopes.includes('apim:admin_settings');
    // eslint-disable-next-line no-unused-vars
    const [saving, setSaving] = useState(false);
    const intl = useIntl();
    const classes = useStyles();
    const [data, setData] = useState(null);
    const [localKMs, setLocalKMs] = useState(null);
    const [globalKMs, setGlobalKMs] = useState(null);
    const [searchText, setSearchText] = useState('');
    const [error, setError] = useState(null);
    const editComponentProps = {};

    const setKeyManagers = (keyManagers) => {
        setLocalKMs(keyManagers.filter((item) => {
            return !item.isGlobal;
        }));
        setGlobalKMs(keyManagers.filter((item) => {
            return item.isGlobal;
        }));
    };

    const fetchData = () => {
        // Fetch data from backend when an apiCall is provided
        setData(null);
        if (apiCall) {
            const promiseAPICall = apiCall();
            promiseAPICall.then((LocalData) => {
                if (LocalData) {
                    setData(LocalData);
                    setKeyManagers(LocalData);
                    setError(null);
                } else {
                    setError(intl.formatMessage({
                        id: 'AdminPages.Addons.ListBase.noDataError',
                        defaultMessage: 'Error while retrieving data.',
                    }));
                }
            })
                .catch((e) => {
                    setError(e.message);
                });
        }
        setSearchText('');
    };

    const addedActions = [
        (props) => {
            const { rowData, updateList } = props;
            const updateSomething = () => {
                const restApi = new API();
                const kmName = rowData[0];
                const kmId = rowData[4];
                (rowData[5] ? restApi.globalKeyManagerGet(kmId) : restApi.keyManagerGet(kmId)).then((result) => {
                    let editState;
                    if (result.body.name !== null) {
                        editState = {
                            ...result.body,
                        };
                    }
                    editState.enabled = !editState.enabled;
                    restApi.updateKeyManager(kmId, editState).then(() => {
                        Alert.success(` ${kmName} ${intl.formatMessage({
                            id: 'KeyManagers.ListKeyManagers.edit.success',
                            defaultMessage: ' Key Manager updated successfully.',
                        })}`);
                        setSaving(false);
                        updateList();
                    }).catch((e) => {
                        const { response } = e;
                        if (response.body) {
                            Alert.error(response.body.description);
                        }
                        setSaving(false);
                        updateList();
                    });
                });
            };
            const kmEnabled = rowData[3];
            return (
                <Switch
                    disabled={rowData[5] ? !isSuperAdmin : false}
                    checked={kmEnabled}
                    onChange={updateSomething}
                    color='primary'
                    size='small'
                />
            );
        },
    ];

    const columns = [
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'KeyManagers.ListKeyManagers.table.header.label.name',
                defaultMessage: 'Name',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (typeof tableMeta.rowData === 'object') {
                        const artifactId = tableMeta.rowData[4];
                        return (
                            <RouterLink
                                to={{
                                    pathname: `/settings/key-managers/${artifactId}`,
                                    state: { isGlobal: tableMeta.rowData[5] },
                                }}
                            >
                                {value}
                            </RouterLink>
                        );
                    } else {
                        return <div />;
                    }
                },
            },
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'KeyManagers.ListKeyManagers.table.header.label.description',
                defaultMessage: 'Description',
            }),
            options: {
                sort: false,
            },
        },
        {
            name: 'type',
            label: intl.formatMessage({
                id: 'KeyManagers.ListKeyManagers.table.header.label.type',
                defaultMessage: 'Type',
            }),
            options: {
                sort: false,
            },
        },
        { name: 'enabled', options: { display: false } },
        { name: 'id', options: { display: false } },
        { name: 'isGlobal', options: { display: false } },
        {
            name: '',
            label: 'Actions',
            options: {
                filter: false,
                sort: false,
                customBodyRender: (value, tableMeta) => {
                    const dataRow = tableMeta.rowData[5] ? globalKMs[tableMeta.rowIndex] : localKMs[tableMeta.rowIndex];
                    const itemName = (typeof tableMeta.rowData === 'object') ? tableMeta.rowData[0] : '';
                    if (editComponentProps && editComponentProps.routeTo) {
                        if (typeof tableMeta.rowData === 'object') {
                            const artifactId = tableMeta.rowData[tableMeta.rowData.length - 2];
                            return (
                                <div data-testid={`${itemName}-actions`}>
                                    <RouterLink to={editComponentProps.routeTo + artifactId}>
                                        <IconButton color='primary' component='span'>
                                            <EditIcon />
                                        </IconButton>
                                    </RouterLink>
                                    <Delete
                                        dataRow={dataRow}
                                        updateList={fetchData}
                                        isDisabled={dataRow.isGlobal && !isSuperAdmin}
                                    />
                                    {addedActions && addedActions.map((action) => {
                                        const AddedComponent = action;
                                        return (
                                            <AddedComponent rowData={tableMeta.rowData} updateList={fetchData} />
                                        );
                                    })}
                                </div>
                            );
                        } else {
                            return (<div />);
                        }
                    }
                    return (
                        <div data-testid={`${itemName}-actions`}>
                            <Delete
                                dataRow={dataRow}
                                updateList={fetchData}
                                isDisabled={dataRow.isGlobal && !isSuperAdmin}
                            />
                            {addedActions && addedActions.map((action) => {
                                const AddedComponent = action;
                                return (
                                    <AddedComponent rowData={tableMeta.rowData} updateList={fetchData} />
                                );
                            })}
                        </div>

                    );
                },
                setCellProps: () => {
                    return {
                        style: { width: 200 },
                    };
                },
            },
        },
    ];

    const pageProps = {
        pageStyle: 'paperLess',
        title: intl.formatMessage({
            id: 'KeyManagers.ListKeyManagers.List.title',
            defaultMessage: 'Key Managers',
        }),
    };
    const addButtonOverride = (
        <RouterLink to='/settings/key-managers/create'>
            <Button variant='contained' color='primary' size='small'>
                <FormattedMessage
                    id='KeyManagers.ListKeyManagers.addButtonProps.triggerButtonText'
                    defaultMessage='Add Key Manager'
                />
            </Button>
        </RouterLink>
    );
    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.KeyManagers.List.empty.content.keymanagers'
                    defaultMessage='It is possible to register an OAuth Provider.'
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='KeyManagers.ListKeyManagers.empty.title'
                    defaultMessage='Key Managers'
                />
            </Typography>
        ),
    };

    useEffect(() => {
        fetchData();
    }, []);

    const sortBy = (field, reverse, primer) => {
        const key = primer
            ? (x) => {
                return primer(x[field]);
            }
            : (x) => {
                return x[field];
            };

        // eslint-disable-next-line no-param-reassign
        reverse = !reverse ? 1 : -1;

        return (a, b) => {
            const aValue = key(a);
            const bValue = key(b);
            return reverse * ((aValue > bValue) - (bValue > aValue));
        };
    };

    const onColumnSortChange = (changedColumn, direction) => {
        const sorted = [...data].sort(sortBy(changedColumn, direction === 'descending'));
        setData(sorted);
    };

    const options = {
        filterType: 'checkbox',
        selectableRows: 'none',
        filter: false,
        search: false,
        print: false,
        download: false,
        viewColumns: false,
        customToolbar: null,
        responsive: 'stacked',
        searchText,
        onColumnSortChange,
    };

    const globalDTOptions = {
        selectableRows: 'none',
        filter: false,
        search: false,
        print: false,
        download: false,
        viewColumns: false,
        customToolbar: null,
        responsive: 'stacked',
        pagination: false,
    };

    const filterData = (event) => {
        setSearchText(event.target.value);
    };

    // If no apiCall is provided OR,
    // retrieved data is empty, display an information card.
    if (!apiCall || (data && data.length === 0)) {
        return (
            <ContentBase
                {...pageProps}
                pageStyle='small'
            >
                <Card className={classes.root}>
                    <CardContent>
                        {emptyBoxProps.title}
                        {emptyBoxProps.content}
                    </CardContent>
                    <CardActions>
                        {addButtonOverride}
                    </CardActions>
                </Card>
            </ContentBase>
        );
    }
    // If apiCall is provided and data is not retrieved yet, display progress component
    if (!error && apiCall && !data) {
        return (
            <ContentBase pageStyle='paperLess'>
                <InlineProgress />
            </ContentBase>

        );
    }
    if (error) {
        return (
            <ContentBase {...pageProps}>
                <Alert severity='error'>{error}</Alert>
            </ContentBase>
        );
    }

    return (
        <>
            <ContentBase {... pageProps}>
                <div style={{ marginBottom: '40px' }}>
                    <Typography gutterBottom variant='h4' component='h2'>
                        <FormattedMessage
                            id='KeyManagers.ListKeyManagers.global.heading'
                            defaultMessage='Global Key Manager'
                        />
                    </Typography>
                    <div className={classes.tableCellWrapper}>
                        {globalKMs && globalKMs.length > 0 && (
                            <MUIDataTable
                                title={null}
                                data={globalKMs}
                                columns={columns}
                                options={globalDTOptions}
                            />
                        )}
                    </div>
                    {globalKMs && globalKMs.length === 0 && (
                        <RouterLink
                            to={{
                                pathname: '/settings/key-managers/create',
                                state: { isGlobal: true },
                            }}
                        >
                            <Button variant='contained' color='primary' size='small' disabled={!isSuperAdmin}>
                                <FormattedMessage
                                    id='KeyManagers.ListKeyManagers.addGlobalKeyManager'
                                    defaultMessage='Add Global Key Manager'
                                />
                            </Button>
                        </RouterLink>
                    )}
                </div>

                <div>
                    <Typography gutterBottom variant='h4' component='h2'>
                        <FormattedMessage
                            id='KeyManagers.ListKeyManagers.local.heading'
                            defaultMessage='Local Key Managers'
                        />
                    </Typography>

                    <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
                        <Toolbar>
                            <Grid container spacing={2} alignItems='center'>
                                <Grid item>
                                    <SearchIcon className={classes.block} color='inherit' />
                                </Grid>
                                <Grid item xs>
                                    <TextField
                                        fullWidth
                                        placeholder=''
                                        InputProps={{
                                            disableUnderline: true,
                                            className: classes.searchInput,
                                        }}
                                        onChange={filterData}
                                        value={searchText}
                                    />
                                </Grid>
                                <Grid item>
                                    {addButtonOverride}
                                    <Tooltip title={(
                                        <FormattedMessage
                                            id='AdminPages.Addons.ListBase.reload'
                                            defaultMessage='Reload'
                                        />
                                    )}
                                    >
                                        <IconButton onClick={fetchData}>
                                            <RefreshIcon className={classes.block} color='inherit' />
                                        </IconButton>
                                    </Tooltip>
                                </Grid>
                            </Grid>
                        </Toolbar>
                    </AppBar>
                    <div className={classes.tableCellWrapper}>
                        {localKMs && localKMs.length > 0 && (
                            <MUIDataTable
                                title={null}
                                data={localKMs}
                                columns={columns}
                                options={options}
                            />
                        )}
                    </div>
                    {data && data.length === 0 && (
                        <div className={classes.contentWrapper}>
                            <Typography color='textSecondary' align='center'>
                                <FormattedMessage
                                    id='AdminPages.Addons.ListBase.nodata.message'
                                    defaultMessage='No items yet'
                                />
                            </Typography>
                        </div>
                    )}
                </div>
            </ContentBase>
        </>
    );
}
