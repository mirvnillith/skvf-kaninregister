import React from 'react';
import useFormValidation from "../hooks/FormValidation";

const INITIAL_STATE = {
    subject: "",
    success: true
};

const validate = (values) => {
    const errors = {};

    if (values.success && !values.subject) {
        errors.subject = "Du måste ange ett namn!";
    }

    return errors;
}

const SignOfflineForm = (props) => {

    const {
        handleSubmit,
        handleChange,
        handleChangeProvideValue,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, props.submitHandler);

    return (
        <div className="row py-2">
            <form onSubmit={handleSubmit} >
                <div className="col-md-12 mb-5">
                    <h2>Fejkad signering</h2>
					Den här sidan är istället för en riktig BankID-signering i Addo för att kunna testa signeringsflödet.
                </div>
                <div className="col-md-12 mt-5">
                    <div className="row mb-2">
                        <div className="col-md-12">
                            <input
                                id="success"
                                name="success"
                                type="radio"
                                className="form-check-input me-3"
								checked={values.success}
                                onChange={(event => handleChangeProvideValue(event, event.target.checked))}
                            />
							<label htmlFor="success" className="form-check-label">
								Lyckad signering. Namn på den som signerar:
							</label>
                            <input autoFocus
                                id="subject"
                                name="subject"
                                type="text"
                                value={values.subject}
                                className={"form-control ms-5 mt-2 w-50" + (errors.subject ? " is-invalid" : "")}
								disabled={!values.success}
                                onChange={handleChange}
                            />
                            {errors.subject && <div className="invalid-feedback ms-5">{errors.subject}</div>}
						</div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-md-12">
                            <input
                                id="failure"
                                name="success"
                                type="radio"
                                className="form-check-input me-3"
								checked={!values.success}
                                onChange={(event) => handleChangeProvideValue(event, !event.target.checked)}
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
                            <button type="submit" className="btn btn-primary float-start" disabled={isSubmitting} >
                                { isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
                                Signera
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default SignOfflineForm;
