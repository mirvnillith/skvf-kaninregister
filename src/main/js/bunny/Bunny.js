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

    const submitHandler = (values) => {
        clearNotifications();

        const bunny = {
			id: params.bunnyId,
            name: values.name,
            chip: values.chip,
            leftEar:  values.leftEar,
            rightEar: values.rightEar,
            ring: values.ring,
            picture: values.picture,
            gender: values.gender ? values.gender : null,
            neutered: values.neutered !== undefined ? values.neutered : null,
            birthDate: values.birthDate,
            race: values.race,
            coat: values.coat,
            colourMarkings: values.colourMarkings,
            features: values.features
        }

		if (bunny.id) {
        	return updateBunny(session.user.id, bunny, successfulSave, notifyError);			
		} else {
			if (values.ownerBreeder) bunny.breeder = session.user.id;
        	return createBunny(session.user.id, bunny, successfulSave, notifyError);
		}
    }

    const cancelHandler = async () => {
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
        : <BunnyForm bunny={bunny} submitHandler={submitHandler} cancelHandler={cancelHandler}/>
    );
}

export default Bunny;
