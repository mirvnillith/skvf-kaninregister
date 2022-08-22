import React from 'react';
import Help from "../help/Help";

const Contact = (props) => {

    return 	props.contact && props.contact.name
				?	<div className="fw-normal mt-2">
						<b>{props.title}:</b> {props.contact.name}&nbsp;{props.contact.nonPublic && <Help topic="publicprivate"/>}
						<br/>&nbsp;
						<b>E-post:</b> {props.contact.email ? props.contact.email : '-'}
						<br/>&nbsp;
						<b>Telefon:</b> {props.contact.phone ? props.contact.phone : '-'}
						<br/>&nbsp;
						<b>Adress:</b> {props.contact.address ? props.contact.address : '-'}
					</div>
				:	<div className="fw-normal mt-2">
						<b>{props.title}:</b> -
					</div>
}

export default Contact;