import React from 'react';
import useInterval from "../hooks/Interval";
import {approve} from "../utils/api";
import {useSession} from "../hooks/SessionContext";

const ApprovalOngoing = (props) => {
    const session = useSession();
	
    useInterval(async () => {
        console.log('Checking for approval status');
        await approve(session.user.id, props.approvedOwnerHandler, props.approvalFailedHandler, props.approvalOngoingHandler, props.setError);
    }, 3000);

    return (
		<div>
			<h2 className="text-center dark"> Godkännande av datahantering </h2>
		
			För att kunna använda kaninregistret måste du signera vår hantering av den information du kommer att skriva in.
			<p/>
			Länken nedan kommer att öppnas i ett nytt fönster där själva singeringen kommer att ske, med hjälp av BankID.
			Det är den som signerar som därmed godkänner att vi hanterar all information om denna ägare och dess kaniner
			enligt den beskrivning som kommer att visas.
			<p/>
			Efter du har signerat ska du gå tillbaka till det här fönstret för att komma vidare in till kaninregistret.
			<p/>
			<a rel="noopener noreferrer" href={props.approvalOngoing} target="_blank">Signering</a>
		</div>
	)
}

export default ApprovalOngoing;