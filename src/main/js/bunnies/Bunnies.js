import React, { useState, useEffect } from 'react';
import BunniesForm from './BunniesForm';
import { useSession } from "../hooks/SessionContext";
import { getBunnies, deleteBunny } from "../utils/api";
import Spinner from "react-bootstrap/Spinner";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Bunnies = (props) => {

    const [bunnies, setBunnies] = useState();

    const session = useSession();
    const [_, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onBunnies = (bunnies) => {
        setBunnies(bunnies.bunnies);
    }

    const onRemovedBunny = () => {
        setBunnies(undefined);
    }

	useEffect(() => {
		if (bunnies === undefined) {
			getBunnies(session.user.id, onBunnies, notifyError);
		}
	});
	
	const removeBunny = async (id) => {
		clearNotifications();
        await deleteBunny(session.user.id, id, onRemovedBunny, notifyError);		
	}
	
    return (
		bunnies === undefined
		? <Spinner animation="border" role="status"> <span className="visually-hidden">laddar innehåll...</span> </Spinner>
		: <BunniesForm bunnies={bunnies} removeBunny={removeBunny}/>
    );
}

export default Bunnies;