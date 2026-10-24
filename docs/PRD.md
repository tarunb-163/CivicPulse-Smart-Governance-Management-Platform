# CivicPulse — Product Requirements Document (PRD)

**Product:** CivicPulse — Smart Governance Platform for Administrative Operations with Citizen Service Assistance
**Program:** Infosys Virtual Internship 7.0 (Java Full-Stack)
**Version:** 1.0  |  **Status:** Draft for implementation

---

## 1. Overview

CivicPulse is a centralized web platform where citizens file civic complaints (potholes, garbage, water, streetlights, etc.), the system auto-categorizes and routes each complaint to the right government department, officers resolve it through a tracked workflow, and administrators monitor everything through a live analytics dashboard. Citizens can follow progress in real time and rate the outcome.

### 1.1 Problem
- Manual complaint handling, slow resolution, no visibility for the citizen.
- Fragmented services; tracking, classification and analytics rarely exist in one platform.
- Weak analytics, minimal citizen engagement after submission, no predictive insight, no department comparison.

### 1.2 Vision
One transparent, fast, citizen-friendly platform that turns grievances into accountable, measurable action.

### 1.3 Goals (from the project deck)
1. Centralized web platform for registering, tracking and managing grievances.
2. Automated categorization, department-wise routing, role-based workflows.
3. Analytics dashboard: real-time statistics, resolution status, department performance.
4. Transparency, accountability and engagement: live tracking, notifications, feedback.

### 1.4 Success metrics
| Metric | Target |
|---|---|
| Complaint filing time (logged-in citizen) | under 90 seconds |
| Auto-routing accuracy (keyword rules, demo dataset) | 85%+ |
| Page interaction latency | under 100 ms feel, animations at 60 fps |
| API p95 response (local) | under 300 ms |
| SLA breach visibility | 100% of breaches flagged on dashboard |
| Lighthouse (performance / accessibility) | 85+ / 90+ |

---

## 2. Users and Roles

| Role | Description | Key abilities |
|---|---|---|
| **Citizen** | Registered public user | File, track, upvote, comment, give feedback, reopen |
| **Guest** | Not logged in | Track by complaint ID, view public transparency dashboard, use CivicBot |
| **Officer** | Staff in one department | View assigned queue, update status, add remarks and proof, request reassignment |
| **Department Head** | Senior officer (optional role) | Assign/reassign inside department, see team workload |
| **Admin** | System administrator | Manage users, departments, categories, SLA, keyword rules; full analytics; audit logs |

---

## 3. Scope

### 3.1 In scope (MVP + differentiators)
All modules in section 4 marked **MVP** and **WOW**.

### 3.2 Future (matches deck "Future Enhancement")
Real ML classification, native mobile apps, full multilingual content, government portal integration, real predictive models. Rule-based and statistical versions of some of these are included now as stepping stones.

---

## 4. Functional Requirements

Priority: **MVP** = must ship, **WOW** = unique/differentiating, **LATER** = stretch.

### 4.1 Authentication and Authorization (AUTH)
| ID | Requirement | Priority |
|---|---|---|
| AUTH-1 | Citizen registration with name, email, phone, password (BCrypt) | MVP |
| AUTH-2 | Login returns JWT access token (plus refresh token) | MVP |
| AUTH-3 | Role-based access control: CITIZEN, OFFICER, DEPT_HEAD, ADMIN | MVP |
| AUTH-4 | Logout, token expiry handling, auto-redirect on 401 | MVP |
| AUTH-5 | Forgot / reset password via emailed token | MVP |
| AUTH-6 | Email verification on signup | WOW |
| AUTH-7 | Profile page: edit details, change password, notification preferences | MVP |
| AUTH-8 | Account lockout after repeated failed logins (rate limiting) | WOW |
| AUTH-9 | Admin creates officer accounts and assigns department | MVP |

