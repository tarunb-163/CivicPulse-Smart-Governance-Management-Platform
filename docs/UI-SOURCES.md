# CivicPulse UI Sources & Component Inspiration Index

This document records all components inspected and adapted from the 21st.dev MCP component catalog during the Government of India style presentation layer redesign.

> **Design Translation Rule:** All 21st.dev components are originally authored in React + Tailwind CSS with Framer Motion animations. In accordance with project architecture constraints, only the **structural layout, accessibility semantics, and spacing hierarchies** were referenced. Every component was re-implemented strictly in **plain HTML5, vanilla ES modules, and custom CSS classes (`cp-*`) referencing `tokens.css`**. No React, Tailwind CSS, glassy effects, gradients, or unnecessary motion libraries were imported.

---

## Component Inspiration Catalog

### 1. Government Header & Main Navigation
- **21st.dev Component:** `karthikmudunuri/header-02` (Component ID: 18258) & `originui/navbar` (Component ID: 432)
- **Structural Inspirations Taken:** Clean multi-tier sticky header hierarchy, compact mobile navigation drawer toggle, clear separation of brand seal and administrative identity badges.
- **Modifications & Government Adaptations:**
  - Removed all Tailwind utility classes and React state hooks.
  - Added 4px National Tricolour strip (Saffron `#FF9933` / White `#FFFFFF` / Green `#138808`).
  - Added GIGW 3.0 accessibility utility bar: Screen Reader Access link, Text Resizer (**A- A A+**), High-Contrast Mode toggle, and Trilingual language switcher (English / ಕನ್ನಡ / हिन्दी).
  - Integrated neutral CivicPulse Dharma-Chakra SVG seal with dual English & regional typography.
  - Main nav converted to accessible semantic `<nav>` with keyboard-operable dropdowns.
- **Pages Used:** Reusable portal-wide layout across all 25 public, citizen, officer, and admin modules.

### 2. Multi-Step Grievance Filing Wizard (Stepper)
- **21st.dev Component:** `originui/stepper` (Component ID: 769) & `sean0205/c-stepper-15` (Component ID: 29815)
- **Structural Inspirations Taken:** Horizontal step numbers connected by progress track, step validation indicators, active step highlighting.
- **Modifications & Government Adaptations:**
  - Re-implemented with `.cp-stepper`, `.cp-step-item`, and `.cp-step-num` using Gov Navy (`--primary-color`) and Saffron (`--accent-color`).
  - Marked all mandatory form fields with red asterisks and hidden `(required)` tags for screen readers.
  - Retained all duplicate detection dialog hooks, Leaflet coordinate pickers, photo preview chips, and anonymous toggles.
- **Pages Used:** `citizen/new-complaint.html`.

### 3. Accessible Breadcrumb Navigation
- **21st.dev Component:** `olivier_1b6cd5bc/breadcrumb-1` (Component ID: 5675) & `originui/breadcrumb` (Component ID: 440)
- **Structural Inspirations Taken:** Semantic ordered list structure `<ol>`, `aria-label="Breadcrumb"`, clear hierarchy separators.
- **Modifications & Government Adaptations:**
  - Removed custom Tailwind SVG icons in favor of simple, standardized `/` separators.
  - Styled with `.cp-breadcrumb-bar`, `.cp-breadcrumb-list`, ensuring 4.5:1 text contrast on light grey canvas.
- **Pages Used:** All inner portal modules (`citizen/*`, `officer/*`, `admin/*`, `track.html`, `transparency.html`, statutory pages).

### 4. Sarkari Metric & KPI Stat Cards
- **21st.dev Component:** `ravikatiyar162/stats-card-1` (Component ID: 8321) & `shadcnstore/statistic-2` (Component ID: 30001)
- **Structural Inspirations Taken:** Grid card container, primary metric numeral with small uppercase semantic descriptor, subtle icon grounding.
- **Modifications & Government Adaptations:**
  - Completely stripped glassmorphic backdrop filters, border glows, and tilting scripts (`cp-tilt`).
  - Redesigned as crisp 1px solid bordered cards (`border: 1px solid var(--border-color)`) with 4px border-radius, white background, and navy/saffron/green status indicators.
