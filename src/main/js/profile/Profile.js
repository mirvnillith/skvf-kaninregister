import React, { useState, useEffect } from 'react';
import ProfileForm from './ProfileForm';
import { getBunny } from '../utils/api';
import { useParams } from "react-router-dom";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import Spinner from "react-bootstrap/Spinner";

const Profile = (_) => {

    const [loading, setLoading] = useState(false);
    const [bunny, setBunny] = useState();

    const params = useParams();
    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

	useEffect(() => {
		if (!loading && bunny === undefined) {
			setLoading(true);
			clearNotifications();
			if (params.bunnyId) {
				getBunny(params.bunnyId, setBunny, notifyError);
			} else {
				setBunny({});
			}
		}
	});
	
    return (
		bunny === undefined
		? <Spinner animation="border" role="status"> <span className="visually-hidden">laddar innehåll...</span> </Spinner>
        : <ProfileForm bunny={bunny} />
    );
}

export default Profile;
