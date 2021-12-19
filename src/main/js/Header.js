import React from 'react';
import Logout from './Logout';
import Edit from './owner/Edit';
import Finder from './find/Finder';

const Header = (props) => {
	
	const noButtons = props.loading ||
						window.location.pathname.indexOf('/help') == 0 ||
						window.location.pathname.indexOf('/signOffline') == 0;
	const showButtons = !noButtons;
	
    return (
        <div className="row">
           <div className="col-sm-4 gx-4">
            <a href="https://skvf.se/" rel="noopener noreferrer" target="_blank"><img className="logo" src="/assets/logo.jpg"/></a>
           </div>
           <div className="col-sm-4 align-self-center">
				{showButtons  && <Finder />}
				<h1 className="text-center green"> Kaninregister </h1>
		   </div>
			{showButtons
			? 	<div className="col-sm-4 align-self-right">
					<div className="w-100">
               			<Logout />
               			<Edit />
					</div>
					<div className="mt-5">
						<br/>
						<span className="float-end"><a href="mailto:jonas201973@gmail.com?Subject=Åsikter om kaninregistret">Åsikter?</a></span>
					</div>
           		</div>
			:	<div className="col-sm-4 align-self-right">
					<div className="mt-5">
						<br/>
						<span className="float-end"><a href="mailto:jonas201973@gmail.com?Subject=Åsikter om kaninregistret">Åsikter?</a></span>
					</div>
				</div>
			}
       	</div>
    );
}

export default Header;