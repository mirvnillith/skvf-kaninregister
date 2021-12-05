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
			notifySuccess("Registrering lyckades");
			notifyError(msg);
			navigate("/login");
        }

        return async (_) => {
			if (autoLogin) {
            	await loginUser(userName, pwd, onSuccessfulLogin, onFailedLogin);
			} else {
				navigate("/login");
			}
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        clearNotifications();
        await createOwner(user, pwd, createRegistrationSuccessHandler(user, pwd, autoLogin), notifyError)
    }

    return (
        <RegisterForm submitForm={submitForm} />
    );
}

export default Register;
