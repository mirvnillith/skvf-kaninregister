import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Login from './Login'
import Register from './Register'
import Header from './Header'
import Notification from './common/Notification'
import Activation from './activation/Activation'
import {
    BrowserRouter,
    Routes,
    Route,
    Outlet,
    useNavigate
} from "react-router-dom";

const NoSession = () => {
    return (<Login setSession={appProps.setSession} setNotification={appProps.setNotification} />);
}
	
const Bunnies = () => {
    return (
        <div className="row">
            <div className="col-md-12 align-self-center p-4">
                <h2 className="text-center dark"> Mina Kaniner </h2>
           </div>
       </div>
    );
}

const sessionCookieExists = () => {
    return document.cookie.match(RegExp('(?:^|;\\s*)BunnyRegistryApi=([^;]*)'));
}

const appProps = {};

const App = () => {
    const [session, setSession] = useState({noSession: !sessionCookieExists()});
	appProps.session = session;
	appProps.setSession = setSession;
    const [notifications, setNotificationState] = useState([]);
    appProps.setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        setNotificationState(newNotificationState);
    }

	return (
        <Routes>
            <Route path="/" element={<Layout notifications={notifications} />}>
                <Route index element={<Login setSession={appProps.setSession} setNotification={appProps.setNotification} />} />
                <Route path="/login" element={<Login setSession={appProps.setSession} setNotification={appProps.setNotification} />} />
                <Route path="/register" element={<Register setSession={appProps.setSession} setNotification={appProps.setNotification} />} />
                <Route path="/bunnies" element={appProps.session.noSession ? <NoSession /> : <Bunnies session={appProps.session}/>} />
                <Route path="/activation/:ownerId" element={<Activation setSession={appProps.setSession} setNotification={appProps.setNotification} />} />
            </Route>
        </Routes>
    );
}

const Layout = (props) => {

    return (
        <div className="container-md px-0">
            <Header setNotification={appProps.setNotification} session={appProps.session} setSession={appProps.setSession}/>
            <div className="row">
                <div className="col-md-12 align-self-center p-4">
                    <h1 className="text-center green"> Kaninregister </h1>
                </div>
            </div>
            { props.notifications.map(({type, msg}, index) => {
                return (<Notification type={type} msg={msg} key={index}/>)
            })}
            <div className="container">
                <Outlet/>
            </div>
        </div>
    );
}

ReactDOM.render(
    <BrowserRouter>
        <App/>
    </BrowserRouter>, document.getElementById('app'));

