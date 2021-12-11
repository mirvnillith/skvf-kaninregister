import React from 'react';
import { logoutUser } from './utils/api';
import { useNavigate } from "react-router-dom";
import {useSession, useSessionUpdater} from "./hooks/SessionContext";
import { useNotificationUpdater } from "./hooks/NotificationContext";
import useFormValidation from "./hooks/FormValidation";

const Logout = (_) => {
    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulLogout = () => {
        sessionUpdater(undefined);
		navigate("/login");
    }

    const logoutHandler = (_) => {
        clearNotifications();
        return logoutUser(onSuccessfulLogout, notifyError);
    }

    const {
        handleSubmit,
        isSubmitting
    } = useFormValidation({}, () => { return {}}, logoutHandler);

    return (
        <div>
            {session ?
                <form onSubmit={handleSubmit} >
                    <button className="btn btn-secondary float-end" disabled={isSubmitting} >
                        { isSubmitting && <span className="spinner-border spinner-border-sm mr-1" /> }
                        Logga ut
                    </button>
                </form> :
                null
            }
        </div>
    );
}

export default Logout;