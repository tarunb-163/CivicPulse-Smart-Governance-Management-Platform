
/*
 * =========================================================
 * CIVICPULSE - GLOBAL JAVASCRIPT
 * main.js
 *
 * Shared by:
 *
 * - Officer module
 * - Admin module
 * - Citizen module
 *
 * Contains:
 *
 * - Sidebar toggle
 * - Mobile drawer
 * - Sidebar overlay
 * - Escape key handling
 *
 * Logout is handled directly by Spring Security
 * using POST /logout with the CSRF token.
 * =========================================================
 */


document.addEventListener(
    "DOMContentLoaded",
    function () {

        initializeSidebar();

    }
);


/*
 * =========================================================
 * SIDEBAR INITIALIZATION
 * =========================================================
 */

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


    const closeButtons =
        document.querySelectorAll(
            "#sidebarClose, .sidebar-close"
        );


    /*
     * =====================================================
     * NO SIDEBAR
     * =====================================================
     */

    if (!sidebar) {

        return;

    }


    /*
     * =====================================================
     * TOGGLE BUTTON
     * =====================================================
     */

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


    /*
     * =====================================================
     * CLOSE BUTTON
     * =====================================================
     */

    closeButtons.forEach(
        function (button) {

            button.addEventListener(
                "click",
                function () {

                    closeSidebar();

                }
            );

        }
    );


    /*
     * =====================================================
     * OVERLAY
     * =====================================================
     */

    if (overlay) {

        overlay.addEventListener(
            "click",
            function () {

                closeSidebar();

            }
        );

    }


    /*
     * =====================================================
     * SIDEBAR LINKS
     * =====================================================
     */

    const sidebarLinks =
        sidebar.querySelectorAll(
            "a"
        );


    sidebarLinks.forEach(
        function (link) {

            link.addEventListener(
                "click",
                function () {

                    if (window.innerWidth <= 991) {

                        closeSidebar();

                    }

                }
            );

        }
    );


    /*
     * =====================================================
     * WINDOW RESIZE
     * =====================================================
     */

    window.addEventListener(
        "resize",
        function () {

            if (window.innerWidth > 991) {

                closeSidebar();

            }

        }
    );

}


/*
 * =========================================================
 * TOGGLE SIDEBAR
 * =========================================================
 */

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


/*
 * =========================================================
 * CLOSE SIDEBAR
 * =========================================================
 */

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


/*
 * =========================================================
 * ESCAPE KEY
 * =========================================================
 */

document.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Escape") {

            closeSidebar();

        }

    }
);

