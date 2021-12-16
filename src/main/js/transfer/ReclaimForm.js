import React from 'react';
import useFormValidation from "../hooks/FormValidation";

const ReclaimForm = (props) => {

    const {
        handleSubmit,
        isSubmitting
    } = useFormValidation({}, (_) => {return {}}, props.submitHandler);

    return (
        <div>
        	<h2  className="text-center dark">Återta kanin</h2>

			Att återta din kanin betyder att du avbryter ägarbytet och överlåtelsekoden blir ogiltig (vid nästa ägarbyte skapas en ny kod).
			Anledningar att göra detta kan vara om kaninen inte längre ska byta ägare eller du påbörjat ett byte av misstag.
            <form onSubmit={handleSubmit} >
                <div className="row mt-2">
                    <div className="col-sm-8 align-self-end"/>
                    <div className="col-sm-4">
                        <button type="submit" className="btn btn-primary float-end"  disabled={isSubmitting} >
                            { isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
                            Återta
                        </button>
                        <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={props.cancelHandler}>Avbryt</button>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default ReclaimForm;