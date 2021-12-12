import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import { useSession } from "../hooks/SessionContext";

const Bunny = (props) => {
	
	const navigate = useNavigate();
	
    const [thisConfirm, setThisConfirm] = useState(false);

	const picture = props.bunny.picture
					? props.bunny.picture
					: '/assets/bunny.jpg';
			
    const removeHandler = async (_) => {
		props.setRemove(true);
		await props.removeBunny(props.bunny.id);
	}
	
    const unconfirmHandler = async (_) => {
		props.setConfirm(false);
		setThisConfirm(false);
	}
	
    const confirmHandler = async (_) => {
		props.setConfirm(true);
		setThisConfirm(true);
	}
	
	return (
	<div>
		<div className="bunny-row d-flex">
			<a href={picture} rel="noopener noreferrer" target="_blank"><img className="bunny-picture self-align-start" src={picture}/></a>
			<span className="w-100">
			<div className="bunny-name extra-large">
				{props.bunny.name}
			</div>
			<div className="bunny-buttons">
			{thisConfirm
				?	<div className="h-100 d-flex justify-content-end">
						<button className="btn btn-danger me-2 float-end mt-auto" onClick={removeHandler} disabled={props.remove}>Bekräfta avregistrering</button>
						<button className="btn btn-secondary float-end mt-auto" onClick={unconfirmHandler} disabled={props.remove}>Avbryt</button>
					</div>
				:	<div className="h-100 d-flex justify-content-end">
						<button className="btn btn-warning me-2 float-end mt-auto" onClick={confirmHandler} disabled={props.confirm}>Avregistrera</button>
						<button className="btn btn-primary float-end mt-auto" onClick={(_) => navigate('/bunny/'+props.bunny.id)} disabled={props.confirm}>Ändra</button>
					</div>
			}
			</div>
            </span>
		</div>
	</div>
	);
}

const BunnyList = (props) => {

    return (
	<div className="d-grid gap-3">
		{ props.bunnies.map(bunny => {
        	return (<Bunny bunny={bunny} key={bunny.id} confirm={props.confirm} setConfirm={props.setConfirm} removeBunny={props.removeBunny} remove={props.remove} setRemove={props.setRemove}/>)
        })}
	</div>
    );
}

const BunniesForm = (props) => {
	
    const [confirm, setConfirm] = useState(false);
    const [remove, setRemove] = useState(false);

	const session = useSession();
	const navigate = useNavigate();
		
    return (
	<div>
		<div className="row">
			<div className="col-md-12 align-self-center p-4">
				<h2 className="text-center dark"> Mina kaniner </h2>
				{session.user.approved
					? <button className="btn btn-primary float-end" onClick={() => navigate("/bunny")} disabled={confirm || remove}>Jag har en ny märkt kanin</button>
					: null
				}
			</div>
			<BunnyList bunnies={props.bunnies} confirm={confirm} setConfirm={setConfirm} removeBunny={props.removeBunny} remove={remove} setRemove={setRemove}/>
		</div>
	</div>
    );
}

export default BunniesForm;