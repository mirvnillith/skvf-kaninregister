import React, { useState, useEffect } from 'react';
import BunniesForm from './BunniesForm';
import { useSession } from "../hooks/SessionContext";
import { getBunnies, deleteBunny } from "../utils/api";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import Spinner from "react-bootstrap/Spinner";

const Bunnies = (_) => {

    const [bunnies, setBunnies] = useState();

    const session = useSession();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onBunnies = (bunnies) => {
        setBunnies(bunnies.bunnies);
    }

    const onBunniesError = (msg) => {
        setBunnies([]);
		notifyError(msg);
    }

	const changeOrder = (newOrderEvent) => {
		session.order = newOrderEvent.target.value;
		setBunnies(undefined);
	}
	
    const onRemovedBunny = () => {
        setBunnies(undefined);
    }

	useEffect(() => {
		if (bunnies === undefined) {
			if (!session.order) {
				session.order = "NAME_ASC"
			}
			getBunnies(session.user.id, session.order, onBunnies, onBunniesError);
		}
	});
	
	const removeBunny = async (id) => {
		clearNotifications();
        await deleteBunny(session.user.id, id, onRemovedBunny, notifyError);		
	}
	
    return <BunniesForm order={session.order} changeOrder={changeOrder} bunnies={bunnies} removeBunny={removeBunny}/>;
}

export default Bunnies;