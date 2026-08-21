/* =========================================================
   CIVICPULSE - OFFICER PROFILE
   profile.js

   TEMPORARY FRONTEND VERSION

   This version:
   - Loads temporary officer profile information
   - Allows profile editing
   - Saves changes to localStorage
   - Handles change-password UI
   - Handles logout
   - Does NOT connect to the backend yet

   Later this will connect to Spring Boot APIs.
========================================================= */


/* =========================================================
   TEMPORARY PROFILE DATA
========================================================= */

const defaultOfficerProfile = {

    fullName: "",

    employeeId: "",

    email: "",

    phone: "",

    designation: "",

    department: "",

    assignedArea: "",

    officeLocation: "",

    address: "",

    role: "Officer",

    jurisdiction: "",

    joinedDate: ""

};


/* =========================================================
   CURRENT PROFILE
========================================================= */

let currentOfficerProfile = null;


/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        initializeProfile();

    }
);


/* =========================================================
   INITIALIZE
========================================================= */

function initializeProfile() {

    loadProfile();

    initializeProfileForm();

    initializePasswordForm();

    initializeSecurityActions();

}


/* =========================================================
   LOAD PROFILE
========================================================= */

function loadProfile() {

    const storedProfile =
        getStoredProfile();


    if (storedProfile) {

        currentOfficerProfile = {
            ...defaultOfficerProfile,
            ...storedProfile
        };

    } else {

        /*
         * No fake personal information is added.
         * Empty fields remain empty until the backend
         * provides the actual officer information.
         */

        currentOfficerProfile = {
            ...defaultOfficerProfile
        };

    }


    populateProfile(
        currentOfficerProfile
    );

}


/* =========================================================
   GET STORED PROFILE
========================================================= */

function getStoredProfile() {

    try {

        const stored =
            localStorage.getItem(
                "civicpulse_officer_profile"
            );


        if (!stored) {

            return null;

        }


        return JSON.parse(
            stored
        );

    } catch (error) {

        console.error(
            "Unable to load profile:",
            error
        );


        return null;

    }

}


/* =========================================================
   POPULATE PROFILE
========================================================= */

function populateProfile(
    profile
) {


    /* =====================================================
       FORM FIELDS
    ====================================================== */

    setInputValue(
        "fullName",
        profile.fullName
    );


    setInputValue(
        "employeeId",
        profile.employeeId
    );


    setInputValue(
        "email",
        profile.email
    );


    setInputValue(
        "phone",
        profile.phone
    );


    setInputValue(
        "designation",
        profile.designation
    );


    setInputValue(
        "department",
        profile.department
    );


    setInputValue(
        "assignedArea",
        profile.assignedArea
    );


    setInputValue(
        "officeLocation",
        profile.officeLocation
    );


    setInputValue(
        "address",
        profile.address
    );


    /* =====================================================
       PROFILE CARD
    ====================================================== */

    const displayName =
        profile.fullName ||
        "Officer";


    setText(
        "profileDisplayName",
        displayName
    );


    setText(
        "profileRole",
        profile.role ||
        "Officer"
    );


    setText(
        "profileOfficerId",
        profile.employeeId ||
        "—"
    );


    setText(
        "profileDepartment",
        profile.department ||
        "—"
    );


    setText(
        "profileArea",
        profile.assignedArea ||
        "—"
    );


    setText(
        "profileJoinedDate",
        profile.joinedDate
            ? formatDate(
                profile.joinedDate
            )
            : "—"
    );


    /* =====================================================
       OFFICIAL INFORMATION
    ====================================================== */

    setText(
        "officialRole",
        profile.role ||
        "—"
    );


    setText(
        "officialDepartment",
        profile.department ||
        "—"
    );


    setText(
        "officialJurisdiction",
        profile.jurisdiction ||
        profile.assignedArea ||
        "—"
    );


    /* =====================================================
       AVATAR
    ====================================================== */

    updateAvatar(
        displayName
    );

}


/* =========================================================
   PROFILE FORM
========================================================= */

function initializeProfileForm() {

    const form =
        document.getElementById(
            "profileForm"
        );


    if (!form) {

        return;

    }


    form.addEventListener(
        "submit",
        handleProfileSubmit
    );


    const resetButton =
        document.getElementById(
            "resetProfileButton"
        );


    if (resetButton) {

        resetButton.addEventListener(
            "click",
            function () {

                populateProfile(
                    currentOfficerProfile
                );

                clearFormValidation();

            }
        );

    }

}


