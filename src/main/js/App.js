import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Login from './Login'
import Header from './Header'
import Notification from './Notification'
import Register from './Register'

const Content = (props) => {
    if (props.view === "login") {
        return <Login setView={props.setView}/>
    }
    if (props.view === "register") {
        return <Register setView={props.setView} setNotification={props.setNotification}/>
    }
}

const App = () => {
    const [view, setView] = useState("login");
    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        console.error(newNotificationState);
        setNotificationState(newNotificationState);
    }
    useEffect(() => {
        console.error(notifications)
    }, [notifications])

    return (
        <div className="container-md px-0">
            <Header />
            <div className="row">
                <div className="col-md-12 align-self-center p-4">
                    <h1 className="text-center green"> Kaninregister </h1>
               </div>
            </div>
            { notifications.map(({type, msg}, index) => {
                return (<Notification type={type} msg={msg} key={index}/>)
            })}
            <div className="container">
                <Content view={view} setView={setView} setNotification={setNotification}/>
            </div>
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById('app'));

