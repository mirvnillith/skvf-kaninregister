import React, {useState, useEffect} from 'react';
import Spinner from 'react-bootstrap/Spinner'
import { approve } from '../utils/api';
import ApprovalFailed from "./ApprovalFailed";
import { useSession, useSessionUpdater } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import { Navigate } from "react-router-dom";
import ApprovalOngoing from "./ApprovalOngoing";

const checkApprove = (props) => {
    const session = useSession();
    const [loading, setLoading] = useState(true);
    const [approved, setApproved] = useState(false);
    const [approvalOngoing, setApprovalOngoing] = useState(undefined);
    const [_, { notifyError } ] = useNotificationUpdater();

    const setError = (message) => {
        setLoading(false);
        notifyError(message)
    }

    const approvedOwnerHandler = () => {
        setLoading(false);
        setApproved(true);
        setApprovalOngoing(undefined);
    }

    const approvalFailedHandler = () => {
        setLoading(false);
        setApproved(false);
        setApprovalOngoing(undefined);
    }

    const approvalOngoingHandler = (location) => {
        setLoading(false);
        setApproved(false);
        setApprovalOngoing(location);
    }

    useEffect(() => {
        approve(session.user.id, approvedOwnerHandler, approvalFailedHandler, approvalOngoingHandler, setError);
    }, []);

    return (<div>
            {loading
                ? <Spinner animation="border" role="status">
                    <span className="visually-hidden">laddar innehåll...</span>
                </Spinner>
                : approved
                    ? <Navigate to="/bunnies"/>
                    : approvalOngoing !== undefined
                        ? <ApprovalOngoing approvalOngoing={approvalOngoing}
                                           approvedOwnerHandler={approvedOwnerHandler}
                                           approvalFailedHandler={approvalFailedHandler}
                                           approvalOngoingHandler={approvalOngoingHandler}
                                           setError={setError}/>
                        : <ApprovalFailed setNotification={props.setNotification}/>}
        </div>
    );
}

export default checkApprove;