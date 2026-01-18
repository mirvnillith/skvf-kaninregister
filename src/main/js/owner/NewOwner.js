import React from 'react';
import {useNavigate} from "react-router-dom";
import {useSession} from "../hooks/SessionContext";

const NewOwner = (props) => {
	const navigate = useNavigate();
	const session = useSession();

    return (
		<div>
			{session &&
				session.user &&
				session.user.name == 'Ny' &&
				<button className="w-100 btn btn-info" onClick={() => navigate('/owner')}>
					Hej, du verkar ny här! Fyll i namn och kontaktuppgifter så din kanin hittar hem.
				</button>
			}
		</div>
	);
}

export default NewOwner;