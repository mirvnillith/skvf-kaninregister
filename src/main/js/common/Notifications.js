import React from 'react';
import Notification from "./Notification";
import { useNotification } from "../hooks/NotificationContext";

const Notifications = () => {
    const notifications = useNotification();

    return (<div>
        {
            notifications.map(({type, msg}, index) => {
                return (<Notification type={type} msg={msg} key={index}/>)
            })
        }
    </div>)
}

export default Notifications;