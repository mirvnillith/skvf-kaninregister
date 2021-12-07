import React from 'react';
import { useNavigate } from "react-router-dom";
import {useSession} from "../hooks/SessionContext";

const Edit = (_) => {
    const navigate = useNavigate();
    const session = useSession();

    return (
        <div>
            {session && window.location.pathname.indexOf('/owner') != 0
				?	<button className="btn btn-secondary float-end me-2" onClick={() => navigate('/owner')}> Mitt konto </button> 
				:	null
            }
        </div>
    );
}

export default Edit;