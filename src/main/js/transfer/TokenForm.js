import React from 'react';
import { useNavigate } from "react-router-dom";

const TokenForm = (props) => {

	const navigate = useNavigate();
	
    return (
        <div>
        	<h2 className="text-center dark">Överlåtelsekod</h2>
		
			Här är den överlåtelsekod du ska ge kaninens nya ägare:
			<p className="text-center dark large">	
			<code >{props.token}</code>
			</p>
			Du kommer att fortsätta se kaninen i din lista, tillsammans med koden och möjlighet att återta den,
			tills den nya ägaren slutfört ägarbytet.
			<p className="text-center">
            <button type="cancel" className="btn btn-primary" onClick={(_) => navigate('/bunnies')}>OK</button>
			</p>
        </div>
    );
}

export default TokenForm;