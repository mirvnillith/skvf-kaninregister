import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Login from './Login'
import Register from './Register'
import Header from './Header'
import Notification from './common/Notification'
import Activation from './activation/Activation'
import {useRoutes, useRedirect, navigate} from 'hookrouter'

const NoSession = () => {
	navigate("/login");
	return null;
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
const routes = {
	'/login': () => <Login setSession={appProps.setSession} setNotification={appProps.setNotification} />,
	'/register': () => <Register setSession={appProps.setSession} setNotification={appProps.setNotification} />,
	'/bunnies': () => appProps.session.noSession ? <NoSession /> : <Bunnies session={appProps.session} />,
	'/activation/:ownerId': ({ownerId}) => <Activation setSession={appProps.setSession} setNotification={appProps.setNotification} ownerId={ownerId}/>,
	'/*': () => <NoSession />
}

const App = () => {
    const [session, setSession] = useState({noSession: !sessionCookieExists()});
	appProps.session = session;
	appProps.setSession = setSession;
    const [notifications, setNotificationState] = useState([]);
    appProps.setNotification = (notification) => {
        const newNotificationState = [...notifications, notification];
        setNotificationState(newNotificationState);
    }

	useRedirect('/', '/login');
	const route = useRoutes(routes);
	
    return (
        <div className="container-md px-0">
            <Header setNotification={appProps.setNotification} session={appProps.session} setSession={appProps.setSession}/>
            <div className="row">
                <div className="col-md-12 align-self-center p-4">
                    <h1 className="text-center green"> Kaninregister </h1>
               </div>
            </div>
            { notifications.map(({type, msg}, index) => {
                return (<Notification type={type} msg={msg} key={index}/>)
            })}
            <div className="container">
				{route}
            </div>
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById('app'));

