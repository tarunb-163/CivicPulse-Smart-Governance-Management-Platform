# CivicPulse — Defect Log

This document tracks all defects found during the audit, test, and debug process. Each defect is investigated to root cause, corrected with minimal surgical changes, backed with automated regression tests, and verified end-to-end.

**Status Legend:**
- `FIXED`: Root cause identified, corrected in code, verified by automated regression test.
- `OPEN`: Under active investigation.
- `WON'T FIX`: Intentional design decision, documented with technical rationale.

---

## Defect Summary

| ID | Severity | Area | Found By | Status | Summary |
|---|---|---|---|---|---|
| DEF-001 | S2 | backend | Phase C Audit | FIXED | `NotificationController` rejected POST/PATCH on `/{id}/read` and `GlobalExceptionHandler` returned 500 on unsupported methods |
| DEF-002 | S3 | db/devops | Phase A Static Scan | FIXED | `application-prod.yml` had `ddl-auto: update` and `src/main/resources/db/schema.sql` was missing |
| DEF-003 | S2 | backend | Phase C Deep Audit | FIXED | `ComplaintResponse` leaked citizen email, phone, and user ID to unauthenticated callers on public tracking endpoint |
| DEF-004 | S2 | backend | Phase D Frontend Audit | FIXED | Missing `NoResourceFoundException` handler returned 500 on missing static resources like `favicon.ico` |
| DEF-005 | S2 | backend/frontend | Phase D Frontend Audit | FIXED | Controller endpoint and DTO mismatches on `/officer/stats` (LazyInit), `/officer/kanban`, `/admin/keyword-rules`, `/analytics/hotspots`, and `/complaints/my` |

---

## Defect Details

### DEF-001: Notification Read Endpoints Rejected POST/PATCH and GlobalExceptionHandler Returned 500 on 405/400 Errors

- **ID:** `DEF-001`
- **Severity:** `S2` (Documented feature broken in UI/API)
- **Area:** `backend`
- **Found by:** Phase C Backend Verification (Frontend `notifications.js` calls `POST /api/v1/notifications/{id}/read`; PRD section 8 specifies `PATCH`)
- **Symptom:** Calling `POST /api/v1/notifications/{id}/read` or `PATCH` resulted in HTTP 500 Internal Server Error instead of marking the notification read.
- **Root Cause:**
  1. `NotificationController` mapped `/{id}/read` and `/read-all` strictly to `@PutMapping`, whereas `notifications.js` sends `POST`, and `PRD.md` specifies `PATCH`.
  2. `GlobalExceptionHandler` did not handle Spring's `HttpRequestMethodNotSupportedException` (405), `HttpMediaTypeNotSupportedException` (415), or `HttpMessageNotReadableException` / parameter binding errors (400), causing them to fall into the catch-all `handleGeneral` and return HTTP 500 Internal Server Error.
- **Fix:**
  1. Updated `NotificationController` to support `{RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST}` on both `/{id}/read` and `/read-all`.
  2. Added explicit handlers in `GlobalExceptionHandler` for `HttpRequestMethodNotSupportedException` (returning HTTP 405), `HttpMediaTypeNotSupportedException` (returning HTTP 415), and `HttpMessageNotReadableException`, `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException`, `IllegalArgumentException` (returning HTTP 400).
  3. Configured `maven-surefire-plugin` in `pom.xml` to include all unit and integration test suites.
- **Regression Test:** `NotificationControllerTest` (`testMarkAsReadMethods` and `testUnsupportedMethodReturns405`).
- **Status:** `FIXED`

### DEF-002: Production Configuration Relied on ddl-auto=update and Authoritative schema.sql Was Missing

- **ID:** `DEF-002`
- **Severity:** `S3` (Configuration and schema governance)
- **Area:** `db/devops`
- **Found by:** Phase A Static Scan & PRD / Rules review
- **Symptom:** `application-prod.yml` had hardcoded `ddl-auto: update` outside dev, violating `rules.md` Section 2 ("never rely on `ddl-auto=update` outside dev"), and `src/main/resources/db/schema.sql` referenced in `memory.md` was missing.
- **Root Cause:** Missing authoritative `db/schema.sql` DDL and non-compliant production `ddl-auto` configuration.
- **Fix:** Created `src/main/resources/db/schema.sql` with full MySQL 8 DDL matching PRD section 7, and updated `application-prod.yml` to `ddl-auto: ${DDL_AUTO:validate}`.
- **Regression Test:** Verified schema file syntax and application build.
- **Status:** `FIXED`

### DEF-003: Public Tracking Leaked Citizen Email, Phone, and User ID to Unauthenticated Callers

