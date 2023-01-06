
const genericErrors = (status, errorHandler, operation) => {
	if (status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (status === 409) {
        errorHandler("Användarnamnet finns redan!")
    }
    else if (status === 412) {
        errorHandler("Du måste ha godkänt datahantering!")
    }
    else if (status == 429) {
		errorHandler("Tillfällig överbelastning, försök igen om ett tag!")
	} else {
		errorHandler("Något gick fel vid " + operation + "!")
	}
}

const createOwner = async (user, pwd, successHandler, errorHandler) => {
    const response = await fetch("/api/owners", {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({userName: user, password: pwd})
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 400) {
        errorHandler("Felaktigt användarnamn eller lösenord!")
    }
    else if (response.status === 401) {
        errorHandler("Du är redan inloggad!")
    }
    else if (response.status === 409) {
        errorHandler("Användarnamnet finns redan!")
    }
    else {
		genericErrors(response.status, errorHandler, "registrering")
    }
}

const updateOwner = async (id, owner, successHandler, errorHandler) => {
    const data = {};
    if (owner.name !== undefined) data.name = owner.name;
    if (owner.email !== undefined) data.email = owner.email;
    if (owner.address !== undefined) data.address = owner.address;
    if (owner.phone !== undefined) data.phone = owner.phone;
    if (owner.breederName !== undefined) data.breederName = owner.breederName;
    if (owner.breederEmail !== undefined) data.breederEmail = owner.breederEmail;
    if (owner.breederPhone !== undefined) data.breederPhone = owner.breederPhone;
    if (owner.breederAddress !== undefined) data.breederAddress = owner.breederAddress;
    if (owner.userName !== undefined) data.userName = owner.userName;
    if (owner.publicOwner !== undefined) data.publicOwner = owner.publicOwner;
    if (owner.publicBreeder !== undefined) data.publicBreeder = owner.publicBreeder;

    const response = await fetch(`/api/owners/${id}`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 409) {
        errorHandler("Användarnamnet finns redan!")
    }
    else {
		genericErrors(response.status, errorHandler, "uppdatering")
    }
}

const activateOwner = async (id, user, pwd, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/activate`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({userName: user, password: pwd})
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 400) {
        errorHandler("Felaktigt användarnamn eller lösenord!")
    }
    else if (response.status === 401) {
        errorHandler("Du är redan inloggad!")
    }
    else if (response.status === 409) {
        errorHandler("Användarnamnet finns redan!")
    }
    else {
		genericErrors(response.status, errorHandler, "aktivering")
    }
}

const changeUserPassword = async (id, currentPassword, newPassword, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/password`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({currentPassword, newPassword})
    });

    if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 400) {
        errorHandler("Ogiltigt lösenord!")
    }
    else if (response.status === 401) {
        errorHandler("Det gamla lösenordet är felaktigt!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd användare!")
    }
    else {
		genericErrors(response.status, errorHandler, "updatering av lösenord")
    }
}

const recoverUser = async (user, pwd, bunnyIdentifiers, successHandler, errorHandler) => {
    const data = {};
    data.newPassword = pwd;
	data.bunnyIdentifiers = bunnyIdentifiers;
	
    const response = await fetch(`/api/owners/${user}/recover`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 400) {
        errorHandler("Ogiltigt lösenord!")
    }
    else if (response.status === 401) {
        errorHandler("Du är redan inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd användare eller kanin!")
    }
    else {
		genericErrors(response.status, errorHandler, "återställning")
    }
}

const getOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}`, {
        method: 'GET'
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 204) {
        errorHandler("Icke-publik ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
		genericErrors(response.status, errorHandler, "hämtning")
    }
}

const getBunny = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}`, {
        method: 'GET'
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
		genericErrors(response.status, errorHandler, "hämtning")
    }
}