### 4.2 Complaint Submission (CMP)
| ID | Requirement | Priority |
|---|---|---|
| CMP-1 | Multi-step wizard: Details → Location → Evidence → Review | MVP |
| CMP-2 | Fields: title, description, category (optional, auto-suggest), urgency, address, date | MVP |
| CMP-3 | Upload up to 5 images/documents (jpg, png, pdf; 5 MB each), drag and drop with preview | MVP |
| CMP-4 | Map pin selection (Leaflet + OpenStreetMap), "use my location", reverse address fill | WOW |
| CMP-5 | Live category suggestion while typing (calls classify endpoint) | WOW |
| CMP-6 | Duplicate detection: show similar nearby open complaints before submit, offer "Support this instead" | WOW |
| CMP-7 | Unique complaint ID (format `CP-2026-000123`) and downloadable PDF receipt | MVP |
| CMP-8 | Anonymous / hide-my-identity option (officers see content, not identity) | WOW |
| CMP-9 | Draft autosave in localStorage | WOW |
| CMP-10 | Voice-to-text for description (Web Speech API) | LATER |

### 4.3 Auto-Categorization and Routing (RTE)
| ID | Requirement | Priority |
|---|---|---|
| RTE-1 | Keyword-rule classifier maps text to category, category maps to department | MVP |
| RTE-2 | Fallback to "General" queue when confidence is low; admin manually routes | MVP |
| RTE-3 | Priority score computed from urgency, category severity, upvotes, age | WOW |
| RTE-4 | Auto-assignment to least-loaded officer in department (round robin by workload) | WOW |
| RTE-5 | Admin-editable keyword rules and category-department mapping | MVP |
| RTE-6 | Pluggable `ClassifierService` interface so ML/AI can replace rules later | MVP |

### 4.4 Complaint Processing / Officer Workflow (WRK)
**Status lifecycle:** `SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED` plus `REJECTED`, `REOPENED`, `ESCALATED`, `MERGED` (duplicate merged into another complaint).

| ID | Requirement | Priority |
|---|---|---|
| WRK-1 | Officer queue with filters (status, priority, category, date, SLA state) and search | MVP |
| WRK-2 | Status updates with mandatory remark; each change stored in history | MVP |
| WRK-3 | Upload resolution proof photos | MVP |
| WRK-4 | Kanban board view with drag-and-drop between status columns | WOW |
| WRK-5 | Internal notes visible only to staff | MVP |
| WRK-6 | Reassign to another officer/department with reason | MVP |
| WRK-7 | SLA timer per complaint (e.g., 72 h) with countdown and colour states | WOW |
| WRK-8 | Auto-escalation: scheduled job escalates overdue complaints to department head, then admin | WOW |
| WRK-9 | Rejection requires reason; citizen can appeal (reopen) | MVP |
| WRK-10 | Officer workload and performance summary | WOW |

### 4.5 Citizen Tracking and Engagement (TRK)
| ID | Requirement | Priority |
|---|---|---|
| TRK-1 | "My Complaints" list with status chips, search, filter | MVP |
| TRK-2 | Complaint detail with animated vertical timeline of status history | MVP |
| TRK-3 | Public tracking by complaint ID (+ phone/email last digits) without login | MVP |
| TRK-4 | Comments thread between citizen and assigned officer | WOW |
| TRK-5 | Upvote / "Me too" on public complaints, one per user | WOW |
| TRK-6 | Reopen resolved complaint within 7 days with reason | MVP |
| TRK-7 | Citizen feedback: 1–5 star rating, comment, resolved-satisfactorily yes/no | MVP |
| TRK-8 | Citizen impact stats ("You raised 6 issues, 4 resolved") and badges (Active Citizen, etc.) | WOW |
| TRK-9 | Share complaint link (public view hides personal data) | WOW |

### 4.6 Notifications (NTF)
| ID | Requirement | Priority |
|---|---|---|
| NTF-1 | In-app notification center (bell icon, unread count, mark read) | MVP |
| NTF-2 | Near-real-time updates via polling every 15 s, upgradeable to SSE/WebSocket | MVP |
| NTF-3 | Email notification on submit, assignment, status change, resolution (Spring Mail) | MVP |
| NTF-4 | SMS hook (interface + mock provider) | LATER |
| NTF-5 | Per-user preference toggles | WOW |
| NTF-6 | Toast popups for live events on current page | MVP |

