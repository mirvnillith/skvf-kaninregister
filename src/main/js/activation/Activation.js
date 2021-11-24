import React from 'react';
import ActivationForm from './ActivationForm';
import { activateOwner, loginUser } from '../utils/api';
import { useNavigate, useParams } from "react-router-dom";

const Activation = (props) => {
    const navigate = useNavigate();
    const params = useParams();

    const setError = (msg) => props.setNotification({type: "danger", msg: msg});
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            props.setSession({...user, noSession: false});
			navigate("/bunnies");
        }

        return async (_) => {
			if (autoLogin) {
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
