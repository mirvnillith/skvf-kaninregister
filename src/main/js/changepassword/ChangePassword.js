import React from 'react';
import { changeUserPassword } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import ChangePasswordForm from "./ChangePasswordForm";

const ChangePassword = (_) => {
    const navigate = useNavigate();
    const session = useSession();
    const [__, { notifyError, notifySuccess, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulUpdate = async () => {
        notifySuccess("Lösenord uppdaterat!")
        navigate("/bunnies");
    }

    const submitHandler = (values) => {
        clearNotifications();
        return changeUserPassword(session.user.id, values.currentPassword, values.newPassword, onSuccessfulUpdate, notifyError);
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/owner");
    }

    return (
        <ChangePasswordForm submitHandler={submitHandler} cancelHandler={cancelHandler} />
    );
}

export default ChangePassword;
