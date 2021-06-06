const React = require('react');
const ReactDOM = require('react-dom');

const Header = () => {
    return (
        <div className="row">
           <div className="col-sm-4 gx-3">
            <img className="logo" src="assets/logo.jpg"/>
           </div>
           <div className="col-sm-8 align-self-center">
              <p className=""> Menu placeholder </p>
           </div>
       </div>
    )
}
const Register = () => {
   return (
       <div className="row py-2">
           <div className="col-md-12 mb-3">
               <h2 >Registrera dig</h2>
           </div>
            <div className="col-md-12">
                <form>
                    <div className="row mb-3">
                        <label htmlFor="userName" className="col-md-4 col-form-label">Användarnamn</label>
                        <div className="col-md-8">
                            <input type="text" className="form-control" id="userName" />
                        </div>
                    </div>
                    <div className="row mb-3">
                        <label htmlFor="password" className="col-md-4 col-form-label">Lösenord</label>
                        <div className="col-md-8">
                            <input type="password" className="form-control" id="password" />
                        </div>
                    </div>
                    <div className="row mb-3">
                        <label htmlFor="password2" className="col-md-4 col-form-label">Lösenord igen</label>
                        <div className="col-md-8">
                            <input type="password" className="form-control" id="password2" />
                        </div>
                    </div>
                    <button type="submit" className="btn btn-primary">Logga in</button>
                </form>
            </div>
       </div>
   );
}

const Login = () => {
    return (
        <div className="row py-2">
            <div className="col-md-12">
                <h2>Logga in</h2>
            </div>
            <div className="col-md-12">
                <form>
                    <div className="row mb-3">
                        <label htmlFor="userName" className="col-md-4 col-form-label">Användarnamn</label>
                        <div className="col-md-8">
                            <input type="text" className="form-control" id="userName" />
                        </div>
                    </div>
                    <div className="row mb-3">
                        <label htmlFor="password" className="col-md-4 col-form-label">Lösenord</label>
                        <div className="col-md-8">
                            <input type="password" className="form-control" id="password" />
                        </div>
                    </div>
                    <button type="submit" className="btn btn-primary">Logga in</button>
                </form>
            </div>
        </div>
    )
}

const App = () => {
    return (
        <div className="container-md px-0">
            <Header />
            <div className="row">
                <div className="col-md-12 align-self-center p-4">
                    <h1 className="text-center green"> Kaninregister </h1>
               </div>
            </div>
            <Login />
            <Register />
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById('app'))

