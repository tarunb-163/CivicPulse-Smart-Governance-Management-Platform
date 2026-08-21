/* =========================================================
   CIVICPULSE - OFFICER COMPLAINTS
   complaints.js

   DATA SOURCE:
   Complaints are supplied by Spring Boot / Thymeleaf
   through window.officerComplaints.
========================================================= */

/* =========================================================
   GLOBAL STATE
========================================================= */

const complaintsState = {
    complaints: [],
    filteredComplaints: [],
    currentPage: 1,
    pageSize: 10,
    search: "",
    status: "",
    priority: "",
    category: "",
    loading: false
};

/* =========================================================
   DOM READY
========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    initializeComplaintsPage();
});

/* =========================================================
   INITIALIZE PAGE
========================================================= */

function initializeComplaintsPage() {
    if (Array.isArray(window.officerComplaints)) {
        complaintsState.complaints = window.officerComplaints;
    } else {
        complaintsState.complaints = [];
    }

    complaintsState.filteredComplaints = [...complaintsState.complaints];

    updateComplaintSummary();
    initializeFilters();
    initializeRefreshButtons();
    initializePagination();
    initializeSearch();
    initializeStatusModalForm();
    applyFilters();
}

/* =========================================================
   FILTER INITIALIZATION
========================================================= */

function initializeFilters() {
    const statusFilter = document.getElementById("statusFilter");
    const priorityFilter = document.getElementById("priorityFilter");
    const categoryFilter = document.getElementById("categoryFilter");

    if (statusFilter) {
        statusFilter.addEventListener("change", function () {
            complaintsState.status = this.value;
            complaintsState.currentPage = 1;
            markFilterActive(this);
            applyFilters();
        });
    }

    if (priorityFilter) {
        priorityFilter.addEventListener("change", function () {
            complaintsState.priority = this.value;
            complaintsState.currentPage = 1;
            markFilterActive(this);
            applyFilters();
        });
    }

    if (categoryFilter) {
        categoryFilter.addEventListener("change", function () {
            complaintsState.category = this.value;
            complaintsState.currentPage = 1;
            markFilterActive(this);
            applyFilters();
        });
    }

    const resetButton = document.getElementById("resetFilters");
    if (resetButton) {
        resetButton.addEventListener("click", function () {
            resetFilters();
        });
    }
}

/* =========================================================
   SEARCH INITIALIZATION
========================================================= */

function initializeSearch() {
    const searchInput = document.getElementById("complaintSearch");

    if (!searchInput) {
        return;
    }

    searchInput.addEventListener("input", function () {
        complaintsState.search = this.value.trim().toLowerCase();
        complaintsState.currentPage = 1;
        markFilterActive(this);
        applyFilters();
    });
}

/* =========================================================
   APPLY FILTERS
========================================================= */

function applyFilters() {
    let filtered = [...complaintsState.complaints];

    /* SEARCH FILTER */
    if (complaintsState.search) {
        filtered = filtered.filter(function (complaint) {
            const complaintId = String(complaint.complaintNumber || complaint.id || "").toLowerCase();
            const title = String(complaint.title || "").toLowerCase();
            const description = String(complaint.description || "").toLowerCase();
            const category = String(complaint.category || "").toLowerCase();
            const location = String(complaint.location || "").toLowerCase();

            return (
                complaintId.includes(complaintsState.search) ||
                title.includes(complaintsState.search) ||
                description.includes(complaintsState.search) ||
                category.includes(complaintsState.search) ||
                location.includes(complaintsState.search)
            );
        });
    }

    /* STATUS FILTER */
    if (complaintsState.status) {
        filtered = filtered.filter(function (complaint) {
            return normalizeValue(complaint.status) === normalizeValue(complaintsState.status);
        });
    }

    /* PRIORITY FILTER */
    if (complaintsState.priority) {
        filtered = filtered.filter(function (complaint) {
            return normalizeValue(complaint.priority) === normalizeValue(complaintsState.priority);
        });
    }

    /* CATEGORY FILTER */
    if (complaintsState.category) {
        filtered = filtered.filter(function (complaint) {
            return normalizeValue(complaint.category) === normalizeValue(complaintsState.category);
        });
    }

    complaintsState.filteredComplaints = filtered;
    renderComplaints();
}

