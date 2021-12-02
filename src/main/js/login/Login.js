import React from 'react';
import { useNavigate } from "react-router-dom";
import LoginForm from './LoginForm';
import { loginUser } from '../utils/api';
import { useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Login = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();
    const notificationUpdater = useNotificationUpdater();

    const notifyError = (message) => notificationUpdater([{type: "danger", msg: message}]);
    const clearNotifications = () => notificationUpdater([]);

    const onSuccessfulLogin = (user) => {
        sessionUpdater({user});
		navigate("/approval");
    }

    const submitForm = async (user, pwd) => {
        clearNotifications();
        await loginUser(user, pwd, onSuccessfulLogin, notifyError);
    }

    return (
        <LoginForm submitForm={submitForm}/>
    );
}

export default Login;