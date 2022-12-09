/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { AccordionDetails, AccordionSummary } from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Typography from '@material-ui/core/Typography';
import WarningOutlined from '@material-ui/icons/WarningOutlined';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Accordion from '@material-ui/core/Accordion';
import ErrorOutlineOutlinedIcon from '@material-ui/icons/ErrorOutlineOutlined';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const useStyles = makeStyles((theme) => ({
    errorAccordion: {
        borderColor: theme.palette.error.main,
        color: theme.custom.swaggerValidationErrors.errorColor,
        border: '2px solid',
        boxShadow: 'none',
    },
    warningAccordion: {
        borderColor: theme.palette.warning.main,
        color: theme.custom.swaggerValidationErrors.warningColor,
        boxShadow: 'none',
        border: '2px solid',
    },
    warningContentGrid: {
        padding: '10px',
        backgroundColor: theme.custom.swaggerValidationErrors.warningBackgroundColor,
    },
    errorContentGrid: {
        padding: '10px',
        backgroundColor: theme.custom.swaggerValidationErrors.errorBackgroundColor,
    },
    errorGrid: {
        margin: theme.spacing(1),
        border: '2px solid',
        borderColor: theme.palette.error.main,
    },
    warningGrid: {
        margin: theme.spacing(1),
        border: '2px solid',
        borderColor: theme.palette.warning.main,
    },
    warningPaper: {
        borderColor: theme.palette.warning.light,
        color: theme.custom.swaggerValidationErrors.warningColor,
        alignItems: 'center',
        display: 'flex',
        padding: '5px 10px 5px 10px',
        wordBreak: 'break-word',
    },
    errorPaper: {
        borderColor: theme.palette.error.main,
        color: theme.custom.swaggerValidationErrors.errorColor,
        alignItems: 'center',
        display: 'flex',
        padding: '5px 10px 5px 10px',
        wordBreak: 'break-word',
    },
    warningIcon: {
        float: 'left',
        marginRight: 4,
        color: theme.palette.warning.main,
    },
    errorIcon: {
        float: 'left',
        marginRight: 4,
        color: theme.palette.error.main,
    },
}));

/**
 *
 *
 * @class SwaggerValidationErrors
 * @extends {Component}
 */
export default function SwaggerValidationErrors(props) {
    const { errorDetails, noOfErrors, isValid } = props;
    const classes = useStyles();

    return (
        <>
            {errorDetails.isValid && noOfErrors > 0
                && (
                    <Accordion className={classes.warningAccordion}>
                        <AccordionSummary
                            expandIcon={<ExpandMoreIcon />}
                            aria-controls='panel1a-content'
                            id='panel1a-header'
                        >
                            <Typography>
                                <WarningOutlined className={classes.warningIcon} />
                                {' Found '}
                                {noOfErrors}
                                {' warnings while parsing the file'}
                            </Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                            <Grid item xs={12}>
                                <Paper className={classNames(
                                    classes.warningAccordion,
                                )}
                                >
                                    <Grid item xs={12} className={classes.warningContentGrid}>
                                        {errorDetails.errors.map((error) => {
                                            const warnDescription = error.description.charAt(0).toUpperCase()
                                                + error.description.slice(1);
                                            return (
                                                <Grid item xs={12} className={classes.warningGrid}>
                                                    <Paper elevation={2} className={classes.warningPaper}>
                                                        <Typography>
                                                            {warnDescription}
                                                        </Typography>
                                                    </Paper>
                                                </Grid>
                                            );
                                        })}
                                    </Grid>
                                </Paper>
                            </Grid>
                        </AccordionDetails>
                    </Accordion>
                )}
            {isValid.file && (
                <Accordion className={classes.errorAccordion}>
                    <AccordionSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls='panel1a-content'
                    >
                        <Typography>
                            <ErrorOutlineOutlinedIcon className={classes.errorIcon} />
                            {' Found '}
                            {noOfErrors}
                            {' errors while parsing the file'}
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <Grid item xs={12}>
                            <Paper
                                className={classNames(
                                    classes.errorAccordion,
                                )}
                                elevation={3}
                            >
                                <Grid item xs={12} className={classes.errorContentGrid}>
                                    {errorDetails.errors.map((error) => {
                                        const errorDescription = error.description.charAt(0).toUpperCase()
                                            + error.description.slice(1);
                                        return (
                                            <Grid item xs={12} className={classes.errorGrid}>
                                                <Paper elevation={2} className={classes.errorPaper}>
                                                    <Typography>
                                                        {errorDescription}
                                                    </Typography>
                                                </Paper>
                                            </Grid>
                                        );
                                    })}
                                </Grid>
                            </Paper>
                        </Grid>
                    </AccordionDetails>
                </Accordion>
            )}
        </>
    );
}

SwaggerValidationErrors.propTypes = {
    errorDetails: PropTypes.objectOf(PropTypes.object).isRequired,
    noOfErrors: PropTypes.number.isRequired,
    isValid: PropTypes.objectOf(PropTypes.object).isRequired,
};