/* =========================================================
   RENDER COMPLAINTS TABLE
========================================================= */

function renderComplaints() {
    const tableWrapper = document.getElementById("complaintsTableWrapper");
    const emptyState = document.getElementById("complaintsEmpty");
    const tableBody = document.getElementById("complaintsTableBody");
    const pagination = document.getElementById("complaintsPagination");

    if (!tableBody) {
        return;
    }

    tableBody.innerHTML = "";
    const complaints = complaintsState.filteredComplaints;

    /* EMPTY STATE */
    if (!complaints || complaints.length === 0) {
        if (tableWrapper) tableWrapper.classList.add("d-none");
        if (pagination) pagination.classList.add("d-none");
        if (emptyState) emptyState.classList.remove("d-none");

        updatePagination();
        return;
    }

    /* POPULATE TABLE */
    if (emptyState) emptyState.classList.add("d-none");
    if (tableWrapper) tableWrapper.classList.remove("d-none");

    const start = (complaintsState.currentPage - 1) * complaintsState.pageSize;
    const end = start + complaintsState.pageSize;
    const pageComplaints = complaints.slice(start, end);

    pageComplaints.forEach(function (complaint) {
        const row = createComplaintRow(complaint);
        tableBody.appendChild(row);
    });

    if (pagination) pagination.classList.remove("d-none");
    updatePagination();
}

/* =========================================================
   CREATE TABLE ROW
========================================================= */

function createComplaintRow(complaint) {
    const row = document.createElement("tr");

    const complaintId = escapeHtml(complaint.complaintNumber || complaint.id || "—");
    const category = escapeHtml(formatCategory(complaint.category));
    const location = escapeHtml(complaint.location || "—");
    const priority = normalizeValue(complaint.priority);
    const status = normalizeValue(complaint.status);
    const submittedDate = formatDate(complaint.createdAt || complaint.submittedAt);

    const rawId = escapeHtml(String(complaint.id || ""));
    const rawStatus = escapeHtml(String(complaint.status || "PENDING"));
    const rawRemarks = escapeHtml(String(complaint.remarks || ""));

    row.innerHTML = `
        <td>
            <span class="complaint-id">${complaintId}</span>
        </td>
        <td>
            <div class="complaint-category">
                <div class="complaint-category-icon">
                    <i class="${getCategoryIcon(complaint.category)}"></i>
                </div>
                <span class="complaint-category-name">${category}</span>
            </div>
        </td>
        <td>
            <div class="complaint-location">
                <i class="bi bi-geo-alt"></i>
                <span title="${location}">${location}</span>
            </div>
        </td>
        <td>
            ${createPriorityBadge(priority)}
        </td>
        <td>
            <span class="complaint-date">${submittedDate}</span>
        </td>
        <td>
            ${createStatusBadge(status)}
        </td>
        <td class="text-end">
            <div class="d-inline-flex gap-1">
                <a href="/officer/complaint-details?id=${encodeURIComponent(complaint.id || "")}"
                   class="complaint-action-button" title="View complaint">
                    <i class="bi bi-eye"></i>
                </a>
                <button type="button"
                        class="complaint-action-button"
                        title="Update status"
                        onclick="openStatusModal('${rawId}', '${rawStatus}', '${rawRemarks}')">
                    <i class="bi bi-pencil-square"></i>
                </button>
            </div>
        </td>
    `;

    return row;
}

/* =========================================================
   STATUS MODAL HANDLERS
========================================================= */

