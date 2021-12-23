import React from 'react';

const Identifiers = (props) => {

    return <div className="fw-normal mt-1">
					<b>Chipnummer:</b> {props.bunny.chip ? props.bunny.chip : '-'}
					&nbsp;
					<b>Vänster öra:</b> {props.bunny.leftEar ? props.bunny.leftEar : '-'}
					&nbsp;
					<b>Höger öra:</b> {props.bunny.rightEar ? props.bunny.rightEar : '-'}
					&nbsp;
					<b>Ring:</b> {props.bunny.ring ? props.bunny.ring : '-'}
				</div>
}

export default Identifiers;