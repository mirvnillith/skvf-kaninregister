import React from 'react';
import { changeUserPassword } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSession } from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import useFormValidation from "../hooks/FormValidation";

const INITIAL_STATE = {
    currentPassword: "",
    newPassword: "",
    newPassword2: ""
};

const validate = (values) => {
    const errors = {};

    if (!values.currentPassword) {
        errors.currentPassword = "Du måste ange ditt gamla lösenord!";
    }
    if (!values.newPassword) {
        errors.newPassword = "Du måste ange ett lösenord!";
    }
    if (!values.newPassword2) {
        errors.newPassword2 = "Du måste ange ett lösenord!";
    }
    if (values.newPassword !== values.newPassword2 ) {
        errors.newPassword2 = "Du måste upprepa ditt lösenord!";
    }

    return errors;
}

const ChangePassword = (_) => {
    const navigate = useNavigate();
    const session = useSession();
    const [__, { notifyError, notifySuccess, clearNotifications } ] = useNotificationUpdater();

    const onSuccessfulUpdate = async () => {
        notifySuccess("Lösenord uppdaterat!")
        navigate("/bunnies");
    }

    const submitHandler = async () => {
        clearNotifications();
        await changeUserPassword(session.user.id, values.currentPassword, values.newPassword, onSuccessfulUpdate, notifyError);
    }

    const cancelHandler = async () => {
        clearNotifications();
        navigate("/owner");
    }

    const {
        handleSubmit,
        handleChange,
        handleBlur,
        values,
        errors,
        isSubmitting
    } = useFormValidation(INITIAL_STATE, validate, submitHandler);

    return (
        <div className="row py-2">
            <form onSubmit={handleSubmit} >
                <div className="col-md-12">
                    <h2 >Ändra ditt lösenord</h2>
                </div>
                <div className="col-md-12">
                    <div className="row mb-2">
                        <label htmlFor="currentpassword" className="col-md-6 col-form-label">Ditt gamla lösenord</label>
                        <div className="col-md-6">
                            <input autoFocus
                                   id="currentPassword"
                                   name="currentPassword"
                                   type="password"
                                   value={values.currentPassword}
                                   className={errors.currentPassword ? "form-control is-invalid" : "form-control"}
                                   onChange={handleChange}
                                   autoComplete="current-password"
                            />
                            {errors.currentPassword && <div className="invalid-feedback">{errors.currentPassword}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="newPassword" className="col-md-6 col-form-label">Nytt lösenord</label>
                        <div className="col-md-6">
                            <input id="newPassword"
                                   name="newPassword"
                                   type="password"
                                   value={values.newPassword}
                                   className={errors.newPassword ? "form-control is-invalid" : "form-control"}
                                   onChange={handleChange}
                                   autoComplete="new-password"
                            />
                            {errors.newPassword && <div className="invalid-feedback">{errors.newPassword}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <label htmlFor="newPassword2" className="col-md-6 col-form-label">Upprepa nytt lösenord</label>
                        <div className="col-md-6">
                            <input id="newPassword2"
                                   name="newPassword2"
                                   type="password"
                                   value={values.newPassword2}
                                   className={errors.newPassword2 ? "form-control is-invalid" : "form-control"}
                                   onChange={handleChange}
                                   autoComplete="new-password"
                            />
                            {errors.newPassword2 && <div className="invalid-feedback">{errors.newPassword2}</div>}
                        </div>
                    </div>
                    <div className="row mb-2">
                        <div className="col-sm-6 align-self-end" />
                        <div className="col-sm-6">
                            <button type="submit" className="btn btn-primary float-end" disabled={isSubmitting} >Ändra ditt lösenord</button>
                            <button type="cancel" className="btn btn-secondary float-end me-2" disabled={isSubmitting} onClick={cancelHandler}>Avbryt</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default ChangePassword;
