import React, {useState} from 'react';
import {Alert} from "react-bootstrap";

const Notification = (props) => {
    const [show, setShow] = useState(true);

	const Content = (props) => {
		return (<div>
				{props.message.length > 0
				? <p>{props.message[0]} <Alert.Link href={props.message[1]}>{props.message[2]}</Alert.Link> {props.message[3]}</p>
				: <p>{props.message}</p>
				}
				</div>);
	}

    return (show
    		? <Alert variant={props.type} onClose={() => setShow(false)} dismissible>
    			<Content message={props.msg}/>
              </Alert>
              : null
    		)
}

export default Notification;