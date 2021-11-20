
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

export {
    createOwner,
    updateOwner,
    loginUser,
    logoutUser
}