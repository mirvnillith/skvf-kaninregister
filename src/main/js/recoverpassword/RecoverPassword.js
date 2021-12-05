import React from 'react';
import RecoverPasswordForm from './RecoverPasswordForm';
import { recoverUser, loginUser } from '../utils/api';
import { useNavigate } from "react-router-dom";
import { useSessionUpdater} from "../hooks/SessionContext";
import { useNotificationUpdater } from "../hooks/NotificationContext";

const RecoverPassword = (_) => {
    const navigate = useNavigate();
    const sessionUpdater = useSessionUpdater();
    const [__, { notifyError, notifySuccess, clearNotifications } ] = useNotificationUpdater();

    const createRegistrationSuccessHandler = (userName, pwd, autoLogin) => {
	
        const onSuccessfulLogin = (user) => {
            sessionUpdater({user});
			clearNotifications();
			navigate("/approval");
        }

        const onFailedLogin = (msg) => {
			notifyError(msg);
			navigate("/login");
        }

        return async (_) => {
			if (autoLogin) {
				notifySuccess("Nytt lösenord sattes, loggar in ...");
            	await loginUser(userName, pwd, onSuccessfulLogin, onFailedLogin);
			} else {
				notifySuccess("Nytt lösenord sattes");
				navigate("/login");
			}
        }
    }

    const submitForm = async (values) => {
        clearNotifications();
		var identifiers = [];
		if (values.chip) {
			identifiers.push({location:'Chip', identifier: values.chip});
		}
		if (values.leftEar) {
			identifiers.push({location:'Left Ear', identifier: values.leftEar});
		}
		if (values.rightEar) {
			identifiers.push({location:'Right Ear', identifier: values.rightEar});
		}
		if (values.ring) {
			identifiers.push({location:'Ring', identifier: values.ring});
		}
        await recoverUser(values.user, values.pwd, identifiers, createRegistrationSuccessHandler(values.user, values.pwd, values.autoLogin), notifyError)
    }

    return (
        <RecoverPasswordForm submitForm={submitForm} />
    );
}

export default RecoverPassword;
