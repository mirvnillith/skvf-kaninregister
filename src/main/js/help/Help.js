import React from 'react';

const Help = (props) => {

    return (
        <a href={"/help/"+props.topic} target='_blank'><i class="bi bi-question-circle-fill text-info"/></a>
    );
}

export default Help;