### 4.7 Analytics and Dashboards (ANA)
| ID | Requirement | Priority |
|---|---|---|
| ANA-1 | KPI cards with animated counters: total, pending, in-progress, resolved, SLA-breached, avg resolution time | MVP |
| ANA-2 | Charts (Chart.js): complaints over time, by category (doughnut), by status, by department | MVP |
| ANA-3 | Department performance leaderboard: resolution rate, avg time, avg rating | MVP |
| ANA-4 | Heatmap / cluster map of complaint density by area (Leaflet heat layer) | WOW |
| ANA-5 | Date-range and department filters on all charts | MVP |
| ANA-6 | Hotspot prediction: rolling average + weekday seasonality flags "likely spike" areas/categories | WOW |
| ANA-7 | Export reports to CSV and PDF | MVP |
| ANA-8 | Public Transparency Dashboard (anonymous aggregate data, no personal info) | WOW |
| ANA-9 | Sentiment from feedback comments (simple keyword score) | LATER |

### 4.8 Administration (ADM)
| ID | Requirement | Priority |
|---|---|---|
| ADM-1 | CRUD departments (name, head, contact, SLA default) | MVP |
| ADM-2 | CRUD categories and sub-categories, severity weight | MVP |
| ADM-3 | Keyword rules manager with test box ("type text → see predicted category") | WOW |
| ADM-4 | User management: list, search, activate/deactivate, change role | MVP |
| ADM-5 | SLA policy configuration per category | WOW |
| ADM-6 | Audit log of sensitive actions (who, what, when, IP) | MVP |
| ADM-7 | Announcements banner shown to citizens | WOW |
| ADM-8 | System settings (upload limits, SLA defaults, email toggle) | MVP |

### 4.9 CivicBot — Citizen Service Assistance (BOT)
| ID | Requirement | Priority |
|---|---|---|
| BOT-1 | Floating chat widget with animated open/close | WOW |
| BOT-2 | Rule-based intents: how to file, track complaint by ID, department info, FAQs, working hours | WOW |
| BOT-3 | "Track CP-2026-000123" returns live status inside chat | WOW |
| BOT-4 | Quick-reply chips, typing indicator animation | WOW |
| BOT-5 | Optional LLM backend behind the same endpoint | LATER |

### 4.10 Cross-cutting UX features
| ID | Requirement | Priority |
|---|---|---|
| UX-1 | Light/dark theme with smooth switch, remembered | WOW |
| UX-2 | English + Kannada + Hindi UI strings via JSON i18n files | WOW |
| UX-3 | Command palette (Ctrl+K) to jump to pages or search complaints | WOW |
| UX-4 | Skeleton loaders, empty-state illustrations, friendly errors | MVP |
| UX-5 | Fully responsive (mobile first), installable PWA manifest | WOW |
| UX-6 | Accessibility: keyboard nav, focus rings, ARIA, reduced-motion support | MVP |

### 4.11 DevOps and Deployment (OPS)
| ID | Requirement | Priority |
|---|---|---|
| OPS-1 | Multi-stage `Dockerfile` (Maven build stage, slim JRE 17 runtime), non-root user, container `HEALTHCHECK` | MVP |
| OPS-2 | `docker-compose.yml` with app + MySQL 8, named volumes (database, uploads), env file, healthcheck-based start order; `docker compose up` brings up a fully seeded, working app | MVP |
| OPS-3 | GitHub Actions CI on every push and PR: build, unit and integration tests, JaCoCo coverage report (uploaded as artifact), start the compose stack and run the smoke test; status badge in README | MVP |
| OPS-4 | Live deployment on a public HTTPS URL (Render, Railway, Fly.io or a VPS) with hosted MySQL, `prod` profile, secrets from environment, persistent storage for uploads; the smoke test passes against the live URL | MVP |
| OPS-5 | Actuator health/readiness endpoint (`/actuator/health`), all other actuator endpoints restricted to admin; README shows live URL, CI badge and demo credentials | MVP |

### 4.12 Privacy and Terms Pages (LEG)
| ID | Requirement | Priority |
|---|---|---|
| LEG-1 | Public **Privacy Policy** page (`privacy.html`) describing what is actually collected (account details, complaint text, photos, location, notifications, audit logs), why, who can see it (including anonymous-mode rules), third-party processing (map tiles, image-classification provider), retention, user contact | MVP |
| LEG-2 | Public **Terms of Service** page (`terms.html`): acceptable use (no false or abusive complaints), content licence, moderation and account suspension, service-availability disclaimer, contact | MVP |
| LEG-3 | Registration requires ticking "I accept the Terms and Privacy Policy"; server rejects registration without consent and stores consent version and timestamp | MVP |
| LEG-4 | Footer links to both pages on every public and app page, plus links beside the registration and complaint-submit forms | MVP |

