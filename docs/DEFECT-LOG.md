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

