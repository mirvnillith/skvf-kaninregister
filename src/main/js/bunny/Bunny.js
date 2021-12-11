import React, { useState, useEffect } from 'react';
import BunnyForm from './BunnyForm';
import { createBunny, getBunny, updateBunny } from '../utils/api';
import { useNavigate, useParams } from "react-router-dom";
import { useSession} from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import Spinner from "react-bootstrap/Spinner";

const Bunny = (_) => {

    const [bunny, setBunny] = useState();

    const navigate = useNavigate();
    const params = useParams();
    const session = useSession();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const successfulSave = () => navigate("/bunnies");

    const submitForm = async (bunny) => {
        clearNotifications();
		if (bunny.id) {
        	await updateBunny(session.user.id, bunny, successfulSave, notifyError);			
		} else {
        	await createBunny(session.user.id, bunny, successfulSave, notifyError);
		}
    }

    const cancelForm = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    const onBunny = (bunny) => {
        setBunny(bunny);
    }

	useEffect(() => {
		if (bunny === undefined) {
			if (params.bunnyId) {
				getBunny(params.bunnyId, onBunny, notifyError);
			} else {
				setBunny({});
			}
		}
	});
	
    return (
		bunny === undefined
		? <Spinner animation="border" role="status"> <span className="visually-hidden">laddar innehåll...</span> </Spinner>
        : <BunnyForm bunny={bunny} submitForm={submitForm} cancelForm={cancelForm}/>
    );
}

export default Bunny;
