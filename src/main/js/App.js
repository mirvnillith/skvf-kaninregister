import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Login from './Login'
import Register from './Register'

const Header = () => {
    return (
        <div className="row">
           <div className="col-sm-4 gx-4">
            <img className="logo" src="assets/logo.jpg"/>
           </div>
           <div className="col-sm-8 align-self-center">
              <p className=""> Menu placeholder </p>
           </div>
       </div>
    );
}

const Content = (props) => {
    if (props.view === "login") {
        return <Login setView={props.setView}/>
    }
    if (props.view === "register") {
        return <Register setView={props.setView} setErrorMsg={props.setErrorMsg}/>
    }
}

const Error = (props) => {
   if (props.errorMsg) {
       return (
            <div className="alert alert-danger alert-dismissible fade show" role="alert">
                {props.errorMsg}
                <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
       );
   }
   else {
       return (null)
   }
}


const App = () => {
    const [view, setView] = useState("login");
    const [errorMsg, setErrorMsg] = useState("");

    return (
        <div className="container-md px-0">
            <Header />
            <div className="row">
                <div className="col-md-12 align-self-center p-4">
                    <h1 className="text-center green"> Kaninregister </h1>
               </div>
            </div>
            <Error errorMsg={errorMsg}/>
            <div className="container">
                <Content view={view} setView={setView} setErrorMsg={setErrorMsg}/>
            </div>
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById('app'));

