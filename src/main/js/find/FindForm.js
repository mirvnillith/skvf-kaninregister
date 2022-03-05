import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import useFormValidation from "../hooks/FormValidation";
import Identifiers from "../common/Identifiers";
import Contact from "../common/Contact";
import Details from "../common/Details";
import { getBunnyOwner, getBunnyBreeder, getBunnyPreviousOwner } from "../utils/api";
import { nonPublic } from "../utils/data.js";

const INITIAL_STATE = {
    chip: "",
    leftEar: "",
    rightEar: "",
    ring: ""
};

const validate = (values) => {
    const errors = {};

    const oneIdentifier = values.chip || values.leftEar || values.rightEar || values.ring;
    if (!oneIdentifier) {
		errors.chip = "Du måste ange minst en märkning!"
		errors.leftEar = "Du måste ange minst en märkning!"
		errors.rightEar = "Du måste ange minst en märkning!"
		errors.ring = "Du måste ange minst en märkning!"
	}
	if (values.chip && values.chip.split("?").length > 3) {
		errors.chip = "Du får inte använda mer än 2 frågetecken!"		
	}
	if (values.leftEar && values.leftEar.split("?").length > 3) {
		errors.leftEar = "Du får inte använda mer än 2 frågetecken!"		
	}
	if (values.rightEar && values.rightEar.split("?").length > 3) {
		errors.rightEar = "Du får inte använda mer än 2 frågetecken!"		
	}
	if (values.ring && values.ring.split("?").length > 3) {
		errors.ring = "Du får inte använda mer än 2 frågetecken!"		
	}
    return errors;
}

const Found = (props) => {
	
    const [showDetails, setShowDetails] = useState(false);
    const [showContacts, setShowContacts] = useState(false);
    const [loadingContacts, setLoadingContacts] = useState(false);
    const [contacts, setContacts] = useState();

	const picture = props.bunny.picture
					? props.bunny.picture
					: '/assets/bunny.jpg';
			
	const bunnyDetails = <div>
							<Details bunny={props.bunny}/>
						 </div>;
			
	const bunnyGender = props.bunny.gender==='Unknown'
						?	'Okänt kön'
						:	(props.bunny.neutered?'Kastrerad ':'Ej kastrerad ') +
							(props.bunny.gender==='Female'?'hona':'hane');
	
    const setBunnyOwner = (owner) => {
		if (owner === undefined) {
			owner = nonPublic();
		}
		const onLoad = setBunnyBreeder(owner);
		getBunnyBreeder(props.bunny.id, onLoad, () => onLoad({ name: "Hämtning misslyckades"}));
	}
	
    const setBunnyBreeder = (owner) => (breeder) => {
		if (breeder === undefined) {
			breeder = nonPublic();
		}
		const onLoad = setBunnyPreviousOwner(owner, breeder);
		getBunnyPreviousOwner(props.bunny.id, onLoad, () => onLoad({ name: "Hämtning misslyckades"}));
	}
	
    const setBunnyPreviousOwner = (owner, breeder) => (previousOwner) => {
		if (previousOwner === undefined) {
			previousOwner = nonPublic();
		}
		setContacts({owner, breeder, previousOwner});
		setLoadingContacts(false);
	}
	
	const loadContacts = async (_) => {
		if (contacts === undefined) {
			setLoadingContacts(true);
			getBunnyOwner(props.bunny.id, setBunnyOwner, () => setBunnyOwner({ name: "Hämtning misslyckades"}));
		}
		
		setShowContacts(!showContacts);
	}
	
	const bunnyContacts = contacts && <div>
							<Contact title='Ägare' contact={contacts.owner} />
							<Contact title='Uppfödare' contact={contacts.breeder} />
							<Contact title='Föregående ägare' contact={contacts.previousOwner} />
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
			</div>
			<div className="w-100">
			<div className="bunny-name extra-large">
				{props.bunny.name}
			</div>
			<div className="fw-normal">
			{bunnyGender}
			<Identifiers bunny={props.bunny} />
			{showDetails && bunnyDetails}
			<div><p/></div>
			{showContacts && bunnyContacts}
			</div>
			</div>
		</div>
	</div>
	);
}

const FindList = (props) => {

    return (
	<div className="d-grid gap-3">
		{ props.bunnies.length == 0
		?	<h2>Inga kaniner hittades</h2>
		:	props.bunnies.map(bunny => {
        		return (<Found bunny={bunny} key={bunny.id} loadDetails={props.loadDetails}/>)
        	})}
	</div>
    );
}

const FindForm = (props) => {
	
	const navigate = useNavigate();
		
	const {
        handleSubmit,
        handleChange,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, props.find);

    return (
	<div>
		<div className="row">
			<div className="col-md-12 align-self-center p-4">
				<h2 className="text-center dark mb-3"> Hitta kanin </h2>
				Skriv in den märkning du funnit på kaninen.
				Om du inte kan tyda alla tecken kan du skriva ? istället, men inte fler än två per märkning.
				Sökningar som ger fler än 10 träffar visas inte.
			</div>
       <div className="row py-2">
            <form onSubmit={handleSubmit} >
                    <div className="row mb-2">
                        <label htmlFor="chip" className="col-md-6 col-form-label">Chipnummer</label>
                        <div className="col-md-6">
                            <input autoFocus
                                id="chip"
                                name="chip"
                                type="text"
								value={values.chip}
                                className={"form-control" + (errors.chip ? " is-invalid" : "")}
                                onChange={handleChange}
                            />
							{errors.chip && <div className="invalid-feedback">{errors.chip}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="leftEar" className="col-md-6 col-form-label">Vänster öra</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                id="leftEar"
                                name="leftEar"
								value={values.leftEar}
                                className={"form-control" + (errors.leftEar ? " is-invalid" : "")}
                                onChange={handleChange}
                            />
							{errors.leftEar && <div className="invalid-feedback">{errors.leftEar}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="rightEar" className="col-md-6 col-form-label">Höger öra</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                id="rightEar"
                                name="rightEar"
								value={values.rightEar}
                                className={"form-control" + (errors.rightEar ? " is-invalid" : "")}
                                onChange={handleChange}
                            />
							{errors.rightEar && <div className="invalid-feedback">{errors.rightEar}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="ring" className="col-md-6 col-form-label">Ringnummer</label>
                        <div className="col-md-6">
                            <input
                                id="ring"
                                name="ring"
                                type="text"
								value={values.ring}
                                className={"form-control" + (errors.ring ? " is-invalid" : "")}
                                onChange={handleChange}
                            />
							{errors.ring && <div className="invalid-feedback">{errors.ring}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-sm-8"/>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting} >
                                { isSubmitting && <span className="spinner-border spinner-border-sm me-1"/> }
                                Sök</button>
                            <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={() => navigate(-1)}>Avbryt</button>
                        </div>
                    </div>
            </form>
        </div>
			{ props.bunnies && <FindList bunnies={props.bunnies} loadDetails={props.loadDetails}/> }
		</div>
	</div>
    );
}

export default FindForm;
