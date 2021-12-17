import React, { useState } from 'react';
import { useSession } from "../hooks/SessionContext";

const CloseAccountForm = (props) => {
    const session = useSession();

	const [isConfirming, setConfirming] = useState(false);
	const [isConfirmingUnapprove, setConfirmingUnapprove] = useState(false);
	const [isConfirmingDeactivate, setConfirmingDeactivate] = useState(false);
	const [isConfirmingDelete, setConfirmingDelete] = useState(false);
	const [isSubmitting, setSubmitting] = useState(false);
		
    const cancelConfirm = async (_) => {
		setConfirmingUnapprove(false);
		setConfirmingDeactivate(false);
		setConfirmingDelete(false);
		setConfirming(false);
		setSubmitting(false);
	}
	
    const confirmUnapprove = async (_) => {
		setConfirming(true);
		setConfirmingUnapprove(true);
	}
    const submitUnapprove = async (_) => {
		setSubmitting(true);
		await props.unapprove(cancelConfirm);
	}
	
    const confirmDeactivate = async (_) => {
		setConfirming(true);
		setConfirmingDeactivate(true);
	}
    const submitDeactivate = async (_) => {
		setSubmitting(true);
		await props.deactivate(cancelConfirm);
	}
	
    const confirmDelete = async (_) => {
		setConfirming(true);
		setConfirmingDelete(true);
	}
    const submitDelete = async (_) => {
		setSubmitting(true);
		await props.delete(cancelConfirm);
	}

    return (
		<div>
			<h2 className="text-center dark"> Avsluta konto </h2>
		
			Du har {session.user.approved ? "tre":"två"} alternativ när det gäller att avsluta ditt konto som ägare i kaninregistret:
			<p/>
			{session.user.approved && <div>
				<b>Avsignera</b>
				<p/>
				<ul>
				<li> Alla dina kontouppgifter tas bort och du visas som Okänd ägare </li>
				<li> Du behåller din inloggning till Kaninregistret, men behöver åter godkänna datahantering för att kunna ändra på något </li>
				<li> Alla dina kaniner ligger kvar opåverkade i registret </li>
				</ul>
				{isConfirmingUnapprove
				?	<div className="h-100 d-flex justify-content-end">
						<button className="btn btn-secondary me-2 float-end mt-auto" onClick={cancelConfirm} disabled={isSubmitting}>Avbryt</button>
						<button className="btn btn-danger float-end mt-auto" onClick={submitUnapprove} disabled={isSubmitting}>
							{ isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
							Bekräfta avsignering
						</button>
					</div>
				:	<div className="h-100 d-flex justify-content-end">
						<button className="btn btn-warning me-2 float-end mt-auto" onClick={confirmUnapprove} disabled={isConfirming}>Avsignera</button>
					</div>
				}
			</div>}
			<b>Deaktivera</b>
			<p/>
			<ul>
			<li> Alla dina kontouppgifter tas bort och du visas som Okänd ägare </li>
			<li> Din inloggning till Kaninregistret tas bort och du behöver kontakta Kaninregistret för att kunna återaktivera </li>
			<li> Alla dina kaniner ligger kvar opåverkade i registret </li>
			</ul>
			{isConfirmingDeactivate
			?	<div className="h-100 d-flex justify-content-end">
					<button className="btn btn-secondary me-2 float-end mt-auto" onClick={cancelConfirm} disabled={isSubmitting}>Avbryt</button>
					<button className="btn btn-danger float-end mt-auto" onClick={submitDeactivate} disabled={isSubmitting}>
						{ isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
						Bekräfta deaktivering
					</button>
				</div>
			:	<div className="h-100 d-flex justify-content-end">
					<button className="btn btn-warning me-2 float-end mt-auto" onClick={confirmDeactivate} disabled={isConfirming}>Deaktivera</button>
				</div>
			}
			<b>Radera</b>
			<ul>
			<li> Ditt konto tas helt bort ur Kaninregistret</li>
			<li> Du får inte ha några kaniner registrerade för att kunna göra detta </li>
			</ul>
			{isConfirmingDelete
			?	<div className="h-100 d-flex justify-content-end">
					<button className="btn btn-secondary me-2 float-end mt-auto" onClick={cancelConfirm} disabled={isSubmitting}>Avbryt</button>
					<button className="btn btn-danger float-end mt-auto" onClick={submitDelete} disabled={isSubmitting}>
						{ isSubmitting && <span className="spinner-border spinner-border-sm me-1" /> }
						Bekräfta radering
					</button>
				</div>
			:	<div className="h-100 d-flex justify-content-end">
					<button className="btn btn-warning me-2 float-end mt-auto" onClick={confirmDelete} disabled={isConfirming}>Radera</button>
				</div>
			}
			<p/>
			<button type="cancel" className="btn btn-secondary" disabled={isConfirming} onClick={props.cancel}>Avbryt</button>
		</div>
	)
}

export default CloseAccountForm;