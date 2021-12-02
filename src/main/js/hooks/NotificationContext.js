import React, { useContext, useState } from 'react';

const NotificationContext = React.createContext();
const UpdateNotificationContext = React.createContext();

const useNotification = () => {
    return useContext(NotificationContext);
}

const useNotificationUpdater = () => {
    return useContext(UpdateNotificationContext);
}

const NotificationProvider = ( {children} ) => {
    const [notifications, setNotifications] = useState([]);

    return (
        <NotificationContext.Provider value={notifications}>
            <UpdateNotificationContext.Provider value={setNotifications}>
                {children}
            </UpdateNotificationContext.Provider>
        </NotificationContext.Provider>
    );
}

export {
    useNotification,
    useNotificationUpdater,
    NotificationProvider
}