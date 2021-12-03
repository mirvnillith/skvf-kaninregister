import React from 'react';
import BunnyForm from './BunnyForm';
import { createBunny } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession} from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const Bunny = (props) => {
    const navigate = useNavigate();
    const session = useSession();
    const [_, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const successfulCreation = () => navigate("/bunnies");

    const submitForm = async (bunny) => {
        clearNotifications();
        await createBunny(session.user.id, bunny, successfulCreation, notifyError);
    }

    const cancelForm = async () => {
        clearNotifications();
        navigate("/bunnies");
    }

    return (
        <BunnyForm submitForm={submitForm} cancelForm={cancelForm}/>
    );
}

export default Bunny;
