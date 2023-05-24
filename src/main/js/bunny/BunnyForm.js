import React from 'react';
import useFormValidation from "../hooks/FormValidation";
import Help from "../help/Help";
import { useSession} from "../hooks/SessionContext";

const validate = (values) => {
    const errors = {};
	
    if (!values.name) {
        errors.name = "Du måste ange ett namn!";
	}
	
    const oneIdentifier = values.chip || values.leftEar || values.rightEar || values.ring;
    if (!oneIdentifier) {
        errors.chip = "Du måste ange minst en märkning!";
        errors.leftEar = "Du måste ange minst en märkning!";
        errors.rightEar = "Du måste ange minst en märkning!";
        errors.ring = "Du måste ange minst en märkning!";
    }
	
    return errors;
}

const BunnyForm = (props) => {

	const session = useSession();

	const initialState = {
	    name: props.bunny.name ? props.bunny.name : "",
	    chip: props.bunny.chip ? props.bunny.chip : "",
	    leftEar: props.bunny.leftEar ? props.bunny.leftEar : "",
	    rightEar: props.bunny.rightEar ? props.bunny.rightEar : "",
	    ring: props.bunny.ring ? props.bunny.ring : "",
	    picture: props.bunny.picture ? props.bunny.picture : "",
	    gender: props.bunny.gender ? props.bunny.gender : "Unknown",
	    neutered: props.bunny.neutered ? true : false,
	    birthDate: props.bunny.birthDate ? props.bunny.birthDate : "",
	    race: props.bunny.race ? props.bunny.race : "",
	    coat: props.bunny.coat ? props.bunny.coat : "",
	    colourMarkings: props.bunny.colourMarkings ? props.bunny.colourMarkings : "",
	    features: props.bunny.features ? props.bunny.features : "",
	    ownerBreeder: props.bunny.breeder === session.user.id,
	    clearBreeder: false,
	    clearPreviousOwner: false
	};
	
	
    const {
        handleSubmit,
        handleChange,
        handleChangeProvideValue,
        values,
        errors,
        isSubmitting
    } = useFormValidation(initialState, validate, props.submitHandler);
	
    return (
        <div className="row py-2">
            <form onSubmit={handleSubmit} >
                <div className="col-md-12">
                    <div className="row mb-2">
                        <div className="col-sm-12">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting} >
                                { isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
                                Spara</button>
                            <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={props.cancelHandler}>Avbryt</button>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="name" className="col-md-6 col-form-label large">Namn</label>
                        <div className="col-md-6">
                            <input 
                                id="name"
                                name="name"
                                type="text"
                                value={values.name}
								size="20"
                                className={errors.name ? "form-control large is-invalid" : "form-control large"}
                                onChange={handleChange}
                            />
                            {errors.name && <div className="invalid-feedback">{errors.name}</div>}
                        </div>
                    </div>
					<fieldset>
                        <legend>Märkning (lämna tomt där ingen finns)</legend>
                        <div className="row mb-2">
                            <label htmlFor="chip" className="col-md-6 col-form-label">Chipnummer</label>
                            <div className="col-md-6">
                                <input
                                    id="chip"
                                    name="chip"
                                    type="text"
                                    value={values.chip}
                                    className={errors.chip ? "form-control is-invalid" : "form-control"}
                                    onChange={handleChange}
                                />
                                {errors.chip && <div className="invalid-feedback">{errors.chip}</div>}
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="leftEar" className="col-md-6 col-form-label">Vänster öra</label>
                            <div className="col-md-6">
                                <input
                                    id="leftEar"
                                    name="leftEar"
                                    type="text"
                                    value={values.leftEar}
                                    className={errors.leftEar ? "form-control is-invalid" : "form-control"}
                                    onChange={handleChange}
                                />
                                {errors.leftEar && <div className="invalid-feedback">{errors.leftEar}</div>}
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="rightEar" className="col-md-6 col-form-label">Höger öra</label>
                            <div className="col-md-6">
                                <input
                                    id="rightEar"
                                    name="rightEar"
                                    type="text"
                                    value={values.rightEar}
                                    className={errors.rightEar ? "form-control is-invalid" : "form-control"}
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
                                    className={errors.ring ? "form-control is-invalid" : "form-control"}
                                    onChange={handleChange}
                                />
                                {errors.ring && <div className="invalid-feedback">{errors.ring}</div>}
                            </div>
                        </div>
					</fieldset>
					<fieldset>
                        <legend>Information</legend>
                        <div className="row mb-2">
                            <label htmlFor="birthDate" className="col-md-6 col-form-label">Födelsedag</label>
                            <div className="col-md-6">
                                <input
                                    id="birthDate"
                                    name="birthDate"
                                    type="text"
                                    value={values.birthDate}
                                    className={errors.birthDate ? "form-control is-invalid" : "form-control"}
                                    onChange={handleChange}
                                />
                                {errors.birthDate && <div className="invalid-feedback">{errors.birthDate}</div>}
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="gender" className="col-md-6 col-form-label">Kön</label>
                            <div className="col-md-6">
                                <select
                                    id="gender"
                                    name="gender"
                                    value={values.gender}
                                    className="form-select"
                                    onChange={handleChange}>
                                        <option value="Unknown">Okänt</option>
                                        <option value="Female">Hona</option>
                                        <option value="Male">Hane</option>
                                </select>
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="neutered" className="col-md-6 form-check-label">Kastrerad</label>
                            <div className="col-md-6">
                                <input
                                    id="neutered"
                                    name="neutered"
                                    type="checkbox"
									className="form-check-input"
                                    defaultChecked={values.neutered}
                                    onChange={(event => handleChangeProvideValue(event, event.target.checked))}
                                />
                            </div>
                        </div>
					</fieldset>
					<fieldset>
                        <legend>Utseende</legend>
                        <div className="row mb-2">
                            <label htmlFor="picture" className="col-md-6 col-form-label">Länk till bild <Help topic="picture"/></label>
                            <div className="col-md-6">
                                <input
                                    id="picture"
                                    name="picture"
                                    type="text"
                                    value={values.picture}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="race" className="col-md-6 col-form-label">Ras</label>
                            <div className="col-md-6">
                                <input
                                    id="race"
                                    name="race"
                                    type="text"
                                    value={values.race}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="coat" className="col-md-6 col-form-label">Hårlag</label>
                            <div className="col-md-6">
                                <input
                                    id="coat"
                                    name="coat"
                                    type="text"
                                    value={values.coat}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="colourMarkings" className="col-md-6 col-form-label">Färgteckning</label>
                            <div className="col-md-6">
                                <input
                                    id="colourMarkings"
                                    name="colourMarkings"
                                    type="text"
                                    value={values.colourMarkings}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="features" className="col-md-6 col-form-label">Kännetecken</label>
                            <div className="col-md-6">
                                <input
                                    id="features"
                                    name="features"
                                    type="text"
                                    value={values.features}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
					</fieldset>
					<fieldset>
                    <legend>Uppfödare</legend>
					{(props.bunny.id === undefined || props.breeder === undefined)
					?	<div className="row mb-2">
	                    	<label htmlFor="ownerBreeder" className="col-md-6 form-check-label">Jag är uppfödaren</label>
	                     	<div className="col-md-6">
	                       		<input
	                          			id="ownerBreeder"
	                          			name="ownerBreeder"
	                           		type="checkbox"
								defaultChecked={values.ownerBreeder}
									className="form-check-input"
	                               	onChange={(event => handleChangeProvideValue(event, event.target.checked))}
	                       		/>
	                    	</div>
						</div>
					:	props.breeder && <div>
								<div className="row mb-2">
									<label htmlFor="breederName" className="col-md-6 col-form-label">Namn</label>
									<label id="breederName" className="col-md-6 col-form-label">{props.breeder.name} 
									&nbsp;{props.breeder.nonPublic && <Help topic="publicprivate"/>}</label>
								</div>
								<div className="row mb-2">
									<label htmlFor="breederEmail" className="col-md-6 col-form-label">E-post</label>
	                       	 		<label id="breederEmail" className="col-md-6 col-form-label">{props.breeder.email}</label>
								</div>
								<div className="row mb-2">
	                        		<label htmlFor="clearBreeder" className="col-md-6 form-check-label">Radera information om kaninens uppfödare</label>
	                       	 		<div className="col-md-6">
	                            		<input
	                               			id="clearBreeder"
	                               			name="clearBreeder"
	                                		type="checkbox"
											defaultChecked={values.clearBreeder}
											className="form-check-input"
	                                    	onChange={(event => handleChangeProvideValue(event, event.target.checked))}
	                            		/>
	                        		</div>
	                    		</div>
						</div>
					}
					</fieldset>

					{props.previousOwner &&	<fieldset>
                    <legend>Föregående ägare</legend>
						<div className="row mb-2">
							<label htmlFor="previousOwnerName" className="col-md-6 col-form-label">Namn</label>
							<label id="previousOwnerName" className="col-md-6 col-form-label">{props.previousOwner.name} 
							&nbsp;{props.previousOwner.nonPublic && <Help topic="publicprivate"/>}</label>
						</div>
						<div className="row mb-2">
							<label htmlFor="previousOwnerEmail" className="col-md-6 col-form-label">E-post</label>
							<label id="previousOwnerEmail" className="col-md-6 col-form-label">{props.previousOwner.email}</label>
						</div>
						<div className="row mb-2">
							<label htmlFor="previousOwnerPhone" className="col-md-6 col-form-label">Telefon</label>
							<label id="previousOwnerPhone" className="col-md-6 col-form-label">{props.previousOwner.phone}</label>
						</div>
						<div className="row mb-2">
							<label htmlFor="previousOwnerAddress" className="col-md-6 col-form-label">Adress</label>
							<label id="previousOwnerAddress" className="col-md-6 col-form-label">{props.previousOwner.address}</label>
						</div>
						<div className="row mb-2">
	                       	<label htmlFor="clearPreviousOwner" className="col-md-6 form-check-label">Radera information om kaninens föregående ägare</label>
	                       		<div className="col-md-6">
	                        		<input
	                           			id="clearPreviousOwner"
	                           			name="clearPreviousOwner"
	                            		type="checkbox"
										defaultChecked={values.clearPreviousOwner}
										className="form-check-input"
	                                	onChange={(event => handleChangeProvideValue(event, event.target.checked))}
	                        		/>
	                       	</div>
	                    </div>
					</fieldset>}
                    <div className="row mt-2">
                        <div className="col-sm-12">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting} >
                                { isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
                                Spara</button>
                            <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={props.cancelHandler}>Avbryt</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default BunnyForm;
