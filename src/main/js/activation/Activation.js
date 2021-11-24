import React from 'react';
import ActivationForm from './ActivationForm';
import { activateOwner, loginUser } from '../utils/api';
import { navigate } from 'hookrouter';

const Activation = (props) => {
    const setError = (msg) => props.setNotification({type: "danger", msg: msg});
	
    const createActivationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            props.setSession({...user, noSession: false});
			navigate("/bunnies", true);
        }

        return async (_) => {
			if (autoLogin) {
           		await loginUser(userName, pwd, onSuccessfulLogin, setError);
			} else {
				navigate("/login", true);
			}
        }
    }

    const submitForm = async (user, pwd, autoLogin) => {
        await activateOwner(props.ownerId, user, pwd, createActivationSuccessHandler(user, pwd, autoLogin), setError);
    }

    return (
        <ActivationForm submitForm={submitForm}/>
    );
}

export default Activation;
