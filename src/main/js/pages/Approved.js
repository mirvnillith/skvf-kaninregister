import React, {useState, useEffect} from 'react';
import { useSession } from "../utils/SessionContext";

const Approved = (props) => {

    const session = useSession();

    return (<div>
            <p>Signering är godkänd!</p>
        </div>
    );
}
export default Approved;