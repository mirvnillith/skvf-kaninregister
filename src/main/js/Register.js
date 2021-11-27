import React, { useState } from 'react';
import RegisterForm from './common/RegisterForm';
import { createOwner, updateOwner, loginUser } from './utils/api';
import { useNavigate } from "react-router-dom";
import { useSessionUpdater} from "./utils/SessionContext";


const Register = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);
    const clearPreviousErrors = () => props.setNotification([]);

    const createRegistrationSuccessHandler = (userName, pwd) => {
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/bunnies");
        }

        return async (_) => {
            clearPreviousErrors();
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
