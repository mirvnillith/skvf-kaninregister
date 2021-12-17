import React from 'react';
import RegisterForm from './RegisterForm';
import { createOwner, loginUser } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSessionUpdater} from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Register = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();
    const [_, { notifyError, notifySuccess, clearNotifications } ] = useNotificationUpdater();

    const createRegistrationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/approval");
        }

        const onFailedLogin = (msg) => {
			notifyError(msg);
			navigate("/login");
        }

        return async (_) => {
			if (autoLogin) {
				notifySuccess("Konto registrerat, loggar in ...");
            	await loginUser(userName, pwd, onSuccessfulLogin, onFailedLogin);
			} else {
				notifySuccess("Konto registrerat");
				navigate("/login");
			}
        }
    }

    const submitHandler = (values) => {
        clearNotifications();
        return createOwner(values.user, values.pwd, createRegistrationSuccessHandler(values.user, values.pwd, values.autoLogin), notifyError)
    }

    return (
        <RegisterForm submitHandler={submitHandler} />
    );
}

export default Register;
