/**
 * Login management
 */


/**
 * Checks if the values in the form are correct
 */
function checkForm() {
    const password = document.getElementById("password_first").value;
    const password_repeat = document.getElementById("password_repeat").value;
    if (!(password===password_repeat)) {
        document.getElementById("id_warning_signup").textContent = "Passwords don't match";
        document.getElementById("id_warning_signup").style.display="block";
        return false;
    } else {
        const email = document.getElementById("email").value;
        const regex = /^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$/;
        if (regex.test(email)) {
            return true;
        } else {
            document.getElementById("id_warning_signup").textContent = "Email is not correct";
            document.getElementById("id_warning_signup").style.display="block";
            return false;
        }

    }

}

function showSignUp() {
    document.getElementById("id_signup_section").style.display="block";
    document.getElementById("id_login_section").style.display="none";
}

function showLogin() {
    document.getElementById("id_login_section").style.display="block";
    document.getElementById("id_signup_section").style.display="none";
}

(function() { // avoid variables ending up in the global scope

    document.getElementById("id_signup_section").hidden = true;
    document.getElementById("open_register_button").addEventListener('click', (e) => {
        document.getElementById("id_register_form").hidden = false;

    });
    document.getElementById("id_signup_button").addEventListener('click', (e) => {
       if (checkForm()) {
           const form = e.target.closest("form");
           if (form.checkValidity()) {
               makeCall("POST", 'CheckRegistration', e.target.closest("form"),
                   function(req) {
                       if (req.readyState === XMLHttpRequest.DONE) {
                           const message = req.responseText;
                           switch (req.status) {
                               case 200:
                                   sessionStorage.setItem('user', message);
                                   window.location.href = "homepage.html";
                                   break;
                               case 400: // bad request
                                   document.getElementById("id_warning_signup").textContent = message;
                                   document.getElementById("id_warning_signup").style.display="block";
                                   break;
                               case 500: // server error
                                   document.getElementById("id_warning_signup").textContent = message;
                                   document.getElementById("id_warning_signup").style.display="block";
                                   break;
                           }
                       }
                   }
               );
           } else {
               form.reportValidity();
           }
       }
    });

    document.getElementById("id_login_button").addEventListener('click', (e) => {
        const form = e.target.closest("form");
        if (form.checkValidity()) {
            makeCall("POST", 'CheckLogin', e.target.closest("form"),
                function(req) {
                    if (req.readyState === XMLHttpRequest.DONE) {
                        const message = req.responseText;
                        switch (req.status) {
                            case 200:
                                sessionStorage.setItem('user', message);
                                window.location.href = "homepage.html";
                                break;
                            case 400: // bad request
                                document.getElementById("id_warning_login").textContent = message;
                                document.getElementById("id_warning_login").style.display="block";
                                break;
                            case 401: // unauthorized
                                document.getElementById("id_warning_login").textContent = message;
                                document.getElementById("id_warning_login").style.display="block";
                                break;
                            case 500: // server error
                                document.getElementById("id_warning_login").textContent = message;
                                document.getElementById("id_warning_login").style.display="block";
                                break;
                        }
                    }
                }
            );
        } else {
            form.reportValidity();
        }
    });

})();

