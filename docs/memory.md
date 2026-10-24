# CivicPulse — Project Memory

> Context file for any developer or AI coding assistant working on this repo. Read this first. Update the **Current State** and **Decisions Log** sections whenever something meaningful changes.

---

## 1. Project in One Paragraph
CivicPulse is a Java full-stack smart governance platform built for the Infosys Virtual Internship 7.0. Citizens file civic complaints with photos and a map pin; the system auto-categorizes and routes them to a department; officers resolve them through a status workflow with SLA tracking; admins see analytics and manage the system; citizens track progress, get notifications and give feedback. The frontend is a modern, highly interactive "Aurora Glass" UI.

Related docs: `PRD.md` (what to build), `design.md` (how it looks and moves), `rules.md` (how to code), `task.md` (what to do next), `FEATURE-VERIFICATION.md` (proof that everything works), `QA-CHECKLIST.md` (QA test suite & verification steps), `prompt.md` (one-shot build prompt).

---

## 2. Team
- Project associates: Sriya, Tarun B, Syed Zain, Yogendra Kumar.
- Mentor: Ragul S.
- Module ownership:
  - Auth, security, users, admin management
  - Complaints, uploads, classification, routing
  - Officer workflow, SLA scheduler, notifications
  - Analytics, charts, maps, exports
  - Frontend design system, animations, CivicBot, i18n

---

## 3. Tech Stack (fixed)
| Layer | Choice |
|---|---|
| Frontend | HTML5, CSS3, JavaScript (ES modules), Bootstrap 5 |
| Charts / Maps | Chart.js, Leaflet (+ heat, cluster) |
| Backend | Java 17+, Spring Boot 3.3.4, REST APIs |
| Security | Spring Security + JWT, BCrypt |
| Persistence | Spring Data JPA (Hibernate), MySQL 8 |
| Build | Maven |
| Testing | JUnit 5, Mockito, Spring Boot Test, Postman / Smoke test suites |
| VCS / IDE | Git + GitHub |
| Email | Spring Boot Starter Mail + Dev Outbox |
| QR codes | ZXing (`core` + `javase`), server-side PNG |
| Image suggestion | Vision-capable AI API behind `ImageClassifierService` (provider and key from env; graceful fallback) |
| DevOps | Docker, Docker Compose, GitHub Actions CI, JaCoCo, Actuator |

Maven dependencies: `spring-boot-starter-web`, `-data-jpa`, `-security`, `-validation`, `-mail`, `-actuator`, `mysql-connector-j`, `jjwt` (api, impl, jackson), `lombok`, `springdoc-openapi-starter-webmvc-ui`, `openpdf` (PDF receipt/report), `commons-csv` (export), `zxing` core + javase (QR codes), `okhttp3 mockwebserver` (test), `spring-boot-starter-test`, `spring-security-test`.

---

## 4. Repository Layout
```
civicpulse/
├── pom.xml
├── Dockerfile, docker-compose.yml, .env.example
├── .github/workflows/ci.yml
├── scripts/         smoke-test.sh, smoke-test.ps1
├── README.md
├── docs/            PRD.md, design.md, memory.md, rules.md, task.md, FEATURE-VERIFICATION.md, QA-CHECKLIST.md, DEPLOYMENT.md
└── src/
    ├── main/java/com/civicpulse/
    │   ├── CivicPulseApplication.java
    │   ├── config/        SecurityConfig, CorsConfig, WebConfig, AsyncConfig, OpenApiConfig
    │   ├── security/      JwtService, JwtAuthFilter, UserDetailsServiceImpl
    │   ├── controller/    Auth, User, Complaint, Public, Officer, Admin, Analytics, Notification, Bot
    │   ├── service/       (interfaces) + service/impl/
    │   ├── repository/
    │   ├── entity/        User, Department, Category, KeywordRule, Complaint, Attachment, StatusHistory, Comment, Upvote, Feedback, Notification, AuditLog, Announcement, ...
    │   ├── dto/           request/ and response/
    │   ├── enums/         Role, ComplaintStatus, Urgency, AttachmentKind, NotificationType
    │   ├── exception/     custom exceptions + GlobalExceptionHandler
    │   ├── scheduler/     SlaScheduler, AutoCloseScheduler
    │   └── util/          CodeGenerator, GeoUtils, TextSimilarity, FileStorage, ContentNormalizer, ImageHash
    ├── main/resources/
    │   ├── application.yml, application-dev.yml, application-prod.yml
    │   ├── db/ (schema.sql, data.sql seed)
    │   └── static/  (frontend: 26 HTML pages, css, js modules, assets)
    └── test/java/com/civicpulse/...
```