const getBunnies = async (owner, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies`, {
        method: 'GET'
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else {
		genericErrors(response.status, errorHandler, "hämtning")
    }
}

const createBunny = async (owner, bunny, successHandler, errorHandler) => {
    const data = {};
    if (bunny.name !== undefined) data.name = bunny.name;
    if (bunny.leftEar !== undefined) data.leftEar = bunny.leftEar;
    if (bunny.rightEar !== undefined) data.rightEar = bunny.rightEar;
    if (bunny.chip !== undefined) data.chip = bunny.chip;
    if (bunny.ring !== undefined) data.ring = bunny.ring;
    if (bunny.picture !== undefined) data.picture = bunny.picture;
    if (bunny.breeder !== undefined) data.breeder = bunny.breeder;
    if (bunny.previousOwner !== undefined) data.previousOwner = bunny.previousOwner;
    if (bunny.gender !== undefined) data.gender = bunny.gender;
    if (bunny.neutered !== undefined) data.neutered = bunny.neutered;
    if (bunny.birthDate !== undefined) data.birthDate = bunny.birthDate;
    if (bunny.race !== undefined) data.race = bunny.race;
    if (bunny.coat !== undefined) data.coat = bunny.coat;
    if (bunny.colourMarkings !== undefined) data.colourMarkings = bunny.colourMarkings;
    if (bunny.features !== undefined) data.features = bunny.features;

    const response = await fetch(`/api/owners/${owner}/bunnies`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 400) {
        errorHandler("Felaktig information om din kanin!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else if (response.status === 409) {
        errorHandler("Kaninen finns redan!")
    }
    else {
 		genericErrors(response.status, errorHandler, "skapande")
    }
}

const updateBunny = async (owner, bunny, successHandler, errorHandler) => {
    const data = {};
    if (bunny.name !== undefined) data.name = bunny.name;
    if (bunny.leftEar !== undefined) data.leftEar = bunny.leftEar;
    if (bunny.rightEar !== undefined) data.rightEar = bunny.rightEar;
    if (bunny.chip !== undefined) data.chip = bunny.chip;
    if (bunny.ring !== undefined) data.ring = bunny.ring;
    if (bunny.picture !== undefined) data.picture = bunny.picture;
    if (bunny.breeder !== undefined) data.breeder = bunny.breeder;
    if (bunny.previousOwner !== undefined) data.previousOwner = bunny.previousOwner;
    if (bunny.gender !== undefined) data.gender = bunny.gender;
    if (bunny.neutered !== undefined) data.neutered = bunny.neutered;
    if (bunny.birthDate !== undefined) data.birthDate = bunny.birthDate;
    if (bunny.race !== undefined) data.race = bunny.race;
    if (bunny.coat !== undefined) data.coat = bunny.coat;
    if (bunny.colourMarkings !== undefined) data.colourMarkings = bunny.colourMarkings;
    if (bunny.features !== undefined) data.features = bunny.features;

    const response = await fetch(`/api/owners/${owner}/bunnies/${bunny.id}`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 400) {
        errorHandler("Felaktig information om din kanin!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else if (response.status === 409) {
        errorHandler("Kaninen finns redan!")
    }
    else {
 		genericErrors(response.status, errorHandler, "sparande")
    }
}

const transferBunny = async (owner, bunny, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies/${bunny}/transfer`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
 		genericErrors(response.status, errorHandler, "överföring")
    }
}

const claimBunny = async (owner, token, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies/claim`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({claimToken: token})
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 404) {
        errorHandler("Okänd överlåtelsekod!")
    }
    else {
 		genericErrors(response.status, errorHandler, "mottagning")
    }
}

const reclaimBunny = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/reclaim`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 204) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 400) {
        errorHandler("Kaninen har ingen tidigare ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else if (response.status === 409) {
        errorHandler("Kaninen har en aktiv ägare!")
    }
    else {
 		genericErrors(response.status, errorHandler, "återtagning")
    }
}

const findBunnies = async (identifiers, successHandler, errorHandler) => {
	
	function toPairs(identifier) {
		return `identifierLocation=${identifier.location}&identifier=${identifier.identifier}`;
	}
	const query = identifiers.map(toPairs).join("&");
	
    const response = await fetch(`/api/bunnies?${query}`, {
        method: 'GET'
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 204) {
        errorHandler("Sökningen gav för många träffar!")
    }
    else if (response.status === 400) {
        errorHandler("Felaktig sökning!")
    }
    else {
 		genericErrors(response.status, errorHandler, "sökning")
    }
}

const deleteBunny = async (owner, bunny, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies/${bunny}`, {
        method: 'DELETE'
    });

    if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
 		genericErrors(response.status, errorHandler, "borttagning")
    }
}

const getBunnyOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/owner`, {
        method: 'GET'
    });

    if (response.status === 200){
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    } 
	else if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
 		genericErrors(response.status, errorHandler, "hämtning")
    }
}

