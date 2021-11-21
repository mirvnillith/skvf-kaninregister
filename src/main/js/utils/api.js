
const createOwner = async (user, pwd, successHandler, errorHandler) => {
    const response = await fetch("/api/owners", {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({userName: user, password: pwd})
    });

    const responseMsg = await response.json();
    if (response.status === 200){
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
        errorHandler("Något gick fel vid registrering!")
    }
}

const updateOwner = async (id, owner, successHandler, errorHandler) => {
    const data = {};
    if (owner.name) data.name = owner.name;
    if (owner.email) data.email = owner.email;
    if (owner.address) data.address = owner.address;
    if (owner.phone) data.phone = owner.phone;
    if (owner.breederName) data.breederName = owner.breederName;
    if (owner.breederEmail) data.breederEmail = owner.breederEmail;
    if (owner.userName) data.userName = owner.userName;
    if (owner.publicOwner) data.publicOwner = owner.publicOwner;
    if (owner.publicBreeder) data.publicBreeder = owner.publicBreeder;

    const response = await fetch(`/api/owners/${id}`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else {
        errorHandler("Något gick fel vid uppdatering!")
    }
}

const activateOwner = async (id, user, pwd, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/activate`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({userName: user, password: pwd})
    });

    const responseMsg = await response.json();
    if (response.status === 200){
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
        errorHandler("Något gick fel vid aktivering!")
    }
}

const changeUserPassword = async (id, oldPwd, newPwd, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/activate`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({currentPassword: oldPwd, newPassword: newPwd})
    });

    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 400) {
        errorHandler("Ogiltigt lösenord!")
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd användare!")
    }
    else {
        errorHandler("Något gick fel vid aktivering!")
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

    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 400) {
        errorHandler("Ogiltigt lösenord!")
    }
    else if (response.status === 401) {
        errorHandler("Du är redan inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd användare!")
    }
    else {
        errorHandler("Något gick fel vid återställning!")
    }
}

const getOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}`, {
        method: 'GET'
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 204) {
        errorHandler("Icke-publik ägare!")
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
        errorHandler("Något gick fel vid hämtning!")
    }
}

const getBunny = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}`, {
        method: 'GET'
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
        errorHandler("Något gick fel vid hämtning!")
    }
}

const getBunnies = async (owner, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies`, {
        method: 'GET'
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else {
        errorHandler("Något gick fel vid hämtning!")
    }
}

const createBunny = async (id, bunny, successHandler, errorHandler) => {
    const data = {};
    if (bunny.name) data.name = bunny.name;
    if (bunny.leftEar) data.leftEar = bunny.leftEar;
    if (bunny.rightEar) data.rightEar = bunny.rightEar;
    if (bunny.chip) data.chip = bunny.chip;
    if (bunny.ring) data.ring = bunny.ring;
    if (bunny.picture) data.picture = bunny.picture;
    if (bunny.breeder) data.breeder = bunny.breeder;
    if (bunny.gender) data.gender = bunny.gender;
    if (bunny.neutered) data.neutered = bunny.neutered;
    if (bunny.birthDate) data.birthDate = bunny.birthDate;
    if (bunny.race) data.race = bunny.race;
    if (bunny.coat) data.coat = bunny.coat;
    if (bunny.colourMarkings) data.colourMarkings = bunny.colourMarkings;
    if (bunny.features) data.features = bunny.features;

    const response = await fetch(`/api/owners/${id}/bunnies`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 400) {
        errorHandler("Fel ägare av kanin!")
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else if (response.status === 409) {
        errorHandler("Kaninen finns redan!")
    }
    else {
        errorHandler("Något gick fel vid skapande!")
    }
}

const updateBunny = async (owner, bunny, successHandler, errorHandler) => {
    const data = {};
    if (bunny.name) data.name = bunny.name;
    if (bunny.leftEar) data.leftEar = bunny.leftEar;
    if (bunny.rightEar) data.rightEar = bunny.rightEar;
    if (bunny.chip) data.chip = bunny.chip;
    if (bunny.ring) data.ring = bunny.ring;
    if (bunny.picture) data.picture = bunny.picture;
    if (bunny.breeder) data.breeder = bunny.breeder;
    if (bunny.gender) data.gender = bunny.gender;
    if (bunny.neutered) data.neutered = bunny.neutered;
    if (bunny.birthDate) data.birthDate = bunny.birthDate;
    if (bunny.race) data.race = bunny.race;
    if (bunny.coat) data.coat = bunny.coat;
    if (bunny.colourMarkings) data.colourMarkings = bunny.colourMarkings;
    if (bunny.features) data.features = bunny.features;

    const response = await fetch(`/api/owners/${owner}/bunnies/${bunny.id}`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 400) {
        errorHandler("Fel kanin ID!")
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else if (response.status === 409) {
        errorHandler("Kaninen finns redan!")
    }
    else {
        errorHandler("Något gick fel vid skapande!")
    }
}

const transferBunny = async (owner, bunny, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies/${bunny.id}/transfer`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
        errorHandler("Något gick fel vid överföring!")
    }
}

