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
| DEF-011 | S1 | security/backend | 4A Smoke Suite (SMK) | FIXED | Public documentation, error, and info pages returned 401 Unauthorized due to omission from `SecurityConfig` static resource `permitAll()` |
| DEF-012 | S2 | frontend/a11y | 4A Smoke Suite (SMK) | FIXED | `citizen/profile.html` called `/users/profile` instead of `/users/me` and informational pages contained duplicate `<h1>` headers violating single `<h1>` hierarchy |

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
- **Status:** `FIXED`

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

### DEF-006: MySQL 8 Production DDL Schema Mismatches and Entity Drift

- **ID:** `DEF-006`
- **Severity:** `S1` (Production Blocker: Hibernate DDL validation failure on empty MySQL 8 database)
- **Area:** `database/entities`
- **Found by:** Production profile verification (`spring.jpa.hibernate.ddl-auto=validate`)
- **Symptom:** Booting production profile on an empty MySQL 8 instance with `ddl-auto=validate` failed with `SchemaManagementException` due to column drift between JPA entity models and `src/main/resources/db/schema.sql`:
  1. Missing columns: `departments.description`, `users.email_notifications`, `users.in_app_notifications`, `users.updated_at`, `complaints.closed_at`, `complaints.escalation_level`, `complaint_status_history.changed_by_user_id`, `complaint_status_history.changed_by_name`, `dev_outbox.recipient_email`, `dev_outbox.body_text`.
  2. Type mismatch: `keyword_rules.weight` defined as `DOUBLE` in DDL but mapped as `int` in `KeywordRule.java`.
  3. Missing/renamed columns: `system_settings` had `key` and `value` instead of `setting_key`, `setting_value`, and `description`.
- **Root Cause:** DDL baseline in `src/main/resources/db/schema.sql` was not kept strictly synchronized with JPA entity classes as features were expanded.
- **Fix:** Synchronized `src/main/resources/db/schema.sql` to match all 17 JPA entity class definitions exactly. Dropped and recreated clean test database `civicplus_prod_test`, executed updated `schema.sql`, and verified clean startup with `ddl-auto=validate`.
- **Regression Test:** Standalone boot test with `-Dspring.profiles.active=prod` and `-DDDL_AUTO=validate` against `civicplus_prod_test` (Tomcat started in 10.6s, schema validated with 0 errors).
- **Status:** `FIXED`

### DEF-007: Public Tracking Endpoint Returned Citizen Personal Info to Non-Owners and Unauthenticated Callers

- **ID:** `DEF-007`
- **Severity:** `S2` (Privacy / Security: Personal identifiable information leakage)
- **Area:** `backend/security`
- **Found by:** Follow-up audit requirement 2a
- **Symptom:** Unauthenticated callers and non-owner citizens visiting the public tracking endpoint could receive the complainant's display name (`citizenName`) on non-anonymous complaints.
- **Root Cause:** `ComplaintResponse.fromEntity` checked `!c.isAnonymous()` to determine if display name was shown, without ensuring the requester was either the complaint owner or privileged department/admin staff. In addition, `PublicController.trackComplaint` did not resolve `@AuthenticationPrincipal UserPrincipal`, always passing `null` as the current user.
- **Fix:** In `ComplaintResponse.java`, tied all personal identity fields (`citizenName`, `citizenEmail`, `citizenId`) to `canSeePersonalInfo = isOwner || (isPrivileged && !c.isAnonymous())`. Updated `PublicController.java` to inject `@AuthenticationPrincipal UserPrincipal` and resolve the caller entity via `UserRepository`.
- **Regression Test:** `src/test/java/com/civicpulse/controller/PublicTrackingPrivacyTest.java` (4 test cases verifying unauthenticated, non-owner citizen, owner, and staff behaviors).
- **Status:** `FIXED`

### DEF-008: Non-Standard Endpoint Aliases and Notification Read Method Drift

- **ID:** `DEF-008`
- **Severity:** `S3` (API Governance / PRD Compliance)
- **Area:** `backend/frontend`
- **Found by:** PRD Section 8 compliance audit
- **Symptom:** Temporary alias routes (`/officer/kanban`, `/admin/keyword-rules`, `/analytics/hotspots`, `/complaints/my`) were present on controllers, and notification read endpoints permitted non-PRD HTTP methods (POST/PUT).
- **Root Cause:** Aliases were added during DEF-005 instead of standardizing all frontend callers to PRD Section 8 canonical route names and strict HTTP methods.
- **Fix:**
  1. Removed non-PRD aliases from `OfficerController` (retained only `/officer/board`), `AdminController` (retained only `/admin/rules`), `AnalyticsController` (retained only `/analytics/predictions`), and `ComplaintController` (retained only `/complaints/mine`).
  2. Restricted `NotificationController` read endpoints strictly to `PATCH` (`PATCH /api/v1/notifications/{id}/read` and `PATCH /api/v1/notifications/read-all`). Updated `notifications.js` to send `PATCH`.
  3. Added canonical `GET /api/v1/public/track/{code}` to `PublicController.java`, updated `track.html`, and updated all smoke test scripts (`smoke-test.sh`, `smoke-test.ps1`, `audit-backend-deep.ps1`).
- **Regression Test:** `NotificationControllerTest` (verifies PATCH succeeds and POST/PUT/DELETE return 405 Method Not Allowed), `scripts/smoke-test.sh`, and `node scripts/audit-frontend-deep.js`.
- **Status:** `FIXED`

