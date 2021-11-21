import React, { useContext, useState } from 'react';

const SessionContext = React.createContext();
const UpdateSessionContext = React.createContext();

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
    useSession,
    useSessionUpdater,
    SessionProvider
}