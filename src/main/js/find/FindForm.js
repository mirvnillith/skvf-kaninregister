import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import useFormValidation from "../hooks/FormValidation";
import Help from "../help/Help";

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
	
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [details, setDetails] = useState();

	const picture = props.bunny.picture
					? props.bunny.picture
					: '/assets/bunny.jpg';
			
	
    const loadDetails = async (_) => {
		setLoadingDetails(true);
		props.loadDetails(props.bunny, setDetails);
	}
	
	return (
	<div>
		<div className="bunny-row d-flex">
			<a href={picture} rel="noopener noreferrer" target="_blank"><img className="bunny-picture self-align-start" src={picture}/></a>
			<span className="w-100">
			<div className="bunny-name extra-large">
				{props.bunny.name}
			</div>
					<fieldset className="fw-normal">
                        <legend>Märkning</legend>
                        <div className="row">
                            <label htmlFor="chip" className="col-md-6 col-form-label">Chipnummer</label>
                            <label id="chip" className="col-md-6 col-form-label">{props.bunny.chip}</label>
                        </div>
                        <div className="row">
                            <label htmlFor="leftEar" className="col-md-6 col-form-label">Vänster öra</label>
                            <label id="leftEar" className="col-md-6 col-form-label">{props.bunny.leftEar}</label>
                        </div>
                        <div className="row">
                            <label htmlFor="rightEar" className="col-md-6 col-form-label">Höger öra</label>
                            <label id="rightEar" className="col-md-6 col-form-label">{props.bunny.rightEar}</label>
                        </div>
                        <div className="row">
                            <label htmlFor="ring" className="col-md-6 col-form-label">Ringnummer</label>
                            <label id="ring" className="col-md-6 col-form-label">{props.bunny.ring}</label>
                        </div>
					</fieldset>
			<div className="bunny-buttons">
			{details
			?	<div className="h-100 fw-normal">
				<form>
				<div className="col-md-12">
				<fieldset>
				<legend>Ägare</legend>
						<div className="row">
							<label htmlFor="ownerName" className="col-md-6 col-form-label">Namn</label>
			       	 		<label id="ownerName" className="col-md-6 col-form-label">{details.owner.name} 
							&nbsp;{details.owner.nonPublic && <Help topic="publicprivate"/>}</label>
						</div>
						<div className="row">
							<label htmlFor="ownerEmail" className="col-md-6 col-form-label">E-post</label>
			       	 		<label id="ownerEmail" className="col-md-6 col-form-label">{details.owner.email}</label>
						</div>
						<div className="row">
							<label htmlFor="ownerPhone" className="col-md-6 col-form-label">Telefon</label>
			       	 		<label id="ownerPhone" className="col-md-6 col-form-label">{details.owner.phone}</label>
						</div>
						<div className="row">
							<label htmlFor="ownerAddress" className="col-md-6 col-form-label">Adress</label>
			       	 		<label id="ownerAddress" className="col-md-6 col-form-label">{details.owner.address}</label>
						</div>
					</fieldset>
					</div>
					</form>
				</div>
			:	<div className="h-100 d-flex justify-content-end">
					<button className="btn btn-info me-2 float-end mt-auto" disabled={loadingDetails} onClick={loadDetails}>
						{ loadingDetails && <span className="spinner-border spinner-border-sm me-1"/> }
						Detaljer</button>
				</div>
			}
			</div>
            </span>
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