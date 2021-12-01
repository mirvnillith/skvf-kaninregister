import React from 'react';
import { useNavigate, useParams } from "react-router-dom";
import ActivationForm from './ActivationForm';
import { activateOwner, loginUser } from '../utils/api';
import { useSessionUpdater } from "../hooks/SessionContext";

const Activation = (props) => {
    const navigate = useNavigate();
    const params = useParams();

    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);
    const clearPreviousErrors = () => props.setNotification([]);
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			navigate("/approval");
        }

        return async (_) => {
			if (autoLogin) {
                clearPreviousErrors();
           		await loginUser(userName, pwd, onSuccessfulLogin, setError);
			} else {
				navigate("/login");
			}
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        await activateOwner(params.ownerId, user, pwd, createActivationSuccessHandler(user, pwd, autoLogin), setError);
    }

    return (
        <ActivationForm submitForm={submitForm}/>
    );
}

export default Activation;
