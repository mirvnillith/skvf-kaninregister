import React, {useState, useEffect} from 'react';
import Spinner from 'react-bootstrap/Spinner'
import { approve } from '../utils/api';
import ApprovalFailed from "./ApprovalFailed";
import { useSession, useSessionUpdater } from "../hooks/SessionContext";
import { Navigate } from "react-router-dom";
import ApprovalOngoing from "./ApprovalOngoing";

const checkApprove = (props) => {
    const session = useSession();
    const sessionUpdater = useSessionUpdater();
    const [loading, setLoading] = useState(true);
    const [approved, setApproved] = useState(false);
    const [approvalOngoing, setApprovalOngoing] = useState(undefined);

    const setError = (msg) => {
        setLoading(false);
        props.setNotification([{type: "danger", msg: msg}]);
    }

    const approvedOwnerHandler = () => {
        setLoading(false);
        sessionUpdater((existingSession) => {
            return {...existingSession, approved: true};
        });
        setApproved(true);
        setApprovalOngoing(undefined);
    }

    const approvalFailedHandler = () => {
        setLoading(false);
        sessionUpdater((existingSession) => {
            return {...existingSession, approved: false};
        });
        setApproved(false);
        setApprovalOngoing(undefined);
    }

    const approvalOngoingHandler = (location) => {
        setLoading(false);
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