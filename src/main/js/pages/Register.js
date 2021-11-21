import React, { useState } from 'react';
import RegisterForm from '../forms/RegisterForm';
import { createOwner, updateOwner, loginUser } from '../utils/api';
import { useSessionUpdater} from "../utils/SessionContext";


const Register = (props) => {
    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);
    const clearPreviousErrors = () => props.setNotification([]);

    const createRegistrationSuccessHandler = (userName, pwd) => {
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
        }

        return async (_) => {
            clearPreviousErrors();
            await loginUser(userName, pwd, onSuccessfulLogin, setError)
        }
    }

    const loginHandler = () => props.setView("login");

    const submitForm = async (user, pwd) => {
        clearPreviousErrors();
        await createOwner(user, pwd, createRegistrationSuccessHandler(user, pwd), setError)
    }

    return (
        <RegisterForm submitForm={submitForm} loginHandler={loginHandler} />
    );
}

export default Register;