const getBunnyBreeder = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/breeder`, {
        method: 'GET'
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    } 
	else if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
 		genericErrors(response.status, errorHandler, "hämtning")
    }
}

const getBunnyPreviousOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/previousOwner`, {
        method: 'GET'
    });

    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    } 
	else if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
 		genericErrors(response.status, errorHandler, "hämtning")
    }
}

const deleteOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}`, {
        method: 'DELETE'
    });

    if (response.status === 204) {
        successHandler();
    }
    else if (response.status === 400) {
        errorHandler("Du har fortfarande kaniner!")
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad som denna ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
 		genericErrors(response.status, errorHandler, "borttagning")
    }
}

const deactivateOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/deactivate`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 200){
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad som denna ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
 		genericErrors(response.status, errorHandler, "deaktivering")
    }
}

const unapproveOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/unapprove`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 200){
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad som denna ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
 		genericErrors(response.status, errorHandler, "deaktivering")
    }
}

const existingSession = async (sessionUpdater) => {
    const response = await fetch("/api/session", {
        method: 'GET',
        headers: new Headers({'content-type': 'application/json'})
    });
    if (response.status === 200) {
        const user = await response.json();
        sessionUpdater({user});
    }
    else {
        sessionUpdater(undefined);
    }
}

const loginUser = async (user, pwd, successHandler, errorHandler) => {
    const response = await fetch("/api/login", {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({userName: user, password: pwd})
    });
    if (response.status === 200) {
    	const responseMsg = await response.json();
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Felaktigt användarnamn eller lösenord!")
    }
    else if (response.status === 409) {
        errorHandler("Du är redan inloggad!")
    }
    else {
 		genericErrors(response.status, errorHandler, "inloggning")
    }
}

const logoutUser = async (successHandler, errorHandler) => {
    const response = await fetch("/api/logout", {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });
    if (response.status === 204) {
        successHandler();
    }
    else {
 		genericErrors(response.status, errorHandler, "utloggning")
    }
}

const signOffline = async (token, signature, successHandler, errorHandler) => {
	const data = {};
    if (signature.subject !== undefined) data.subject = signature.subject;
    if (signature.success !== undefined) data.success = signature.success;
	
    const response = await fetch(`/api/signOffline/${token}`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });
    if (response.status === 204) {
        successHandler();
    }
    else {
 		genericErrors(response.status, errorHandler, "signering")
    }
}

const approve = async (id, approvedOwnerHandler, approvalFailedHandler, approvalOngoingHandler, errorHandler) => {
    const response = await fetch(`/api/owners/${id}/approve`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 200) {
        approvedOwnerHandler();
    }
    else if (response.status === 202) {
        const location = response.headers.get('Location')
        approvalOngoingHandler(location)
    }
    else if (response.status === 204) {
        approvalFailedHandler();
    }
    else if (response.status === 401) {
        errorHandler("Något gick fel när signeringen skulle verifieras, prova att logga ut och logga in igen.")
    }
    else if (response.status === 402) {
        errorHandler(["Det saknas krediter för BankID-signering. Vänligen informera ","mailto:kaninregistret@skvf.se?Subject=BankID-krediter","kaninregistret@skvf.se","."])
    }
    else if (response.status === 404) {
        errorHandler("Ägaren kunde inte hittas")
    }
    else {
 		genericErrors(response.status, errorHandler, "verifiering av signering")
    }
}

export {
	createOwner,
	activateOwner,
	deactivateOwner,
	getOwner,
	updateOwner,
	deleteOwner,
    existingSession,
	loginUser,
	logoutUser,
	signOffline,
    approve,
	unapproveOwner,
	changeUserPassword,
	recoverUser,
	createBunny,
	updateBunny,
	deleteBunny,
	getBunnies,
	transferBunny,
	claimBunny,
	findBunnies,
	reclaimBunny,
	getBunnyOwner,
	getBunnyBreeder,
	getBunnyPreviousOwner,
	getBunny
}