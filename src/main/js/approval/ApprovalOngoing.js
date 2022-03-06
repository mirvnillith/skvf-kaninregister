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
			<h2 className="text-center dark"> Samtycke av personuppgiftshantering </h2>
		
			För att kunna använda kaninregistret måste du signera ett samtycke av vår hantering av den information du kommer att skriva in.
			<p/>
			Länken nedan kommer att öppnas i ett nytt fönster där själva signeringen kommer att ske, med hjälp av BankID.
			Det är den som signerar som samtycker till att vi hanterar all information om denna ägare och dess kaniner
			enligt den beskrivning som kommer att visas.
			För barn, utan BankID, krävs därmed att en förälder deltar i skapandet av kontot och då står som samtyckande.
			<p/>
			Efter du har signerat ska du gå tillbaka till det här fönstret för att komma vidare in till kaninregistret.
			<p/>
			<a rel="noopener noreferrer" href={props.approvalOngoing} target="_blank">Signering</a>
		</div>
	)
}

export default ApprovalOngoing;