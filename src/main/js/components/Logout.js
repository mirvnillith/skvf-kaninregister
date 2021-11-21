import React from 'react';
import {logoutUser} from '../utils/api';
import {useSession, useSessionUpdater} from "../utils/SessionContext";

const Logout = (props) => {
    const session = useSession();
    const sessionUpdater = useSessionUpdater();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);

    const onSuccessfulLogout = () => {
        sessionUpdater(undefined);
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
        </div>);
}

export default Logout;