---

## 5. Domain Glossary
- **Complaint / Grievance:** a citizen-reported civic issue.
- **Public code:** human-friendly ID like `CP-2026-000123`.
- **Department:** government unit that resolves complaints (Roads, Sanitation, Water, Electricity, Parks, Health, Police liaison, General).
- **Category:** issue type mapped to exactly one department.
- **SLA:** target resolution time; `sla_due_at` on each complaint.
- **Escalation:** automatic hand-off to a higher role when SLA is missed.
- **Priority score:** numeric ranking used to sort officer queues (`urgency*3 + severity*2 + min(upvotes,20)*0.5 + ageDays*0.7`).
- **Transparency dashboard:** public, anonymized statistics page.

### Enums
- `Role`: CITIZEN, OFFICER, DEPT_HEAD, ADMIN
- `ComplaintStatus`: SUBMITTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, REJECTED, REOPENED, ESCALATED, MERGED
- `Urgency`: LOW, MEDIUM, HIGH, CRITICAL

---

## 6. Core Business Rules
1. Every status change goes through `ComplaintService.changeStatus(...)`, which validates the transition, writes `complaint_status_history`, creates a notification and an audit log in one transaction.
2. Allowed transitions are defined in PRD section 9; anything else returns HTTP 400.
3. Feedback: once per complaint, only when status is RESOLVED or CLOSED.
4. Reopen: only by the owning citizen, within 7 days of resolution.
5. Anonymous complaints never expose citizen identity to officers or public endpoints.
6. Public endpoints return DTOs without personal data.
7. Passwords are BCrypt hashed; JWT secret comes from environment, never committed.
8. Uploaded files are stored outside `static/`, with random UUID names, validated MIME type and size.
9. Duplicates are prevented on the server: exact hash within 24 h, similar nearby complaint needs `confirmNotDuplicate`, per-user rate limits. `MERGED` is reachable only through the merge action; only an admin unmerge leaves it.
10. QR codes always encode `{app.public-base-url}/track.html?code=…`; public QR and track responses contain no personal data.
11. Image classification is advisory: never auto-applied, never blocks submission, API key stays on the server.
12. Registration requires accepted terms; consent version and timestamp are stored. Privacy and Terms text matches real data practices.

---

## 7. Environment and Running
```bash
# prerequisites: JDK 17+, Maven, MySQL 8
# Database:
CREATE DATABASE civicpulse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# env vars (dev example)
DB_URL=jdbc:mysql://localhost:3306/civicpulse
DB_USER=root
DB_PASS=Root@2806.
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
UPLOAD_DIR=./uploads
APP_PUBLIC_BASE_URL=http://localhost:8080

# Start app in dev profile:
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# Run all automated tests:
mvn test

# Run smoke test suite (PowerShell or Bash):
./scripts/smoke-test.ps1
./scripts/smoke-test.sh

# Or start the whole stack (app + MySQL) with Docker:
docker compose up --build
```

### Seed Accounts (Password: `Password@123`)
- Admin: `admin@civicpulse.test`
- Dept Head: `head.roads@civicpulse.test`
- Officer: `officer.roads@civicpulse.test`
- Citizen: `citizen@civicpulse.test`

---

## 8. Conventions Snapshot
- API prefix `/api/v1`, JSON, camelCase fields, ISO-8601 UTC timestamps.
- Pagination: `?page=0&size=10&sort=createdAt,desc`.
- Frontend uses a single `api.js` wrapper that attaches the JWT and handles 401/403/500 globally.
- CSS tokens in `tokens.css` only; components never hardcode colours.
- Full conventions in `rules.md`.

---

