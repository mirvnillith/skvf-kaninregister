import React, {useState, useEffect} from 'react';
import Spinner from 'react-bootstrap/Spinner'
import {approve} from '../utils/api';
import ApprovalFailed from "../pages/ApprovalFailed";
import Approved from "../pages/Approved";

const checkApprove = (props) => {
    const [loading, setLoading] = useState(true);
    const [approved, setApproved] = useState(true);

    const setError = (msg) => {
        setLoading(false);
        props.setNotification({type: "danger", msg: msg});
    }

    const approvedOwnerHandler = () => {
        setLoading(false);
        setApproved(true);
    }

    const approvalFailedHandler = () => {
        setLoading(false);
        setApproved(false);
    }

    const approvalOngoingHandler = (location) => {
        setLoading(false);
        //TODO: Use react router-hook instead
        window.location.replace(location);
    }

    //TODO: This fails on F5 in the browser since the id for the user is gone after a refresh though the user still has a session on the backend and a cookie for the session
    useEffect(() => {
        approve(props.session.user.id, approvedOwnerHandler, approvalFailedHandler, approvalOngoingHandler, setError);
    }, []);

    return (<div>
            {loading ?
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">laddar innehåll...</span>
                </Spinner> : approved ?
                    <Approved setNotification={props.setNotification} session={props.session} setSession={props.setSession}/> :
                    <ApprovalFailed setNotification={props.setNotification} session={props.session} setSession={props.setSession}/>}
        </div>
    );
}

export default checkApprove;