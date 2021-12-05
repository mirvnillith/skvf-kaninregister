import React, { useState } from 'react';
import { useSession } from "../hooks/SessionContext";

const OwnerForm = (props) => {
	
	const session = useSession();
	
    const [name, setName] = useState(session.user.name ? session.user.name : "");
    const [userName, setUserName] = useState(session.user.userName ? session.user.userName : "");
    const [publicOwner, setPublicOwner] = useState(session.user.publicOwner ? session.user.publicOwner : "");
    const [email, setEmail] = useState(session.user.email ? session.user.email : "");
    const [address, setAddress] = useState(session.user.address ? session.user.address : "");
    const [phone, setPhone] = useState(session.user.phone ? session.user.phone : "");
    const [breederName, setBreederName] = useState(session.user.breederName ? session.user.breederName : "");
    const [breederEmail, setBreederEmail] = useState(session.user.breederEmail ? session.user.breederEmail : "");
    const [publicBreeder, setPublicBreeder] = useState(session.user.publicBreeder ? session.user.publicBreeder : "");
    const [isValidated, setIsValidated] = useState(false);
    const [submit, setSubmit] = useState(true);

    const submitHandler = async (e) => {
		setSubmit(false);
        e.preventDefault();
        setIsValidated(true);
        if (name && userName) {
			const owner = {};
			owner.name = name;
			owner.userName = userName;
			owner.publicOwner = publicOwner;
			owner.email = email;
			owner.address = address;
			owner.phone = phone;
			owner.breederName = breederName;
			owner.breederEmail = breederEmail;
			owner.publicBreeder = publicBreeder;
            await props.submitForm(owner);
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
                    <div className="row mb-2">
                        <label htmlFor="userName" className="col-md-6 col-form-label">Användarnamn</label>
                        <div className="col-md-6">
                            <input autoFocus
                                type="text"
								size="20"
                                className={"form-control" + (isValidated && userName === "" ? " is-invalid" : "")}
                                id="userName"
								value={userName}
                                onChange={e => setUserName(e.target.value)}
                            />
                            <div className="invalid-feedback">
                                Du måste ange ett användarnamn!
                            </div>
                        </div>
                    </div>
					<fieldset> <legend>Kontaktinformation</legend>
                    <div className="row mb-2">
                        <label htmlFor="publicOwner" className="col-md-6 col-form-label">Synlig som ägare för användare av kaninregistret</label>
                        <div className="col-md-6">
                            <input
                                type="checkbox"
								defaultChecked={publicOwner}
                                id="publicOwner"
                                onChange={e => setPublicOwner(e.target.checked)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="email" className="col-md-6 col-form-label">E-post</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="email"
								value={email}
                                onChange={e => setEmail(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="phone" className="col-md-6 col-form-label">Telefon</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="phone"
								value={phone}
                                onChange={e => setPhone(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="address" className="col-md-6 col-form-label">Adress</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="address"
								value={address}
                                onChange={e => setAddress(e.target.value)}
                            />
                        </div>
                    </div>
					</fieldset>
					<fieldset> <legend>Uppfödare</legend>
                    <div className="row mb-2">
                        <label htmlFor="publicBreeder" className="col-md-6 col-form-label">Synlig som uppfödare för användare av kaninregistret</label>
                        <div className="col-md-6">
                            <input
                                type="checkbox"
								defaultChecked={publicBreeder}
                                id="publicBreeder"
                                onChange={e => setPublicBreeder(e.target.checked)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="breederName" className="col-md-6 col-form-label">Namn som uppfödare</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="breederName"
								value={breederName}
                                onChange={e => setBreederName(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="breederEmail" className="col-md-6 col-form-label">E-post som uppfödare</label>
                        <div className="col-md-6">
                            <input
                                type="text"
                                className="form-control"
                                id="breederEmail"
								value={breederEmail}
                                onChange={e => setBreederEmail(e.target.value)}
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

export default OwnerForm;