const claimBunny = async (owner, token, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies/claim`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({claimToken: token})
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd överföring!")
    }
    else {
        errorHandler("Något gick fel vid mottagning!")
    }
}

const reclaimBunny = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/reclaim`, {
        method: 'PUT',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 204) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 400) {
        errorHandler("Kaninen har ingen tidigare ägare!")
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else if (response.status === 409) {
        errorHandler("Kaninen har en aktiv ägare!")
    }
    else {
        errorHandler("Något gick fel vid återtagning!")
    }
}

const findBunnies = async (criterias, successHandler, errorHandler) => {
	
	function toPairs(criteria) {
		return `identifierLocation=${criteria.location}&identifier=${criteria.identifier}`;
	}
	const query = criterias.map(toPairs).join("&");
	
    const response = await fetch(`/api/bunnies?${query}`, {
        method: 'GET'
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 204) {
        errorHandler("Sökningen gav för många träffar!")
    }
    else if (response.status === 400) {
        errorHandler("Felaktig sökning!")
    }
    else {
        errorHandler("Något gick fel vid sökning!")
    }
}

const deleteBunny = async (owner, bunny, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${owner}/bunnies/${bunny}`, {
        method: 'DELETE'
    });

    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
        errorHandler("Något gick fel vid borttagning!")
    }
}

const getBunnyOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/owner`, {
        method: 'GET'
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
        errorHandler("Något gick fel vid hämtning!")
    }
}

const getBunnyBreeder = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/bunnies/${id}/breeder`, {
        method: 'GET'
    });

    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 404) {
        errorHandler("Okänd kanin!")
    }
    else {
        errorHandler("Något gick fel vid hämtning!")
    }
}

const deleteOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}`, {
        method: 'DELETE'
    });

    if (response.status === 204){
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
        errorHandler("Något gick fel vid borttagning!")
    }
}

const deactivateOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/deactivate`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad som denna ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
        errorHandler("Något gick fel vid deaktivering!")
    }
}

const unapproveOwner = async (id, successHandler, errorHandler) => {

    const response = await fetch(`/api/owners/${id}/unapprove`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });

    if (response.status === 204){
        successHandler();
    }
    else if (response.status === 401) {
        errorHandler("Du måste vara inloggad som denna ägare!")
    }
    else if (response.status === 404) {
        errorHandler("Okänd ägare!")
    }
    else {
        errorHandler("Något gick fel vid deaktivering!")
    }
}

const loginUser = async (user, pwd, successHandler, errorHandler) => {
    const response = await fetch("/api/login", {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({userName: user, password: pwd})
    });
    const responseMsg = await response.json();
    if (response.status === 200){
        successHandler(responseMsg);
    }
    else if (response.status === 401) {
        errorHandler("Felaktigt användarnamn eller lösenord!")
    }
    else if (response.status === 409) {
        errorHandler("Du är redan inloggad!")
    }
    else {
        errorHandler("Något gick fel vid inloggning!")
    }
}

const logoutUser = async (successHandler, errorHandler) => {
    const response = await fetch("/api/logout", {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify({})
    });
    if (response.status === 204){
        successHandler();
    }
    else {
        errorHandler("Något gick fel vid utloggning!")
    }
}

const signOffline = async (token, signature, successHandler, errorHandler) => {
	const data = {};
    if (signature.subject) data.subject = signature.subject;
    if (signature.success) data.success = signature.success;
	
    const response = await fetch(`/api/signOffline/${token}`, {
        method: 'POST',
        headers: new Headers({'content-type': 'application/json'}),
        body: JSON.stringify(data)
    });
    if (response.status === 204){
        successHandler();
    }
    else {
        errorHandler("Något gick fel vid signering!")
    }
}

export {
	createOwner,
	activateOwner,
	deactivateOwner,
	getOwner,
	updateOwner,
	deleteOwner,
	loginUser,
	logoutUser,
	signOffline,
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
	getBunny
}