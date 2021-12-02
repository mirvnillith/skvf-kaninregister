import React from 'react';

const Notification = (props) => {
    const type = props.type;
    const msg = props.msg
    return (
        <div className={`alert alert-${type} alert-dismissible fade show`} role="alert">
            {msg}
            <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Close" />
        </div>
    )
}

export default Notification;