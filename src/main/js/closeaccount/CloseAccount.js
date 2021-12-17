import React, { useState } from 'react';
import CloseAccountForm from './CloseAccountForm';
import { unapproveOwner, deactivateOwner, deleteOwner } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession, useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const CloseAccount = (_) => {

    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const reportError = (resetForm) => (msg) => {
		notifyError(msg);
		resetForm();
	}

    const successfulUnapprove = (user) => {
		sessionUpdater({user});
		navigate("/owner");
	}
    const unapproveHandler = (resetForm) => {
        clearNotifications();
		return unapproveOwner(session.user.id, successfulUnapprove, reportError(resetForm));
    }

    const successfulDeactivate = (user) => {
		navigate("/activation/" + user.id);
        sessionUpdater(undefined);
	}
    const deactivateHandler = (resetForm) => {
        clearNotifications();
		return deactivateOwner(session.user.id, successfulDeactivate, reportError(resetForm));
    }

    const successfulDelete = () => {
		navigate("/register");
        sessionUpdater(undefined);
	}
    const deleteHandler = (resetForm) => {
        clearNotifications();
		return deleteOwner(session.user.id, successfulDelete, reportError(resetForm));
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/owner");
    }

    return <CloseAccountForm unapprove={unapproveHandler} deactivate={deactivateHandler} delete={deleteHandler} cancel={cancelHandler}/>;
}

export default CloseAccount;
