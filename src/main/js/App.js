import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Login from './pages/Login'
import Register from './pages/Register'
import Header from './components/Header'
import Notification from './components/Notification'
import CheckApproval from "./pages/CheckApproval";

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

const sessionCookieExists = () => {
    return document.cookie.match(RegExp('(?:^|;\\s*)BunnyRegistryApi=([^;]*)'));
}

const App = () => {
    //TODO: useContext instead of useState for the session so we don't need to send it down into all components
    const [session, setSession] = useState({noSession: !sessionCookieExists()});
    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        setNotificationState(newNotificationState);
    }

    return (
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

