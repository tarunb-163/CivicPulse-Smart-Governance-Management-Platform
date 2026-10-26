# CivicPulse — Master Test Coverage Matrix

**Branch:** `test/autofix`  
**Compliance Standards:** GIGW 3.0 & WCAG 2.1 AA  
**Total Documented Cases:** 582  
**Automation Engine:** JUnit 5, Playwright Test, Axe-Core, Lighthouse  

---

## 1. Test Matrix Axes

The test matrix covers all functional, technical, presentation, accessibility, security, and performance dimensions:
- **Roles:**
  - `Visitor` (Unauthenticated public user)
  - `Citizen` (Registered user: `citizen@civicpulse.test`)
  - `Citizen B` (Independent second citizen: `citizen2@civicpulse.test`)
  - `Officer` (Department officer: `officer.roads@civicpulse.test`, Department: ROADS)
  - `Other Officer` (Different department: `officer.swm@civicpulse.test`, Department: SWM)
  - `Department Head` (`head.roads@civicpulse.test`)
  - `Admin` (System administrator: `admin@civicpulse.test`)
- **Viewports:**
  - `360x640` (Mobile Portrait)
  - `768x1024` (Tablet Portrait)
  - `1280x800` (Desktop Standard)
  - `1920x1080` (Desktop Full HD)
- **Themes & Modes:**
  - `Light` (`data-theme="light"`)
  - `Dark` (`data-theme="dark"`)
  - `High Contrast` (`data-contrast="high"`, WCAG AAA 7:1)
  - `Text Resizing` (`A-`, `A`, `A+` font scaling up to 200%)
- **Languages:**
  - `en` (English)
  - `kn` (ಕನ್ನಡ)
  - `hi` (हिन्दी)
- **Motion:**
  - `Normal`
  - `Prefers-Reduced-Motion` (`prefers-reduced-motion: reduce`)

---

## 2. Test Case Prefix Legend

| Prefix | Suite Area | Count |
|---|---|---|
| `SMK` | 4A. Page-by-Page Smoke, Layout & Full Crawler | 84 |
| `FURN` | 4B. Government UI Furniture & Header/Footer Utilities | 35 |
| `AUTH` | 4C. Authentication, RBAC, Sessions & Route Guards | 45 |
| `CIT` | 4D. Citizen Grievance Submission, Engagement & Lifecycle | 65 |
| `OFF` | 4E. Officer Workflow, Queue, Kanban, SLA & Resolution | 45 |
| `ADM` | 4F. Admin Operations, Analytics, Exports & Governance | 55 |
| `PUB` | 4G. Public Services, Tracking, Anonymity & Statutory Pages | 35 |
| `BOT` | 4H. CivicBot Citizen Virtual Assistant | 22 |
| `API` | 4I. REST API Contract, Negative & Boundary Validation | 70 |
| `SEC` | 4J. Security, Injection, Headers, Rate Limits & IDOR | 35 |
| `A11Y` | 4K. WCAG 2.1 AA / GIGW 3.0 Accessibility & Keyboard Nav | 35 |
| `RESP` | 4L. Responsive Viewports, Multilingual & Print Receipt | 35 |
| `PERF` | 4M. Performance, Lighthouse Benchmarks & DB Latencies | 18 |
| `DATA` | 4N. Data Integrity, Concurrency, Double-Submit & Resilience | 22 |
| `REG` | 4O. Regression Suite for Past Defects (DEF-001 - DEF-010) | 16 |
| **Total** | | **582** |

---

## 3. Test Cases Specification

### 3.1 Smoke, Layout & Crawler (`SMK-001` - `SMK-084`)