function openStatusModal(complaintId, currentStatus, currentRemarks) {
    const modalIdInput = document.getElementById('modalComplaintId');
    const modalStatusSelect = document.getElementById('modalStatusSelect');
    const modalRemarksInput = document.getElementById('modalRemarks');

    if (modalIdInput) modalIdInput.value = complaintId;
    if (modalStatusSelect) modalStatusSelect.value = currentStatus || 'PENDING';
    if (modalRemarksInput) modalRemarksInput.value = currentRemarks || '';

    const modalElement = document.getElementById('updateStatusModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    }
}

function initializeStatusModalForm() {
    const updateForm = document.getElementById('updateStatusForm');
    if (!updateForm) return;

    updateForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const complaintId = document.getElementById('modalComplaintId').value;
        const status = document.getElementById('modalStatusSelect').value;
        const remarks = document.getElementById('modalRemarks').value;

        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        const headers = { 'Content-Type': 'application/json' };
        if (token && header) {
            headers[header] = token;
        }

        fetch(`/officer/api/complaints/${complaintId}/status`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ status: status, remarks: remarks })
        })
        .then(response => {
            if (response.ok) {
                const modalElement = document.getElementById('updateStatusModal');
                const modalInstance = bootstrap.Modal.getInstance(modalElement);
                if (modalInstance) {
                    modalInstance.hide();
                }
                window.location.reload();
            } else {
                alert("Failed to update complaint status. Please try again.");
            }
        })
        .catch(error => {
            console.error("Error updating status:", error);
            alert("An error occurred while updating the status.");
        });
    });
}

/* =========================================================
   STATUS BADGE CREATOR
========================================================= */

function createStatusBadge(status) {
    if (!status) {
        return `<span class="status-badge">Unknown</span>`;
    }

    let cssClass = "status-pending";
    let text = "Pending";

    switch (status) {
        case "PENDING":
            cssClass = "status-pending";
            text = "Pending";
            break;
        case "IN_PROGRESS":
            cssClass = "status-progress";
            text = "In Progress";
            break;
        case "RESOLVED":
            cssClass = "status-resolved";
            text = "Resolved";
            break;
        case "REJECTED":
            cssClass = "status-rejected";
            text = "Rejected";
            break;
        default:
            text = formatStatus(status);
    }

    return `<span class="status-badge ${cssClass}">${escapeHtml(text)}</span>`;
}

/* =========================================================
   PRIORITY BADGE CREATOR
========================================================= */

function createPriorityBadge(priority) {
    if (!priority) {
        return `<span class="badge bg-light text-secondary">Unknown</span>`;
    }

    let cssClass = "priority-low";
    let text = "Low";

    switch (priority) {
        case "LOW":
            cssClass = "priority-low";
            text = "Low";
            break;
        case "MEDIUM":
            cssClass = "priority-medium";
            text = "Medium";
            break;
        case "HIGH":
            cssClass = "priority-high";
            text = "High";
            break;
        case "CRITICAL":
            cssClass = "priority-critical";
            text = "Critical";
            break;
        default:
            text = formatStatus(priority);
    }

    return `<span class="badge ${cssClass}">${escapeHtml(text)}</span>`;
}

/* =========================================================
   CATEGORY ICON GETTER
========================================================= */

function getCategoryIcon(category) {
    const value = normalizeValue(category);

    switch (value) {
        case "WATER":
            return "bi bi-droplet-fill";
        case "ROADS":
            return "bi bi-signpost-2-fill";
        case "ELECTRICITY":
            return "bi bi-lightning-charge-fill";
        case "SANITATION":
            return "bi bi-trash3-fill";
        case "HEALTH":
            return "bi bi-heart-pulse-fill";
        default:
            return "bi bi-file-earmark-text-fill";
    }
}

/* =========================================================
   CATEGORY FORMATTER
========================================================= */

function formatCategory(category) {
    if (!category) return "Other";

    const value = normalizeValue(category);
    const categories = {
        WATER: "Water Supply",
        ROADS: "Roads",
        ELECTRICITY: "Electricity",
        SANITATION: "Sanitation",
        HEALTH: "Health",
        OTHER: "Other"
    };

    return categories[value] || formatStatus(value);
}

/* =========================================================
   STATUS FORMATTER
========================================================= */

