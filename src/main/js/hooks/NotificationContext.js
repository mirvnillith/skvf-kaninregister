import React, { useContext, useState, useMemo } from 'react';

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

    const handlers = useMemo(
        () => ({
            notifyInfo: (message) => {
                setNotifications([{type: "info", msg: message}]);
            },
            notifyWarning: (message) => {
                setNotifications([{type: "warning", msg: message}]);
            },
            notifyError: (message) => {
                setNotifications([{type: "danger", msg: message}]);
            },
            clearNotifications: () => {
                setNotifications([]);
            },
        }),[] )

    return (
        <NotificationContext.Provider value={notifications}>
            <UpdateNotificationContext.Provider value={[setNotifications, handlers]}>
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