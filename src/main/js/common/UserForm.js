import React, { useState } from 'react';

const UserForm = (props) => {
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [isValidated, setIsValidated] = useState(false);

    const submitHandler = async (e) => {
        e.preventDefault();
        setIsValidated(true);
        if (name && surname) {
            await props.submitForm(name, surname);
        }
    }

    return (
        <div className="row py-2">
            <form onSubmit={submitHandler} >
                <div className="col-md-12">
                    <h2 >Mitt Konto</h2>
                </div>
                <div className="col-md-12">
                    <div className="row mb-2">
                        <label htmlFor="name" className="col-md-6 col-form-label">Namn</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                className={isValidated && name === "" ? "form-control is-invalid" : "form-control"}
                                id="name"
                                onChange={e => setName(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange ett namn!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="surname" className="col-md-6 col-form-label">Lösenord</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className={isValidated && surname === "" ? "form-control is-invalid" : "form-control"}
                                id="surname"
                                onChange={e => setSurname(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange ett efternamn!
                            </div>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-12">
                            <button type="submit" className="btn btn-primary float-end" >Spara</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default UserForm;
