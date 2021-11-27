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
	Navigate
} from "react-router-dom";

const WithoutSession = (props) => {
    return props.session.noSession
			? props.element
			: <Navigate to="/bunnies" />
}

const RequiresSession = (props) => {
    return props.session.noSession
			? <Navigate to="/login" />
			: props.element;
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
    return document.cookie.indexOf('BunnyRegistryApi=') != -1;
}

const App = () => {
    const [session, setSession] = useState({noSession: !sessionCookieExists()});
    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        setNotificationState(newNotificationState);
    }

	return (
        <Routes>
            <Route path="/" element={<Layout session={session} setSession={setSession} notifications={notifications} />}>
                <Route index element={session.noSession ? <Navigate to="/login" /> : <Navigate to="/bunnies" />} />
                <Route path="/login" element={<WithoutSession session={session} element={ <Login setSession={setSession} setNotification={setNotification}/> }/>}/>
                <Route path="/register" element={<WithoutSession session={session} element={ <Register setSession={setSession} setNotification={setNotification}/> }/>}/>
                <Route path="/bunnies" element={<RequiresSession session={session} element={ <Bunnies session={session}/> }/>}/>
                <Route path="/activation/:ownerId" element={<Activation setSession={setSession} setNotification={setNotification} />} />
                <Route path="/*" element={<Navigate replace to="/" />} />
            </Route>
        </Routes>
    );
}

const Layout = (props) => {

    return (
        <div className="container-md px-0">
            <Header setNotification={props.setNotification} session={props.session} setSession={props.setSession} />
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

