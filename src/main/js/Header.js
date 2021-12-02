import React from 'react';
import Logout from './Logout';

const Header = (props) => {
    return (
        <div className="row">
           <div className="col-sm-4 gx-4">
            <img className="logo" src="/assets/logo.jpg"/>
           </div>
           <div className="col-sm-4 align-self-center">
              <p className=""> Menu placeholder </p>
           </div>
           <div className="col-sm-4 align-self-right">
               <Logout />
           </div>
       </div>
    );
}

export default Header;