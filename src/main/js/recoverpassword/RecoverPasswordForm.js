import React, { useState } from 'react';
import { Link } from "react-router-dom";
import useFormValidation from "../hooks/FormValidation";

const INITIAL_STATE = {
    user: "",
    pwd: "",
    pwd2: "",
    chip: "",
    leftEar: "",
    rightEar: "",
    ring: "",
    autoLogin: true
};

const validate = (values) => {
    const errors = {};

    if (!values.user) {
        errors.user = "Du måste ange ditt användarnamn!";
    }
    if (!values.pwd) {
        errors.pwd = "Du måste ange ett nytt lösenord!";
    }
    if (!values.pwd2 || values.pwd !== values.pwd2) {
        errors.pwd2 = "Du måste upprepa ditt nya lösenord!";
    }
    const oneIdentifier = values.chip || values.leftEar || values.rightEar || values.ring;
    if (!oneIdentifier) {
		errors.chip = "Du måste ange minst en märkning!"
		errors.leftEar = "Du måste ange minst en märkning!"
		errors.rightEar = "Du måste ange minst en märkning!"
		errors.ring = "Du måste ange minst en märkning!"
	}
    return errors;
}

const RecoverPasswordForm = (props) => {
	
	const {
        handleSubmit,
        handleChange,
        handleChangeProvideValue,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, props.submitForm);

    return (
        <div className="row py-2">
            <form onSubmit={handleSubmit} >
                <div className="col-md-12 mb-3">
                    <h2 >Återställ ditt lösenord</h2>
					Om du glömt ditt lösenord kan du här ange ett nytt, om du kan identifiera en kanin
					du registrerat på dig.
                </div>
                <div className="col-md-12">
                    <div className="row mb-2">
                        <label htmlFor="user" className="col-md-6 col-form-label">Användarnamn</label>
                        <div className="col-md-6">
                            <input autoFocus
                                id="user"
                                name="user"
                                type="text"
								value={values.user}
                                className={"form-control" + (errors.user ? " is-invalid" : "")}
                                onChange={handleChange}
								autoComplete="username"
                            />
							{errors.user && <div className="invalid-feedback">{errors.user}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="pwd" className="col-md-6 col-form-label">Nytt lösenord</label>
                        <div className="col-md-6">
                            <input
                                id="pwd"
                                name="pwd"
                                type="password"
								value={values.pwd}
                                className={"form-control" + (errors.pwd ? " is-invalid" : "")}
                                onChange={handleChange}
								autoComplete="new-password"
                            />
							{errors.pwd && <div className="invalid-feedback">{errors.pwd}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="pwd2" className="col-md-6 col-form-label">Upprepa nytt lösenord</label>
                        <div className="col-md-6">
                            <input
                                id="pwd2"
                                name="pwd2"
                                type="password"
								value={values.pwd2}
                                className={"form-control" + (errors.pwd2 ? " is-invalid" : "")}
                                onChange={handleChange}
								autoComplete="new-password"
                            />
							{errors.pwd2 && <div className="invalid-feedback">{errors.pwd2}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="autoLogin" className="col-md-6 form-check-label">Logga in automatiskt efter återställning</label>
                        <div className="col-md-6">
                            <input
                                id="autoLogin"
                                name="autoLogin"
                                type="checkbox"
								defaultChecked={values.autoLogin}
								className="form-check-input"
                                onChange={(event => handleChangeProvideValue(event, event.target.checked))}
                            />
                        </div>
                    </div>
					<fieldset> <legend>Märkning av en av dina registrerade kaniner</legend>
                    <div className="row mb-2">
                        <label htmlFor="chip" className="col-md-6 col-form-label">Chipnummer</label>
                        <div className="col-md-6">
                            <input
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
					</fieldset>
                    <div className="row mb-2">
                        <div className="col-sm-8 align-self-end">
                            <p className="mb-0">
                                Kom du på lösenordet igen?
                                &nbsp;
                                <Link className="link-primary" to="/login">Logga in här!</Link>
                            </p>
                        </div>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting} >
                                { isSubmitting && <span className="spinner-border spinner-border-sm me-1"/> }
                                Återställ</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default RecoverPasswordForm;
