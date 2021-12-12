import React, {useState, useEffect} from 'react';
import Spinner from 'react-bootstrap/Spinner'
import { approve } from '../utils/api';
import ApprovalFailed from "./ApprovalFailed";
import { useSession } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import { Navigate } from "react-router-dom";
import ApprovalOngoing from "./ApprovalOngoing";

const checkApprove = (_) => {
    const session = useSession();
    const [loading, setLoading] = useState(true);
    const [approved, setApproved] = useState(session.user.approved);
    const [approvalOngoing, setApprovalOngoing] = useState(undefined);
    const [__, { notifyError } ] = useNotificationUpdater();

    const setError = (message) => {
        notifyError(message)
        setLoading(false);
    }

    const approvedOwnerHandler = () => {
        session.user.approved = true;
		setApproved(true);
        setApprovalOngoing(undefined);
        setLoading(false);
    }

    const approvalFailedHandler = () => {
		setApproved(false);
        setApprovalOngoing(undefined);
        setLoading(false);
    }

    const approvalOngoingHandler = (location) => {
        setApprovalOngoing(location);
        setLoading(false);
    }

    useEffect(() => {
        if (!approved) {
			approve(session.user.id, approvedOwnerHandler, approvalFailedHandler, approvalOngoingHandler, setError);
		}
    }, []);

    return (approved
		? <Navigate to="/bunnies" replace />
		: <div>
            {loading
                ? <Spinner animation="border" role="status">
                    <span className="visually-hidden">laddar innehåll...</span>
                </Spinner>
                : approved 
					? <Navigate to="/bunnies" replace />
					: approvalOngoing !== undefined
                        ? <ApprovalOngoing approvalOngoing={approvalOngoing}
                                           approvedOwnerHandler={approvedOwnerHandler}
                                           approvalFailedHandler={approvalFailedHandler}
                                           approvalOngoingHandler={approvalOngoingHandler}
                                           setError={setError}/>
                        : <ApprovalFailed />}
        </div>
    );
}

export default checkApprove;