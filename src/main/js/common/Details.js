import React from 'react';

const Details = (props) => {

    return <div className="fw-normal mt-1">
					<b>Ras:</b> {props.bunny.race ? props.bunny.race : '-'}
					&nbsp;
					<b>Hårlag:</b> {props.bunny.coat ? props.bunny.coat : '-'}
					&nbsp;
					<b>Färgteckning:</b> {props.bunny.colourMarkings ? props.bunny.colourMarkings : '-'}
					&nbsp;
					<b>Kännetecken:</b> {props.bunny.features ? props.bunny.features : '-'}
				</div>
}

export default Details;