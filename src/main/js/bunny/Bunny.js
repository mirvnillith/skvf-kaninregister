import React from 'react';
import BunnyForm from './BunnyForm';
import { createBunny } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession} from "../utils/SessionContext";


const Bunny = (props) => {
    const navigate = useNavigate();
    const session = useSession();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);
    const clearPreviousErrors = () => props.setNotification([]);
    const successfulCreation = () => navigate("/bunnies");

    const submitForm = async (bunny) => {
        clearPreviousErrors();
        await createBunny(session.user.id, bunny, successfulCreation, setError);
    }

    return (
        <BunnyForm submitForm={submitForm} />
    );
}

export default Bunny;
