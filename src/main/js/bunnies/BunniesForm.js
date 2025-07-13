import React, { useState } from 'react';
import { useNavigate, Link } from "react-router-dom";
import { useSession } from "../hooks/SessionContext";
import Spinner from "react-bootstrap/Spinner";
import { getBunnyBreeder, getBunnyPreviousOwner } from "../utils/api";
import { nonPublic } from "../utils/data";
import Contact from "../common/Contact";
import Identifiers from "../common/Identifiers";
import Details from "../common/Details";
import Help from "../help/Help";

const Bunny = (props) => {
	
	const navigate = useNavigate();
	
    const [thisConfirm, setThisConfirm] = useState(false);
    const [showDetails, setShowDetails] = useState(false);
    const [showContacts, setShowContacts] = useState(false);
    const [loadingContacts, setLoadingContacts] = useState(false);
    const [contacts, setContacts] = useState();

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
	
	const confirmButtons = <div className="d-flex justify-content-end">
						<button className="btn btn-secondary me-2 float-end mt-auto" onClick={unconfirmHandler} disabled={props.remove}>Avbryt</button>
						<button className="btn btn-danger float-end mt-auto" onClick={removeHandler} disabled={props.remove}>
							{ props.remove && <span className="spinner-border spinner-border-sm me-1" /> }
							Bekräfta avregistrering
						</button>
					</div>;
	
	const reclaimButtons = <div className="d-flex justify-content-end">
							<span className="btn-align align-middle">
							Överlåtelsekod:&nbsp;<code className="bunny-token">{props.bunny.claimToken}</code>
							</span>
							<button className="btn btn-primary float-end mt-auto ms-3" onClick={(_) => navigate('/reclaim/'+props.bunny.id)} disabled={props.confirm}>Återta</button>
						</div>;
						
	const ownerButtons = <div className="d-flex justify-content-end">
							<button className="btn btn-warning me-2" onClick={confirmHandler} disabled={props.confirm}>Avregistrera</button>
							<button className="btn btn-secondary me-2" onClick={(_) => navigate('/transfer/'+props.bunny.id)} disabled={props.confirm}>Ägarbyte</button>
							<button className="btn btn-primary" onClick={(_) => navigate('/bunny/'+props.bunny.id)} disabled={props.confirm}>Ändra</button>
						</div>;
					
	const bunnyGender = props.bunny.gender==='Unknown'
						?	'Okänt kön'
						:	(props.bunny.neutered?'Kastrerad ':'Ej kastrerad ') +
							(props.bunny.gender==='Female'?'hona':'hane');
	const bunnyBirthDate = props.bunny.birthDate
						?	', födelsedag '+props.bunny.birthDate
						:	'';
					
	const bunnyDetails = <div>
							<Identifiers bunny={props.bunny} />
							<Details bunny={props.bunny} />
						</div>;
			
    const setBunnyBreeder = (breeder) => {
		if (breeder === undefined) {
			breeder = nonPublic();
		}
		const setBunnyPreviousOwner = setBunnyBreederAndPreviousOwner(breeder);
		getBunnyPreviousOwner(props.bunny.id, setBunnyPreviousOwner, () => setBunnyPreviousOwner({ name: "Hämtning misslyckades"}));
	}
	
    const setBunnyBreederAndPreviousOwner = (breeder) => (previousOwner) => {
		if (previousOwner === undefined) {
			previousOwner = nonPublic();
		}
		setContacts({breeder, previousOwner});
		setLoadingContacts(false);
	}
	
	const loadContacts = async (_) => {
		if (contacts === undefined) {
			setLoadingContacts(true);
			getBunnyBreeder(props.bunny.id, setBunnyBreeder, () => setBunnyBreeder({ name: "Hämtning misslyckades"}));
		}
		
		setShowContacts(!showContacts);
	}
	
	const bunnyContacts = contacts && <div>
							<Contact title='Föregående ägare' contact={contacts.previousOwner} />
							<Contact title='Uppfödare' contact={contacts.breeder} />
						</div>;
				
	return (
	<div>
		<div className="bunny-row d-flex">
			<div className="bunny-picture-sidebar">
			<a href={picture} rel="noopener noreferrer" target="_blank"><img className="bunny-picture self-align-start" src={picture}/></a>
			<button className="btn btn-info w-100 mt-1 shadow-none" onClick={(_) => setShowDetails(!showDetails)} >Detaljer</button>
			<button className="btn btn-info w-100 mt-1 shadow-none" onClick={loadContacts} disabled={loadingContacts}>
				{ loadingContacts && <span className="spinner-border spinner-border-sm me-1" /> }
				Kontakter</button>
			<div className="m-1">
				<Link className="link-primary" to={'/bunny/'+props.bunny.id+'/profile'} target='_blank'>Profil</Link>
				&nbsp;
				<Help topic="profile"/>
			</div>
			</div>
			<div className="w-100">
			<div className={"bunny-name extra-large"+(props.bunny.claimToken?" fst-italic":"")}>
				{props.bunny.name}
			</div>
			<div className="fw-normal">
			{bunnyGender}{bunnyBirthDate}
			{showDetails && bunnyDetails}
			{showDetails && showContacts && <div><p/></div>}
			{showContacts && bunnyContacts}
			{!showDetails && (!showContacts || loadingContacts) && <div>&nbsp;<p/></div>}
			</div>
			<div className="d-flex justify-content-end">
			{thisConfirm
				?	confirmButtons
				:	props.bunny.claimToken
					?	reclaimButtons
					:	ownerButtons
			}
			</div>
            </div>
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
				<h2 className="text-center dark mb-3"> Mina kaniner </h2>
				{session.user.approved
					? <span>
						<button className="btn btn-primary float-end" onClick={() => navigate("/bunny")} disabled={confirm || remove}>Jag har en ny märkt kanin</button>
						<button className="btn btn-secondary float-end me-2" onClick={() => navigate("/claim")} disabled={confirm || remove}>Jag har tagit över en registrerad kanin</button>
					  </span>
					: null
				}
			</div>
			<div className="mb-1">
				<span>
				    <select
				        id="order"
				        name="order"
				        value={props.order}
						disabled = {props.bunnies === undefined}
				        className="float-end"
				        onChange={props.changeOrder}>
				            <option value="NAME_ASC">Namn A-Ö</option>
				            <option value="NAME_DESC">Namn Ö-A</option>
				            <option value="BIRTHDATE_ASC">Födelsedag 0-9</option>
				            <option value="BIRTHDATE_DESC">Födelsedag 9-0</option>
				            <option value="GENDER_ASC">Kön ♂-♀</option>
				            <option value="GENDER_DESC">Kön ♀-♂</option>
				            <option value="RACE_ASC">Ras A-Ö</option>
				            <option value="RACE_DESC">Ras Ö-A</option>
				    </select>
					<label htmlFor="order" className="float-end me-2">Sortering:</label>
				</span>
			</div>
		</div>

		{props.bunnies === undefined
			?	<Spinner animation="border" role="status"> <span className="visually-hidden">laddar innehåll...</span> </Spinner>
			:	<BunnyList bunnies={props.bunnies} order={props.order} changeOrder={props.changeOrder} confirm={confirm} setConfirm={setConfirm} removeBunny={props.removeBunny} remove={remove} setRemove={setRemove}/>
		}
	</div>
    );
}

export default BunniesForm;