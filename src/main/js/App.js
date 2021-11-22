import React, { useState, useEffect } from 'react';
import Spinner from "react-bootstrap/Spinner";
import ReactDOM from 'react-dom';
import Login from './pages/Login'
import Register from './pages/Register'
import Header from './components/Header'
import Notification from './components/Notification'
import CheckApproval from "./pages/CheckApproval";
import {existingSession} from './utils/api';
import {
    maybeOngoingSession,
    useSession,
    useSessionUpdater,
    SessionProvider
} from "./utils/SessionContext";

const NoSessionContent = (props) => {
    const [view, setView] = useState("login");
    if (view === "login") {
        return <Login setView={setView} setNotification={props.setNotification} />
    }
    if (view === "register") {
        return <Register setView={setView} setNotification={props.setNotification} />
    }
}

const SessionContent = (props) => {
    return <CheckApproval setNotification={props.setNotification} />
}

const Content = (props) => {
    const [loading, setLoading] = useState(true);
    const session = useSession();
    const sessionUpdater = useSessionUpdater();

    // On initial rendering, or a refresh of the browser, we retrieve any initial session content
    useEffect(() => {
        if (maybeOngoingSession()) {
            existingSession((sessionContent) => {
                sessionUpdater(sessionContent);
                setLoading(false);
            });
        }
        else {
            sessionUpdater(undefined);
            setLoading(false);
        }
    }, []);

    return (loading ?
            <Spinner animation="border" role="status">
                <span className="visually-hidden">laddar innehåll...</span>
            </Spinner> :
            session ?
                <SessionContent setNotification={props.setNotification} />:
                <NoSessionContent setNotification={props.setNotification} />
    );
}

const App = () => {
    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        setNotificationState(notification);
    }

    return (
        <SessionProvider>
            <div className="container-md px-0">
                <Header setNotification={setNotification} />
                { notifications.map(({type, msg}, index) => {
                    return (<Notification type={type} msg={msg} key={index}/>)
                })}
                <div className="container">
                    <Content setNotification={setNotification} />
                </div>
            </div>
        </SessionProvider>
    );
}

ReactDOM.render(<App />, document.getElementById('app'));

