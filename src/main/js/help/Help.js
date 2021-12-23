import React from 'react';

const Help = (props) => {

    return (
        <a href={"/help/"+props.topic} target='_blank'><i className="bi bi-question-circle-fill text-info"/></a>
    );
}

export default Help;