import React from 'react';
import useFormValidation from "../hooks/FormValidation";

const TransferForm = (props) => {

    const {
        handleSubmit,
        isSubmitting
    } = useFormValidation({}, (_) => {return {}}, props.submitHandler);

    return (
        <div>
        	<h2  className="text-center dark">Ägarbyte</h2>

			När du nu ska lämna över din kanin till en ny ägare kommer du att få en överlåtelsekod.
			Denna kod ska du ge till nya ägaren så att hen själv kan koppla kaninen till sig i registret.
			Om nya ägaren inte har ett konto i kaninregistret kan hen enkelt registrera sig för att
			kunna ta emot sin nya kanin.
			<p/>
			Koden kan du exempelvis skicka via SMS eller mail. Annars räcker det att du skriver ner den och skickar med på en lapp.
			Den är tillfällig, men inte tidsbegränsad, och gäller alltså bara för just den här kaninen.
			<p/>
			Om du gjort detta av misstag, eller om ägarbytet inte går igenom, så kan du alltid återta din kanin, så länge
			nya ägaren inte använt koden på sin sida.
            <form onSubmit={handleSubmit} >
                <div className="row mt-2">
                    <div className="col-sm-8 align-self-end"/>
                    <div className="col-sm-4">
                        <button type="submit" className="btn btn-primary float-end"  disabled={isSubmitting} >
                            { isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
                            Överlåt
                        </button>
                        <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={props.cancelHandler}>Avbryt</button>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default TransferForm;