import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Redirect, Route, Switch } from 'react-router-dom';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import { addLocaleData, defineMessages, IntlProvider } from 'react-intl';
import qs from 'qs';
import Log from 'log4javascript';
import Utils from 'AppData/Utils';
import ConfigManager from 'AppData/ConfigManager';
// import MaterialDesignCustomTheme from 'AppComponents/Shared/CustomTheme';
import { PageNotFound } from 'AppComponents/Base/Errors';
import Apis from 'AppComponents/Apis/Apis';
import Endpoints from 'AppComponents/Endpoints';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import Header from 'AppComponents/Base/Header';
import Avatar from 'AppComponents/Base/Header/avatar/Avatar';

const themes = [];
const darkTheme = createMuiTheme({
    palette: {
        type: 'dark', // Switching the dark mode on is a single property value change.
        background: {
            active: 'rgba(27, 94, 32, 1)',
            navBar: '#29434e',
            container: '#001114',
            paper: '#29434e',
        },
        primary: {
            light: '#315564',
            main: '#002c3a',
            dark: '#000115',
        },
    },
    overrides: {
        MuiButton: {
            textPrimary: {
                color: '#00ffe0',
                '&:hover': {
                    backgroundColor: '##00c1ff36',
                },
            },
        },
    },
});
const lightTheme = createMuiTheme({
    palette: {
        type: 'light', // Switching the dark mode on is a single property value change.
        background: {
            active: 'rgba(165, 214, 167, 1)',
            navBar: '#fafafa',
            container: '#fefefe',
            contentFrame: 'rgba(227, 242, 253, 1)',
        },
        text: {
            brand: 'rgba(255,255,255,1)',
        },
    },
});
themes.push(darkTheme);
themes.push(lightTheme);
// themes.push(createMuiTheme(MaterialDesignCustomTheme));

/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;

/**
 * Language without region code.
 */
const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];

/**
 * Render protected application paths, Implements container presenter pattern
 */
export default class Protected extends Component {
    /**
     * Creates an instance of Protected.
     * @param {any} props @inheritDoc
     * @memberof Protected
     */
    constructor(props) {
        super(props);
        this.state = {
            themeIndex: 1,
            messages: {},
        };
        this.environments = [];
        this.toggleTheme = this.toggleTheme.bind(this);
        this.loadLocale = this.loadLocale.bind(this);
    }

    /**
     * Initialize i18n.
     */
    componentWillMount() {
        const locale = languageWithoutRegionCode || language || 'en';
        this.loadLocale(locale);
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        const storedThemeIndex = localStorage.getItem('themeIndex');
        if (storedThemeIndex) {
            this.setState({ themeIndex: parseInt(storedThemeIndex, 10) });
        }
        ConfigManager.getConfigs()
            .environments.then((response) => {
                this.environments = response.data.environments;
                // this.handleEnvironmentQueryParam(); todo: do we really need to handle environment query params here ?
            })
            .catch((error) => {
                Log.error('Error while receiving environment configurations : ', error);
            });
    }

    /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     */
    loadLocale(locale = 'en') {
        fetch(`${Utils.CONST.CONTEXT_PATH}/public/app/locales/${locale}.json`)
            .then(resp => resp.json())
            .then((data) => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                addLocaleData(require(`react-intl/locale-data/${locale}`));
                this.setState({ messages: defineMessages(data) });
            });
    }

    /**
     * Change the theme index incrementally
     */
    toggleTheme() {
        this.setState(
            ({ themeIndex }) => ({ themeIndex: (themeIndex + 1) % 2 }),
            () => localStorage.setItem('themeIndex', this.state.themeIndex),
        );
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const user = AuthManager.getUser();
        const header = <Header avatar={<Avatar toggleTheme={this.toggleTheme} />} user={user} />;

        if (!user) {
            const { pathname } = window.location;
            const params = qs.stringify({
                referrer: pathname.split('/').reduce((acc, cv, ci) => (ci <= 1 ? '' : acc + '/' + cv)),
            });
            return (
                <Switch>
                    <Redirect to={{ pathname: '/login', search: params }} />
                </Switch>
            );
        }
        return (
            <IntlProvider locale={language} messages={this.state.messages}>
                <MuiThemeProvider theme={themes[this.state.themeIndex]}>
                    <Base header={header}>
                        <Switch>
                            <Redirect exact from='/' to='/apis' />
                            <Route path='/apis' component={Apis} />
                            <Route path='/endpoints' component={Endpoints} />
                            <Route component={PageNotFound} />
                        </Switch>
                    </Base>
                </MuiThemeProvider>
            </IntlProvider>
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};
