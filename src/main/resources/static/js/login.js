/* =========================================================
   CIVICPULSE - SHARED LOGIN
   login.js

   Used by:
   - Admin
   - Officer
   - Future modules / roles

   IMPORTANT:
   Authentication itself is handled by Spring Security.
   This JavaScript only handles the frontend experience.

   Login request:
   POST /login

   Spring Security will determine the user's role and
   redirect them to the appropriate dashboard.
========================================================= */


/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        initializeLogin();

    }
);


/* =========================================================
   INITIALIZE LOGIN
========================================================= */

function initializeLogin() {

    setCurrentYear();

    initializeLoginForm();

    initializePasswordToggle();

    initializeForgotPassword();

    displayAuthenticationMessages();

}


/* =========================================================
   LOGIN FORM
========================================================= */

function initializeLoginForm() {

    const loginForm =
        document.getElementById(
            "loginForm"
        );


    if (!loginForm) {

        return;

    }


    loginForm.addEventListener(
        "submit",
        handleLoginSubmit
    );


    /*
     * Remove validation error when user starts typing.
     */

    const username =
        document.getElementById(
            "username"
        );


    const password =
        document.getElementById(
            "password"
        );


    if (username) {

        username.addEventListener(
            "input",
            function () {

                clearFieldError(
                    "username"
                );

            }
        );

    }


    if (password) {

        password.addEventListener(
            "input",
            function () {

                clearFieldError(
                    "password"
                );

            }
        );

    }

}


/* =========================================================
   LOGIN SUBMIT
========================================================= */

function handleLoginSubmit(
    event
) {

    /*
     * Do not prevent the default form submission.
     *
     * Spring Security must receive:
     *
     * POST /login
     *
     * Therefore we only validate the form and show
     * the loading state.
     */

    const username =
        document.getElementById(
            "username"
        );


    const password =
        document.getElementById(
            "password"
        );


    let valid = true;


    /* =====================================================
       USERNAME VALIDATION
    ====================================================== */

    if (
        !username ||
        !username.value.trim()
    ) {

        event.preventDefault();

        showFieldError(
            "username",
            "Please enter your username."
        );

        valid = false;

    }


    /* =====================================================
       PASSWORD VALIDATION
    ====================================================== */

    if (
        !password ||
        !password.value
    ) {

        event.preventDefault();

        showFieldError(
            "password",
            "Please enter your password."
        );

        valid = false;

    }


    if (!valid) {

        return;

    }


    /*
     * Allow the browser to submit the form to:
     *
     * POST /login
     *
     * Spring Security handles authentication.
     */

    showLoginLoading();

}


/* =========================================================
   SHOW LOGIN LOADING
========================================================= */

function showLoginLoading() {

    const loginButton =
        document.getElementById(
            "loginButton"
        );


    const loginButtonText =
        document.getElementById(
            "loginButtonText"
        );


    const loginButtonLoading =
        document.getElementById(
            "loginButtonLoading"
        );


    const loginButtonIcon =
        document.getElementById(
            "loginButtonIcon"
        );


    if (loginButton) {

        loginButton.disabled =
            true;

    }


    if (loginButtonText) {

        loginButtonText.classList.add(
            "d-none"
        );

    }


    if (loginButtonLoading) {

        loginButtonLoading.classList.remove(
            "d-none"
        );

    }


    if (loginButtonIcon) {

        loginButtonIcon.classList.add(
            "d-none"
        );

    }

}


/* =========================================================
   RESET LOGIN BUTTON
========================================================= */

function resetLoginButton() {

    const loginButton =
        document.getElementById(
            "loginButton"
        );


    const loginButtonText =
        document.getElementById(
            "loginButtonText"
        );


    const loginButtonLoading =
        document.getElementById(
            "loginButtonLoading"
        );


    const loginButtonIcon =
        document.getElementById(
            "loginButtonIcon"
        );


    if (loginButton) {

        loginButton.disabled =
            false;

    }


    if (loginButtonText) {

        loginButtonText.classList.remove(
            "d-none"
        );

    }


    if (loginButtonLoading) {

        loginButtonLoading.classList.add(
            "d-none"
        );

    }


    if (loginButtonIcon) {

        loginButtonIcon.classList.remove(
            "d-none"
        );

    }

}


/* =========================================================
   PASSWORD TOGGLE
========================================================= */

function initializePasswordToggle() {

    const toggleButton =
        document.getElementById(
            "togglePassword"
        );


    const passwordInput =
        document.getElementById(
            "password"
        );


    if (
        !toggleButton ||
        !passwordInput
    ) {

        return;

    }


    toggleButton.addEventListener(
        "click",
        function () {

            const isPassword =
                passwordInput.type ===
                "password";


            passwordInput.type =
                isPassword
                    ? "text"
                    : "password";


            const icon =
                toggleButton.querySelector(
                    "i"
                );


            if (!icon) {

                return;

            }


            if (isPassword) {

                icon.classList.remove(
                    "bi-eye"
                );

                icon.classList.add(
                    "bi-eye-slash"
                );

                toggleButton.setAttribute(
                    "aria-label",
                    "Hide password"
                );

            } else {

                icon.classList.remove(
                    "bi-eye-slash"
                );

                icon.classList.add(
                    "bi-eye"
                );

                toggleButton.setAttribute(
                    "aria-label",
                    "Show password"
                );

            }

        }
    );

}


