import React from 'react';
import { logoutUser } from './utils/api';
import { useNavigate } from "react-router-dom";
import {useSession, useSessionUpdater} from "./hooks/SessionContext";
import { useNotificationUpdater } from "./hooks/NotificationContext";

const Logout = (_) => {
    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulLogout = () => {
        sessionUpdater(undefined);
		navigate("/login");
    }

    const logoutHandler = (e) => {
        e.preventDefault();
        clearNotifications();
        logoutUser(onSuccessfulLogout, notifyError);
    }

    return (
        <div>
            {session ?
                <button className="btn btn-secondary float-end" onClick={logoutHandler}>Logga ut</button> :
                null
            }
        </div>
    );
}

export default Logout;