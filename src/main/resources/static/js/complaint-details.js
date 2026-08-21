/* =========================================================
   CIVICPULSE - COMPLAINT DETAILS
   complaint-details.js
========================================================= */

let currentComplaint = null;

/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    initializeComplaintDetails();
});

/* =========================================================
   INITIALIZE
========================================================= */

function initializeComplaintDetails() {
    const complaintId = getComplaintIdFromUrl();

    if (!complaintId) {
        showNotFound();
        return;
    }

    loadComplaint(complaintId);
}

/* =========================================================
   GET COMPLAINT ID FROM URL
   Supports both Query Params (?id=CP-2026-001)
   and REST Path Variables (/officer/complaints/CP-2026-001)
========================================================= */

function getComplaintIdFromUrl() {
    // 1. Check for query parameter (?id=...)
    const params = new URLSearchParams(window.location.search);
    const queryId = params.get("id");

    if (queryId) {
        return queryId;
    }

    // 2. Fallback to path segment (/officer/complaints/{id})
    const pathSegments = window.location.pathname.split("/").filter(Boolean);
    const pathId = pathSegments[pathSegments.length - 1];

    if (pathId && pathId.toLowerCase() !== "details" && pathId.toLowerCase() !== "complaints") {
        return decodeURIComponent(pathId);
    }

    return null;
}

/* =========================================================
   LOAD COMPLAINT FROM SPRING BOOT REST API
========================================================= */

async function loadComplaint(complaintId) {
    showLoading();

    try {
        const response = await fetch(`/api/officer/complaints/${encodeURIComponent(complaintId)}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch complaint details. Status: ${response.status}`);
        }

        const complaint = await response.json();

        if (!complaint || !complaint.id) {
            showNotFound();
            return;
        }

        currentComplaint = complaint;
        populateComplaintDetails(complaint);
        showComplaintContent();

    } catch (error) {
        console.error("Error loading complaint details:", error);
        showNotFound();
    }
}

/* =========================================================
   POPULATE COMPLAINT DETAILS
========================================================= */

function populateComplaintDetails(complaint) {

    /* BASIC INFORMATION */
    setText("complaintId", complaint.id);
    setText("complaintTitle", complaint.title);
    setText("complaintDescription", complaint.description);
    setText("complaintLocation", complaint.location);
    setText("complaintCategory", formatCategory(complaint.category));
    setText("complaintDepartment", complaint.department);

    /* DATE */
    const formattedDate = formatDate(complaint.createdAt);
    setText("complaintSubmittedDate", "Submitted " + formattedDate);

    /* STATUS & PRIORITY */
    renderStatus(complaint.status);
    renderPriority(complaint.priority);

    /* ATTACHMENTS */
    renderAttachments(complaint.attachments);

    /* UPDATE BUTTON */
    const updateButton = document.getElementById("updateComplaintButton");
    if (updateButton) {
        updateButton.href = "/officer/update-complaint?id=" + encodeURIComponent(complaint.id);
    }

    /* TIMELINE */
    renderTimeline(complaint);
}
/* =========================================================
   RENDER STATUS
========================================================= */

function renderStatus(status) {
    const container = document.getElementById("complaintStatus");
    if (!container) return;

    const normalized = normalizeValue(status);
    let cssClass = "status-pending";
    let text = "Pending";

    switch (normalized) {
        case "PENDING":
            cssClass = "status-pending";
            text = "Pending";
            break;
        case "IN_PROGRESS":
        case "PROCESSING":
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
            text = formatStatus(normalized);
    }

    container.innerHTML = `
        <span class="status-badge ${cssClass}">
            ${escapeHtml(text)}
        </span>
    `;
}

/* =========================================================
   RENDER PRIORITY
========================================================= */

function renderPriority(priority) {
    const container = document.getElementById("complaintPriority");
    if (!container) return;

    const normalized = normalizeValue(priority);
    let cssClass = "priority-low";
    let text = "Low";

    switch (normalized) {
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
            text = formatStatus(normalized);
    }

    container.innerHTML = `
        <span class="badge ${cssClass}">
            ${escapeHtml(text)}
        </span>
    `;
}

