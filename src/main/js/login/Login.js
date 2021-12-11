import React from 'react';
import { useNavigate } from "react-router-dom";
import LoginForm from './LoginForm';
import { loginUser } from '../utils/api';
import { useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Login = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();
    const [_, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulLogin = (user) => {
        sessionUpdater({user});
		navigate("/approval");
    }

    const submitHandler = (values) => {
        clearNotifications();
        return loginUser(values.user, values.pwd, onSuccessfulLogin, notifyError);
    }

    return (
        <LoginForm submitHandler={submitHandler}/>
    );
}

export default Login;