/* =========================================================
   PROFILE SUBMIT
========================================================= */

function handleProfileSubmit(
    event
) {

    event.preventDefault();


    if (!validateProfileForm()) {

        return;

    }


    const updatedProfile =
        collectProfileFormData();


    currentOfficerProfile = {
        ...currentOfficerProfile,
        ...updatedProfile
    };


    saveProfileToStorage(
        currentOfficerProfile
    );


    populateProfile(
        currentOfficerProfile
    );


    showProfileSuccess();

}


/* =========================================================
   VALIDATE PROFILE
========================================================= */

function validateProfileForm() {

    const fullName =
        document.getElementById(
            "fullName"
        );


    const email =
        document.getElementById(
            "email"
        );


    let valid = true;


    /*
     * Full name
     */

    if (
        fullName &&
        !fullName.value.trim()
    ) {

        fullName.classList.add(
            "is-invalid"
        );

        valid = false;

    } else if (fullName) {

        fullName.classList.remove(
            "is-invalid"
        );

    }


    /*
     * Email
     */

    if (
        email &&
        email.value.trim()
    ) {

        const emailPattern =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


        if (
            !emailPattern.test(
                email.value.trim()
            )
        ) {

            email.classList.add(
                "is-invalid"
            );

            valid = false;

        } else {

            email.classList.remove(
                "is-invalid"
            );

        }

    }


    if (!valid) {

        showProfileMessage(
            "Please enter valid profile information.",
            "error"
        );

    }


    return valid;

}


/* =========================================================
   COLLECT FORM DATA
========================================================= */

function collectProfileFormData() {

    return {

        fullName:
            getInputValue(
                "fullName"
            ),

        employeeId:
            getInputValue(
                "employeeId"
            ),

        email:
            getInputValue(
                "email"
            ),

        phone:
            getInputValue(
                "phone"
            ),

        designation:
            getInputValue(
                "designation"
            ),

        department:
            getInputValue(
                "department"
            ),

        assignedArea:
            getInputValue(
                "assignedArea"
            ),

        officeLocation:
            getInputValue(
                "officeLocation"
            ),

        address:
            getInputValue(
                "address"
            )

    };

}


/* =========================================================
   SAVE PROFILE
========================================================= */

function saveProfileToStorage(
    profile
) {

    try {

        localStorage.setItem(
            "civicpulse_officer_profile",
            JSON.stringify(
                profile
            )
        );

    } catch (error) {

        console.error(
            "Unable to save profile:",
            error
        );

    }

}


/* =========================================================
   PASSWORD FORM
========================================================= */

function initializePasswordForm() {

    const form =
        document.getElementById(
            "changePasswordForm"
        );


    if (!form) {

        return;

    }


    form.addEventListener(
        "submit",
        handlePasswordSubmit
    );

}


/* =========================================================
   SECURITY ACTIONS
========================================================= */




/* =========================================================
   OPEN CHANGE PASSWORD MODAL
========================================================= */

function openChangePasswordModal() {

    const modalElement =
        document.getElementById(
            "changePasswordModal"
        );


    if (
        !modalElement ||
        typeof bootstrap ===
        "undefined"
    ) {

        return;

    }


    const modal =
        new bootstrap.Modal(
            modalElement
        );


    modal.show();

}


/* =========================================================
   CHANGE PASSWORD
========================================================= */

function handlePasswordSubmit(
    event
) {

    event.preventDefault();


    const currentPassword =
        getInputValue(
            "currentPassword"
        );


    const newPassword =
        getInputValue(
            "newPassword"
        );


    const confirmPassword =
        getInputValue(
            "confirmPassword"
        );


    /*
     * Temporary validation only.
     *
     * We do NOT store passwords in localStorage.
     */


    if (!currentPassword) {

        showProfileMessage(
            "Enter your current password.",
            "error"
        );

        return;

    }


    if (
        newPassword.length < 8
    ) {

        showProfileMessage(
            "New password must contain at least 8 characters.",
            "error"
        );

        return;

    }


    if (
        newPassword !==
        confirmPassword
    ) {

        showProfileMessage(
            "New password and confirmation do not match.",
            "error"
        );

        return;

    }


    /*
     * Backend authentication will handle
     * the actual password change.
     */

    showProfileMessage(
        "Password update will be connected to the backend.",
        "success"
    );


    const form =
        document.getElementById(
            "changePasswordForm"
        );


    if (form) {

        form.reset();

    }


    const modalElement =
        document.getElementById(
            "changePasswordModal"
        );


    if (
        modalElement &&
        typeof bootstrap !==
        "undefined"
    ) {

        const modal =
            bootstrap.Modal.getInstance(
                modalElement
            );


        if (modal) {

            modal.hide();

        }

    }

}


