import React from 'react';
import {Link} from "react-router-dom";
import useFormValidation from "../hooks/FormValidation";

const INITIAL_STATE = {
    user: "",
    pwd: "",
    pwd2: "",
    autoLogin: true
};

const validate = (values) => {
    const errors = {};

    if (!values.user) {
        errors.user = "Du måste ange ett användarnamn!";
    }
    if (!values.pwd) {
        errors.pwd = "Du måste ange ett lösenord!";
    }
    if (!values.pwd2) {
        errors.pwd2 = "Du måste ange ett lösenord!";
    }
    if (values.pwd !== values.pwd2) {
        errors.pwd2 = "Du måste upprepa ditt lösenord!";
    }

    return errors;
}

const RegisterForm = (props) => {

    const {
        handleSubmit,
        handleChange,
        handleChangeProvideValue,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, props.submitHandler);

    return (
        <div className="row py-2">
            <form onSubmit={handleSubmit}>
                <div className="col-md-12 mb-3">
                    <h2>Registrera dig</h2>
					För att registrera dig som ägare i Kaninregistret behöver du ett användarnamn och ett lösenord.
					När du väl loggat in kan du komplettera med ditt riktiga namn och kontaktuppgifter.
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
                                   className={errors.user ? "form-control is-invalid" : "form-control"}
                                   autoComplete="username"
                                   onChange={handleChange}
                            />
                            {errors.user && <div className="invalid-feedback">{errors.user}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="pwd" className="col-md-6 col-form-label">Lösenord</label>
                        <div className="col-md-6">
                            <input
                                id="pwd"
                                name="pwd"
                                type="password"
                                value={values.pwd}
                                className={errors.pwd ? "form-control is-invalid" : "form-control"}
                                onChange={handleChange}
                                autoComplete="new-password"
                            />
                            {errors.pwd && <div className="invalid-feedback">{errors.pwd}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="pwd2" className="col-md-6 col-form-label">Upprepa lösenord</label>
                        <div className="col-md-6">
                            <input
                                id="pwd2" 
								name="pwd2"
                                type="password"
                                value={values.pwd2}
                                className={errors.pwd2 ? "form-control is-invalid" : "form-control"}
                                onChange={handleChange}
                                autoComplete="new-password"
                            />
                            {errors.pwd2 && <div className="invalid-feedback">{errors.pwd2}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="autoLogin" className="col-md-6 form-check-label">Logga in automatiskt efter registrering</label>
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
                    <div className="row mb-2">
                        <div className="col-sm-8 align-self-end">
                            <p className="mb-0">
                                Har du redan ett konto?
                                &nbsp;
                                <Link className="link-primary" to="/login">Logga in här!</Link>
                            </p>
                        </div>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting}>
                                {isSubmitting && <span className="spinner-border spinner-border-sm me-1"/>}
                                Registrera dig
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default RegisterForm;
