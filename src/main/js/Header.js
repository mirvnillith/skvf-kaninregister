import React from 'react';
import Logout from './Logout';
import Edit from './owner/Edit';
import { useNavigate } from "react-router-dom";

const Header = (props) => {
	
	const navigate = useNavigate();
	
    return (
        <div className="row">
           <div className="col-sm-4 gx-4">
            <img className="logo" src="/assets/logo.jpg"/>
           </div>
           <div className="col-sm-4 align-self-center">
				{window.location.pathname.indexOf('/find') !== 0
				?	<button className="btn btn-success w-100 py-2" onClick={(_) => navigate('/find')}>Jag har hittat en märkt kanin</button>
				:	null
				}
				<h1 className="text-center green"> Kaninregister </h1>
		   </div>
			{props.loading
			? 	null
           	:	<div className="col-sm-4 align-self-right">
               		<Logout />
               		<Edit />
           		</div>}
       	</div>
    );
}

export default Header;