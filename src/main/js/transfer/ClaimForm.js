import React from 'react';
import useFormValidation from "../hooks/FormValidation";

const INITIAL_STATE = {
    token: ""
};

const validate = (values) => {
    const errors = {};

    if (!values.token) {
        errors.token = "Du måste ange en kod!";
    }
    return errors;
}

const ReclaimForm = (props) => {

    const {
        handleSubmit,
        handleChange,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, props.submitHandler);

    return (
        <div>
        	<h2  className="text-center dark">Överta kanin</h2>

			För att överta en registrerad kanin behöver du en överlåtelsekod som du ska ha fått från föregående ägare.
			
            <div className="col-md-12">
            <form onSubmit={handleSubmit} >
				<div className="row mt-3 mb-3">
                    <label htmlFor="token" className="col-md-6 col-form-label">Överlåtelsekod</label>
                    <div className="col-md-6">
                        <input autoFocus
                            id="token"
                            name="token"
                            type="text"
						value={values.token}
                            className={"form-control" + (errors.token ? " is-invalid" : "")}
                            onChange={handleChange}
                        />
					{errors.token && <div className="invalid-feedback">{errors.token}</div>}
                    </div>
                </div>
	            <div className="row mt-2">
	                <div className="col-sm-8 align-self-end"/>
	                <div className="col-sm-4">
	                    <button type="submit" className="btn btn-primary float-end"  disabled={isSubmitting} >
	                        { isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
	                        Överta
	                    </button>
	                    <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={props.cancelHandler}>Avbryt</button>
	                </div>
	            </div>
            </form>
			</div>
        </div>
    );
}

export default ReclaimForm;