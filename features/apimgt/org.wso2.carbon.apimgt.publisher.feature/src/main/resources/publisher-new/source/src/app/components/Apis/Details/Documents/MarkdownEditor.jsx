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

import React, { useState } from 'react';
import intl, { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import ReactMarkdown from 'react-markdown';
import MonacoEditor from 'react-monaco-editor';
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

const styles = {
    appBar: {
        position: 'relative',
    },
    flex: {
        flex: 1,
    },
    popupHeader: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    splitWrapper: {
        padding: 0,
    },
    docName: {
        alignItems: 'center',
        display: 'flex',
    },
    markdownViewWrapper: {
        height: '100vh',
        overflowY: 'auto',
    },
    button: {
        height: 30,
        marginLeft: 30,
    },
};

function Transition(props) {
    return <Slide direction='up' {...props} />;
}

function MarkdownEditor(props) {
    const { intl } = props;
    const [open, setOpen] = useState(false);
    const [code, setCode] = useState(intl.formatMessage({
        id: 'documents.markdown.editor.default',
        defaultMessage: '#Enter your markdown content',
    }));
    const toggleOpen = () => {
        if (!open) updateDoc();
        setOpen(!open);
    };
    const updateCode = (newCode) => {
        setCode(newCode);
    };
    const editorDidMount = (editor, monaco) => {
        editor.focus();
    };
    const addContentToDoc = () => {
        const restAPI = new Api();
        const docPromise = restAPI.addInlineContentToDocument(props.apiId, props.docId, 'MARKDOWN', code);
        docPromise
            .then((doc) => {
                Alert.info(`${doc.name} ${intl.formatMessage({
                    id: 'documents.markdown.editor.updated.successfully',
                    defaultMessage: 'updated successfully.',
                })}`);
                toggleOpen();
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                }
            });
    };
    const updateDoc = () => {
        const restAPI = new Api();

        const docPromise = restAPI.getInlineContentOfDocument(props.apiId, props.docId);
        docPromise
            .then((doc) => {
                setCode(doc.text);
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ apiNotFound: true });
                }
            });
    };

    const { classes } = props;
    return (
        <div>
            <Button onClick={toggleOpen}>
                <Icon>code</Icon> <FormattedMessage id='documents.markdown.editor.editor' defaultMessage='Editor' />
            </Button>
            <Dialog fullScreen open={open} onClose={toggleOpen} TransitionComponent={Transition}>
                <Paper square className={classes.popupHeader}>
                    <IconButton color='inherit' onClick={toggleOpen} aria-label='Close'>
                        <Icon>close</Icon>
                    </IconButton>
                    <Typography variant='h4' className={classes.docName}>
                        <FormattedMessage
                            id='documents.markdown.editor.edit.content'
                            defaultMessage='Edit Content of'
                        />{' '}
                        "{props.docName}"
                    </Typography>
                    <Button className={classes.button} variant='contained' color='primary' onClick={addContentToDoc}>
                        <FormattedMessage
                            id='documents.markdown.editor.update.content'
                            defaultMessage='Update Content'
                        />
                    </Button>
                    <Button className={classes.button} onClick={toggleOpen}>
                        <FormattedMessage id='documents.markdown.editor.cancel' defaultMessage='Cancel' />
                    </Button>
                </Paper>
                <div className={classes.splitWrapper}>
                    <Grid container spacing={24}>
                        <Grid item xs={6}>
                            <MonacoEditor
                                width='100%'
                                height='calc(100vh - 55px)'
                                language='markdown'
                                theme='vs-dark'
                                value={code}
                                options={{ selectOnLineNumbers: true }}
                                onChange={updateCode}
                                editorDidMount={editorDidMount}
                            />
                        </Grid>
                        <Grid item xs={6}>
                            <div className={classes.markdownViewWrapper}>
                                <ReactMarkdown source={code} />
                            </div>
                        </Grid>
                    </Grid>
                </div>
            </Dialog>
        </div>
    );
}

MarkdownEditor.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(MarkdownEditor));
