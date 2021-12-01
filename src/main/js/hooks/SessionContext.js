import React, { useContext, useState } from 'react';

const SessionContext = React.createContext();
const UpdateSessionContext = React.createContext();

const maybeOngoingSession = () => {return document.cookie.indexOf('BunnyRegistryApi=') !== -1}

const useSession = () => {
    return useContext(SessionContext);
}

const useSessionUpdater = () => {
    return useContext(UpdateSessionContext);
}

const SessionProvider = ( {children} ) => {
    const [session, setSession] = useState(undefined);

    return (
        <SessionContext.Provider value={session}>
            <UpdateSessionContext.Provider value={setSession}>
                {children}
            </UpdateSessionContext.Provider>
        </SessionContext.Provider>
    );
}

export {
    maybeOngoingSession,
    useSession,
    useSessionUpdater,
    SessionProvider
}