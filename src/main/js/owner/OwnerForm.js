import React from 'react';
import { useSession } from "../hooks/SessionContext";
import { Link } from "react-router-dom";
import useFormValidation from "../hooks/FormValidation";
import Help from "../help/Help";

const INITIAL_STATE = (session) => {
    return {
        name: session.user.name ? session.user.name : "",
        userName: session.user.userName ? session.user.userName : "",
        publicOwner: session.user.publicOwner ? session.user.publicOwner : "",
        email: session.user.email ? session.user.email : "",
        address: session.user.address ? session.user.address : "",
        phone: session.user.phone ? session.user.phone : "",
        breederName: session.user.breederName ? session.user.breederName : "",
        breederEmail: session.user.breederEmail ? session.user.breederEmail : "",
        publicBreeder: session.user.publicBreeder ? session.user.publicBreeder : ""
    }
};

const validate = (values) => {
    const errors = {};

    if (!values.name) {
        errors.name = "Du måste ange ett namn!";
    }
    if (!values.userName) {
        errors.userName = "Du måste ange ett användarnamn!";
    }

    return errors;
}

const OwnerForm = (props) => {

    const session = useSession();

    const {
        handleSubmit,
        handleChange,
        handleChangeProvideValue,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE(session), validate, props.submitHandler);

    return (
        <div className="row py-2">
            <form onSubmit={handleSubmit}>
                <div className="col-md-12">
                    <div className="row mb-2">
                        <label htmlFor="name" className="col-md-6 col-form-label large">Namn</label>
                        <div className="col-md-6">
                            <input autoFocus
                                   id="name"
                                   name="name"
                                   type="text"
                                   value={values.name}
                                   size="20"
                                   className={errors.name ? "form-control large is-invalid" : "form-control large"}
                                   onChange={handleChange}
                            />
                            {errors.name && <div className="invalid-feedback">{errors.name}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="userName" className="col-md-6 col-form-label">Användarnamn</label>
                        <div className="col-md-6">
                            <input autoFocus
                                   type="text"
                                   id="userName"
                                   name="userName"
                                   value={values.userName}
                                   size="20"
                                   className={errors.userName ? "form-control is-invalid" : "form-control"}
                                   onChange={handleChange}
                            />
                            {errors.userName && <div className="invalid-feedback">{errors.userName}</div>}
                        </div>
                    </div>
                    <fieldset>
                        <legend>Kontaktinformation</legend>
                        <div className="row mb-2">
                            <div className="col-md-12 fst-italic">
							Detta är vad upphittare av din kanin ska använda för att försöka nå dig, så var frikostig.
							</div>
						</div>
                        <div className="row mb-2">
                            <label htmlFor="publicOwner" className="col-md-6 form-check-label">Synlig som ägare för användare av kaninregistret <Help topic="publicprivate"/></label>
                            <div className="col-md-6">
                                <input
                                    id="publicOwner"
                                    name="publicOwner"
                                    type="checkbox"
                                    defaultChecked={values.publicOwner}
									className="form-check-input"
                                    onChange={(event => handleChangeProvideValue(event, event.target.checked))}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="email" className="col-md-6 col-form-label">E-post</label>
                            <div className="col-md-6">
                                <input
                                    id="email"
                                    name="email"
                                    type="text"
                                    value={values.email}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="phone" className="col-md-6 col-form-label">Telefon</label>
                            <div className="col-md-6">
                                <input
                                    id="phone"
                                    name="phone"
                                    type="text"
                                    value={values.phone}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="address" className="col-md-6 col-form-label">Adress</label>
                            <div className="col-md-6">
                                <input
                                    id="address"
                                    name="address"
                                    type="text"
                                    value={values.address}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </fieldset>
                    <fieldset>
                        <legend>Uppfödare</legend>
                        <div className="row mb-2">
                            <div className="col-md-12 fst-italic">
							När du är angiven som uppfödare av en kanin kan du här välja att visas upp med annat namn och/eller e-post.
							</div>
						</div>
                        <div className="row mb-2">
                            <label htmlFor="publicBreeder" className="col-md-6 form-check-label">Synlig som uppfödare för användare av kaninregistret <Help topic="publicprivate"/></label>
                            <div className="col-md-6">
                                <input 
									id="publicBreeder"
                                    name="publicBreeder"
                                    type="checkbox"
                                    defaultChecked={values.publicBreeder}
									className="form-check-input"
                                    onChange={(event => handleChangeProvideValue(event, event.target.checked))}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="breederName" className="col-md-6 col-form-label">Namn som uppfödare</label>
                            <div className="col-md-6">
                                <input
                                    id="breederName"
                                    name="breederName"
                                    type="text"
                                    value={values.breederName}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                        <div className="row mb-2">
                            <label htmlFor="breederEmail" className="col-md-6 col-form-label">E-post som
                                uppfödare</label>
                            <div className="col-md-6">
                                <input
                                    id="breederEmail"
                                    name="breederEmail"
                                    type="text"
                                    value={values.breederEmail}
                                    className="form-control"
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </fieldset>
                    <div className="row mt-2">
                        <div className="col-sm-8 align-self-end">
                            <Link className="link-primary" to="/changepassword">Vill du byta ditt lösenord?</Link>
							<br/>
                            <Link className="link-primary" to="/closeaccount">Vill du avsluta ditt konto?</Link>
                        </div>
                        <div className="col-sm-4">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting}>
                                {isSubmitting && <span className="spinner-border spinner-border-sm me-1"/>}
                                Spara
                            </button>
                            <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting}
                                    onClick={props.cancelHandler}>Avbryt
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default OwnerForm;
