import React from 'react';
import OwnerForm from './OwnerForm';
import { updateOwner } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession, useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Owner = (_) => {
    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulUpdate = async (user) => {
		sessionUpdater({user});
		navigate("/bunnies");
	}

    const submitHandler = (values) => {
        clearNotifications();

        const owner = {
            name: values.name,
            userName: values.userName,
            publicOwner: values.publicOwner,
            email: values.email,
            address: values.address,
            phone: values.phone,
            breederName: values.breederName,
            breederEmail: values.breederEmail,
            publicBreeder: values.publicBreeder
        }

        return updateOwner(session.user.id, owner, onSuccessfulUpdate, notifyError);
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    return (
        <OwnerForm submitHandler={submitHandler} cancelHandler={cancelHandler}/>
    );
}

export default Owner;