> The legal text is a project draft written from the system's real data practices. Have it reviewed before any real-world use.

### 4.13 QR Codes (QR)
| ID | Requirement | Priority |
|---|---|---|
| QR-1 | The PDF receipt contains a QR code that opens the public tracking page for that complaint | MVP |
| QR-2 | `GET /public/qr/{publicCode}.png` returns a QR PNG encoding `{publicBaseUrl}/track.html?code={publicCode}`; the QR is shown on the submit-success card, complaint detail and track result, with Download PNG and Print buttons | MVP |
| QR-3 | Deep link: `track.html?code=CP-…` prefills the code and loads the public result automatically (if the page asks for verification digits per TRK-3, it asks only for the digits) | MVP |

### 4.14 Photo-Based Category Suggestion (IMG)
| ID | Requirement | Priority |
|---|---|---|
| IMG-1 | `POST /complaints/classify-image` accepts one image (jpg/png/webp, max 5 MB) and returns suggested category, department, confidence and up to 2 alternatives | MVP |
| IMG-2 | `ImageClassifierService` interface with a provider implementation that calls a vision-capable AI API (provider and API key from environment, with timeout and retry); the answer is constrained to the system's own category list | MVP |
| IMG-3 | The wizard shows a suggestion chip after the first photo is added; the user accepts or changes it; combined with the text suggestion (agreement raises confidence, conflict shows both options); never applied without user confirmation | MVP |
| IMG-4 | Graceful fallback: missing key, timeout, provider error or low confidence → no suggestion, wizard continues, submission is never blocked; the photo is sent to the provider only for classification, and this is disclosed in the Privacy Policy | MVP |

### 4.15 Complaint Duplication Prevention (DUP)
Extends CMP-6, which only warns. Duplicates are prevented on the **server**, not just hinted in the UI.

| ID | Requirement | Priority |
|---|---|---|
| DUP-1 | Exact-duplicate guard: the same citizen submitting the same normalized title+description hash within 24 h is rejected with 409 `DUPLICATE_EXACT` and the existing complaint code | MVP |
| DUP-2 | Similar-complaint guard: if an open complaint within the radius (default 300 m) has text similarity at or above the threshold, creation returns 409 `DUPLICATE_SUSPECTED` with a candidate list unless the request carries `confirmNotDuplicate=true` | MVP |
| DUP-3 | "Support this instead": the citizen follows and upvotes the existing complaint (idempotent) and receives its updates; no new complaint is created | MVP |
| DUP-4 | Duplicate image detection: SHA-256 (exact) and perceptual hash (near match, covers resized or re-compressed copies) of uploads compared with open complaints; a match is flagged to the citizen at submit and to officers on the complaint | WOW |
| DUP-5 | Officer merge: an officer merges a duplicate into a parent complaint (status `MERGED`, `duplicate_of_id` set, remark required); followers and upvotes move to the parent without double counting; both citizens are notified; admin can unmerge | MVP |
| DUP-6 | Per-user rate limit on complaint creation (default 5 per hour, 15 per day) → 429 with `Retry-After` | MVP |
| DUP-7 | Thresholds (radius, similarity, hash window, rate limits) configurable by admin in system settings | WOW |

---

## 5. Non-Functional Requirements

- **Security:** BCrypt, JWT, RBAC on every endpoint, input validation, parameterized queries (JPA), upload type and size checks with randomized storage names, CORS whitelist, security headers, rate limit on login and complaint creation.
- **Performance:** pagination on all lists, DB indexes on status, department, created_at, location; lazy loading of images; charts fed by aggregate queries.
- **Reliability:** global exception handler, consistent error JSON, transactional status changes.
- **Maintainability:** layered architecture, DTOs, service interfaces, 70%+ coverage on service layer.
- **Privacy:** anonymous mode, public views strip personal data, audit trail, recorded consent to Terms and Privacy Policy, disclosure of third-party image processing.
- **Operability:** reproducible Docker build, CI on every push, health endpoint, environment-based configuration, persistent storage for uploads in every environment.
- **Compatibility:** latest Chrome, Edge, Firefox, Safari; mobile Chrome/Safari.

