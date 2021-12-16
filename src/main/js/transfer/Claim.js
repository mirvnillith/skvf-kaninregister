import React from 'react';
import { useNavigate } from "react-router-dom";
import ClaimForm from './ClaimForm';
import { claimBunny } from '../utils/api';
import { useNotificationUpdater } from "../hooks/NotificationContext";
import { useSession} from "../hooks/SessionContext";

const Claim = (_) => {
	const navigate = useNavigate();
	const session = useSession();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulClaim = () => {
        navigate("/bunnies");
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    const submitHandler = (values) => {
        clearNotifications();
        return claimBunny(session.user.id, values.token, onSuccessfulClaim, notifyError);
    }

    return (
        	<ClaimForm submitHandler={submitHandler} cancelHandler={cancelHandler}/>
    );
}

export default Claim;