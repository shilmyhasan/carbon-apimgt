import React, { Component } from 'react';
import {
    IconButton,
    Popper,
    Paper,
    ClickAwayListener,
    MenuItem,
    MenuList,
    Fade,
    ListItemIcon,
    ListItemText,
    Divider,
} from '@material-ui/core';
import AccountCircle from '@material-ui/icons/AccountCircle';
import NightMode from '@material-ui/icons/Brightness2';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
// import qs from 'qs';
import PropTypes from 'prop-types';

const styles = theme => ({
    profileMenu: {
        zIndex: theme.zIndex.modal + 1,
        paddingTop: '5px',
    },
    userLink: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: theme.typography.fontSize,
        textTransform: 'uppercase',
    },
    accountIcon: {
        marginRight: 10,
    },
});

/**
 * Render the User Avatar with their name inside the Top AppBar component
 *
 * @class Avatar
 * @extends {Component}
 */
class Avatar extends Component {
    /**
     *Creates an instance of Avatar.
     * @param {Object} props @inheritdoc
     * @memberof Avatar
     */
    constructor(props) {
        super(props);
        this.state = {
            openMenu: false,
            profileIcon: null,
        };
        this.toggleMenu = this.toggleMenu.bind(this);
    }

    /**
     *
     * Open and Close (Toggle) Avatar dropdown menu
     * @param {React.SyntheticEvent} event `click` event on Avatar
     * @memberof Avatar
     */
    toggleMenu(event) {
        this.setState({
            openMenu: !this.state.openMenu,
            profileIcon: event.currentTarget,
        });
    }

    /**
     * Do OIDC logout redirection
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    doOIDCLogout = (e) => {
        e.preventDefault();
        window.location = '/publisher-new/services/logout';
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Avatar
     */
    render() {
        const { classes, user } = this.props;
        // const { pathname } = window.location;
        // const params = qs.stringify({
        //     referrer: pathname.split('/').reduce((acc, cv, ci) => (ci <= 1 ? '' : acc + '/' + cv)),
        // });
        const { openMenu, profileIcon } = this.state;
        return (
            <React.Fragment>
                <IconButton
                    aria-owns='profile-menu-appbar'
                    aria-haspopup='true'
                    color='inherit'
                    onClick={this.toggleMenu}
                    className={classes.userLink}
                >
                    <AccountCircle className={classes.accountIcon} /> {user.name}
                </IconButton>
                <Popper className={classes.profileMenu} open={openMenu} anchorEl={profileIcon} transition>
                    {({ TransitionProps }) => (
                        <Fade in={openMenu} {...TransitionProps} id='profile-menu-appbar'>
                            <Paper>
                                <ClickAwayListener onClickAway={this.toggleMenu}>
                                    <MenuList>
                                        <MenuItem onClick={this.toggleMenu}>Profile</MenuItem>
                                        <MenuItem onClick={this.toggleMenu}>My account</MenuItem>
                                        <Link to={{ pathname: '/services/logout' }}>
                                            <MenuItem onClick={this.doOIDCLogout}>Logout</MenuItem>
                                        </Link>

                                        <Divider />
                                        <MenuItem className={classes.menuItem} onClick={this.props.toggleTheme}>
                                            <ListItemText primary='Night Mode' />
                                            <ListItemIcon className={classes.icon}>
                                                <NightMode />
                                            </ListItemIcon>
                                        </MenuItem>
                                    </MenuList>
                                </ClickAwayListener>
                            </Paper>
                        </Fade>
                    )}
                </Popper>
            </React.Fragment>
        );
    }
}
Avatar.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    user: PropTypes.shape({ name: PropTypes.string.isRequired }).isRequired,
    toggleTheme: PropTypes.func.isRequired,
};

export default withStyles(styles)(Avatar);
