import React, { useState, useEffect } from 'react';
import BunniesForm from './BunniesForm';
import { useSession } from "../utils/SessionContext";
import { getBunnies } from "../utils/api";
import Spinner from "react-bootstrap/Spinner";

const Bunnies = (props) => {

    const [bunnies, setBunnies] = useState();

    const session = useSession();

    const setError = (msg) => props.setNotification([{type: "danger", msg: msg}]);

    const onBunnies = (bunnies) => {
        setBunnies(bunnies.bunnies);
    }

	useEffect(() => {
		if (bunnies === undefined) {
			getBunnies(session.user.id, onBunnies, setError);
		}
	});
	
    return (
		bunnies === undefined
		? <Spinner animation="border" role="status"> <span className="visually-hidden">laddar innehåll...</span> </Spinner>
		: <BunniesForm bunnies={bunnies}/>
    );
}

export default Bunnies;