---

## 6. System Architecture

```
Browser (HTML5, CSS3, JS, Bootstrap 5, Chart.js, Leaflet)
        │  HTTPS / JSON (JWT in Authorization header)
        ▼
Spring Boot REST API
  ├─ Security layer (Spring Security + JWT filter, RBAC)
  ├─ Controllers → Services → Repositories (Spring Data JPA / Hibernate)
  ├─ ClassifierService, PriorityService, AssignmentService, SlaScheduler
  ├─ NotificationService (in-app + email)
  └─ AnalyticsService (aggregate queries)
        ▼
MySQL 8   +   local file storage (/uploads)
```

---

## 7. Data Model (MySQL)

| Table | Key columns |
|---|---|
| `users` | id, name, email (unique), phone, password_hash, role, department_id (nullable), enabled, email_verified, locale, consent_version, consent_at, created_at |
| `departments` | id, name, code, head_user_id, contact_email, default_sla_hours, active |
| `categories` | id, name, department_id, severity_weight, default_sla_hours, icon |
| `keyword_rules` | id, keyword, category_id, weight |
| `complaints` | id, public_code, title, description, category_id, department_id, citizen_id, assigned_officer_id, status, priority_score, urgency, anonymous, address, latitude, longitude, upvote_count, content_hash, duplicate_of_id (nullable), image_suggestion, sla_due_at, resolved_at, created_at, updated_at |
| `complaint_attachments` | id, complaint_id, file_name, stored_name, content_type, size, sha256, phash, kind (EVIDENCE / PROOF) |
| `complaint_status_history` | id, complaint_id, from_status, to_status, remark, changed_by, created_at |
| `complaint_comments` | id, complaint_id, author_id, body, internal (bool), created_at |
| `complaint_upvotes` | id, complaint_id, user_id (unique pair) |
| `complaint_followers` | id, complaint_id, user_id, created_at (unique pair); citizens who support or follow a complaint |
| `feedback` | id, complaint_id (unique), rating, comment, satisfied, created_at |
| `notifications` | id, user_id, type, title, message, link, read, created_at |
| `audit_logs` | id, actor_id, action, entity, entity_id, details, ip, created_at |
| `announcements` | id, title, body, active, starts_at, ends_at |
| `password_reset_tokens` | id, user_id, token, expires_at, used |
| `system_settings` | key, value |

**Indexes:** complaints(status), complaints(department_id, status), complaints(created_at), complaints(latitude, longitude), notifications(user_id, read).

---

## 8. API Specification (REST, prefix `/api/v1`)

### Auth
`POST /auth/register` · `POST /auth/login` · `POST /auth/refresh` · `POST /auth/forgot-password` · `POST /auth/reset-password` · `GET /auth/verify-email?token=`

### Users / Profile
`GET /users/me` · `PUT /users/me` · `PUT /users/me/password` · `PUT /users/me/preferences`
Admin: `GET /admin/users` · `POST /admin/users` · `PUT /admin/users/{id}` · `PATCH /admin/users/{id}/status`

### Complaints (citizen)
`POST /complaints` (multipart) · `GET /complaints/mine` · `GET /complaints/{id}` · `POST /complaints/classify` · `GET /complaints/similar?lat=&lng=&text=` · `POST /complaints/{id}/upvote` · `POST /complaints/{id}/comments` · `POST /complaints/{id}/reopen` · `POST /complaints/{id}/feedback` · `GET /complaints/{id}/receipt.pdf`

### Public
`GET /public/track/{publicCode}` · `GET /public/stats` · `GET /public/map` · `GET /public/announcements` · `POST /bot/message`

### Officer
`GET /officer/complaints` (filters, pagination) · `GET /officer/board` · `PATCH /officer/complaints/{id}/status` · `POST /officer/complaints/{id}/proof` · `POST /officer/complaints/{id}/notes` · `POST /officer/complaints/{id}/reassign` · `GET /officer/stats`

### Admin
`GET/POST/PUT/DELETE /admin/departments` · `/admin/categories` · `/admin/keyword-rules` · `POST /admin/keyword-rules/test` · `GET/PUT /admin/sla-policies` · `GET /admin/audit-logs` · `GET/POST /admin/announcements` · `GET/PUT /admin/settings`

