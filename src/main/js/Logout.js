import React from 'react';
import { logoutUser } from './utils/api';
import { useNavigate } from "react-router-dom";

const Logout = (props) => {
    const navigate = useNavigate();
    const setError = (msg) => props.setNotification({type: "danger", msg: msg});

    const onSuccessfulLogout = () => {
        props.setSession({noSession : true});
		navigate("/login");
    }

    const logoutHandler = (e) => {
        e.preventDefault();
        logoutUser(onSuccessfulLogout, setError);
    }
    
    if (props.session.noSession) {
        return <div/>;
    }
    else {
        return (
            <div>
                <button className="btn btn-primary" onClick={logoutHandler}>Logga ut</button>
            </div>
            );
        } 
}

export default Logout;