import React, { useState } from 'react';

const SignOfflineForm = (props) => {
    const [subject, setSubject] = useState("");
    const [success, setSuccess] = useState(true);
    const [isValidated, setIsValidated] = useState(false);
    const [submit, setSubmit] = useState(true);

    const submitHandler = async (e) => {
		setSubmit(false);
		e.preventDefault();
        setIsValidated(true);
        if (!success || subject) {
            await props.submitForm(subject, success);
        }
		setSubmit(true);
    }

    return (
        <div className="row py-2">
            <form onSubmit={submitHandler} >
                <div className="col-md-12 mb-5">
                    <h2>Fejkad signering</h2>
					Den här sidan är istället för en riktig BankID-signering i Addo för att kunna testa signeringsflödet.
                </div>
                <div className="col-md-12 mt-5">
                    <div className="row mb-2">
                        <div className="col-md-12">
                            <input
                                type="radio"
                                className="form-check-input me-3"
								checked={success}
                                id="success"
                                onChange={e => setSuccess(e.target.value)}
                            />
							<label htmlFor="success" className="form-check-label">
								Lyckad signering. Namn på den som signerar:
							</label>
                            <input
                                type="text"
                                className={ "form-control ms-5 w-50"+(isValidated && success && subject === "" ? " is-invalid" : "") }
                                id="subject"
								disabled={!success}
                                onChange={e => setSubject(e.target.value)}
                            />
                            <div className="invalid-feedback ms-5">
                                Du måste ange ett namn!
                            </div>
						</div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-md-12">
                            <input
                                type="radio"
                                className="form-check-input me-3"
								checked={!success}
                                id="failure"
                                onChange={e => setSuccess(!e.target.value)}
                            />
							<label htmlFor="failure" className="form-check-label">
								Misslyckad signering
							</label>
                        </div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-sm-8 align-self-end">
                            <p className="mb-0">&nbsp;</p>
                        </div>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-start" disabled={!submit} >Signera</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default SignOfflineForm;
