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
                setNotifications((previous) => [...previous, {type: "info", msg: message}]);
            },
            notifySuccess: (message) => {
                setNotifications((previous) => [...previous, {type: "success", msg: message}]);
            },
            notifyWarning: (message) => {
                setNotifications((previous) => [...previous, {type: "warning", msg: message}]);
            },
            notifyError: (message) => {
                setNotifications((previous) => [...previous, {type: "danger", msg: message}]);
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