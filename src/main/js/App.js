import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Spinner from "react-bootstrap/Spinner";
import Login from './login/Login'
import Register from './register/Register'
import Header from './Header'
import Activation from './activation/Activation'
import Bunnies from './bunnies/Bunnies'
import Bunny from './bunny/Bunny'
import Owner from './owner/Owner'
import {
    BrowserRouter,
    Routes,
    Route,
    Outlet,
	Navigate
} from "react-router-dom";
import { existingSession } from './utils/api';
import {
    maybeOngoingSession,
    useSession,
    useSessionUpdater,
    SessionProvider
} from "./hooks/SessionContext";
import CheckApproval from "./approval/CheckApproval";
import Notifications from "./common/Notifications";
import { NotificationProvider } from "./hooks/NotificationContext";

const WithoutSession = (props) => {
    const session = useSession();
    return session === undefined
			? props.element
			: <Navigate to="/approval" />
}

const RequiresSession = (props) => {
    const session = useSession();
    return session === undefined
			? <Navigate to="/login" />
			: props.element;
}
	
const App = () => {
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
                <Route path="/" element={<Layout />}>
                    <Route index element={session === undefined ? <Navigate to="/login" /> : <Navigate to="/bunnies" />} />
                    <Route path="/login" element={<WithoutSession element={ <Login /> }/>}/>
                    <Route path="/register" element={<WithoutSession element={ <Register /> }/>}/>
                    <Route path="/approval" element={<RequiresSession element={ <CheckApproval /> }/>}/>
                    <Route path="/owner" element={<RequiresSession element={ <Owner /> }/>}/>
                    <Route path="/bunnies" element={<RequiresSession element={ <Bunnies /> }/>}/>
                    <Route path="/bunny" element={<RequiresSession element={ <Bunny /> }/>}/>
                    <Route path="/bunny/:bunnyId" element={<RequiresSession element={ <Bunny /> }/>}/>
                    <Route path="/activation/:ownerId" element={<Activation />} />
                    <Route path="/*" element={<Navigate replace to="/" />} />
                </Route>
              </Routes>
    );
}

const Layout = () => {

    return (
        <div className="container-md px-0">
            <Header />
            <Notifications />
            <div className="container">
                <Outlet/>
            </div>
        </div>
    );
}

ReactDOM.render(
    <SessionProvider>
        <NotificationProvider>
            <BrowserRouter>
                <App/>
            </BrowserRouter>
        </NotificationProvider>
    </SessionProvider>, document.getElementById('app'));

