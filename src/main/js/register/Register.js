import React from 'react';
import RegisterForm from './RegisterForm';
import { createOwner, loginUser } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSessionUpdater} from "../utils/SessionContext";


const Register = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);
    const clearPreviousErrors = () => props.setNotification([]);

    const createRegistrationSuccessHandler = (userName, pwd, autoLogin) => {
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/bunnies");
        }

        return async (_) => {
            clearPreviousErrors();
			if (autoLogin) {
            	await loginUser(userName, pwd, onSuccessfulLogin, setError);
			} else {
				navigate("/login");
			}
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        await createOwner(user, pwd, createRegistrationSuccessHandler(user, pwd, autoLogin), setError)
    }

    return (
        <RegisterForm submitForm={submitForm} />
    );
}

export default Register;
