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

    return (<a rel="noopener noreferrer" href={props.approvalOngoing} target="_blank">Den här länken kommer starta
        signeringen. Kom tillbaka hit när du har signerat så kommer du kunna lägga in kaniner i kaninregistret</a>)
}

export default ApprovalOngoing;