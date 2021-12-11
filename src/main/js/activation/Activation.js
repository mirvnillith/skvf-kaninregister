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
    const [_, { notifyError, notifySuccess, clearNotifications } ] = useNotificationUpdater();
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/approval");
        }

        const onFailedLogin = (msg) => {
			notifySuccess("Aktiveringen lyckades");
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

    const submitHandler = (values) => {
        clearNotifications();
        return activateOwner(params.ownerId, values.user, values.pwd, createActivationSuccessHandler(values.user, values.pwd, values.autoLogin), notifyError);
    }

    return (
        <ActivationForm submitHandler={submitHandler}/>
    );
}

export default Activation;