/* =========================================================
   FORGOT PASSWORD
========================================================= */

function initializeForgotPassword() {

    const forgotLink =
        document.getElementById(
            "forgotPasswordLink"
        );


    if (!forgotLink) {

        return;

    }


    forgotLink.addEventListener(
        "click",
        function (event) {

            event.preventDefault();

            openForgotPasswordModal();

        }
    );

}


/* =========================================================
   OPEN FORGOT PASSWORD MODAL
========================================================= */

function openForgotPasswordModal() {

    const modalElement =
        document.getElementById(
            "forgotPasswordModal"
        );


    if (!modalElement) {

        return;

    }


    /*
     * Bootstrap modal.
     */

    if (
        typeof bootstrap !==
        "undefined"
    ) {

        const modal =
            bootstrap.Modal.getOrCreateInstance(
                modalElement
            );


        modal.show();

    }

}


/* =========================================================
   AUTHENTICATION MESSAGES
========================================================= */

function displayAuthenticationMessages() {

    const params =
        new URLSearchParams(
            window.location.search
        );


    /*
     * Spring Security commonly redirects to:
     *
     * /login?error
     */

    if (
        params.has("error")
    ) {

        showLoginError(
            "Invalid username or password."
        );

        resetLoginButton();

    }


    /*
     * Logout:
     *
     * /login?logout
     */

    if (
        params.has("logout")
    ) {

        showLogoutMessage();

        resetLoginButton();

    }

}


/* =========================================================
   SHOW LOGIN ERROR
========================================================= */

function showLoginError(
    message
) {

    const errorContainer =
        document.getElementById(
            "loginError"
        );


    const errorMessage =
        document.getElementById(
            "loginErrorMessage"
        );


    if (errorMessage) {

        errorMessage.textContent =
            message ||
            "Invalid username or password.";

    }


    if (errorContainer) {

        errorContainer.classList.remove(
            "d-none"
        );

    }

}


/* =========================================================
   HIDE LOGIN ERROR
========================================================= */

function hideLoginError() {

    const errorContainer =
        document.getElementById(
            "loginError"
        );


    if (errorContainer) {

        errorContainer.classList.add(
            "d-none"
        );

    }

}


/* =========================================================
   SHOW LOGOUT MESSAGE
========================================================= */

function showLogoutMessage() {

    const logoutMessage =
        document.getElementById(
            "logoutMessage"
        );


    if (logoutMessage) {

        logoutMessage.classList.remove(
            "d-none"
        );

    }

}


/* =========================================================
   FIELD ERROR
========================================================= */

function showFieldError(
    fieldName,
    message
) {

    const input =
        document.getElementById(
            fieldName
        );


    const error =
        document.getElementById(
            fieldName + "Error"
        );


    if (input) {

        input.classList.add(
            "input-error"
        );

        input.focus();

    }


    if (error) {

        if (message) {

            error.textContent =
                message;

        }

        error.classList.remove(
            "d-none"
        );

    }

}


/* =========================================================
   CLEAR FIELD ERROR
========================================================= */

function clearFieldError(
    fieldName
) {

    const input =
        document.getElementById(
            fieldName
        );


    const error =
        document.getElementById(
            fieldName + "Error"
        );


    if (input) {

        input.classList.remove(
            "input-error"
        );

    }


    if (error) {

        error.classList.add(
            "d-none"
        );

    }

}


/* =========================================================
   CLEAR ALL ERRORS
========================================================= */

function clearAllErrors() {

    clearFieldError(
        "username"
    );


    clearFieldError(
        "password"
    );


    hideLoginError();

}


/* =========================================================
   CURRENT YEAR
========================================================= */

function setCurrentYear() {

    const yearElement =
        document.getElementById(
            "currentYear"
        );


    if (!yearElement) {

        return;

    }


    yearElement.textContent =
        new Date().getFullYear();

}


/* =========================================================
   PREVENT DOUBLE SUBMISSION
========================================================= */

let loginSubmitting = false;


document.addEventListener(
    "submit",
    function (event) {

        const form =
            event.target;


        if (
            !form ||
            form.id !==
            "loginForm"
        ) {

            return;

        }


        if (loginSubmitting) {

            event.preventDefault();

            return;

        }


        /*
         * Only mark as submitted when the required
         * fields contain values.
         */

        const username =
            document.getElementById(
                "username"
            );


        const password =
            document.getElementById(
                "password"
            );


        if (
            username &&
            password &&
            username.value.trim() &&
            password.value
        ) {

            loginSubmitting =
                true;

        }

    }
);


/* =========================================================
   ENTER KEY SUPPORT
========================================================= */

document.addEventListener(
    "keydown",
    function (event) {

        if (
            event.key !==
            "Enter"
        ) {

            return;

        }


        const activeElement =
            document.activeElement;


        if (
            activeElement &&
            (
                activeElement.id ===
                "username" ||
                activeElement.id ===
                "password"
            )
        ) {

            const form =
                document.getElementById(
                    "loginForm"
                );


            if (form) {

                /*
                 * Let the browser perform the
                 * normal form submission.
                 */

                form.requestSubmit();

            }

        }

    }
);