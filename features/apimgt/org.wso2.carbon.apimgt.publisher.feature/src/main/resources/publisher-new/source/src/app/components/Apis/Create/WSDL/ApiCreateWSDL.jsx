import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import APIInputForm from 'AppComponents/Apis/Create/Endpoint/APIInputForm';
import Progress from 'AppComponents/Shared/Progress';

import ProvideWSDL from './Steps/ProvideWSDL';
import BindingInfo from './BindingInfo';

const styles = theme => ({
    instructions: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    root: {
        flexGrow: 1,
        marginLeft: 0,
        marginTop: 0,
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit*2,
        width: theme.custom.contentAreaWidth,
    },
    buttonProgress: {
        position: 'relative',
        marginTop: theme.spacing.unit * 5,
        marginLeft: theme.spacing.unit * 6.25,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    buttonSection: {
        paddingTop: theme.spacing.unit * 2,
    },
    subTitle: {
        color: theme.palette.grey[500],
    },
    stepper: {
        paddingLeft: 0,
        marginLeft: 0,
        width: 400,
    },
});

function getSteps() {
    return [<FormattedMessage id='select.wsdl' defaultMessage='Select WSDL' />, <FormattedMessage id='create.api' defaultMessage='Create API' />];
}

function isEmpty(obj) {
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) return false;
    }
    return true;
}

class APICreateWSDL extends React.Component {
    /**
     * Creates an instance of ApiCreateWSDL.
     * @param {any} props @inheritDoc
     * @memberof ApiCreateWSDL
     */
    constructor(props) {
        super(props);
        this.state = {
            doValidate: false,
            wsdlBean: {},
            activeStep: 0,
            api: new API('', 'v1.0.0'),
            loading: false,
            valid: {
                wsdlUrl: { empty: false, invalidUrl: false },
                wsdlFile: { empty: false, invalidFile: false },
                name: { empty: false, alreadyExists: false },
                context: { empty: false, alreadyExists: false },
                version: { empty: false },
                endpoint: { empty: false },
            },
        };
        this.updateWSDLBean = this.updateWSDLBean.bind(this);
        this.updateApiInputs = this.updateApiInputs.bind(this);
        this.createWSDLAPI = this.createWSDLAPI.bind(this);
        this.updateFileErrors = this.updateFileErrors.bind(this);
        this.provideWSDL = null;
    }

    /**
     * Check the WSDL file or URL validity through REST API
     * @param {Object} wsdlBean Bean object holding WSDL file/url info
     * @memberof ApiCreateWSDL
     */
    updateWSDLBean(wsdlBean) {
        console.info(wsdlBean);
        this.setState({
            wsdlBean,
        });
    }

    /**
     * Update user inputs in the form with onChange event trigger
     * @param {React.SyntheticEvent} event Event containing user action
     * @memberof ApiCreateWSDL
     */
    updateApiInputs({ target }) {
        const { name, value } = target;
        this.setState(({ api, valid }) => {
            const changes = api;
            if (name === 'endpoint') {
                changes[name] = [
                    {
                        inline: {
                            name: `${api.name}_inline_prod`,
                            endpointConfig: {
                                list: [
                                    {
                                        url: value,
                                        timeout: '1000',
                                    },
                                ],
                                endpointType: 'SINGLE',
                            },
                            type: 'soap',
                            endpointSecurity: {
                                enabled: false,
                            },
                        },
                        type: 'Production',
                    },
                    {
                        inline: {
                            name: `${api.name}_inline_sandbx`,
                            endpointConfig: {
                                list: [
                                    {
                                        url: value,
                                        timeout: '1000',
                                    },
                                ],
                                endpointType: 'SINGLE',
                            },
                            type: 'soap',
                            endpointSecurity: {
                                enabled: false,
                            },
                        },
                        type: 'Sandbox',
                    },
                ];
            } else {
                changes[name] = value;
            }

             // Checking validity.
             const validUpdated = valid;
             validUpdated.name.empty = !api.name;
             validUpdated.context.empty = !api.context;
             validUpdated.version.empty = !api.version;
             validUpdated.endpoint.empty = !api.endpoint;
             // TODO we need to add the already existing error for (context) by doing an api call ( the swagger definition does not contain such api call)
             return { api: changes, valid: validUpdated };
        });
    }