function formatStatus(value) {
    if (!value) return "Unknown";

    return String(value)
        .toLowerCase()
        .split("_")
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

/* =========================================================
   NORMALIZE VALUE
========================================================= */

function normalizeValue(value) {
    if (value === null || value === undefined) return "";
    return String(value).trim().toUpperCase();
}

/* =========================================================
   DATE FORMATTER
========================================================= */

function formatDate(value) {
    if (!value) return "—";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return escapeHtml(String(value));
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}

/* =========================================================
   UPDATE SUMMARY COUNTS
========================================================= */

function updateComplaintSummary() {
    const complaints = complaintsState.complaints;

    const total = complaints.length;
    const pending = complaints.filter(c => normalizeValue(c.status) === "PENDING").length;
    const inProgress = complaints.filter(c => normalizeValue(c.status) === "IN_PROGRESS").length;
    const resolved = complaints.filter(c => normalizeValue(c.status) === "RESOLVED").length;

    setElementText("totalComplaints", total);
    setElementText("pendingComplaints", pending);
    setElementText("inProgressComplaints", inProgress);
    setElementText("resolvedComplaints", resolved);
}

/* =========================================================
   PAGINATION INITIALIZATION
========================================================= */

function initializePagination() {
    const previous = document.getElementById("previousPage");
    const next = document.getElementById("nextPage");

    if (previous) {
        previous.addEventListener("click", function () {
            if (complaintsState.currentPage > 1) {
                complaintsState.currentPage--;
                renderComplaints();
            }
        });
    }

    if (next) {
        next.addEventListener("click", function () {
            const totalPages = Math.ceil(complaintsState.filteredComplaints.length / complaintsState.pageSize);
            if (complaintsState.currentPage < totalPages) {
                complaintsState.currentPage++;
                renderComplaints();
            }
        });
    }
}

/* =========================================================
   UPDATE PAGINATION
========================================================= */

function updatePagination() {
    const total = complaintsState.filteredComplaints.length;
    const totalPages = Math.max(1, Math.ceil(total / complaintsState.pageSize));
    const currentPage = complaintsState.currentPage;

    const start = total === 0 ? 0 : (currentPage - 1) * complaintsState.pageSize + 1;
    const end = total === 0 ? 0 : Math.min(currentPage * complaintsState.pageSize, total);

    setElementText("paginationStart", start);
    setElementText("paginationEnd", end);
    setElementText("paginationTotal", total);
    setElementText("currentPage", currentPage);

    const previous = document.getElementById("previousPage");
    const next = document.getElementById("nextPage");

    if (previous) previous.disabled = currentPage <= 1;
    if (next) next.disabled = currentPage >= totalPages || total === 0;
}

/* =========================================================
   RESET FILTERS
========================================================= */

function resetFilters() {
    complaintsState.search = "";
    complaintsState.status = "";
    complaintsState.priority = "";
    complaintsState.category = "";
    complaintsState.currentPage = 1;

    const searchInput = document.getElementById("complaintSearch");
    const statusFilter = document.getElementById("statusFilter");
    const priorityFilter = document.getElementById("priorityFilter");
    const categoryFilter = document.getElementById("categoryFilter");

    if (searchInput) {
        searchInput.value = "";
        searchInput.classList.remove("filter-active");
    }

    if (statusFilter) {
        statusFilter.value = "";
        statusFilter.classList.remove("filter-active");
    }

    if (priorityFilter) {
        priorityFilter.value = "";
        priorityFilter.classList.remove("filter-active");
    }

    if (categoryFilter) {
        categoryFilter.value = "";
        categoryFilter.classList.remove("filter-active");
    }

    applyFilters();
}

/* =========================================================
   FILTER ACTIVE VISUAL
========================================================= */

function markFilterActive(element) {
    if (!element) return;

    if (element.value && element.value.trim() !== "") {
        element.classList.add("filter-active");
    } else {
        element.classList.remove("filter-active");
    }
}

/* =========================================================
   REFRESH BUTTONS
========================================================= */

function initializeRefreshButtons() {
    const refreshButton = document.getElementById("refreshComplaints");
    const refreshTable = document.getElementById("refreshTable");

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            window.location.reload();
        });
    }

    if (refreshTable) {
        refreshTable.addEventListener("click", function () {
            window.location.reload();
        });
    }
}

/* =========================================================
   UTILITIES
========================================================= */

function setElementText(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
    }
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}