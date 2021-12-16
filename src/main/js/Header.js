import React from 'react';
import Logout from './Logout';
import Edit from './owner/Edit';

const Header = (props) => {
    return (
        <div className="row">
           <div className="col-sm-4 gx-4">
            <img className="logo" src="/assets/logo.jpg"/>
           </div>
           <div className="col-sm-4 align-self-center">
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