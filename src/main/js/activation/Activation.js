import React from 'react';
import ActivationForm from './ActivationForm';
import { activateOwner, loginUser } from '../utils/api';

const Activation = (props) => {
    const setError = (msg) => props.setNotification({type: "danger", msg: msg});
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            props.setSession({...user, noSession: false});
        }

        return async (_) => {
			if (autoLogin) {
           		await loginUser(userName, pwd, onSuccessfulLogin, setError);
			}
			window.location.replace("/");
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        await activateOwner(props.token, user, pwd, createActivationSuccessHandler(user, pwd, autoLogin), setError);
    }

    return (
        <ActivationForm submitForm={submitForm}/>
    );
}

export default Activation;