/* =========================================================
   LOGOUT
========================================================= */



/* =========================================================
   SUCCESS MODAL
========================================================= */

function showProfileSuccess() {

    const modalElement =
        document.getElementById(
            "profileSuccessModal"
        );


    if (
        !modalElement ||
        typeof bootstrap ===
        "undefined"
    ) {

        showProfileMessage(
            "Profile updated successfully.",
            "success"
        );

        return;

    }


    const modal =
        new bootstrap.Modal(
            modalElement
        );


    modal.show();

}


/* =========================================================
   FORM MESSAGE
========================================================= */

function showProfileMessage(
    message,
    type
) {

    const existing =
        document.querySelector(
            ".profile-form-message"
        );


    if (existing) {

        existing.remove();

    }


    const messageElement =
        document.createElement(
            "div"
        );


    messageElement.className =
        "profile-form-message " +
        (
            type === "error"
                ? "error"
                : "success"
        );


    messageElement.innerHTML = `

        <i class="${
        type === "error"
            ? "bi bi-exclamation-circle"
            : "bi bi-check-circle"
    }"></i>

        <span>
            ${escapeHtml(message)}
        </span>

    `;


    const form =
        document.getElementById(
            "profileForm"
        );


    if (form) {

        form.prepend(
            messageElement
        );

    } else {

        document.body.prepend(
            messageElement
        );

    }


    setTimeout(
        function () {

            if (
                messageElement &&
                messageElement.parentNode
            ) {

                messageElement.remove();

            }

        },
        4000
    );

}


/* =========================================================
   UPDATE AVATAR
========================================================= */

function updateAvatar(
    name
) {

    const initialsElement =
        document.getElementById(
            "profileInitials"
        );


    if (!initialsElement) {

        return;

    }


    const initials =
        getInitials(
            name
        );


    initialsElement.textContent =
        initials;

}


/* =========================================================
   GET INITIALS
========================================================= */

function getInitials(
    name
) {

    if (
        !name ||
        !name.trim()
    ) {

        return "O";

    }


    const words =
        name
            .trim()
            .split(/\s+/)
            .filter(Boolean);


    if (words.length === 1) {

        return words[0]
            .substring(0, 2)
            .toUpperCase();

    }


    return (
        words[0].charAt(0) +
        words[
        words.length - 1
            ].charAt(0)
    ).toUpperCase();

}


/* =========================================================
   SET INPUT VALUE
========================================================= */

function setInputValue(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
        );


    if (!element) {

        return;

    }


    element.value =
        value === null ||
        value === undefined
            ? ""
            : value;

}


/* =========================================================
   GET INPUT VALUE
========================================================= */

function getInputValue(
    elementId
) {

    const element =
        document.getElementById(
            elementId
        );


    if (!element) {

        return "";

    }


    return element.value.trim();

}


/* =========================================================
   SET TEXT
========================================================= */

function setText(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
        );


    if (!element) {

        return;

    }


    element.textContent =
        value === null ||
        value === undefined ||
        value === ""
            ? "—"
            : value;

}


/* =========================================================
   CLEAR FORM VALIDATION
========================================================= */

function clearFormValidation() {

    const fields =
        document.querySelectorAll(
            "#profileForm .form-control"
        );


    fields.forEach(
        function (field) {

            field.classList.remove(
                "is-invalid",
                "is-valid"
            );

        }
    );

}


/* =========================================================
   FORMAT DATE
========================================================= */

function formatDate(
    value
) {

    if (!value) {

        return "—";

    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return String(value);

    }


    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );

}


/* =========================================================
   ESCAPE HTML
========================================================= */

function escapeHtml(
    value
) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)

        .replace(
            /&/g,
            "&amp;"
        )

        .replace(
            /</g,
            "&lt;"
        )

        .replace(
            />/g,
            "&gt;"
        )

        .replace(
            /"/g,
            "&quot;"
        )

        .replace(
            /'/g,
            "&#039;"
        );

}