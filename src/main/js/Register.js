import React, { useState } from 'react';
import RegisterForm from './common/RegisterForm';
import { createOwner, updateOwner, loginUser } from './utils/api';


const Register = (props) => {
    const setError = (msg) => props.setNotification({type: "danger", msg: msg});
    const createRegistrationSuccessHandler = (userName, pwd) => {
        const success = (user) => {
            props.setSession({...user, noSession: true});
        }
        return async (_) => {
            await loginUser(userName, pwd, success, setError)
        }
    }
    const loginHandler = () => props.setView("login");

    const submitForm = async (user, pwd) => {
        await createOwner(user, pwd, createRegistrationSuccessHandler(user, pwd), setError)
    }

    return (
        <RegisterForm submitForm={submitForm} loginHandler={loginHandler} />
    );
}

export default Register;
