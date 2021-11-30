import React from 'react';
import { useNavigate } from "react-router-dom";

const Bunny = (props) => {
	
	

	const picture = props.bunny.picture
					? props.bunny.picture
					: '/assets/bunny.jpg'
	return (
	<div>
		<div className="bunny-row extra-large">
			<a href={picture} target="_blank"><img className="bunny-picture" width='100' height='100' src={picture}/></a>
			&nbsp;&nbsp;&nbsp;
			{props.bunny.name}
		</div>
		<div className="row spacer"/>
	</div>
	);
}

const BunnyList = (props) => {
	
    return (
	<div>
		{ props.bunnies.map(bunny => {
        	return (<Bunny bunny={bunny} key={bunny.id}/>)
        })}
	</div>
    );
}

const BunniesForm = (props) => {
	
	const navigate = useNavigate();
		
    return (
	<div>
		<div className="row">
			<div className="col-md-12 align-self-center p-4">
				<h2 className="text-center dark"> Mina kaniner </h2>
				<button className="btn btn-primary float-end" onClick={() => navigate("/bunny")}>Jag har en ny märkt kanin</button>
			</div>
			<BunnyList bunnies={props.bunnies} />
		</div>
	</div>
    );
}

export default BunniesForm;