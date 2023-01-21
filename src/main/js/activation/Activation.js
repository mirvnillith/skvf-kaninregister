import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Navigate } from "react-router-dom";
import ActivationForm from './ActivationForm';
import { activateOwner, loginUser, isOwnerActivated } from '../utils/api';
import { useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Activation = (props) => {
    const navigate = useNavigate();
    const params = useParams();
    const sessionUpdater = useSessionUpdater();
    const [_, { notifyError, notifySuccess, clearNotifications } ] = useNotificationUpdater();
    const [loading, setLoading] = useState(false);
    const [activated, setActivated] = useState();
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
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
 				notifySuccess("Konto aktiverat, loggar in ...");
          		await loginUser(userName, pwd, onSuccessfulLogin, onFailedLogin);
			} else {
 				notifySuccess("Konto aktiverat");
				navigate("/login");
			}
        }
    }

    const onResult = async (status) => {
        setActivated(status.activated);
    }

	useEffect(() => {
		if (!loading && activated === undefined) {
			setLoading(true);
			isOwnerActivated(params.ownerId, onResult, notifyError);
		}
	});
	
    const submitHandler = (values) => {
        clearNotifications();
        return activateOwner(params.ownerId, values.user, values.pwd, createActivationSuccessHandler(values.user, values.pwd, values.autoLogin), notifyError);
    }

    return activated === undefined
    		? <Spinner animation="border" role="status"> <span className="visually-hidden">laddar innehåll...</span> </Spinner>
    		: activated
    		  ? <Navigate to="/login" />
    		  : <ActivationForm submitHandler={submitHandler}/>
    	;
}

export default Activation;
