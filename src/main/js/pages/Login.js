import React, { useState } from 'react';
import { loginUser } from '../utils/api';

const Login = (props) => {
    const [user, setUser] = useState("");
    const [pwd, setPwd] = useState("");
    const [isValidated, setIsValidated] = useState(false);

    const setError = (msg) => props.setNotification({type: "danger", msg: msg});

    const onSuccessfulLogin = (user) => {
        props.setSession({user, noSession: false});
    }

    const submitHandler = async (e) => {
        e.preventDefault();
        setIsValidated(true);
        if (user && pwd) {
            await loginUser(user, pwd, onSuccessfulLogin, setError)
        }
    }

    const registerHandler = (e) => {
        e.preventDefault();
        props.setView("register");
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
                            <input
                                type="text"
                                className={isValidated && user === "" ? "form-control is-invalid" : "form-control"}
                                id="username"
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
                                <a className="link-primary" onClick={registerHandler}>Registrera dig här!</a>
                            </p>
                        </div>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end">Logga in</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Login;