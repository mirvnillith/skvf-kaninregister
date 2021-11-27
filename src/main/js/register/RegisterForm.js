import React, { useState } from 'react';
import { Link } from "react-router-dom";

const RegisterForm = (props) => {
    const [user, setUser] = useState("");
    const [pwd, setPwd] = useState("");
    const [pwd2, setPwd2] = useState("");
    const [autoLogin, setAutoLogin] = useState(true);
    const [isValidPwd, setIsValidPwd] = useState(true);
    const [isValidated, setIsValidated] = useState(false);

    const submitHandler = async (e) => {
        e.preventDefault();
        setIsValidated(true);
        if (pwd !== pwd2) {
           setIsValidPwd(false);
        }
        if (user && pwd && pwd2 && isValidPwd) {
            await props.submitForm(user, pwd, autoLogin);
        }
    }

    return (
        <div className="row py-2">
            <form onSubmit={submitHandler} >
                <div className="col-md-12">
                    <h2 >Registrera dig</h2>
                </div>
                <div className="col-md-12">
                    <div className="row mb-2">
                        <label htmlFor="userName" className="col-md-6 col-form-label">Användarnamn</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className={isValidated && user === "" ? "form-control is-invalid" : "form-control"}
                                id="userName"
                                onChange={e => setUser(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange ett användarnamn!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="password" className="col-md-6 col-form-label">Lösenord</label>
                        <div className="col-md-6">
                            <input
                                type="password"
                                className={isValidated && pwd === "" ? "form-control is-invalid" : "form-control"}
                                id="password"
                                onChange={e => setPwd(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange ett lösenord!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="password2" className="col-md-6 col-form-label">Upprepa Lösenord</label>
                        <div className="col-md-6">
                            <input
                                type="password"
                                className={isValidPwd ? "form-control" : "form-control is-invalid"}
                                id="password2"
                                onChange={e => setPwd2(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste upprepa ditt lösenord!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="autoLogin" className="col-md-6 col-form-label">Logga in automatiskt efter registrering</label>
                        <div className="col-md-6">
                            <input
                                type="checkbox"
								defaultChecked={autoLogin}
                                id="autoLogin"
                                onChange={e => setAutoLogin(e.target.checked)}
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
                            <button type="submit" className="btn btn-primary float-end" >Registrera dig</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default RegisterForm;