### Analytics
`GET /analytics/summary` · `/analytics/trend?range=` · `/analytics/by-category` · `/analytics/by-department` · `/analytics/leaderboard` · `/analytics/heatmap` · `/analytics/predictions` · `/analytics/export?format=csv|pdf`

### Notifications
`GET /notifications` · `GET /notifications/unread-count` · `PATCH /notifications/{id}/read` · `PATCH /notifications/read-all`

### Additional endpoints (OPS, LEG, QR, IMG, DUP)
- **Registration:** `POST /auth/register` now requires `acceptedTerms=true`; stores consent version and timestamp (400 without it).
- **Complaints:** `POST /complaints/classify-image` (multipart, one image) · `POST /complaints/{id}/support` (follow + upvote, idempotent). `POST /complaints` may return `409 DUPLICATE_EXACT` (with `existingCode`), `409 DUPLICATE_SUSPECTED` (with `candidates`; resend with `confirmNotDuplicate=true` to proceed) or `429` (rate limit, `Retry-After` header). Upload responses include `imageDuplicateOf` when an image match is found.
- **Officer / Admin:** `POST /officer/complaints/{id}/merge` (body: `parentId`, `remark`) · `POST /admin/complaints/{id}/unmerge`
- **Public:** `GET /public/qr/{publicCode}.png`
- **Ops:** `GET /actuator/health` (public, minimal output); other actuator endpoints admin only
- **Static pages:** `/privacy.html`, `/terms.html`

**Error format:** `{ "timestamp", "status", "error", "message", "path", "fieldErrors": [...] }`

---

## 9. Key Business Logic (Functions to Implement)

| Function | Behaviour |
|---|---|
| `generatePublicCode()` | `CP-{year}-{6-digit sequence}` |
| `classify(text)` | tokenize, sum keyword weights per category, return best category + confidence |
| `routeToDepartment(category)` | category.department; fallback General |
| `computePriority(complaint)` | `urgency*3 + severityWeight*2 + min(upvotes,20)*0.5 + ageDays*0.7` |
| `assignOfficer(department)` | active officer with fewest open complaints |
| `findSimilar(lat,lng,text)` | open complaints within 300 m with text similarity above threshold |
| `changeStatus(id, newStatus, remark, actor)` | validates allowed transition, writes history, notifies citizen, audit-logs |
| `slaScheduler()` | every 15 min: flag complaints past `sla_due_at`, escalate level by level |
| `submitFeedback()` | allowed once, only after RESOLVED/CLOSED; updates department rating |
| `autoClose()` | resolved complaints with no reopen after 7 days become CLOSED |
| `predictHotspots()` | per category/area: 4-week moving average vs last week; flag if above 1.5× |
| `buildLeaderboard()` | resolution rate, avg hours, avg rating combined score |
| `botReply(message)` | intent match, track-by-ID lookup, canned FAQ |
| `normalizeContent(title, description)` | lowercase, strip punctuation and extra spaces, SHA-256 hash for the exact-duplicate check |
| `guardDuplicates(request, citizen)` | rate-limit check (429) → exact hash within 24 h (409) → similar nearby complaint above threshold without confirmation (409 with candidates) |
| `supportComplaint(id, citizen)` | add follower and upvote idempotently, notify on later updates |
| `hashImage(file)` | SHA-256 plus perceptual hash; compare with open complaints; return match info |
| `mergeComplaint(childId, parentId, remark, officer)` | child → `MERGED`, set `duplicate_of_id`, move followers and upvotes without double counting, notify both citizens, audit-log |
| `generateQr(publicCode)` | PNG via ZXing for `{publicBaseUrl}/track.html?code={publicCode}`; also embedded in the PDF receipt |
| `classifyImage(file)` | validate file, call vision provider with the allowed category list, map to a known category, apply confidence threshold, return `available:false` when the provider is missing or fails |

### Allowed status transitions
```
SUBMITTED → ASSIGNED | REJECTED | MERGED
ASSIGNED → IN_PROGRESS | REJECTED | ESCALATED | MERGED
IN_PROGRESS → RESOLVED | ESCALATED | MERGED
ESCALATED → IN_PROGRESS | RESOLVED | MERGED
RESOLVED → CLOSED | REOPENED
REJECTED → REOPENED
REOPENED → ASSIGNED
MERGED → ASSIGNED (admin unmerge only; MERGED is otherwise terminal and reachable only through the merge action)
```

