import React from 'react';
import { useNavigate } from "react-router-dom";

const BunniesForm = () => {
	
	const navigate = useNavigate();
	
    return (
	<div>
		<div className="row">
			<div className="col-md-12 align-self-center p-4">
				<h2 className="text-center dark"> Mina Kaniner </h2>
				<button className="btn btn-primary float-end" onClick={() => navigate("/bunny")}>Jag har märkt en ny kanin</button>
			</div>
		</div>
	</div>
    );
}

export default BunniesForm;