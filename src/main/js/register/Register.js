import React from 'react';
import RegisterForm from './RegisterForm';
import { createOwner, loginUser } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSessionUpdater} from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Register = (props) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();
    const [_, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const createRegistrationSuccessHandler = (userName, pwd, autoLogin) => {
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/approval");
        }

        return async (_) => {
            clearNotifications();
			if (autoLogin) {
            	await loginUser(userName, pwd, onSuccessfulLogin, notifyError);
			} else {
				navigate("/login");
			}
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        await createOwner(user, pwd, createRegistrationSuccessHandler(user, pwd, autoLogin), notifyError)
    }

    return (
        <RegisterForm submitForm={submitForm} />
    );
}

export default Register;
