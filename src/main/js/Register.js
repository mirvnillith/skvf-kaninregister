import React, { useState } from 'react';

const Register = (props) => {
    const [user, setUser] = useState("");
    const [pwd, setPwd] = useState("");
    const [pwd2, setPwd2] = useState("");
    const [isValidPwd, setIsValidPwd] = useState(true);
    const [isValidated, setIsValidated] = useState(false);

    const submitHandler = async (e) => {
        e.preventDefault();
        setIsValidated(true);
        if (pwd !== pwd2) {
           setIsValidPwd(false);
        }
        if(user && pwd && pwd2 && isValidPwd) {
            const response = await fetch("/api/owners", {
                method: 'POST',
                headers: new Headers({'content-type': 'application/json'}),
                body: JSON.stringify({userName: user, password: pwd})
            });
            const responseMsg = await response.json();
            if (response.status === 200){
                console.log(responseMsg);
            }
            else if (response.status === 400) {
                console.error(responseMsg);
                props.setErrorMsg("Felaktigt användarnamn eller användare!")
            }
            else if (response.status === 409) {
                console.error(responseMsg);
                props.setErrorMsg("Användarnamnet finns redan!")
            }
            else {
                console.error(responseMsg);
                props.setErrorMsg("Ops, något gick fel!")
            }
        }
    }

    const loginHandler = (e) => {
        e.preventDefault();
        props.setView("login");
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
                                className="form-control"
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
                        <div className="col-sm-8 align-self-end">
                            <p className="mb-0">
                                Har du redan ett konto?
                                &nbsp;
                                <a className="link-primary" onClick={loginHandler}>Logga in här!</a>
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

export default Register;