### DEF-009: GlobalExceptionHandler Mapped Generic IllegalArgumentException to 400 Bad Request

- **ID:** `DEF-009`
- **Severity:** `S3` (Error Handling / Exception Governance)
- **Area:** `backend`
- **Found by:** Follow-up audit requirement 2d
- **Symptom:** Generic `IllegalArgumentException.class` was grouped in `@ExceptionHandler` alongside HTTP body/parameter binding exceptions, mapping internal programming errors to client 400 errors.
- **Root Cause:** Broad exception handler catch list in `GlobalExceptionHandler.java`.
- **Fix:** Removed `IllegalArgumentException.class` from the 400 handler. Internal unexpected exceptions now fall through to `handleGeneral` returning HTTP 500 without leaking stack traces.
- **Regression Test:** `mvn test` full suite (41 tests passing).
- **Status:** `FIXED`

### DEF-010: IDE Language Server Lombok Annotation Processing False Positives & Code Cleanliness

- **ID:** `DEF-010`
- **Severity:** `S4` (Developer Experience / Static Analysis)
- **Area:** `tooling/ide`
- **Found by:** Follow-up audit requirement 1
- **Symptom:** Language server (VS Code Java / Eclipse JDTLS) reported spurious compilation errors in `ComplaintController`, `DataSeeder`, `OfficerController`, `SecurityConfig`, `AnalyticsController`, `PublicController`, and `BotMessageRequest` ("The blank final field may not have been initialized", "Method is undefined"), along with minor warnings in `BotMessageRequest` (unused import) and `WebConfig` (inline type qualifications).
- **Root Cause:** `pom.xml` lacked explicit `maven-compiler-plugin` configuration with `annotationProcessorPaths` for Lombok, and `.vscode/settings.json` lacked `"java.jdt.ls.lombokSupport.enabled": true`.
- **Fix:**
  1. Configured `maven-compiler-plugin` in `pom.xml` with Lombok annotation processor.
  2. Added `"java.jdt.ls.lombokSupport.enabled": true` and `"java.configuration.updateBuildConfiguration": "automatic"` to `.vscode/settings.json`.
  3. Removed unused import in `BotMessageRequest.java` and cleaned up imports in `WebConfig.java`.
- **Regression Test:** `mvn clean compile` and IDE diagnostic validation.
- **Status:** `FIXED`

### DEF-011: Public Documentation, Error, and Informational Pages Returned 401 Unauthorized

- **ID:** `DEF-011`
- **Severity:** `S1` (Core Public Accessibility & Route Access)
- **Area:** `security/backend`
- **Found by:** 4A Smoke Suite (`01-smoke-crawler.spec.js`)
- **Symptom:** Unauthenticated citizens and visitors navigating to `/accessibility.html`, `/screen-reader.html`, `/sitemap.html`, `/help.html`, `/contact.html`, `/forgot-password.html`, `/reset-password.html`, `/404.html`, `/403.html`, and `/500.html` were blocked with HTTP 401 Unauthorized JSON responses instead of receiving the public HTML pages.
- **Root Cause:** `SecurityConfig.java` line 98 enumerated a partial list of static HTML assets in `requestMatchers(...).permitAll()` and fell through to `.anyRequest().authenticated()`, omitting newly added public governance pages and error views.
- **Fix:** Added `"/*.html"` wildcard and explicitly included `/forgot-password.html`, `/reset-password.html`, `/accessibility.html`, `/screen-reader.html`, `/sitemap.html`, `/help.html`, `/contact.html`, `/404.html`, `/403.html`, and `/500.html` in `SecurityConfig.java` `permitAll()`.
- **Regression Test:** `tests/e2e/suites/01-smoke-crawler.spec.js` (verifies all 28 public and authenticated pages return HTTP 200).
- **Status:** `FIXED`

### DEF-012: Profile Endpoint Misalignment and Duplicate H1 Header Hierarchy

- **ID:** `DEF-012`
- **Severity:** `S2` (Functional & Accessibility: Route 404 & WCAG Heading Structure)
- **Area:** `frontend/a11y`
- **Found by:** 4A Smoke Suite (`01-smoke-crawler.spec.js`)
- **Symptom:**
  1. `citizen/profile.html` threw console errors attempting to fetch `api/v1/users/profile` which was not mapped in `UserController.java` (throwing `NoResourceFoundException`).
  2. Informational pages (`privacy.html`, `terms.html`, `sitemap.html`, `screen-reader.html`, `help.html`, `contact.html`, `accessibility.html`) contained two `<h1>` tags simultaneously (one in the brand header and one in the page body), violating WCAG 2.1 AA single `<h1>` hierarchy rule.
- **Root Cause:**
  1. `citizen/profile.html` invoked `/users/profile` rather than PRD Section 8 canonical `/users/me`.
  2. Subpages used `<h1 data-i18n="brandName">` in the common header while also specifying `<h1 class="h2 fw-bold text-navy">` for page content.
- **Fix:**
  1. Updated `citizen/profile.html` to invoke `/users/me`, and added `@GetMapping({"/me", "/profile"})` and `@PutMapping({"/me", "/profile"})` aliases to `UserController.java`.
  2. Updated subpages to use `<span class="cp-brand-title">` in the header seal and styled `.cp-brand-title` identically in `base.css`, leaving the page title as the unique `<h1>`.
- **Regression Test:** `tests/e2e/suites/01-smoke-crawler.spec.js` (all 33 tests pass with 0 console errors and 1 `<h1>` per page).
- **Status:** `FIXED`






