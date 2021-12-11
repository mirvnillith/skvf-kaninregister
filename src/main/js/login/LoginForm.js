import React, { useState } from 'react';
import { Link } from "react-router-dom";
import useFormValidation from "../hooks/FormValidation";

const INITIAL_STATE = {
    user: "",
    pwd: ""
};

const validate = (values) => {
    const errors = {};

    if (!values.user) {
        errors.user = "Du måste ange ett användarnamn!";
    }
    if (!values.pwd) {
        errors.pwd = "Du måste ange ett lösenord!";
    }

    return errors;
}

const LoginForm = (props) => {

    const {
        handleSubmit,
        handleChange,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, props.submitHandler);

    return (
        <div className="row py-2">
            <div className="col-md-12">
                <h2>Logga in</h2>
            </div>
            <div className="col-md-12">
                <form onSubmit={handleSubmit} >
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
                        <div className="col-sm-8 align-self-end">
                            <p className="mb-0">
                                Saknar du ett konto?
                                &nbsp;
                                <Link className="link-primary" to="/register">Registrera dig här!</Link>
                            </p>
                        </div>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end"  disabled={isSubmitting} >
                                { isSubmitting && <span className="spinner-border spinner-border-sm mr-1" /> }
                                Logga in
                            </button>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-sm-8 align-self-end">
                            <p className="mb-0">
                                Har du glömt ditt lösenord?
                                &nbsp;
                                <Link className="link-primary" to="/recoverpassword">Återställ lösenordet här!</Link>
                            </p>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default LoginForm;