## 9. Current State (Complete)
- [x] Repository created, Maven project initialised
- [x] Database schema created & seeded with departments, categories, rules, and demo accounts
- [x] Auth module (Registration, Login, Refresh, Password Reset, Email Verification, Lockout)
- [x] Complaint module (4-step wizard, file uploads, auto-categorization, routing, map pin, receipt)
- [x] Officer workflow (Queue, Kanban board, Status transitions, Proof upload, Notes, Reassign, SLA timer)
- [x] Notifications (In-app center, Polling, Async email, Dev outbox, Preferences, Toast popups)
- [x] Analytics (Summary, Trends, By-Category, By-Department, Leaderboard, Heatmap, Predictions, CSV/PDF export)
- [x] Admin panel (Departments, Categories, Keyword Rules live test, Users, SLA policies, Announcements, Settings, Audit logs)
- [x] Frontend design system (Aurora Glass aesthetic, Dark/Light mode toggle, 3D tilt, Magnetic buttons, Spotlight cards)
- [x] CivicBot (Floating chat, FAQ intents, Track CP-... live lookup, Quick replies), i18n (English, Kannada, Hindi)
- [x] Duplicate prevention (24h exact hash, Proximity similarity check, Community upvoting, Perceptual image hashing, Merge/Unmerge, Rate limiting)
- [x] QR codes (ZXing server-side PNG generation, PDF receipt QR embedding, Deep linking)
- [x] Photo category suggestion (ImageClassifierService with fallback `available:false` and confidence weighting)
- [x] Privacy and Terms pages (`privacy.html`, `terms.html`, registration consent tracking)
- [x] Docker, CI, live deployment (`Dockerfile`, `docker-compose.yml`, `.github/workflows/ci.yml`, `DEPLOYMENT.md`)
- [x] Testing and docs (`mvn test` 31/31 PASS, smoke tests S01–S33 PASS, QA-CHECKLIST.md, FEATURE-VERIFICATION.md)

---

## 10. Decisions Log
| Date | Decision | Reason |
|---|---|---|
| 2026-10-24 | Keep vanilla JS + Bootstrap 5 frontend served from Spring Boot `static/` | Matches the internship stack, zero separate build step needed |
| 2026-10-24 | Rule-based classifier behind `ClassifierService` interface | Fast offline classification; AI/ML can plug in later |
| 2026-10-24 | Uniform `Password@123` across all seeded demo accounts | Simplifies QA and local smoke testing across all 4 roles |
| 2026-10-24 | Leaflet + OpenStreetMap for maps | Free, no API key required, reliable offline |
| 2026-10-24 | Local file storage with random UUID filenames | Prevents path traversal and filename collisions; portable |
| 2026-10-24 | Image classification fallback `available:false` | Operates gracefully without blocking submission when API key is unset |
| 2026-10-24 | QR codes generated server-side with ZXing | Same PNG endpoint for tracking page and PDF receipt, zero client bloat |
| 2026-10-24 | Reversible `MERGED` status for duplicates | Preserves historical traceability while merging community upvotes |
| 2026-10-24 | `@Transactional` on `SlaScheduler` & fetch joins in notification creation | Eliminates `LazyInitializationException` on detached proxy objects |
| 2026-10-24 | Flexible JSON aliases on `MergeComplaintRequest` | Accommodates both `parentComplaintId`/`parentId` and `reason`/`remark` |
| 2026-10-24 | Python-backed JSON fallback in `smoke-test.sh` | Guarantees smoke tests pass in Git Bash / MSYS2 without manual `jq` install |
| 2026-10-24 | Docker Compose + GitHub Actions CI | Reproducible containerized execution; automated test gate on commit |
| 2026-10-25 | Multi-method HTTP mapping for notification read endpoints | Accommodates PATCH, PUT, and POST gracefully without 405/500 errors (DEF-001) |
| 2026-10-25 | Authoritative schema.sql and ddl-auto=validate for production | Enforces PRD schema compliance and stops dangerous DDL updates outside development (DEF-002) |
| 2026-10-25 | Restrict citizen contact PII on public tracking DTO | Prevents citizen email/phone leakage to unauthenticated visitors while preserving display names (DEF-003) |
| 2026-10-25 | Explicit NoResourceFoundException handling and static favicon asset | Converts Spring 6 static resource misses to clean 404s without polluting 500 error logs (DEF-004) |
| 2026-10-25 | Route aliases for Officer Board, Admin Rules, and Hotspots | Eliminates path divergence between frontend HTML modules and backend REST controllers (DEF-005) |

---

## 11. Known Pitfalls and Gotchas
- Spring Security 6: uses `SecurityFilterChain` bean with method chaining.
- Server uploads: stored outside `static/` at `./uploads`, served safely through application endpoints.
- Transactionality: status transitions, notifications, and audits must be wrapped in `@Transactional` to avoid partial state updates.
- JPA Entity Loading: avoid uninitialized lazy proxies in asynchronous services; fetch eagerly or reload from repository.
- JSON Aliasing: request DTOs should declare `@JsonAlias` where multiple naming conventions exist.
- Bash `set -e`: avoid `((COUNT++))` when starting from 0, use `COUNT=$((COUNT + 1))`.
- Curl on Windows: use relative project paths (`./target/tmp`) instead of absolute Unix paths (`/tmp`) for file upload arguments.