    /**
     * Make API POST call and create send WSDL file or URL
     * @memberof ApiCreateWSDL
     */
    createWSDLAPI() {
        this.setState({ loading: true });
        const newApi = new API();
        const { wsdlBean, api } = this.state;
        const {
            name, version, context, endpoint, implementationType,
        } = api;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const apiAttributes = {
            name,
            version,
            context,
            endpoint,
        };
        const apiData = {
            additionalProperties: JSON.stringify(apiAttributes),
            implementationType,
            [uploadMethod]: wsdlBean[uploadMethod],
        };

        newApi
            .importWSDL(apiData)
            .then((response) => {
                Alert.success(`${name} API Created Successfully.`);
                const uuid = response.obj.id;
                const redirectURL = '/apis/' + uuid + '/overview';
                this.setState({ loading: false });
                this.props.history.push(redirectURL);
            })
            .catch((errorResponse) => {
                this.setState({ loading: false });
                console.error(errorResponse);
                const error = errorResponse.response.obj;
                const messageTxt = 'Error[' + error.code + ']: ' + error.description + ' | ' + error.message + '.';
                Alert.error(messageTxt);
            });
    }
    updateFileErrors(newValid) {
        this.setState({ valid: newValid });
    }
    handleNext = () => {
        const { activeStep, wsdlBean, valid } = this.state;
        let uploadMethod;
        if(this.provideWSDL){
          uploadMethod = this.provideWSDL.getUploadMethod() ;
        } else if (wsdlBean.file) {
          uploadMethod = "file";
        } else {
          uploadMethod = "url";
        }
        const validNew = JSON.parse(JSON.stringify(valid));

        // Handling next ( getting wsdl file/url info and validating)
        if (activeStep === 0) {
            if (isEmpty(wsdlBean)) {
                if (uploadMethod === 'file') {
                    validNew.wsdlFile.empty = true;
                } else {
                    validNew.wsdlUrl.empty = true;
                }
                this.setState({ valid: validNew });
                return;
            } else {
                if (wsdlBean.file && uploadMethod === 'url') {
                    validNew.wsdlUrl.empty = true;
                    this.setState({ valid: validNew });
                    return;
                }
                if (wsdlBean.url && uploadMethod === 'file') {
                    validNew.wsdlFile.empty = true;
                    this.setState({ valid: validNew });
                    return;
                }
            }
            // No errors so let's fill the inputs with the wsdlBean
            if (wsdlBean.info) {
                if (wsdlBean.info.version) {
                    this.updateApiInputs({ target: { name: 'version', value: wsdlBean.info.version } });
                }
                if (wsdlBean.info.endpoints && wsdlBean.info.endpoints.length > 0) {
                    this.updateApiInputs({ target: { name: 'endpoint', value: wsdlBean.info.endpoints[0].location } });
                }
            }
            this.setState({
              activeStep: activeStep + 1,
            });
        } else if (activeStep === 1) { // Handling Finish step ( validating the input fields )
          const { api } = this.state;
          if (!api.name || !api.context || !api.version || !api.endpoint) {
            // Checking the api name,version,context undefined or empty states
            this.setState((oldState) => {
                const { valid, api } = oldState;
                const validUpdated = valid;
                validUpdated.name.empty = !api.name;
                validUpdated.context.empty = !api.context;
                validUpdated.version.empty = !api.version;
                validUpdated.endpoint.empty = !api.endpoint;
                return { valid: validUpdated };
            });
            return;
          }
          this.createWSDLAPI();
        }
    };

    handleBack = () => {
        this.setState(state => ({
            activeStep: state.activeStep - 1,
        }));
    };

    handleReset = () => {
        this.setState({
            activeStep: 0,
        });
    };

    render() {
        const { classes } = this.props;
        const steps = getSteps();
        const {
            doValidate, activeStep, wsdlBean, api, loading, valid,
        } = this.state;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const provideWSDLProps = {
            uploadMethod,
            [uploadMethod]: wsdlBean[uploadMethod],
            updateWSDLBean: this.updateWSDLBean,
            validate: doValidate,
            valid,
            updateFileErrors: this.updateFileErrors,
        };
        return (
            <Grid container spacing={24} className={classes.root}>
                <Grid item xs={12} xl={6}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage id='design.a.new.rest.api.using.wsdl' defaultMessage='Design a new REST API using WSDL' />
                        </Typography>
                    </div>
                    <Stepper activeStep={activeStep} className={classes.stepper}>
                        {steps.map((label, index) => {
                            const props = {};
                            const labelProps = {};

                            return (
                                <Step key={label} {...props}>
                                    <StepLabel {...labelProps}>{label}</StepLabel>
                                </Step>
                            );
                        })}
                    </Stepper>
                    <div>
                        {activeStep === 0 && (
                            <ProvideWSDL
                                {...provideWSDLProps}
                                innerRef={(instance) => {
                                    this.provideWSDL = instance;
                                }}
                            />
                        )}
                        {activeStep === 1 && (
                            <React.Fragment>
                                <APIInputForm api={api} handleInputChange={this.updateApiInputs} valid={valid} />
                                <BindingInfo updateApiInputs={this.updateApiInputs} wsdlBean={wsdlBean} classes={classes} api={api} />
                            </React.Fragment>
                        )}
                    </div>
                    <div>
                        {activeStep === steps.length ? (
                            <div>
                                <Typography className={classes.instructions}>All steps completed - you&quot;re finished</Typography>
                                <Button onClick={this.handleReset} className={classes.button}>
                                    Reset
                                </Button>
                            </div>
                        ) : (
                            <div>
                                <div>
                                    <Button disabled={activeStep === 0} onClick={this.handleBack} className={classes.button}>
                                        Back
                                    </Button>
                                    <Button variant='contained' color='primary' onClick={this.handleNext} className={classes.button} disabled={(valid.wsdlFile.invalidFile && uploadMethod === 'file') || (valid.wsdlUrl.invalidUrl && uploadMethod === 'url')}>
                                        {activeStep === steps.length - 1 ? 'Finish' : <FormattedMessage id='next' defaultMessage='Next' />}
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </Grid>
            </Grid>
        );
    }
}

APICreateWSDL.propTypes = {
    classes: PropTypes.object,
};

export default withStyles(styles)(APICreateWSDL);
