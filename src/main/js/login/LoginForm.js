import React, { useState } from 'react';
import { Link } from "react-router-dom";

const LoginForm = (props) => {

    const [user, setUser] = useState("");
    const [pwd, setPwd] = useState("");
    const [isValidated, setIsValidated] = useState(false);
    const [submit, setSubmit] = useState(true);

    const submitHandler = async (e) => {
		setSubmit(false);
        e.preventDefault();
        setIsValidated(true);
        if (user && pwd) {
            await props.submitForm(user, pwd);
        }
		setSubmit(true);
    }

    return (
        <div className="row py-2">
            <div className="col-md-12">
                <h2>Logga in</h2>
            </div>
            <div className="col-md-12">
                <form onSubmit={submitHandler}>
                    <div className="row mb-2">
                        <label htmlFor="userName" className="col-md-6 col-form-label">Användarnamn</label>
                        <div className="col-md-6">
                            <input autoFocus
                                type="text"
                                className={isValidated && user === "" ? "form-control is-invalid" : "form-control"}
                                id="username"
                                autoComplete="username"
                                onChange={e => setUser(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="password" className="col-md-6 col-form-label">Lösenord</label>
                        <div className="col-md-6">
                            <input
                                type="password"
                                className={isValidated && pwd === "" ? "form-control is-invalid" : "form-control"}
                                id="password"
                                autoComplete="current-password"
                                onChange={e => setPwd(e.target.value)}
                            />
                        </div>
                        <div className="invalid-feedback">
                            Ogiltigt lösenord!
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
                            <button type="submit" className="btn btn-primary float-end" disabled={!submit}>Logga in</button>
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