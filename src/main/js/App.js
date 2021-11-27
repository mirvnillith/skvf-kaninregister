import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Spinner from "react-bootstrap/Spinner";
import Login from './login/Login'
import Register from './register/Register'
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
import {existingSession} from './utils/api';
import {
    maybeOngoingSession,
    useSession,
    useSessionUpdater,
    SessionProvider
} from "./utils/SessionContext";

const WithoutSession = (props) => {
    const session = useSession();
    return session === undefined
			? props.element
			: <Navigate to="/bunnies" />
}

const RequiresSession = (props) => {
    const session = useSession();
    return session === undefined
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

const App = () => {
    const [notifications, setNotificationState] = useState([]);
    const setNotification = (notification) => {
        setNotificationState(notification);
    }

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

    return (loading
            ? <Spinner animation="border" role="status">
                <span className="visually-hidden">laddar innehåll...</span>
            </Spinner>
            : <Routes>
                <Route path="/" element={<Layout notifications={notifications} />}>
                    <Route index element={session === undefined ? <Navigate to="/login" /> : <Navigate to="/bunnies" />} />
                    <Route path="/login" element={<WithoutSession element={ <Login setNotification={setNotification}/> }/>}/>
                    <Route path="/register" element={<WithoutSession element={ <Register setNotification={setNotification}/> }/>}/>
                    <Route path="/bunnies" element={<RequiresSession element={ <Bunnies /> }/>}/>
                    <Route path="/activation/:ownerId" element={<Activation setNotification={setNotification} />} />
                    <Route path="/*" element={<Navigate replace to="/" />} />
                </Route>
              </Routes>
    );
}

const Layout = (props) => {

    return (
        <div className="container-md px-0">
            <Header setNotification={props.setNotification} />
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
    <SessionProvider>
        <BrowserRouter>
            <App/>
        </BrowserRouter>
    </SessionProvider>, document.getElementById('app'));

