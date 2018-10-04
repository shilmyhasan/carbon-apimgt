import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Chip from '@material-ui/core/Chip';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import green from '@material-ui/core/colors/green';
import API from 'AppData/api.js';

import Alert from 'AppComponents/Shared/Alert';
import DeleteApiButton from 'AppComponents/Apis/Details/components/DeleteApiButton';

import ImageGenerator from './ImageGenerator';

const styles = theme => ({
    card: {
        margin: theme.spacing.unit * (3 / 2),
        maxWidth: theme.spacing.unit * 32,
        transition: 'box-shadow 0.3s ease-in-out',
    },
    providerText: {
        textTransform: 'capitalize',
    },
    apiDetails: { padding: theme.spacing.unit },
    apiActions: { justifyContent: 'space-between', padding: `0px 0px ${theme.spacing.unit}px 0px` },
    deleteProgress: {
        color: green[200],
        position: 'absolute',
        marginLeft: '200px',
    },
});

/**
 *
 * Render API Card component in API listing card view,containing essential API information like name , version ect
 * @class APIThumb
 * @extends {Component}
 */
class APIThumb extends Component {
    /**
     *Creates an instance of APIThumb.
     * @param {*} props
     * @memberof APIThumb
     */
    constructor(props) {
        super(props);
        this.state = { isHover: false, loading: false };
        this.toggleMouseOver = this.toggleMouseOver.bind(this);
        this.handleApiDelete = this.handleApiDelete.bind(this);
    }

    /**
     *
     * Delete an API listed in the listing page
     * @param {React.SyntheticEvent} event OnClick event of delete button
     * @param {String} [name=''] API Name use for alerting purpose only
     * @memberof Listing
     */
    handleApiDelete() {
        const { id } = this.props.api;
        this.setState({ loading: true });
        const { updateAPIsList } = this.props;
        const promisedDelete = API.delete(id);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                Alert.info('Something went wrong while deleting the API!');
                return;
            }
            updateAPIsList(id);
            Alert.info(`API ${id} deleted Successfully`);
            this.setState({ loading: false });
        });
    }

    /**
     * Toggle mouse Hover state to set the card `raised` property
     *
     * @param {React.SyntheticEvent} event mouseover and mouseout
     * @memberof APIThumb
     */
    toggleMouseOver(event) {
        this.setState({ isHover: event.type === 'mouseover' });
    }
    /**
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof APIThumb
     */
    render() {
        const { classes, api } = this.props;
        const { isHover, loading } = this.state;

        return (
            <Card
                onMouseOver={this.toggleMouseOver}
                onFocus={this.toggleMouseOver}
                onMouseOut={this.toggleMouseOver}
                onBlur={this.toggleMouseOver}
                raised={isHover}
                className={classes.card}
            >
                <CardMedia src='None' component={ImageGenerator} height={140} title='Contemplative Reptile' api={api} />
                <CardContent className={classes.apiDetails}>
                    <Typography gutterBottom variant='headline' component='h2'>
                        {api.name}
                    </Typography>
                    <Grid container>
                        <Grid item md={6}>
                            <FormattedMessage id='by' defaultMessage='By' />:
                            <Typography className={classes.providerText} variant='body2' gutterBottom>
                                {api.provider}
                            </Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage id='context' defaultMessage='Context' />:
                            <Typography variant='body2' gutterBottom>
                                {api.context}
                            </Typography>
                        </Grid>
                        <Grid item md={6}>
                            <FormattedMessage id='version' defaultMessage='Version' />:
                            <Typography variant='body2'>{api.version}</Typography>
                        </Grid>
                    </Grid>
                </CardContent>
                <CardActions className={classes.apiActions}>
                    <Chip label={api.lifeCycleStatus} color='default' />
                    <DeleteApiButton onClick={this.handleApiDelete} api={api} />
                    {loading && <CircularProgress className={classes.deleteProgress} />}
                </CardActions>
            </Card>
        );
    }
}

APIThumb.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    updateAPIsList: PropTypes.func.isRequired,
};

export default withStyles(styles)(APIThumb);
