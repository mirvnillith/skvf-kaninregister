import React from 'react';
import BunnyForm from './BunnyForm';
import { createBunny } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession} from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Bunny = (props) => {
    const navigate = useNavigate();
    const session = useSession();
    const notificationUpdater = useNotificationUpdater();

    const notifyError = (message) => notificationUpdater([{type: "danger", msg: message}]);
    const clearNotifications = () => notificationUpdater([]);

    const successfulCreation = () => navigate("/bunnies");

    const submitForm = async (bunny) => {
        clearNotifications();
        await createBunny(session.user.id, bunny, successfulCreation, notifyError());
    }

    return (
        <BunnyForm submitForm={submitForm} />
    );
}

export default Bunny;
