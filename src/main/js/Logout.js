import React from 'react';
import { logoutUser } from './utils/api';
import { useNavigate } from "react-router-dom";
import {useSession, useSessionUpdater} from "./hooks/SessionContext";
import { useNotificationUpdater } from "./hooks/NotificationContext";

const Logout = (props) => {
    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const notificationUpdater = useNotificationUpdater();

    const notifyError = (message) => notificationUpdater([{type: "danger", msg: message}]);

    const onSuccessfulLogout = () => {
        sessionUpdater(undefined);
		navigate("/login");
    }

    const logoutHandler = (e) => {
        e.preventDefault();
        logoutUser(onSuccessfulLogout, notifyError);
    }

    return (
        <div>
            {session ?
                <button className="btn btn-primary" onClick={logoutHandler}>Logga ut</button> :
                null
            }
        </div>
    );
}

export default Logout;