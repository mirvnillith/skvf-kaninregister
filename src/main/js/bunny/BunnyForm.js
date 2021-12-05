import React, { useState } from 'react';

const BunnyForm = (props) => {
    const [name, setName] = useState(props.bunny.name ? props.bunny.name : "");
    const [chip, setChip] = useState(props.bunny.chip ? props.bunny.chip : "");
    const [leftEar, setLeftEar] = useState(props.bunny.leftEar ? props.bunny.leftEar : "");
    const [rightEar, setRightEar] = useState(props.bunny.rightEar ? props.bunny.rightEar : "");
    const [ring, setRing] = useState(props.bunny.ring ? props.bunny.ring : "");
    const [picture, setPicture] = useState(props.bunny.picture ? props.bunny.picture : "");
    const [gender, setGender] = useState(props.bunny.gender ? props.bunny.gender : "");
    const [neutered, setNeutered] = useState(props.bunny.neutered ? true : false);
    const [birthDate, setBirthDate] = useState(props.bunny.birthDate ? props.bunny.birthDate : "");
    const [race, setRace] = useState(props.bunny.race ? props.bunny.race : "");
    const [coat, setCoat] = useState(props.bunny.coat ? props.bunny.coat : "");
    const [colourMarkings, setColourMarkings] = useState(props.bunny.colourMarkings ? props.bunny.colourMarkings : "");
    const [features, setFeatures] = useState(props.bunny.features ? props.bunny.features : "");
    const [isValidated, setIsValidated] = useState(false);
    const [oneIdentifier, setOneIdentifier] = useState(false);
    const [submit, setSubmit] = useState(true);

    const submitHandler = async (e) => {
		setSubmit(false);
        e.preventDefault();
        setIsValidated(true);
		setOneIdentifier(chip || leftEar || rightEar || ring);
        if (name && oneIdentifier) {
			const bunny = {};
			bunny.id = props.bunny.id;
			bunny.name = name;
			bunny.chip = chip;
			bunny.leftEar = leftEar;
			bunny.rightEar = rightEar;
			bunny.ring = ring;
			bunny.picture = picture;
			if (gender) bunny.gender = gender;
			bunny.neutered = neutered;
			bunny.birthDate = birthDate;
			bunny.race = race;
			bunny.coat = coat;
			bunny.colourMarkings = colourMarkings;
			bunny.features = features;
            await props.submitForm(bunny);
        }
		setSubmit(true);
    }

    const cancelHandler = async (e) => {
		await props.cancelForm();
	}
	
    return (
        <div className="row py-2">
            <form onSubmit={submitHandler} >
                <div className="col-md-12">
                    <div className="row mb-2">
                        <label htmlFor="name" className="col-md-6 col-form-label large">Namn</label>
                        <div className="col-md-6">
                            <input autoFocus
                                type="text"
								size="20"
                                className={"form-control large" + (isValidated && name === "" ? " is-invalid" : "")}
                                id="name"
								value={name}
                                onChange={e => setName(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange ett namn!
                            </div>
                        </div>
                    </div>
					<fieldset> <legend>Märkning</legend>
                    <div className="row mb-2">
                        <label htmlFor="chip" className="col-md-6 col-form-label">Chipnummer</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className={"form-control" + (isValidated && !oneIdentifier ? " is-invalid" : "")}
                                id="chip"
								value={chip}
                                onChange={e => setChip(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange minst en märkning!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="leftEar" className="col-md-6 col-form-label">Vänster öra</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className={"form-control" + (isValidated && !oneIdentifier ? " is-invalid" : "")}
                                id="leftEar"
								value={leftEar}
                                onChange={e => setLeftEar(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange minst en märkning!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="rightEar" className="col-md-6 col-form-label">Höger öra</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className={"form-control" + (isValidated && !oneIdentifier ? " is-invalid" : "")}
                                id="rightEar"
								value={rightEar}
                                onChange={e => setRightEar(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange minst en märkning!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="ring" className="col-md-6 col-form-label">Ringnummer</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className={"form-control" + (isValidated && !oneIdentifier ? " is-invalid" : "")}
                                id="ring"
								value={ring}
                                onChange={e => setRing(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange minst en märkning!
                            </div>
                        </div>
                    </div>
					</fieldset>
					<fieldset> <legend>Information</legend>
                    <div className="row mb-2">
                        <label htmlFor="birthDate" className="col-md-6 col-form-label">Födelsedag</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="birthDate"
								value={birthDate}
                                onChange={e => setBirthDate(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="gender" className="col-md-6 col-form-label">Kön</label>
                        <div className="col-md-6">
                            <select
                                className="form-select"
                                id="gender"
								value={gender}
                                onChange={e => setGender(e.target.value)}
                            >
								<option value=""></option>
								<option value="Female">Hona</option>
								<option value="Male">Hane</option>
								<option value="Unknown">Okänt</option>
							</select>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="neutered" className="col-md-6 col-form-label">Kastrerad</label>
                        <div className="col-md-6">
                            <input
                                type="checkbox"
								defaultChecked={neutered}
                                id="neutered"
                                onChange={e => setNeutered(e.target.checked)}
                            />
                        </div>
                    </div>
					</fieldset>
					<fieldset> <legend>Utseende</legend>
                    <div className="row mb-2">
                        <label htmlFor="picture" className="col-md-6 col-form-label">Länk till bild</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="picture"
								value={picture}
                                onChange={e => setPicture(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="race" className="col-md-6 col-form-label">Ras</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="race"
								value={race}
                                onChange={e => setRace(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="coat" className="col-md-6 col-form-label">Hårlag</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="coat"
								value={coat}
                                onChange={e => setCoat(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="colourMarkings" className="col-md-6 col-form-label">Färgteckning</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="colourMarkings"
								value={colourMarkings}
                                onChange={e => setColourMarkings(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="features" className="col-md-6 col-form-label">Kännetecken</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="features"
								value={features}
                                onChange={e => setFeatures(e.target.value)}
                            />
                        </div>
                    </div>
					</fieldset>
                    <div className="row mt-2">
                        <div className="col-sm-8"/>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end" disabled={!submit}>Spara</button>
                            <button type="cancel" className="btn btn-secondary float-end me-2" disabled={!submit} onClick={cancelHandler}>Avbryt</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default BunnyForm;
