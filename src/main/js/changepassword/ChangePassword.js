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

    const submitForm = async (owner) => {
        clearNotifications();
        await updateOwner(session.user.id, owner, onSuccessfulUpdate, notifyError);
    }

    const cancelForm = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    return (
        <OwnerForm submitForm={submitForm} cancelForm={cancelForm}/>
    );
}

export default Owner;
