import React, {useState, useEffect} from 'react';
import Spinner from 'react-bootstrap/Spinner'
import {approve} from '../utils/api';
import ApprovalFailed from "../pages/ApprovalFailed";
import Approved from "../pages/Approved";
import { useSession } from "../utils/SessionContext";

const checkApprove = (props) => {
    const session = useSession();
    const [loading, setLoading] = useState(true);
    const [approved, setApproved] = useState(true);

    const setError = (msg) => {
        setLoading(false);
        props.setNotification([{type: "danger", msg: msg}]);
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

    useEffect(() => {
        approve(session.user.id, approvedOwnerHandler, approvalFailedHandler, approvalOngoingHandler, setError);
    }, []);

    return (<div>
            {loading ?
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">laddar innehåll...</span>
                </Spinner> : approved ?
                    <Approved setNotification={props.setNotification} /> :
                    <ApprovalFailed setNotification={props.setNotification} />}
        </div>
    );
}

export default checkApprove;