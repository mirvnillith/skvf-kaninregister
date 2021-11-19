import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Login from './Login'
import Register from './Register'
import Header from './Header'
import Notification from './common/Notification'
import { existingSession } from './utils/api';

const DefaultContent = (props) => {
    const [view, setView] = useState("login");
    if (view === "login") {
        return <Login setView={setView} setSession={props.setSession} setNotification={props.setNotification}/>
    }
    if (view === "register") {
        return <Register setView={setView} setNotification={props.setNotification} setSession={props.setSession}/>
    }
}

const SessionContent = (props) => {
    return (
        <div className="row">
            <div className="col-md-12 align-self-center p-4">
                <h2 className="text-center dark"> Mina Kaniner </h2>
           </div>
       </div>
    );
}

const Content = (props) => {
    if (props.session.noSession) {
        return <DefaultContent setNotification={props.setNotification} setSession={props.setSession}/>
    }
    else {
        return <SessionContent session={props.session} />
    }
}

const App = () => {
    const [session, setSession] = useState(existingSession());
    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        setNotificationState(newNotificationState);
    }

    return (
        <div className="container-md px-0">
            <Header setNotification={setNotification} session={session} setSession={setSession}/>
            <div className="row">
                <div className="col-md-12 align-self-center p-4">
                    <h1 className="text-center green"> Kaninregister </h1>
               </div>
            </div>
            { notifications.map(({type, msg}, index) => {
                return (<Notification type={type} msg={msg} key={index}/>)
            })}
            <div className="container">
                <Content setNotification={setNotification} session={session} setSession={setSession}/>
            </div>
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById('app'));

