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
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import FileIcon from '@material-ui/icons/Description';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Avatar from '@material-ui/core/Avatar';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';

class Documents extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            documentsList: null,
        };
    }
    componentDidMount() {
        const API = new Api();

        const docs = API.getDocuments(this.props.api.id);
        docs.then((response) => {
            this.setState({ documentsList: response.obj.list });
        }).catch((errorResponse) => {
            const errorData = JSON.parse(errorResponse.message);
            const messageTxt =
                'Error[' + errorData.code + ']: ' + errorData.description + ' | ' + errorData.message + '.';
            console.error(messageTxt);
            Alert.error('Error in fetching documents list of the API');
        });
    }
    render() {
        const { parentClasses, api } = this.props;
        const { documentsList } = this.state;
        return (
            <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
                <div className={parentClasses.titleWrapper}>
                    <Typography variant='h5' component='h3' className={parentClasses.title}>
                        Documents
                    </Typography>
                    <Link to={'/apis/' + api.id + '/documents'}>
                        <Button variant='contained' color='default'>
                            Edit
                        </Button>
                    </Link>
                </div>

                {documentsList && documentsList.length !== 0 && (
                    <List className={parentClasses.ListRoot}>
                        {documentsList.map(item => (
                            <ListItem key={item.id}>
                                <Avatar>
                                    <FileIcon />
                                </Avatar>
                                <ListItemText primary={item.name} secondary={item.summary} />
                            </ListItem>
                        ))}
                    </List>
                )}
                {documentsList && documentsList.length === 0 && (
                    <Typography component='p' variant='body1' className={parentClasses.subtitle}>
                        &lt;Not Created&gt;
                    </Typography>
                )}
            </Paper>
        );
    }
}

Documents.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default Documents;