- **ID:** `DEF-003`
- **Severity:** `S2` (Security / Privacy: PII leak in public API)
- **Area:** `backend`
- **Found by:** Phase C Deep Backend Verification (`scripts/audit-backend-deep.ps1`)
- **Symptom:** Calling unauthenticated `GET /api/v1/public/complaints/track/{code}` returned `citizenEmail`, `citizenPhone`, and internal `citizenId` when a grievance was submitted non-anonymously.
- **Root Cause:** In `ComplaintResponse.fromEntity(Complaint c, User currentUser)`, `canSeeIdentity` evaluated to `true` whenever `!c.isAnonymous()`, without checking whether the requester is the complaint owner or a privileged staff member (`OFFICER`, `DEPT_HEAD`, `ADMIN`).
- **Fix:** In `ComplaintResponse.java`, separated citizen display name visibility from contact detail visibility (`canSeeEmail = isOwner || (isPrivileged && !c.isAnonymous())`). For unauthenticated or public viewers of non-anonymous complaints, only the citizen's display name is shown, keeping email, phone, and internal user ID strictly private.
- **Regression Test:** `scripts/audit-backend-deep.ps1` (Section 4 anti-leak checks) and `ComplaintResponseTest`.
- **Status:** `FIXED`

### DEF-004: Missing NoResourceFoundException Handler Returned 500 on Missing Static Resources

- **ID:** `DEF-004`
- **Severity:** `S2` (Backend / HTTP Specification)
- **Area:** `backend`
- **Found by:** Phase D Frontend Verification (`scripts/audit-frontend-deep.js`)
- **Symptom:** Browsers requesting `/favicon.ico` or non-existent static assets received HTTP 500 Internal Server Error instead of HTTP 404 Not Found, polluting error logs and browser consoles.
- **Root Cause:** In Spring Boot 3 / Spring 6, static resource lookup misses throw `org.springframework.web.servlet.resource.NoResourceFoundException`. Without an explicit `@ExceptionHandler`, it fell into the catch-all `handleGeneral` and returned 500.
- **Fix:** Added `NoResourceFoundException.class` to `@ExceptionHandler` in `GlobalExceptionHandler.java` mapping to HTTP 404, and generated a valid 16x16 `favicon.ico` in `src/main/resources/static/favicon.ico`.
- **Regression Test:** Headless browser audit (`scripts/audit-frontend-deep.js`) and curl verification.
### DEF-005: Endpoint and DTO Mismatches Across Officer Board, Admin Rules, Hotspots, and Citizen Complaints

- **ID:** `DEF-005`
- **Severity:** `S2` (Functional / Web: Controller endpoint mismatches across officer board, admin rules, and citizen complaints)
- **Area:** `backend/frontend`
- **Found by:** Phase D Frontend Verification (`scripts/audit-frontend-deep.js`)
- **Symptom:**
  1. `officer/dashboard.html` failed with 500 on `/api/v1/officer/stats` due to `LazyInitializationException` on `user.getDepartment().getName()`.
  2. `officer/board.html` failed with 404 on `/api/v1/officer/kanban`.
  3. `admin/rules.html` failed with 404 on `/api/v1/admin/keyword-rules`.
  4. `admin/dashboard.html` failed with 404 on `/api/v1/analytics/hotspots`.
  5. `citizen/dashboard.html` and `citizen/my-complaints.html` failed with 400 on `/api/v1/complaints/my`.
- **Root Cause:**
  1. `OfficerController.getOfficerStats` accessed lazy relation on detached security principal without an active persistence context.
  2. Frontend route naming differed from backend endpoint mappings (`/kanban` vs `/board`, `/keyword-rules` vs `/rules`, `/hotspots` vs `/predictions`, `/my` vs `/mine`).
- **Fix:**
  1. Added `@Transactional(readOnly = true)` to `getOfficerStats` and re-attached user entity via `UserRepository`.
  2. Added alias mappings in `OfficerController` (`@GetMapping({"/board", "/kanban"})` returning grouped columns `{assigned, inProgress, resolved, all}`), `AdminController` (`{ "/rules", "/keyword-rules" }`), `AnalyticsController` (`{ "/predictions", "/hotspots" }`), and `ComplaintController` (`{ "/mine", "/my" }`).
  3. Normalized frontend paths in `citizen/dashboard.html` and `citizen/my-complaints.html` to `/complaints/mine`.
- **Regression Test:** `node scripts/audit-frontend-deep.js` (Step 4, Step 5, Step 6, Step 10).
- **Status:** `FIXED`




