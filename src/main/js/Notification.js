import React from 'react';

const Notification = (props) => {
    const type = props.type;
    const key = props.key;
    const msg = props.msg
    return (
        <div key={key} className={`alert alert-${type} alert-dismissible fade show`} role="alert">
            {props.msg}
            <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    )
}

export default Notification;