import React from 'react';
import { useNavigate } from "react-router-dom";

const Finder = (_) => {
    const navigate = useNavigate();

    return (
        <div>
            {window.location.pathname.indexOf('/find') !== 0
				?	<button className="btn btn-success w-100 py-2 fw-bold" onClick={(_) => navigate('/find')}>Jag har hittat en märkt kanin</button>
				:	null
            }
        </div>
    );
}

export default Finder;