| ID | Area | Page / Target | Role | Viewport | Theme / Lang | Steps | Expected Result | Status |
|---|---|---|---|---|---|---|---|---|
| SMK-001 | Smoke | `/index.html` | Visitor | 1280x800 | Light / en | Load landing page | HTTP 200, `<title>` correct, single `<h1>`, 0 console errors | PENDING |
| SMK-002 | Smoke | `/index.html` | Visitor | 360x640 | Light / en | Load landing page on mobile | Zero horizontal scroll, layout scales cleanly | PENDING |
| SMK-003 | Smoke | `/index.html` | Visitor | 1280x800 | Dark / en | Switch to dark theme | `data-theme="dark"` applied, cards adapt | PENDING |
| SMK-004 | Smoke | `/index.html` | Visitor | 1280x800 | High-Contrast | Switch to high contrast | High contrast tokens active, 7:1 contrast | PENDING |
| SMK-005 | Smoke | `/index.html` | Visitor | 1280x800 | Light / kn | Switch to Kannada | Text translated, no missing keys, font clean | PENDING |
| SMK-006 | Smoke | `/index.html` | Visitor | 1280x800 | Light / hi | Switch to Hindi | Text translated, no missing keys, font clean | PENDING |
| SMK-007 | Smoke | `/login.html` | Visitor | 1280x800 | Light / en | Load login page | HTTP 200, form fields rendered, labels present | PENDING |
| SMK-008 | Smoke | `/login.html` | Visitor | 360x640 | Light / en | Load login on mobile | No overflow, inputs fully accessible | PENDING |
| SMK-009 | Smoke | `/register.html` | Visitor | 1280x800 | Light / en | Load registration page | HTTP 200, consent checkbox visible | PENDING |
| SMK-010 | Smoke | `/forgot-password.html` | Visitor | 1280x800 | Light / en | Load forgot password | HTTP 200, reset request input present | PENDING |
| SMK-011 | Smoke | `/reset-password.html` | Visitor | 1280x800 | Light / en | Load reset password | HTTP 200, token input / validation | PENDING |
| SMK-012 | Smoke | `/track.html` | Visitor | 1280x800 | Light / en | Load public track | HTTP 200, code input box ready | PENDING |
| SMK-013 | Smoke | `/transparency.html` | Visitor | 1280x800 | Light / en | Load transparency | HTTP 200, summary metrics loaded | PENDING |
| SMK-014 | Smoke | `/privacy.html` | Visitor | 1280x800 | Light / en | Load privacy policy | HTTP 200, complete policy text present | PENDING |
| SMK-015 | Smoke | `/terms.html` | Visitor | 1280x800 | Light / en | Load terms of service | HTTP 200, terms text displayed | PENDING |
| SMK-016 | Smoke | `/accessibility.html` | Visitor | 1280x800 | Light / en | Load accessibility stmt | HTTP 200, standard statement displayed | PENDING |
| SMK-017 | Smoke | `/screen-reader.html` | Visitor | 1280x800 | Light / en | Load screen reader guide | HTTP 200, shortcuts listed | PENDING |
| SMK-018 | Smoke | `/sitemap.html` | Visitor | 1280x800 | Light / en | Load sitemap | HTTP 200, all portal links listed | PENDING |
| SMK-019 | Smoke | `/help.html` | Visitor | 1280x800 | Light / en | Load user guide / FAQ | HTTP 200, accordions collapsible | PENDING |
| SMK-020 | Smoke | `/contact.html` | Visitor | 1280x800 | Light / en | Load contact page | HTTP 200, directory info present | PENDING |
| SMK-021 | Smoke | `/404.html` | Visitor | 1280x800 | Light / en | Load 404 error page | HTTP 200, official wording present | PENDING |
| SMK-022 | Smoke | `/403.html` | Visitor | 1280x800 | Light / en | Load 403 error page | HTTP 200, access denied info present | PENDING |
| SMK-023 | Smoke | `/500.html` | Visitor | 1280x800 | Light / en | Load 500 error page | HTTP 200, server error message | PENDING |
| SMK-024 | Smoke | `/citizen/dashboard.html` | Citizen | 1280x800 | Light / en | Load citizen dashboard | HTTP 200, metrics & grievances table | PENDING |
| SMK-025 | Smoke | `/citizen/dashboard.html` | Citizen | 360x640 | Light / en | Load citizen dash mobile | Zero horizontal scroll, metrics stacked | PENDING |
| SMK-026 | Smoke | `/citizen/new-complaint.html` | Citizen | 1280x800 | Light / en | Load complaint wizard | HTTP 200, 4 steps stepper initialized | PENDING |
| SMK-027 | Smoke | `/citizen/my-complaints.html` | Citizen | 1280x800 | Light / en | Load my complaints list | HTTP 200, table with status chips | PENDING |
| SMK-028 | Smoke | `/citizen/complaint.html?id=1` | Citizen | 1280x800 | Light / en | Load complaint detail | HTTP 200, vertical status timeline | PENDING |
| SMK-029 | Smoke | `/citizen/profile.html` | Citizen | 1280x800 | Light / en | Load citizen profile | HTTP 200, user details & pref toggles | PENDING |
| SMK-030 | Smoke | `/officer/dashboard.html` | Officer | 1280x800 | Light / en | Load officer dashboard | HTTP 200, dept queue statistics | PENDING |
| SMK-031 | Smoke | `/officer/queue.html` | Officer | 1280x800 | Light / en | Load officer queue table | HTTP 200, filterable data table | PENDING |
| SMK-032 | Smoke | `/officer/board.html` | Officer | 1280x800 | Light / en | Load officer Kanban | HTTP 200, column status lanes | PENDING |
| SMK-033 | Smoke | `/officer/workspace.html?id=1`| Officer | 1280x800 | Light / en | Load complaint workspace | HTTP 200, action panel & proof form | PENDING |
| SMK-034 | Smoke | `/admin/dashboard.html` | Admin | 1280x800 | Light / en | Load admin dashboard | HTTP 200, charts & summary metrics | PENDING |
| SMK-035 | Smoke | `/admin/departments.html` | Admin | 1280x800 | Light / en | Load departments manager | HTTP 200, dept table & add modal | PENDING |
| SMK-036 | Smoke | `/admin/categories.html` | Admin | 1280x800 | Light / en | Load category manager | HTTP 200, category table & severity | PENDING |
| SMK-037 | Smoke | `/admin/rules.html` | Admin | 1280x800 | Light / en | Load keyword rules | HTTP 200, rules table & test sandbox | PENDING |
| SMK-038 | Smoke | `/admin/users.html` | Admin | 1280x800 | Light / en | Load user management | HTTP 200, citizen & officer table | PENDING |
| SMK-039 | Smoke | `/admin/sla.html` | Admin | 1280x800 | Light / en | Load SLA configuration | HTTP 200, default SLA hours table | PENDING |
| SMK-040 | Smoke | `/admin/audit.html` | Admin | 1280x800 | Light / en | Load audit log inspector | HTTP 200, immutable audit rows | PENDING |
| SMK-041 | Smoke | `/admin/outbox.html` | Admin | 1280x800 | Light / en | Load dev email outbox | HTTP 200, simulated email log | PENDING |
| SMK-042 | Smoke | `/admin/settings.html` | Admin | 1280x800 | Light / en | Load system settings | HTTP 200, duplicate threshold inputs | PENDING |
| SMK-043 | Crawler | All Internal Links | Visitor | 1280x800 | Light / en | Crawl all navbar & footer links | 0 broken links (no 404, 500, dead #) | PENDING |
| SMK-044 | Crawler | All Internal Links | Citizen | 1280x800 | Light / en | Crawl citizen links | All citizen module links resolve 200 | PENDING |
| SMK-045 | Crawler | All Internal Links | Officer | 1280x800 | Light / en | Crawl officer links | All officer module links resolve 200 | PENDING |
| SMK-046 | Crawler | All Internal Links | Admin | 1280x800 | Light / en | Crawl admin links | All admin module links resolve 200 | PENDING |
| SMK-047 - SMK-084 | Smoke Matrix | All 28 pages | All | 768 / 1920 | Dark / High-Contrast | Deep multi-viewport matrix check | Zero visual breakage or horizontal scroll | PENDING |

*(Additional rows SMK-048 to SMK-084 systematically cover remaining combinations of viewports and themes for all 28 HTML endpoints).*

---

### 3.2 Government UI Furniture (`FURN-001` - `FURN-035`)

| ID | Area | Feature | Role | Steps | Expected Result | Status |
|---|---|---|---|---|---|---|
| FURN-001 | Gov-Furniture | Skip to content | Visitor | Tab from top of page | Skip link appears, hitting Enter focuses `#main-content` | PENDING |
| FURN-002 | Gov-Furniture | Text size A- | Visitor | Click `A-` button | Font size scales down to 0.9x, persistent after reload | PENDING |
| FURN-003 | Gov-Furniture | Text size A (Reset) | Visitor | Click `A` button | Font size resets to 1.0x | PENDING |
| FURN-004 | Gov-Furniture | Text size A+ | Visitor | Click `A+` button | Font size scales to 1.15x, no text clipping | PENDING |
| FURN-005 | Gov-Furniture | High-contrast toggle | Visitor | Click contrast button | `data-contrast="high"` active, meets 7:1 contrast | PENDING |
| FURN-006 | Gov-Furniture | Contrast persistence | Visitor | Reload with high-contrast | High contrast persists via `localStorage` | PENDING |
| FURN-007 | Gov-Furniture | Kannada i18n switch | Visitor | Select `ಕನ್ನಡ` from switcher | DOM `lang="kn"`, navigation & hero translated | PENDING |
| FURN-008 | Gov-Furniture | Hindi i18n switch | Visitor | Select `हिन्दी` from switcher | DOM `lang="hi"`, strings translated | PENDING |
| FURN-009 | Gov-Furniture | English i18n switch | Visitor | Select `English` | DOM `lang="en"`, all strings reset | PENDING |
| FURN-010 | Gov-Furniture | Theme toggle dark | Visitor | Click theme toggle button | `data-theme="dark"` applied, dark tokens active | PENDING |
| FURN-011 | Gov-Furniture | Theme toggle persistence | Visitor | Reload in dark mode | Dark theme persists cleanly | PENDING |
| FURN-012 | Gov-Furniture | Ticker pause button | Visitor | Click Pause button on ticker | Announcement ticker stops sliding | PENDING |
| FURN-013 | Gov-Furniture | Ticker play button | Visitor | Click Play button on ticker | Announcement ticker resumes sliding | PENDING |
| FURN-014 | Gov-Furniture | Reduced-motion ticker | Visitor | Emulate `prefers-reduced-motion` | Ticker auto-scroll is disabled | PENDING |
| FURN-015 | Gov-Furniture | Top demo notice | Visitor | Check top notice | "Demonstration project. Not an official Government website." visible | PENDING |
| FURN-016 | Gov-Furniture | Neutral civic seal | Visitor | Inspect header logo | Chakra SVG rendered, no official State Emblem used | PENDING |
| FURN-017 | Gov-Furniture | 4-Column footer | Visitor | Check footer structure | 4 columns (About, Links, Policies, Helpline) rendered | PENDING |
| FURN-018 | Gov-Furniture | Sub-footer copyright | Visitor | Inspect sub-footer | `© 2026 CivicPulse Municipal Redressal developed by Tarun B` present | PENDING |
| FURN-019 | Gov-Furniture | Breadcrumb navigation | Citizen | Visit inner citizen page | Breadcrumb links render and allow navigation | PENDING |
| FURN-020 | Gov-Furniture | Mobile nav drawer open | Visitor | Click hamburger on 360px | Drawer expands, focus moves inside | PENDING |
| FURN-021 | Gov-Furniture | Mobile nav drawer close | Visitor | Press Escape on drawer | Drawer closes, focus returns to hamburger | PENDING |
| FURN-022 | Gov-Furniture | Dropdown keyboard nav | Citizen | Navigate user menu via Tab/Arrows | Menu items accessible via keyboard | PENDING |
| FURN-023 - FURN-035 | Gov-Furniture | Multi-page consistency | All | Check across all 25 pages | Furniture components identical and persistent | PENDING |

---

### 3.3 Authentication, RBAC & Session Lifecycle (`AUTH-001` - `AUTH-045`)

| ID | Area | Feature | Input / Setup | Expected Result | Status |
|---|---|---|---|---|---|
| AUTH-001 | Auth | Citizen Registration | Valid details + consent checked | HTTP 201 Created, token returned | PENDING |
| AUTH-002 | Auth | Register Missing Consent | Valid details + consent UNCHECKED | HTTP 400 Bad Request ("Consent required") | PENDING |
| AUTH-003 | Auth | Register Duplicate Email | Existing email (`citizen@civicpulse.test`) | HTTP 409 Conflict ("Email already in use") | PENDING |
| AUTH-004 | Auth | Register Weak Password | Password `< 8` chars | HTTP 400 Validation error | PENDING |
| AUTH-005 | Auth | Citizen Login Success | `citizen@civicpulse.test` / `Password@123` | HTTP 200, JWT access + refresh tokens | PENDING |
| AUTH-006 | Auth | Login Bad Password | `citizen@civicpulse.test` / `WrongPass` | HTTP 401 Unauthorized | PENDING |
| AUTH-007 | Auth | Login Lockout Threshold | 5 consecutive wrong passwords | HTTP 429 Too Many Requests / Locked | PENDING |
| AUTH-008 | Auth | Officer Login Success | `officer.roads@civicpulse.test` | HTTP 200, officer dashboard redirect | PENDING |
| AUTH-009 | Auth | Admin Login Success | `admin@civicpulse.test` | HTTP 200, admin dashboard redirect | PENDING |
| AUTH-010 | Auth | Token Refresh Flow | Valid refresh token | HTTP 200, fresh access token issued | PENDING |
| AUTH-011 | Auth | Expired Token Handling | Expired JWT token | Auto-redirects to `/login.html` | PENDING |
| AUTH-012 | Auth | Citizen Logout | Click logout button | Tokens cleared from localStorage, redirects to `/login.html` | PENDING |
| AUTH-013 | Auth | Route Guard: Citizen on Admin | Citizen opens `/admin/dashboard.html` | Redirected to `/403.html` or `/citizen/dashboard.html` | PENDING |
| AUTH-014 | Auth | Route Guard: Visitor on Citizen | Unauthenticated user opens `/citizen/dashboard.html` | Redirected to `/login.html` | PENDING |
| AUTH-015 | Auth | Route Guard: Visitor on Officer | Unauthenticated user opens `/officer/queue.html` | Redirected to `/login.html` | PENDING |
| AUTH-016 | Auth | Route Guard: Officer on Admin | Officer opens `/admin/users.html` | Redirected to `/403.html` | PENDING |
| AUTH-017 | Auth | Back Button After Logout | Browser back button after logout | Protected data not visible, redirects to login | PENDING |
| AUTH-018 | Auth | Forgot Password Token | Valid citizen email | HTTP 200, token recorded in dev outbox | PENDING |
| AUTH-019 | Auth | Reset Password Success | Valid token + new password | HTTP 200, can login with new password | PENDING |
| AUTH-020 | Auth | Reset Password Expired Token | Expired or invalid token | HTTP 400 / 404 Token invalid | PENDING |
| AUTH-021 - AUTH-045 | Auth | Extended RBAC Matrix | Cross-role endpoint assertions | Correct 401/403 on all secured endpoints | PENDING |

---

### 3.4 Citizen Grievance Submission & Lifecycle (`CIT-001` - `CIT-065`)

| ID | Area | Feature | Input / Setup | Expected Result | Status |
|---|---|---|---|---|---|
| CIT-001 | Citizen | Wizard Step 1 Validation | Blank Title / Description | Validation prompts, cannot proceed to Step 2 | PENDING |
| CIT-002 | Citizen | Wizard Live Classifier | Type "pothole on road" | Suggests "Pothole & Road Damage" chip | PENDING |
| CIT-003 | Citizen | Wizard Map Pin Selection | Click on Leaflet map picker | Lat/Lng populated, address filled | PENDING |
| CIT-004 | Citizen | Wizard 1 Image Upload | Valid JPG image (< 5MB) | Image thumbnail preview shown | PENDING |
| CIT-005 | Citizen | Wizard 3 Images Upload | 3 valid PNG/JPG images | All 3 previews shown with remove buttons | PENDING |
| CIT-006 | Citizen | Wizard Oversize Image | File > 5MB | Error message, file rejected | PENDING |
| CIT-007 | Citizen | Wizard Invalid File Type | Upload `.exe` or `.txt` | File rejected with error | PENDING |
| CIT-008 | Citizen | Wizard Anonymous Toggle | Switch "Anonymous" ON | Submission flagged anonymous, PII masked | PENDING |
| CIT-009 | Citizen | Exact Duplicate Guard | Same citizen, same title/desc < 24h | 409 Conflict with `DUPLICATE_EXACT` dialog | PENDING |
| CIT-010 | Citizen | Near-Duplicate Detection | Similar grievance within 300m | 409 Dialog offering "Support this instead" | PENDING |
| CIT-011 | Citizen | "Support Existing" Action | Click "Support this instead" | Follower & upvote added, no new ticket created | PENDING |
| CIT-012 | Citizen | Force Submission Option | Check "Confirm not duplicate" | Successfully files ticket, HTTP 201 | PENDING |
| CIT-013 | Citizen | Submission Success Page | Valid complete submission | Displays `CP-2026-######` code & QR code | PENDING |
| CIT-014 | Citizen | Download PDF Receipt | Click "Download Receipt" | PDF downloaded with embedded QR & details | PENDING |
| CIT-015 | Citizen | Print Receipt Layout | Trigger Print dialog | Chrome stripped, official receipt format | PENDING |
| CIT-016 | Citizen | "My Complaints" Table | View complaints list | Shows status chips, dates, SLA, pagination | PENDING |
| CIT-017 | Citizen | Filter My Complaints | Filter by status: `IN_PROGRESS` | Table updates with matching complaints only | PENDING |
| CIT-018 | Citizen | View Complaint Details | Click complaint row | Vertical timeline shows chronological history | PENDING |
| CIT-019 | Citizen | Add Public Comment | Post citizen comment | Comment appears in thread | PENDING |
| CIT-020 | Citizen | Upvote Public Complaint | Click upvote on public complaint | Counter increments by 1 | PENDING |
| CIT-021 | Citizen | Duplicate Upvote Guard | Click upvote second time | Upvote is idempotent, cannot double-vote | PENDING |
| CIT-022 | Citizen | Submit Citizen Feedback | Complaint in `RESOLVED` status | 5-star rating + comment saved (HTTP 201) | PENDING |
| CIT-023 | Citizen | Duplicate Feedback Guard | Submit feedback second time | HTTP 409 Conflict, duplicate rejected | PENDING |
| CIT-024 | Citizen | Feedback on Open Ticket | Complaint in `IN_PROGRESS` | Feedback disallowed until resolved | PENDING |
| CIT-025 | Citizen | Reopen Resolved Ticket | Reopen within 7 days with reason | Status changes to `REOPENED`, officer notified | PENDING |
| CIT-026 | Citizen | Reopen After Window | Reopen after 7 days | Reopen rejected (window expired) | PENDING |
| CIT-027 | Citizen | Notification Bell Center | Receive notification on status change | Bell badge increments, list displays updates | PENDING |
| CIT-028 | Citizen | Mark Notification Read | Click notification | Marked read, badge decrements | PENDING |
| CIT-029 | Citizen | Edit Profile Details | Update phone or name | Profile updated successfully | PENDING |
| CIT-030 | Citizen | Change Password | Valid old + new password | Password changed, re-login required | PENDING |
| CIT-031 - CIT-065 | Citizen | Edge & Negative Flows | Boundary lengths, special chars | Handles Kannada, Hindi, emojis, 10k chars | PENDING |

---

### 3.5 Officer Operations, Queue & Kanban (`OFF-001` - `OFF-045`)

| ID | Area | Feature | Input / Setup | Expected Result | Status |
|---|---|---|---|---|---|
| OFF-001 | Officer | View Assigned Queue | Officer login | Queue lists complaints assigned to officer's dept | PENDING |
| OFF-002 | Officer | Department Isolation | Roads officer views queue | Does not see SWM or Electricity complaints | PENDING |
| OFF-003 | Officer | Status: SUBMITTED to ASSIGNED | Transition complaint | Status history logged, citizen notified | PENDING |
| OFF-004 | Officer | Status: ASSIGNED to IN_PROGRESS | Add mandatory remark | Status updated to `IN_PROGRESS` | PENDING |
| OFF-005 | Officer | Status: Missing Remark | Transition without remark | HTTP 400 Bad Request ("Remark required") | PENDING |
| OFF-006 | Officer | Upload Resolution Proof | Upload proof photo | Photo attached with `kind=PROOF` | PENDING |
| OFF-007 | Officer | Status: IN_PROGRESS to RESOLVED | Transition with proof photo | Status updated to `RESOLVED`, `resolvedAt` set | PENDING |
| OFF-008 | Officer | Invalid Transition Guard | Attempt `SUBMITTED` to `RESOLVED` | HTTP 400 Illegal status transition blocked | PENDING |
| OFF-009 | Officer | Add Internal Staff Note | Officer adds note with `internal=true` | Note visible to staff, HIDDEN from citizen | PENDING |
| OFF-010 | Officer | Reassign to Department | Reassign with reason | Routed to target department queue | PENDING |
| OFF-011 | Officer | Kanban Board Columns | Load `/officer/board.html` | Lanes: Assigned, In Progress, Resolved | PENDING |
| OFF-012 | Officer | Kanban Drag and Drop | Drag card from Assigned to In Progress | Modal opens for remark, updates status | PENDING |
| OFF-013 | Officer | Kanban Keyboard Move | Keyboard shortcut / menu move | Card transitions lane cleanly | PENDING |
| OFF-014 | Officer | SLA Badge Display | Overdue vs On-time ticket | SLA badge displays warning / breach color | PENDING |
| OFF-015 | Officer | Officer Merge Duplicates | Merge child ticket into parent | Child status `MERGED`, followers migrated | PENDING |
| OFF-016 | Officer | Officer Workload Stats | Load `/officer/stats` | Accurate counts of assigned & resolved | PENDING |
| OFF-017 - OFF-045 | Officer | Workflow Edge Cases | Rapid transitions, large proof files | Consistent history, zero data corruption | PENDING |

---

### 3.6 Admin Operations & Analytics (`ADM-001` - `ADM-055`)

| ID | Area | Feature | Input / Setup | Expected Result | Status |
|---|---|---|---|---|---|
| ADM-001 | Admin | Dashboard KPI Counts | Compare with DB queries | KPI numbers match direct SQL counts | PENDING |
| ADM-002 | Admin | Filter Dashboard by Dept | Select "Solid Waste Management" | Charts update with SWM data only | PENDING |
| ADM-003 | Admin | Chart Table Alternatives | Click "View as table" on chart | Accessible tabular data displayed | PENDING |
| ADM-004 | Admin | Export Analytics CSV | Click "Export CSV" | Valid CSV file downloaded | PENDING |
| ADM-005 | Admin | Export Analytics PDF | Click "Export PDF" | Valid formatted PDF report downloaded | PENDING |
| ADM-006 | Admin | Heatmap & Predictions | View prediction map | Hotspot clusters render without error | PENDING |
| ADM-007 | Admin | Create Department | Name: "Drainage", SLA: 24 | Department created in DB | PENDING |
| ADM-008 | Admin | Update Department | Edit contact email | Details updated cleanly | PENDING |
| ADM-009 | Admin | Create Category | Map category to department | Category active and selectable | PENDING |
| ADM-010 | Admin | Keyword Rule Manager | Add keyword "waterlogging" | Keyword saved and applied in classifier | PENDING |
| ADM-011 | Admin | Test Keyword Sandbox | Type test phrase in test box | Sandbox outputs predicted category & weight | PENDING |
| ADM-012 | Admin | Deactivate User | Toggle user active state to false | Deactivated user cannot login (401/403) | PENDING |
| ADM-013 | Admin | Create Officer Account | Admin creates officer with dept | Officer account created and can login | PENDING |
| ADM-014 | Admin | Configure SLA Policies | Update default SLA hours | New tickets use updated SLA | PENDING |
| ADM-015 | Admin | Publish Announcement | Create banner announcement | Announcement appears in top ticker | PENDING |
| ADM-016 | Admin | System Settings Update | Update duplicate radius to 500m | Settings updated and respected by service | PENDING |
| ADM-017 | Admin | Audit Log Verification | Perform admin action | Structured entry logged with IP & actor | PENDING |
| ADM-018 | Admin | Admin Unmerge Ticket | Unmerge previously merged ticket | Child ticket restored to `ASSIGNED` | PENDING |
| ADM-019 - ADM-055 | Admin | Admin Governance Matrix | Edge cases, pagination, search | Clean responses, zero leaks | PENDING |

---

### 3.7 Public Services, Anonymity & PII Protection (`PUB-001` - `PUB-035`)

| ID | Area | Feature | Input / Setup | Expected Result | Status |
|---|---|---|---|---|---|
| PUB-001 | Public | Track Grievance by Code | Enter valid `CP-2026-000001` | Displays status, dept, SLA, timeline | PASS |
| PUB-002 | Public | Track Non-Existent Code | Enter invalid `CP-9999-999999` | Friendly error ("Grievance not found") | PASS |
| PUB-003 | Public | QR Deep Link | Visit `/track.html?code=CP-2026-000001` | Pre-fills code, loads result automatically | PASS |
| PUB-004 | Public | PII Leak Protection | Track non-anonymous ticket as visitor | Email, phone, citizen ID are strictly hidden | PASS |
| PUB-005 | Public | Anonymous Ticket Tracking | Track anonymous ticket as visitor | Citizen name displayed as "Anonymous Citizen" | PASS |
| PUB-006 | Public | Merged Ticket Tracking | Track merged duplicate ticket | Shows merged notice with link to parent | PASS |
| PUB-007 | Public | Public Transparency Dashboard | Load `/transparency.html` | Aggregate metrics only, zero citizen records | PASS |
| PUB-008 | Public | Public Map View | Load public complaint map | Anonymized pins, no personal details | PASS |
| PUB-009 | Public | Accessibility Statement | Verify `/accessibility.html` | Conforms to GIGW 3.0 / WCAG guidelines | PASS |
| PUB-010 | Public | Screen Reader Guide | Verify `/screen-reader.html` | Screen reader shortcuts clearly explained | PASS |
| PUB-011 | Public | Sitemap Navigation | Verify `/sitemap.html` | All public and portal sections reachable | PASS |
| PUB-012 | Public | Public FAQ Accordion | Expand FAQs on `/help.html` | Accordions expand and collapse cleanly | PASS |
| PUB-013 | Public | Contact Helpline Info | Verify `/contact.html` | Toll-free 1800 number and directory present | PASS |
| PUB-014 - PUB-035 | Public | Public Security & Privacy | Boundary queries, invalid codes | Robust validation, zero data leaks | PASS |

---

### 3.8 CivicBot Virtual Assistant (`BOT-001` - `BOT-022`)

| ID | Area | Feature | Input / Setup | Expected Result | Status |
|---|---|---|---|---|---|
| BOT-001 | Bot | Widget Open / Close | Click chat trigger button | Chat drawer slides open, focus inside | PASS |
| BOT-002 | Bot | Escape Closes Widget | Press Escape key | Chat drawer closes, focus restored | PASS |
| BOT-003 | Bot | Intent: How to File | Type "How do I file a complaint?" | Provides filing guide and link | PASS |
| BOT-004 | Bot | Intent: Live Track by Code | Type "Track CP-2026-000001" | Fetches live status and displays in chat | PASS |
| BOT-005 | Bot | Intent: Working Hours | Type "Office hours" | Responds with official office timings | PASS |
| BOT-006 | Bot | Intent: Department Contacts | Type "Roads department phone" | Returns department contact details | PASS |
| BOT-007 | Bot | Intent: Unknown Input | Type "Random gibberish string" | Polite fallback with suggested options | PASS |
| BOT-008 | Bot | XSS Input in Chat | Type `<script>alert(1)</script>` | Escaped text rendered, no script execution | PASS |
| BOT-009 | Bot | Long Input Handling | Send 1,000-character message | Handled cleanly without layout blowout | PASS |
| BOT-010 | Bot | ARIA Live Announcements | Inspect message stream | Messages announced via `aria-live="polite"` | PASS |
| BOT-011 - BOT-022 | Bot | Quick Replies & Keyboard | Tab through chips, click actions | Quick replies populate input and trigger reply | PASS |

---

### 3.9 REST API Contract & Negative Tests (`API-001` - `API-070`)

| ID | Area | Endpoint | Method | Input / Condition | Expected Result | Status |
|---|---|---|---|---|---|---|
| API-001 | API | `/api/v1/auth/register` | POST | Valid body | 201 Created | PASS |
| API-002 | API | `/api/v1/auth/register` | POST | Missing email | 400 Validation error | PASS |
| API-003 | API | `/api/v1/auth/register` | POST | Missing consent | 400 Consent required | PASS |
| API-004 | API | `/api/v1/auth/login` | POST | Valid credentials | 200 OK with JWT | PASS |
| API-005 | API | `/api/v1/auth/login` | POST | Invalid credentials | 401 Unauthorized | PASS |
| API-006 | API | `/api/v1/auth/refresh` | POST | Valid refresh token | 200 OK | PASS |
| API-007 | API | `/api/v1/auth/refresh` | POST | Invalid token | 401 Unauthorized | PASS |
| API-008 | API | `/api/v1/complaints` | POST | Valid multipart | 201 Created | PASS |
| API-009 | API | `/api/v1/complaints` | POST | No auth token | 401 Unauthorized | PASS |
| API-010 | API | `/api/v1/complaints` | POST | Missing title | 400 Bad Request | PASS |
| API-011 | API | `/api/v1/complaints/mine` | GET | Valid citizen token | 200 OK with page envelope | PASS |
| API-012 | API | `/api/v1/complaints/{id}` | GET | Owner citizen | 200 OK | PASS |
| API-013 | API | `/api/v1/complaints/{id}` | GET | Non-owner citizen | 403 Forbidden | PASS |
| API-014 | API | `/api/v1/complaints/classify` | POST | "pothole road" | 200 OK category suggestion | PASS |
| API-015 | API | `/api/v1/complaints/similar` | GET | `lat=14.4&lng=75.9` | 200 OK candidate list | PASS |
| API-016 | API | `/api/v1/complaints/{id}/upvote` | POST | Valid citizen | 200 OK upvoted | PASS |
| API-017 | API | `/api/v1/complaints/{id}/comments` | POST | Valid body | 201 Created | PASS |
| API-018 | API | `/api/v1/complaints/{id}/feedback` | POST | Valid feedback | 201 Created | PASS |
| API-019 | API | `/api/v1/complaints/{id}/reopen` | POST | Valid reason | 200 OK | PASS |
| API-020 | API | `/api/v1/complaints/{id}/receipt.pdf`| GET | Valid owner | 200 OK application/pdf | PASS |
| API-021 | API | `/api/v1/public/track/{code}` | GET | Valid code | 200 OK | PASS |
| API-022 | API | `/api/v1/public/track/{code}` | GET | Unknown code | 404 Not Found | PASS |
| API-023 | API | `/api/v1/public/stats` | GET | Public | 200 OK | PASS |
| API-024 | API | `/api/v1/public/map` | GET | Public | 200 OK | PASS |
| API-025 | API | `/api/v1/public/announcements` | GET | Public | 200 OK | PASS |
| API-026 | API | `/api/v1/bot/message` | POST | Valid message | 200 OK | PASS |
| API-027 | API | `/api/v1/officer/complaints` | GET | Officer token | 200 OK | PASS |
| API-028 | API | `/api/v1/officer/board` | GET | Officer token | 200 OK grouped lanes | PASS |
| API-029 | API | `/api/v1/officer/complaints/{id}/status` | PATCH | Valid transition | 200 OK | PASS |
| API-030 | API | `/api/v1/officer/complaints/{id}/proof` | POST | Valid image | 201 Created | PASS |
| API-031 | API | `/api/v1/officer/stats` | GET | Officer token | 200 OK | PASS |
| API-032 | API | `/api/v1/admin/departments` | GET | Admin token | 200 OK | PASS |
| API-033 | API | `/api/v1/admin/categories` | GET | Admin token | 200 OK | PASS |
| API-034 | API | `/api/v1/admin/rules` | GET | Admin token | 200 OK | PASS |
| API-035 | API | `/api/v1/admin/rules/test` | POST | Test text | 200 OK | PASS |
| API-036 | API | `/api/v1/admin/users` | GET | Admin token | 200 OK | PASS |
| API-037 | API | `/api/v1/admin/audit-logs` | GET | Admin token | 200 OK | PASS |
| API-038 | API | `/api/v1/admin/settings` | GET | Admin token | 200 OK | PASS |
| API-039 | API | `/api/v1/analytics/summary` | GET | Admin token | 200 OK | PASS |
| API-040 | API | `/api/v1/analytics/export?format=csv` | GET | Admin token | 200 OK text/csv | PASS |
| API-041 | API | `/api/v1/analytics/export?format=pdf` | GET | Admin token | 200 OK application/pdf | PASS |
| API-042 | API | `/api/v1/notifications` | GET | Citizen token | 200 OK | PASS |
| API-043 | API | `/api/v1/notifications/{id}/read` | PATCH | Citizen token | 200 OK | PASS |
| API-044 | API | `/api/v1/notifications/read-all` | PATCH | Citizen token | 200 OK | PASS |
| API-045 | API | Unsupported Method on Auth | DELETE | `/api/v1/auth/login` | 405 Method Not Allowed | PASS |
| API-046 | API | Unsupported Media Type | POST | XML body to JSON endpoint | 415 Unsupported Media Type | PASS |
| API-047 - API-070 | API | Boundary & Envelope Tests | All | Malformed JSON, pagination | Standard error JSON, no stack traces | PASS |

---

### 3.10 Security, Injection & IDOR (`SEC-001` - `SEC-035`)

| ID | Area | Vulnerability Checked | Test Vector | Expected Result | Status |
|---|---|---|---|---|---|
| SEC-001 | Security | SQLi: Login Email | `' OR 1=1 --` | 401 Unauthorized, no SQL execution | PASS |
| SEC-002 | Security | SQLi: Complaint Search | `'; DROP TABLE complaints; --` | Parameterized query, no execution | PASS |
| SEC-003 | Security | Reflected XSS: Search Box | `<script>alert('xss')</script>` | Sanitized / escaped textContent | PASS |
| SEC-004 | Security | Stored XSS: Complaint Title | `<img src=x onerror=alert(1)>` | Escaped on all views, zero execution | PASS |
| SEC-005 | Security | Stored XSS: Comment Body | `<svg/onload=alert(1)>` | Escaped in comment thread | PASS |
| SEC-006 | Security | Upload: MIME Type Spoofing | `.exe` renamed to `.jpg` | Rejected via content-type inspection | PASS |
| SEC-007 | Security | Upload: Path Traversal | `../../../../etc/passwd` filename | Randomized UUID storage name, path safe | PASS |
| SEC-008 | Security | Direct Upload Static Access | Direct HTTP GET to `/uploads/` | Static directory browsing forbidden | PASS |
| SEC-009 | Security | IDOR: Citizen A views Citizen B | GET `/api/v1/complaints/{idOfB}` | HTTP 403 Forbidden | PASS |
| SEC-010 | Security | IDOR: Citizen edits Complaint | PUT status on complaint | HTTP 403 Forbidden | PASS |
| SEC-011 | Security | IDOR: Officer updates other dept | Officer A updates SWM complaint | HTTP 403 Forbidden | PASS |
| SEC-012 | Security | Rate Limiting: Complaint Create | 6 submissions within 1 hour | HTTP 429 Too Many Requests | PASS |
| SEC-013 | Security | Rate Limiting: Login Attempts | Rapid repeated login requests | Rate limited with 429 Retry-After | PASS |
| SEC-014 | Security | Security Headers: X-Frame-Options | Inspect HTTP response headers | `DENY` or `SAMEORIGIN` present | PASS |
| SEC-015 | Security | Security Headers: X-Content-Type | Inspect HTTP response headers | `nosniff` present | PASS |
| SEC-016 | Security | Actuator Lockdown | GET `/actuator/env` as non-admin | HTTP 401 / 403 Forbidden | PASS |
| SEC-017 | Security | Actuator Health Public | GET `/actuator/health` | HTTP 200 status UP | PASS |
| SEC-018 | Security | Tampered JWT Signature | Modified payload with fake key | HTTP 401 Unauthorized | PASS |
| SEC-019 | Security | Secret Exposure Check | Scan repo, JS bundles, logs | Zero API keys, passwords or JWT secrets | PASS |
| SEC-020 - SEC-035 | Security | Comprehensive Security Scan | Cross-site, CSRF, dependency audit | All clean, zero critical CVEs | PASS |

---

### 3.11 Accessibility & Assistive Tech (`A11Y-001` - `A11Y-035`)

| ID | Area | Guideline | Target / Element | Expected Result | Status |
|---|---|---|---|---|---|
| A11Y-001 | A11y | WCAG 2.1 AA | All 28 HTML Pages | Axe-Core audit: 0 Critical / Serious violations | PASS |
| A11Y-002 | A11y | WCAG 1.3.1 | Landmarks | `<header>`, `<nav>`, `<main>`, `<footer>` on all pages | PASS |
| A11Y-003 | A11y | WCAG 2.4.1 | Skip to Content Link | Visible on focus, skips navigation chrome | PASS |
| A11Y-004 | A11y | WCAG 2.4.7 | Focus Visibility | 3px high-contrast outline on all interactive items | PASS |
| A11Y-005 | A11y | WCAG 2.5.3 | Label in Name | Accessible names match visible text (`A-`, `A+`, `Theme`) | PASS |
| A11Y-006 | A11y | WCAG 1.4.3 | Contrast (Normal Text) | Minimum 4.5:1 contrast across all text | PASS |
| A11Y-007 | A11y | WCAG 1.4.6 | High Contrast Mode | 7:1 contrast across high contrast theme | PASS |
| A11Y-008 | A11y | WCAG 1.4.4 | 200% Zoom Resizing | Layout reflows without overlapping text or loss | PASS |
| A11Y-009 | A11y | WCAG 2.1.1 | Keyboard Navigation | Full citizen & officer workflows operable by Tab/Enter | PASS |
| A11Y-010 | A11y | WCAG 2.1.2 | Modal Focus Trapping | Duplicate dialog & drawer trap focus, close on Esc | PASS |
| A11Y-011 | A11y | WCAG 1.3.1 | Form Inputs & Labels | `<label for="...">` associated with all form inputs | PASS |
| A11Y-012 | A11y | WCAG 1.3.1 | Table Accessibility | All data tables have `<caption>` and `<th scope>` | PASS |
| A11Y-013 | A11y | WCAG 1.4.1 | Use of Color | Status chips use icon + text + color (never color alone)| PASS |
| A11Y-014 | A11y | WCAG 2.5.5 | Touch Target Size | Mobile tap targets are $\ge 44\text{px} \times 44\text{px}$ | PASS |
| A11Y-015 - A11Y-035 | A11y | Screen Reader & Forms | ARIA attributes & errors | `aria-live`, `aria-required`, `aria-invalid` active | PASS |

---

### 3.12 Responsive Viewports, Visual & Print (`RESP-001` - `RESP-035`)

| ID | Area | Viewport | Theme / Lang | Target Page | Expected Result | Status |
|---|---|---|---|---|---|---|
| RESP-001 | Responsive | 360x640 | Light / en | Landing (`index.html`) | No horizontal scroll (`scrollWidth <= 360`) | PASS |
| RESP-002 | Responsive | 360x640 | Light / kn | Landing (`index.html`) | Kannada text wraps cleanly, no overflow | PASS |
| RESP-003 | Responsive | 360x640 | Light / hi | Landing (`index.html`) | Hindi text wraps cleanly, no overflow | PASS |
| RESP-004 | Responsive | 360x640 | Light / en | Citizen Wizard | Multi-step form responsive on small screen | PASS |
| RESP-005 | Responsive | 360x640 | Light / en | Citizen Dashboard | Metrics cards stack, table scrolls internally | PASS |
| RESP-006 | Responsive | 360x640 | Light / en | Officer Kanban | Kanban board responsive with horizontal lane scroll | PASS |
| RESP-007 | Responsive | 360x640 | Light / en | Admin Dashboard | Charts stack vertically without clipping | PASS |
| RESP-008 | Responsive | 768x1024 | Light / en | Landing (`index.html`) | 2-column service grid, clean spacing | PASS |
| RESP-009 | Responsive | 768x1024 | Dark / en | Citizen Dashboard | Tablet layout with clean card grid | PASS |
| RESP-010 | Responsive | 1280x800 | Light / en | All Pages | Standard desktop layout | PASS |
| RESP-011 | Responsive | 1920x1080| Light / en | All Pages | Full HD desktop layout with centered max-width | PASS |
| RESP-012 | Responsive | Print Media| Print / en | `/track.html` | Navigation & footers hidden, receipt format | PASS |
| RESP-013 | Responsive | Print Media| Print / en | `/citizen/complaint.html` | Printable timeline & acknowledgment | PASS |
| RESP-014 - RESP-035 | Responsive | Cross-Viewport | All Combos | Inner Pages | 100% verified zero layout regressions | PASS |

---

### 3.13 Performance & Lighthouse (`PERF-001` - `PERF-018`)

| ID | Area | Target Page | Benchmark Metric | Target Threshold | Expected Result | Status |
|---|---|---|---|---|---|
| PERF-001 | Performance | Home (`/index.html`) | Lighthouse Accessibility | $\ge 95$ | Score $\ge 95$ | PASS |
| PERF-002 | Performance | Home (`/index.html`) | Lighthouse Performance | $\ge 85$ | Score $\ge 85$ | PASS |
| PERF-003 | Performance | Home (`/index.html`) | Lighthouse Best Practices| $\ge 90$ | Score $\ge 90$ | PASS |
| PERF-004 | Performance | Home (`/index.html`) | Lighthouse SEO | $\ge 90$ | Score $\ge 90$ | PASS |
| PERF-005 | Performance | Login (`/login.html`) | Lighthouse Accessibility | $\ge 95$ | Score $\ge 95$ | PASS |
| PERF-006 | Performance | Login (`/login.html`) | Lighthouse Performance | $\ge 85$ | Score $\ge 85$ | PASS |
| PERF-007 | Performance | File Complaint (`/citizen/new-complaint.html`)| Lighthouse Accessibility | $\ge 95$ | Score $\ge 95$ | PASS |
| PERF-008 | Performance | File Complaint (`/citizen/new-complaint.html`)| Lighthouse Performance | $\ge 85$ | Score $\ge 85$ | PASS |
| PERF-009 | Performance | Track (`/track.html`)| Lighthouse Accessibility | $\ge 95$ | Score $\ge 95$ | PASS |
| PERF-010 | Performance | Track (`/track.html`)| Lighthouse Performance | $\ge 85$ | Score $\ge 85$ | PASS |
| PERF-011 | Performance | Citizen Dashboard | Lighthouse Accessibility | $\ge 95$ | Score $\ge 95$ | PASS |
| PERF-012 | Performance | Admin Dashboard | Lighthouse Accessibility | $\ge 95$ | Score $\ge 95$ | PASS |
| PERF-013 | Performance | List Complaints API | p95 Latency | $< 500\text{ ms}$ | Responds under 500ms | PASS |
| PERF-014 | Performance | Analytics Summary API | p95 Latency | $< 500\text{ ms}$ | Responds under 500ms | PASS |
| PERF-015 | Performance | Database Query EXPLAIN | Slowest queries check | Proper index scan, no full table scan | PASS |
| PERF-016 - PERF-018| Performance | Asset Payloads | Bundle size check | No oversized bundles or blocking scripts | PASS |

---

### 3.14 Data Integrity, Concurrency & Resilience (`DATA-001` - `DATA-022`)

| ID | Area | Feature | Test Scenario | Expected Result | Status |
|---|---|---|---|---|---|
| DATA-001 | Data | Database Constraints | Check schema.sql vs entities | Foreign keys and NOT NULL constraints aligned | PASS |
| DATA-002 | Data | Idempotent Seeding | Restart application twice | Database state is preserved, no duplicate seeds | PASS |
| DATA-003 | Data | Concurrent Code Generation | 10 simultaneous complaint submissions | All receive unique non-conflicting `CP-...` codes | PASS |
| DATA-004 | Data | Double-Submit Complaint | Double click submit button | Exactly 1 complaint registered | PASS |
| DATA-005 | Data | Double-Submit Feedback | Double click feedback button | Exactly 1 feedback saved, second rejected | PASS |
| DATA-006 | Data | Double-Submit Upvote | Concurrent upvote requests | Exactly 1 upvote incremented | PASS |
| DATA-007 | Data | Double-Submit Merge | Concurrent merge calls on same ticket | History and follower counts consistent | PASS |
| DATA-008 | Data | Concurrent Status Edits | Two officers update same complaint | Optimistic locking or synchronized transition | PASS |
| DATA-009 | Data | SLA Double-Escalation | Trigger SLA scheduler multiple times | Escalation level advances strictly according to SLA | PASS |
| DATA-010 | Data | UTC Timestamp Integrity | Create complaint and verify DB record | Timestamp stored in UTC, converted locally in UI | PASS |
| DATA-011 | Data | Vision Provider Offline | `POST /complaints/classify-image` without API key | Returns HTTP 200 `available:false` gracefully | PASS |
| DATA-012 | Data | SMTP Server Offline | Send notification with mail server down | Mail logged to dev outbox, submission succeeds | PASS |
| DATA-013 - DATA-022 | Data | Resilience & DB Rollback | Simulated transaction rollback | Atomic commits, zero orphaned rows | PASS |

---

### 3.15 Regression Suite for Known Defects (`REG-001` - `REG-016`)

| ID | Area | Defect ID | Regression Description | Expected Result | Status |
|---|---|---|---|---|---|
| REG-001 | Regression | DEF-001 | Notification read endpoint method compliance | PATCH `/api/v1/notifications/{id}/read` succeeds, POST/PUT return 405 | PASS |
| REG-002 | Regression | DEF-001 | Unsupported HTTP method error mapping | Requesting non-supported verb returns HTTP 405 (not 500) | PASS |
| REG-003 | Regression | DEF-002 | Production `ddl-auto` setting | `application-prod.yml` uses validate, not update | PASS |
| REG-004 | Regression | DEF-002 | Authoritative `schema.sql` availability | `src/main/resources/db/schema.sql` valid MySQL 8 DDL | PASS |
| REG-005 | Regression | DEF-003 | Public tracking PII leak prevention | Unauthenticated caller on `/public/track/{code}` receives no email/phone/id | PASS |
| REG-006 | Regression | DEF-004 | Static resource 404 handler | Missing asset request (e.g. `/missing.png`) returns HTTP 404 (not 500) | PASS |
| REG-007 | Regression | DEF-004 | Favicon availability | `GET /favicon.ico` returns HTTP 200 with valid icon | PASS |
| REG-008 | Regression | DEF-005 | Officer stats lazy initialization | `GET /api/v1/officer/stats` returns 200 without LazyInitializationException | PASS |
| REG-009 | Regression | DEF-005 | Officer Kanban board endpoint | `GET /api/v1/officer/board` returns 200 with grouped columns | PASS |
| REG-010 | Regression | DEF-005 | Admin keyword rules canonical route | `GET /api/v1/admin/rules` returns 200 | PASS |
| REG-011 | Regression | DEF-005 | Analytics predictions canonical route | `GET /api/v1/analytics/predictions` returns 200 | PASS |
| REG-012 | Regression | DEF-005 | Citizen complaints canonical route | `GET /api/v1/complaints/mine` returns 200 | PASS |
| REG-013 | Regression | DEF-006 | JPA entity and DDL schema drift | All entity columns match `schema.sql` DDL definitions | PASS |
| REG-014 | Regression | DEF-007 | Citizen name masking on public tracking | Non-owner sees citizen name only if non-anonymous; email/phone masked | PASS |
| REG-015 | Regression | DEF-008 | Strict canonical route enforcement | Non-standard aliases return 404, canonical paths work | PASS |
| REG-016 | Regression | DEF-009 | Generic IllegalArgumentException mapping | Unhandled internal exception returns standard 500 without stack trace | PASS |

---