---

## 10. Screens (summary; detail in design.md)

Public: Landing, Login, Register, Forgot/Reset, Track Complaint, Transparency Dashboard, Privacy Policy, Terms of Service
Citizen: Dashboard, File Complaint wizard, My Complaints, Complaint Detail, Notifications, Profile
Officer: Dashboard, Queue, Kanban Board, Complaint Workspace, Performance
Admin: Analytics Dashboard, Departments, Categories, Keyword Rules, Users, SLA, Announcements, Audit Logs, Settings

---

## 11. Acceptance Criteria (samples)

- **File complaint:** a logged-in citizen can submit with 3 images and a map pin; receives a public code; sees it in "My Complaints" with status SUBMITTED then ASSIGNED within seconds; email sent.
- **Routing:** text "garbage not collected for 4 days" is categorized as Sanitation and routed to the Sanitation department.
- **Workflow:** an officer cannot jump from SUBMITTED to RESOLVED; invalid transitions return 400.
- **Security:** a citizen calling an officer or admin endpoint receives 403; unauthenticated calls receive 401.
- **SLA:** a complaint past its due time shows a red overdue badge and appears in the escalated list after the scheduler runs.
- **Feedback:** rating can be submitted once and updates the department's average.
- **Dashboard:** KPIs match database counts; filters update charts without page reload.
- **UX:** all cards animate on hover, transitions respect `prefers-reduced-motion`, layout works at 360 px width.
- **Duplicates:** submitting the same complaint twice within 24 h returns 409 with the first complaint's code; a similar complaint within 300 m shows "Support this instead" and is only created after explicit confirmation; an officer merge moves followers to the parent and notifies both citizens; the 6th complaint within an hour returns 429.
- **QR:** the PDF receipt and the tracking page show a QR that, when scanned, opens `track.html?code=…` and shows live status.
- **Image suggestion:** with a provider key configured, a photo of a pothole suggests the Roads category and the user can accept or change it; with no key or on provider error, the wizard works normally and shows no suggestion.
- **Legal:** registration is blocked until the terms are accepted; Privacy and Terms pages open from the footer of every page.
- **DevOps:** `docker compose up` on a clean machine gives a working seeded app; CI is green on `main`; the smoke test passes against the live URL.

---

## 12. Milestones

| Phase | Deliverable |
|---|---|
| 1 | Project setup, DB schema, auth and roles |
| 2 | Complaint submission, uploads, classification, routing |
| 3 | Officer workflow, history, SLA |
| 4 | Notifications, tracking, feedback |
| 5 | Analytics, admin panel, exports |
| 6 | UI polish, animations, CivicBot, dark mode, i18n |
| 7 | Testing, documentation, demo, final verification |
| 8 | Docker, CI, live deployment, Privacy/Terms pages, QR codes, photo category suggestion, duplicate prevention (starts after phase 2, finishes before phase 7 sign-off) |

(Detailed checklist in `task.md`.)

---

## 13. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Animation heavy UI feels slow | animate only transform/opacity, lazy init, reduced-motion fallback |
| Keyword classifier inaccurate | admin-editable rules, manual override, interface ready for ML |
| Team merge conflicts | module ownership, feature branches, small PRs |
| File upload abuse | type/size validation, random names, storage outside static path |
| Scope creep | MVP first, WOW features in priority order |
| Vision API cost, quota or outage | Interface plus graceful fallback (IMG-4); suggestion is optional; cache results per image hash |
| Free hosting limits (sleep, ephemeral disk) | Persistent volume or object storage for uploads; document cold-start; keep the smoke test tolerant of first-request delay |
| Privacy text not matching real behaviour | Write it from the implemented data flows; re-check whenever data collection changes |
| False-positive duplicate blocking | Similarity threshold configurable (DUP-7); user can always confirm "not a duplicate" |

---

## 14. Open Questions
- Email provider for demo (Gmail SMTP app password vs Mailtrap)?
- Map tiles: OpenStreetMap (free) assumed.
- Real SMS provider needed, or mock only?
- Vision provider for photo suggestions (Gemini free tier, Anthropic, or other) and who supplies the API key?
- Hosting target for the live deployment (Render, Railway, Fly.io, VPS) and who owns the account?
