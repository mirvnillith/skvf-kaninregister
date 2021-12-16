import React, { useState } from 'react';
import { useNavigate, useParams } from "react-router-dom";
import TransferForm from './TransferForm';
import TokenForm from './TokenForm';
import { transferBunny } from '../utils/api';
import { useNotificationUpdater } from "../hooks/NotificationContext";
import { useSession} from "../hooks/SessionContext";

const Transfer = (_) => {
	const [token, setToken] = useState();
	const params = useParams();
	const session = useSession();
	const navigate = useNavigate();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulTransfer = (transfer) => {
		setToken(transfer.claimToken)
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    const submitHandler = (_) => {
        clearNotifications();
        return transferBunny(session.user.id, params.bunnyId, onSuccessfulTransfer, notifyError);
    }

    return (token === undefined
        	?	<TransferForm submitHandler={submitHandler} cancelHandler={cancelHandler}/>
			:	<TokenForm token={token}/>
    );
}

export default Transfer;