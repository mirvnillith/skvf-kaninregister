import React from 'react';
import { logoutUser } from './utils/api';
import { useNavigate } from "react-router-dom";
import {useSession, useSessionUpdater} from "./hooks/SessionContext";
import { useNotificationUpdater } from "./hooks/NotificationContext";

const Logout = (props) => {
    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const [_, { notifyError } ] = useNotificationUpdater();

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
                <button className="btn btn-primary float-end" onClick={logoutHandler}>Logga ut</button> :
                null
            }
        </div>
    );
}

export default Logout;