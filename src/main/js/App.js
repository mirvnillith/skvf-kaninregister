import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import Spinner from "react-bootstrap/Spinner";
import Login from './login/Login'
import Register from './register/Register'
import RecoverPassword from './recoverpassword/RecoverPassword'
import Header from './Header'
import Activation from './activation/Activation'
import Bunnies from './bunnies/Bunnies'
import Bunny from './bunny/Bunny'
import Profile from './profile/Profile'
import Owner from './owner/Owner'
import CloseAccount from './closeaccount/CloseAccount'
import Transfer from './transfer/Transfer'
import Reclaim from './transfer/Reclaim'
import Claim from './transfer/Claim'
import Find from './find/Find'
import HelpPicture from './help/HelpPicture'
import HelpPublicPrivate from './help/HelpPublicPrivate'
import HelpProfile from './help/HelpProfile'
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
import SignOffline from "./approval/SignOffline";
import Notifications from "./common/Notifications";
import { NotificationProvider } from "./hooks/NotificationContext";
import ChangePassword from "./changepassword/ChangePassword";

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
            ? <div className="container-md px-0">
            	<Header loading/>
				<Spinner animation="border" role="status">
                <span className="visually-hidden">laddar innehåll...</span>
            	</Spinner>
			  </div>
            : <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={session === undefined ? <Navigate to="/login" /> : <Navigate to="/bunnies" />} />
                    <Route path="/login" element={<WithoutSession element={ <Login /> }/>}/>
                    <Route path="/recoverpassword" element={<WithoutSession element={ <RecoverPassword /> }/>}/>
                    <Route path="/register" element={<Register />}/>
                    <Route path="/approval" element={<RequiresSession element={ <CheckApproval /> }/>}/>
                    <Route path="/owner" element={<RequiresSession element={ <Owner /> }/>}/>
                    <Route path="/closeaccount" element={<RequiresSession element={ <CloseAccount /> }/>}/>
                    <Route path="/changepassword" element={<RequiresSession element={ <ChangePassword /> }/>}/>
                    <Route path="/bunnies" element={<RequiresSession element={ <Bunnies /> }/>}/>
                    <Route path="/bunny" element={<RequiresSession element={ <Bunny /> }/>}/>
                    <Route path="/bunny/:bunnyId/profile" element={<Profile />}/>
                    <Route path="/bunny/:bunnyId" element={<RequiresSession element={ <Bunny /> }/>}/>
                    <Route path="/transfer/:bunnyId" element={<RequiresSession element={ <Transfer /> }/>}/>
                    <Route path="/reclaim/:bunnyId" element={<RequiresSession element={ <Reclaim /> }/>}/>
                    <Route path="/claim" element={<RequiresSession element={ <Claim /> }/>}/>
                    <Route path="/activation/:ownerId" element={<Activation />} />
                    <Route path="/signOffline/:token" element={<SignOffline />} />
                    <Route path="/find" element={<Find />} />
                    <Route path="/help/picture" element={<HelpPicture />} />
                    <Route path="/help/publicprivate" element={<HelpPublicPrivate />} />
                    <Route path="/help/profile" element={<HelpProfile />} />
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