- **Pages Used:** `index.html`, `transparency.html`, `citizen/dashboard.html`, `officer/dashboard.html`, `admin/dashboard.html`.

### 5. Frequently Asked Questions (Accordion)
- **21st.dev Component:** `kedhareswer/slit-type-faq` (Component ID: 38218) & `hexui-sh/faq-1` (Component ID: 31986)
- **Structural Inspirations Taken:** Clean question header with right-aligned chevron indicator, expandable text region with proper ARIA attributes.
- **Modifications & Government Adaptations:**
  - Replaced Framer Motion height transitions with standard accessible Bootstrap 5 accordion markup.
  - Applied calm government borders (1px solid `#D0D7DE`), white background, and 150ms rotation transition.
- **Pages Used:** `index.html`, `help.html`.

### 6. Administrative Data Tables with Filters & Pagination
- **21st.dev Component:** `originui/table` (Component ID: 412) & `karthikmudunuri/table-01` (Component ID: 18270)
- **Structural Inspirations Taken:** Compact table header with sorting indicators, responsive wrapper, integrated search/filter input header, bottom pagination footer.
- **Modifications & Government Adaptations:**
  - Re-implemented with native HTML5 `<table>`, `<caption>` (with `visually-hidden` description), and `<th scope="col">`.
  - Applied Sarkari flat borders (`.cp-table`), high-contrast text, monospace styling for grievance IDs, and accessible action buttons with distinct ARIA labels.
- **Pages Used:** `citizen/my-complaints.html`, `officer/queue.html`, `admin/departments.html`, `admin/categories.html`, `admin/rules.html`, `admin/users.html`, `admin/sla.html`, `admin/audit.html`, `admin/outbox.html`, `admin/settings.html`.

### 7. Statutory 4-Column Government Footer
- **21st.dev Component:** `solaceui/footer-section-4` (Component ID: 19356) & `meschacirung/footer-2` (Component ID: 27521)
- **Structural Inspirations Taken:** 4-column categorical layout for structured link navigation, lower copyright and compliance bar.
- **Modifications & Government Adaptations:**
  - Rewritten for Indian governance standards: About CivicPulse, Portal Navigation, Statutory & Policies (Accessibility, Screen Reader, Privacy, Terms, Sitemap), Help & Nodal Contacts.
  - Mandatory compliance disclaimer added: *"Demonstration project. Not an official Government website. Best viewed in latest Chrome, Edge, Firefox, Safari."*
  - Preserved required developer attribution: *"© 2026 CivicPulse Municipal Redressal developed by Tarun B"*.
- **Pages Used:** All 25 HTML pages across the application.

### 8. Announcement Notice Strip ("What's New")
- **21st.dev Component:** `shadcn/marquee` (Component ID: 12054)
- **Structural Inspirations Taken:** Alert banner presenting high-priority public announcements.
- **Modifications & Government Adaptations:**
  - Infinite auto-scrolling marquee was strictly rejected to comply with **WCAG 2.1 AA Criterion 2.2.2 (Pause, Stop, Hide)**.
  - Re-implemented as a calm ticker with a prominent **Pause/Play** control button and support for `prefers-reduced-motion`.
- **Pages Used:** `index.html`.

### 9. Vertical Status Chronology & Official Receipt
- **21st.dev Component:** `originui/timeline` (Component ID: 790)
- **Structural Inspirations Taken:** Vertical chronological node sequence with status icons and departmental remark cards.
- **Modifications & Government Adaptations:**
  - Converted into an official Acknowledgment Receipt format with 2-column definition metadata table, QR code verification box, and dedicated `@media print` stylesheet (pure black-and-white, chrome-hidden layout).
- **Pages Used:** `track.html`, `citizen/complaint.html`.

---

## Rejected Patterns Summary

In compliance with the project's visual language constraints ("Sarkari, clean and calm"):
- **Aurora Glass / Backdrop Blur:** Completely rejected.
- **Glow Borders & Hover Halos:** Completely rejected.
- **3D Card Tilts / Parallax Mouse Tracking:** Completely removed and disabled.
- **Endless Number Spinners / Particle Effects:** Completely removed.
- **Confetti Cannons:** Disabled on grievance submission.
- **Framer Motion / Tailwind CSS:** Zero runtime dependencies added.
