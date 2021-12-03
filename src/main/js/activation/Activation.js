import React from 'react';
import { useNavigate, useParams } from "react-router-dom";
import ActivationForm from './ActivationForm';
import { activateOwner, loginUser } from '../utils/api';
import { useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Activation = (props) => {
    const navigate = useNavigate();
    const params = useParams();
    const sessionUpdater = useSessionUpdater();
    const [_, { notifyError, clearNotifications } ] = useNotificationUpdater();
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/approval");
        }

        return async (_) => {
			if (autoLogin) {
                clearNotifications();
           		await loginUser(userName, pwd, onSuccessfulLogin, notifyError);
			} else {
				navigate("/login");
			}
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        await activateOwner(params.ownerId, user, pwd, createActivationSuccessHandler(user, pwd, autoLogin), notifyError);
    }

    return (
        <ActivationForm submitForm={submitForm}/>
    );
}

export default Activation;
