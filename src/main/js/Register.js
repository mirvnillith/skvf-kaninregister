import React from 'react';

const Register = (props) => {
    const loginHandler = (e) => {
        e.preventDefault();
        props.setView("login");
    }
    return (
        <div className="row py-2">
           <div className="col-md-12">
               <h2 >Registrera dig</h2>
           </div>
            <div className="col-md-12">
                <form>
                    <div className="row mb-2">
                        <label htmlFor="userName" className="col-md-6 col-form-label">Användarnamn</label>
                        <div className="col-md-6">
                            <input type="text" className="form-control" id="userName" />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="password" className="col-md-6 col-form-label">Lösenord</label>
                        <div className="col-md-6">
                            <input type="password" className="form-control" id="password" />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="password2" className="col-md-6 col-form-label">Lösenord igen</label>
                        <div className="col-md-6">
                            <input type="password" className="form-control" id="password2" />
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
                            <button type="submit" className="btn btn-primary float-end">Registrera dig</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Register;
