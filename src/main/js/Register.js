import React, { useState } from 'react';
import RegisterForm from './common/RegisterForm';
import { createOwner, updateOwner, loginUser } from './utils/api';
import { useNavigate } from "react-router-dom";


const Register = (props) => {
    const navigate = useNavigate();
    const setError = (msg) => props.setNotification({type: "danger", msg: msg});
    const createRegistrationSuccessHandler = (userName, pwd) => {
        const onSuccessfulLogin = (user) => {
            props.setSession({...user, noSession: false});
			navigate("/bunnies");
        }

        return async (_) => {
            await loginUser(userName, pwd, onSuccessfulLogin, setError)
        }
    }

    const submitForm = async (user, pwd) => {
        await createOwner(user, pwd, createRegistrationSuccessHandler(user, pwd), setError)
    }

    return (
        <RegisterForm submitForm={submitForm} />
    );
}

export default Register;