/* =========================================================
   RENDER ATTACHMENTS
========================================================= */

function renderAttachments(attachments) {
    const emptyState = document.getElementById("attachmentsEmpty");
    const listContainer = document.getElementById("attachmentsList");

    if (!listContainer || !emptyState) return;

    if (!attachments || !Array.isArray(attachments) || attachments.length === 0) {
        emptyState.classList.remove("d-none");
        listContainer.classList.add("d-none");
        listContainer.innerHTML = "";
        return;
    }

    emptyState.classList.add("d-none");
    listContainer.classList.remove("d-none");

    listContainer.innerHTML = attachments.map(file => `
        <div class="attachment-item d-flex align-items-center justify-content-between p-2 mb-2 border rounded">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-file-earmark-text fs-5 text-primary"></i>
                <span>${escapeHtml(file.fileName || file.name || "Attachment")}</span>
            </div>
            <a href="${escapeHtml(file.fileUrl || file.url || "#")}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-download me-1"></i> View / Download
            </a>
        </div>
    `).join("");
}

/* =========================================================
   RENDER TIMELINE
========================================================= */

function renderTimeline(complaint) {
    setText("submittedTimelineDate", formatDate(complaint.createdAt));
    setText("assignedTimelineDate", formatDate(complaint.assignedAt));
    setText("progressTimelineDate", formatDate(complaint.updatedAt));
    setText("resolvedTimelineDate", formatDate(complaint.resolvedAt));

    updateTimelineState(complaint.status);
}

/* =========================================================
   UPDATE TIMELINE STATE
========================================================= */

function updateTimelineState(status) {
    const normalized = normalizeValue(status);
    const timelineItems = document.querySelectorAll(".timeline-item");

    if (!timelineItems.length) return;

    let activeIndex = 0;

    switch (normalized) {
        case "PENDING":
            activeIndex = 0;
            break;
        case "ASSIGNED":
            activeIndex = 1;
            break;
        case "IN_PROGRESS":
        case "PROCESSING":
        case "REJECTED":
            activeIndex = 2;
            break;
        case "RESOLVED":
            activeIndex = 3;
            break;
        default:
            activeIndex = 0;
    }

    timelineItems.forEach((item, index) => {
        item.classList.remove("completed", "current", "inactive");

        if (index < activeIndex) {
            item.classList.add("completed");
        } else if (index === activeIndex) {
            item.classList.add("current");
        } else {
            item.classList.add("inactive");
        }
    });
}

/* =========================================================
   STATE VISIBILITY HELPERS
========================================================= */

function showLoading() {
    const loading = document.getElementById("complaintLoading");
    const notFound = document.getElementById("complaintNotFound");
    const content = document.getElementById("complaintDetailsContent");

    if (loading) loading.classList.remove("d-none");
    if (notFound) notFound.classList.add("d-none");
    if (content) content.classList.add("d-none");
}

function showComplaintContent() {
    const loading = document.getElementById("complaintLoading");
    const notFound = document.getElementById("complaintNotFound");
    const content = document.getElementById("complaintDetailsContent");

    if (loading) loading.classList.add("d-none");
    if (notFound) notFound.classList.add("d-none");
    if (content) content.classList.remove("d-none");
}

function showNotFound() {
    const loading = document.getElementById("complaintLoading");
    const notFound = document.getElementById("complaintNotFound");
    const content = document.getElementById("complaintDetailsContent");

    if (loading) loading.classList.add("d-none");
    if (content) content.classList.add("d-none");
    if (notFound) notFound.classList.remove("d-none");
}

/* =========================================================
   UTILITY & FORMATTING FUNCTIONS
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

function formatStatus(value) {
    if (!value) return "Unknown";

    return String(value)
        .toLowerCase()
        .split("_")
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

function normalizeValue(value) {
    if (value === null || value === undefined) return "";
    return String(value).trim().toUpperCase();
}

function formatDate(value) {
    if (!value) return "—";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function setText(elementId, value) {
    const element = document.getElementById(elementId);
    if (!element) return;

    element.textContent = (value === null || value === undefined || value === "") ? "—" : value;
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