import React from 'react';
import { useParams } from "react-router-dom";
import SignOfflineForm from './SignOfflineForm';
import { signOffline } from '../utils/api';
import { useNotificationUpdater } from "../hooks/NotificationContext";

const SignOffline = (_) => {
    const params = useParams();
    const [__, { notifyError, notifySuccess, notifyInfo, clearNotifications } ] = useNotificationUpdater();
	
    const onSigned = (success) => {
		if (success)
			notifySuccess("Lyckad signering fejkad!");
		else
			notifySuccess("Misslyckad signering fejkad!");
			
		notifyInfo("Stäng nu denna sidan och gå tillbaka till där du tryckte på länken");
    }

    const submitHandler = (values) => {
        clearNotifications();
        return signOffline(params.token, values, () => onSigned(values.success), notifyError);
    }

    return (
        <SignOfflineForm submitHandler={submitHandler}/>
    );
}

export default SignOffline;
