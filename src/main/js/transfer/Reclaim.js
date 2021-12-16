import React from 'react';
import { useNavigate, useParams } from "react-router-dom";
import ReclaimForm from './ReclaimForm';
import { reclaimBunny } from '../utils/api';
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Reclaim = (_) => {
	const params = useParams();
	const navigate = useNavigate();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulReclaim = () => {
        navigate("/bunnies");
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    const submitHandler = (_) => {
        clearNotifications();
        return reclaimBunny(params.bunnyId, onSuccessfulReclaim, notifyError);
    }

    return (
        	<ReclaimForm submitHandler={submitHandler} cancelHandler={cancelHandler}/>
    );
}

export default Reclaim;