/* =========================================================
   CIVICPULSE - GLOBAL JAVASCRIPT
   main.js

   Shared by:
   - Officer module
   - Admin module
   - Future modules

   Contains common functionality such as:
   - Sidebar
   - Global logout
   - Common UI behavior
========================================================= */


/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        initializeGlobalLogout();

        initializeSidebar();

    }
);


/* =========================================================
   GLOBAL LOGOUT
========================================================= */

function initializeGlobalLogout() {

    const logoutButtons =
        document.querySelectorAll(
            "#logoutButton, .sidebar-logout"
        );


    logoutButtons.forEach(
        function (button) {

            button.addEventListener(
                "click",
                function (event) {

                    event.preventDefault();

                    handleGlobalLogout();

                }
            );

        }
    );

}


/* =========================================================
   HANDLE LOGOUT
========================================================= */

function handleGlobalLogout() {

    const confirmed =
        window.confirm(
            "Are you sure you want to sign out?"
        );

    if (!confirmed) {
        return;
    }

    window.location.href = "/logout";
}


/* =========================================================
   SIDEBAR
========================================================= */

function initializeSidebar() {

    const sidebar =
        document.querySelector(
            ".sidebar"
        );


    const overlay =
        document.getElementById(
            "sidebarOverlay"
        );


    const toggleButtons =
        document.querySelectorAll(
            "#sidebarToggle, .sidebar-toggle"
        );


    if (!sidebar) {

        return;

    }


    /* =====================================================
       OPEN / CLOSE SIDEBAR
    ====================================================== */

    toggleButtons.forEach(
        function (button) {

            button.addEventListener(
                "click",
                function () {

                    toggleSidebar();

                }
            );

        }
    );


    /* =====================================================
       CLOSE USING OVERLAY
    ====================================================== */

    if (overlay) {

        overlay.addEventListener(
            "click",
            function () {

                closeSidebar();

            }
        );

    }


    /* =====================================================
       CLOSE SIDEBAR WHEN CLICKING NAVIGATION
    ====================================================== */

    const sidebarLinks =
        sidebar.querySelectorAll(
            "a"
        );


    sidebarLinks.forEach(
        function (link) {

            link.addEventListener(
                "click",
                function () {

                    if (
                        window.innerWidth <=
                        991
                    ) {

                        closeSidebar();

                    }

                }
            );

        }
    );


    /* =====================================================
       HANDLE WINDOW RESIZE
    ====================================================== */

    window.addEventListener(
        "resize",
        function () {

            if (
                window.innerWidth > 991
            ) {

                closeSidebar();

            }

        }
    );

}


/* =========================================================
   TOGGLE SIDEBAR
========================================================= */

function toggleSidebar() {

    const sidebar =
        document.querySelector(
            ".sidebar"
        );


    const overlay =
        document.getElementById(
            "sidebarOverlay"
        );


    if (!sidebar) {

        return;

    }


    sidebar.classList.toggle(
        "sidebar-open"
    );


    if (overlay) {

        overlay.classList.toggle(
            "active"
        );

    }


    document.body.classList.toggle(
        "sidebar-mobile-open"
    );

}


/* =========================================================
   CLOSE SIDEBAR
========================================================= */

function closeSidebar() {

    const sidebar =
        document.querySelector(
            ".sidebar"
        );


    const overlay =
        document.getElementById(
            "sidebarOverlay"
        );


    if (sidebar) {

        sidebar.classList.remove(
            "sidebar-open"
        );

    }


    if (overlay) {

        overlay.classList.remove(
            "active"
        );

    }


    document.body.classList.remove(
        "sidebar-mobile-open"
    );

}


/* =========================================================
   GLOBAL ESCAPE KEY
========================================================= */

document.addEventListener(
    "keydown",
    function (event) {

        if (
            event.key ===
            "Escape"
        ) {

            closeSidebar();

        }

    }
);