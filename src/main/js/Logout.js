import React from 'react';
import { logoutUser } from './utils/api';
import { useNavigate } from "react-router-dom";
import {useSession, useSessionUpdater} from "./hooks/SessionContext";

const Logout = (props) => {
    const navigate = useNavigate();
    const session = useSession();
    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);

    const onSuccessfulLogout = () => {
        sessionUpdater(undefined);
		navigate("/login");
    }

    const logoutHandler = (e) => {
        e.preventDefault();
        logoutUser(onSuccessfulLogout, setError);
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