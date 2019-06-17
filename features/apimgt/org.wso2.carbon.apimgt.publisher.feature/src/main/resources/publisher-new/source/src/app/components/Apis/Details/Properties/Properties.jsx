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
/* eslint no-param-reassign: ["error", { "props": false }] */
/* eslint-disable react/jsx-no-bind */

import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import AddCircle from '@material-ui/icons/AddCircle';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import EditIcon from '@material-ui/icons/Edit';
import SaveIcon from '@material-ui/icons/Save';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { FormattedMessage } from 'react-intl';
import ApiContext from '../components/ApiContext';

const styles = theme => ({
    root: {
        paddingTop: 0,
        paddingLeft: 0,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
        display: 'flex',
        flexDirection: 'row',
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    buttonWrapper: {
        paddingTop: theme.spacing.unit * 3,
    },
    paperRoot: {
        padding: theme.spacing.unit * 3,
        marginTop: theme.spacing.unit * 3,
    },
    addNewHeader: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing.unit * 2,
    },
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing.unit * 2,
    },
    addProperty: {
        marginRight: theme.spacing.unit * 2,
    },
    buttonIcon: {
        marginRight: 10,
    },
    link: {
        cursor: 'pointer',
    },
});
function EditableRow(props) {
    const {
        oldKey, oldValue, classes, handleUpdateList, handleDelete, apiAdditionalProperties,
    } = props;
    const [newKey, setKey] = useState(null);
    const [newValue, setValue] = useState(null);
    const [editMode, setEditMode] = useState(false);
    const updateEditMode = function () {
        setEditMode(!editMode);
    };
    const handleKeyChange = (event) => {
        const { value } = event.target;
        setKey(value);
    };
    const handleValueChange = (event) => {
        const { value } = event.target;
        setValue(value);
    };
    const validateEmpty = function (itemValue) {
        if (itemValue === null) {
            return false;
        } else if (itemValue === '') {
            return true;
        } else {
            return false;
        }
    };
    const saveRow = function () {
        const oldRow = { oldKey, oldValue };
        const newRow = { newKey: newKey || oldKey, newValue: newValue || oldValue };
        handleUpdateList(apiAdditionalProperties, oldRow, newRow);
        setEditMode(false);
    };
    const deleteRow = function () {
        handleDelete(apiAdditionalProperties, oldKey);
    };
    const handleKeyDown = function (e) {
        if (e.key === 'Enter') {
            saveRow();
        }
    };
    return (
        <TableRow>
            {editMode ? (
                <TableCell>
                    <TextField
                        required
                        id='outlined-required'
                        label='Property Name'
                        margin='normal'
                        variant='outlined'
                        className={classes.addProperty}
                        value={newKey || oldKey}
                        onChange={handleKeyChange}
                        onKeyDown={handleKeyDown}
                        error={validateEmpty(newKey)}
                    />
                </TableCell>
            ) : (
                <TableCell>{oldKey}</TableCell>
            )}
            {editMode ? (
                <TableCell>
                    <TextField
                        required
                        id='outlined-required'
                        label='Property Name'
                        margin='normal'
                        variant='outlined'
                        className={classes.addProperty}
                        value={newValue || oldValue}
                        onChange={handleValueChange}
                        onKeyDown={handleKeyDown}
                        error={validateEmpty(newValue)}
                    />
                </TableCell>
            ) : (
                <TableCell>{oldValue}</TableCell>
            )}
            <TableCell align='right'>
                {editMode ? (
                    <React.Fragment>
                        <a className={classes.link} onClick={saveRow} onKeyDown={() => {}}>
                            <SaveIcon className={classes.buttonIcon} />
                        </a>
                    </React.Fragment>
                ) : (
                    <a className={classes.link} onClick={updateEditMode} onKeyDown={() => {}}>
                        <EditIcon className={classes.buttonIcon} />
                    </a>
                )}
                <a className={classes.link} onClick={deleteRow} onKeyDown={() => {}}>
                    <DeleteForeverIcon className={classes.buttonIcon} />
                </a>
            </TableCell>
        </TableRow>
    );
}
EditableRow.propTypes = {
    oldKey: PropTypes.shape({}).isRequired,
    oldValue: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    handleUpdateList: PropTypes.shape({}).isRequired,
    handleDelete: PropTypes.shape({}).isRequired,
    apiAdditionalProperties: PropTypes.shape({}).isRequired,
};
class Properties extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            additionalProperties: null,
            showAddProperty: false,
            propertyKey: null,
            propertyValue: null,
        };
        this.handleUpdateList = this.handleUpdateList.bind(this);
        this.handleDelete = this.handleDelete.bind(this);
    }

    toggleAddProperty = () => {
        this.setState((oldState) => {
            const { showAddProperty } = oldState;
            return { showAddProperty: !showAddProperty };
        });
    };
    handleChange = name => (event) => {
        const { value } = event.target;

        this.setState({
            [name]: value,
        });
    };
    validateEmpty(itemValue) {
        if (itemValue === null) {
            return false;
        } else if (itemValue === '') {
            return true;
        } else {
            return false;
        }
    }
    handleSubmit(oldAPI, updateAPI) {
        const { additionalProperties } = this.state;

        if (additionalProperties) {
            oldAPI.additionalProperties = additionalProperties;
        }
        updateAPI(oldAPI);
    }
    handleDelete(apiAdditionalProperties, oldKey) {
        this.setState((oldState) => {
            let { additionalProperties } = oldState;
            if (!additionalProperties) {
                additionalProperties = apiAdditionalProperties;
            }
            if (Object.prototype.hasOwnProperty.call(additionalProperties, oldKey)) {
                delete additionalProperties[oldKey];
            }
            return { additionalProperties };
        });
    }
    handleUpdateList(apiAdditionalProperties, oldRow, newRow) {
        this.setState((oldState) => {
            let { additionalProperties } = oldState;
            if (!additionalProperties) {
                additionalProperties = apiAdditionalProperties;
            }
            const { oldKey, oldValue } = oldRow;
            const { newKey, newValue } = newRow;

            if (Object.prototype.hasOwnProperty.call(additionalProperties, newKey) && oldKey === newKey) {
                // Only the value is updated
                if (newValue && oldValue !== newValue) {
                    additionalProperties[oldKey] = newValue;
                }
            } else {
                delete additionalProperties[oldKey];
                additionalProperties[newKey] = newValue;
            }
            return { additionalProperties };
        });
    }
    handleAddToList(apiAdditionalProperties) {
        this.setState((oldState) => {
            const { propertyKey, propertyValue } = oldState;
            let { additionalProperties } = oldState;
            if (!additionalProperties) {
                additionalProperties = apiAdditionalProperties;
            }
            additionalProperties[propertyKey] = propertyValue;
            return { additionalProperties };
        });
    }
    handleKeyDown = apiAdditionalProperties => (event) => {
        if (event.key === 'Enter') {
            this.handleAddToList(apiAdditionalProperties);
        }
    };
    renderAdditionalProperties(additionalProperties, apiAdditionalProperties) {
        let data = additionalProperties;
        if (data === null) {
            data = apiAdditionalProperties;
        }
        const { classes } = this.props;
        const items = [];
        for (const key in data) {
            if (Object.prototype.hasOwnProperty.call(data, key)) {
                items.push(<EditableRow
                    oldKey={key}
                    oldValue={data[key]}
                    classes={classes}
                    handleUpdateList={this.handleUpdateList}
                    handleDelete={this.handleDelete}
                    apiAdditionalProperties={data}
                />);
            }
        }
        return items;
    }
    render() {
        const { classes } = this.props;
        const {
            additionalProperties, showAddProperty, propertyKey, propertyValue,
        } = this.state;
        return (
            <div className={classes.root}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        API Properties
                    </Typography>
                    <Button size='small' className={classes.button} onClick={this.toggleAddProperty}>
                        <AddCircle className={classes.buttonIcon} />
                        Add New Property
                    </Button>
                </div>
                <ApiContext.Consumer>
                    {({ api, updateAPI }) => (
                        <Grid container spacing={24}>
                            <Grid item xs={12}>
                                <Paper className={classes.paperRoot} elevation={1}>
                                    <Table className={classes.table}>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>Property Name</TableCell>
                                                <TableCell>Property Value</TableCell>
                                                <TableCell />
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {showAddProperty && (
                                                <TableRow>
                                                    <TableCell>
                                                        <TextField
                                                            required
                                                            id='outlined-required'
                                                            label='Property Name'
                                                            margin='normal'
                                                            variant='outlined'
                                                            className={classes.addProperty}
                                                            value={propertyKey === null ? '' : propertyKey}
                                                            onChange={this.handleChange('propertyKey')}
                                                            onKeyDown={this.handleKeyDown(api.additionalProperties)}
                                                            error={this.validateEmpty(propertyKey)}
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <TextField
                                                            required
                                                            id='outlined-required'
                                                            label='Property Value'
                                                            margin='normal'
                                                            variant='outlined'
                                                            className={classes.addProperty}
                                                            value={propertyValue === null ? '' : propertyValue}
                                                            onChange={this.handleChange('propertyValue')}
                                                            onKeyDown={this.handleKeyDown(api.additionalProperties)}
                                                            error={this.validateEmpty(propertyValue)}
                                                        />
                                                    </TableCell>
                                                    <TableCell align='right'>
                                                        <Button
                                                            variant='contained'
                                                            color='primary'
                                                            disabled={!propertyValue || !propertyKey}
                                                            onClick={() =>
                                                                this.handleAddToList(api.additionalProperties)
                                                            }
                                                        >
                                                            <FormattedMessage id='add' defaultMessage='Add' />
                                                        </Button>
                                                        <Button onClick={this.toggleAddProperty}>
                                                            <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                                        </Button>
                                                    </TableCell>
                                                </TableRow>
                                            )}
                                            {this.renderAdditionalProperties(
                                                additionalProperties,
                                                api.additionalProperties,
                                            )}
                                        </TableBody>
                                    </Table>
                                </Paper>
                                <div className={classes.buttonWrapper}>
                                    <Grid
                                        container
                                        direction='row'
                                        alignItems='flex-start'
                                        spacing={16}
                                        className={classes.buttonSection}
                                    >
                                        <Grid item>
                                            <div>
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    onClick={() => this.handleSubmit(api, updateAPI)}
                                                >
                                                    <FormattedMessage id='save' defaultMessage='Save' />
                                                </Button>
                                            </div>
                                        </Grid>
                                        <Grid item>
                                            <Link to={'/apis/' + api.id + '/overview'}>
                                                <Button>
                                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                                </Button>
                                            </Link>
                                        </Grid>
                                    </Grid>
                                </div>
                            </Grid>
                        </Grid>
                    )}
                </ApiContext.Consumer>
            </div>
        );
    }
}

Properties.propTypes = {
    state: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
};

export default withStyles(styles)(Properties);
