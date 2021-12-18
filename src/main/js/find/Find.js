import React, { useState } from 'react';
import FindForm from './FindForm';
import { findBunnies, getBunnyOwner } from "../utils/api";
import { useNotificationUpdater } from "../hooks/NotificationContext";
import { nonPublic } from "../utils/data";

const Find = (_) => {

    const [bunnies, setBunnies] = useState();

    const [__, { notifyError, clearNotifications } ] = useNotificationUpdater();

    const onBunnies = (bunnies) => {
        setBunnies(bunnies.bunnies);
    }

    const find = (values) => {
        clearNotifications();
		var identifiers = [];
		if (values.chip) {
			identifiers.push({location:'CHIP', identifier: values.chip});
		}
		if (values.leftEar) {
			identifiers.push({location:'LEFT_EAR', identifier: values.leftEar});
		}
		if (values.rightEar) {
			identifiers.push({location:'RIGHT_EAR', identifier: values.rightEar});
		}
		if (values.ring) {
			identifiers.push({location:'RING', identifier: values.ring});
		}
        return findBunnies(identifiers, onBunnies, notifyError);
    }

    const setBunnyOwner = (setDetails) => (owner) => {
		if (owner === undefined) {
			owner = nonPublic();
		}
		setDetails({owner});
	}
	
    const loadDetails = (bunny, setDetails) => {
		
		getBunnyOwner(bunny.id, setBunnyOwner(setDetails), () => setBunnyOwner({ name: "Hämtning misslyckades"}));
	}

    return <FindForm bunnies={bunnies} find={find} loadDetails={loadDetails}/>;
}

export default Find;