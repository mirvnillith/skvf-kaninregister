import React, {useState} from 'react';
import {Alert} from "react-bootstrap";

const Notification = (props) => {
    const [show, setShow] = useState(true);

    return (show ?
            <Alert variant={props.type} onClose={() => setShow(false)} dismissible>
                <p>{props.msg}</p>
            </Alert> :
            null
    )
}

export default Notification;