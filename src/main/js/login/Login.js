import React from 'react';
import { useNavigate } from "react-router-dom";
import LoginForm from './LoginForm';
import { loginUser } from '../utils/api';
import { useSessionUpdater } from "../utils/SessionContext";

const Login = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);
    const clearPreviousErrors = () => props.setNotification([]);

    const onSuccessfulLogin = (user) => {
        sessionUpdater({user});
		navigate("/bunnies");
    }

    const submitForm = async (user, pwd) => {
		clearPreviousErrors();
        await loginUser(user, pwd, onSuccessfulLogin, setError);
    }

    return (
        <LoginForm submitForm={submitForm}/>
    );
}

export default Login;