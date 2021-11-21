import React, { useState, useEffect } from 'react';
import Spinner from "react-bootstrap/Spinner";
import ReactDOM from 'react-dom';
import Login from './pages/Login'
import Register from './pages/Register'
import Header from './components/Header'
import Notification from './components/Notification'
import CheckApproval from "./pages/CheckApproval";
import {existingSession} from './utils/api';

const NoSessionContent = (props) => {
    const [view, setView] = useState("login");
    if (view === "login") {
        return <Login setView={setView} setSession={props.setSession} setNotification={props.setNotification}/>
    }
    if (view === "register") {
        return <Register setView={setView} setNotification={props.setNotification} setSession={props.setSession}/>
    }
}

const SessionContent = (props) => {
    return <CheckApproval setNotification={props.setNotification} session={props.session} setSession={props.setSession}/>
}

const Content = (props) => {
    if (props.session.noSession) {
        return <NoSessionContent setNotification={props.setNotification} setSession={props.setSession}/>
    }
    else {
        return <SessionContent setNotification={props.setNotification} session={props.session} setSession={props.setSession}/>
    }
}

const App = () => {
    //TODO: useContext instead of useState for the session so we don't need to send it down into all components
    const [session, setSession] = useState(undefined);
    useEffect(() => {
        existingSession(setSession);
    }, []);

    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        setNotificationState(newNotificationState);
    }

    console.log(session);

    return (session === undefined ?
            <Spinner animation="border" role="status">
                <span className="visually-hidden">laddar innehåll...</span>
            </Spinner> :
            <div className="container-md px-0">
                <Header setNotification={setNotification} session={session} setSession={setSession}/>
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

