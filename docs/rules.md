# CivicPulse — Development Rules

These rules apply to every contributor and every AI assistant. If a rule must be broken, record why in `memory.md` (Decisions Log).

---

## 1. General
1. Read `memory.md`, `PRD.md` and `design.md` before starting a task. Work on one task from `task.md` at a time.
2. MVP first, WOW features after. Never leave the app in a broken state on `main`.
3. Prefer simple, readable code over clever code. Small functions, clear names.
4. No secrets in the repo. Use environment variables; commit `.env.example` only.
5. No dead code, no commented-out blocks, no unresolved `TODO` without a task in `task.md`.
6. Everything user-facing must support loading, empty and error states.

---

## 2. Backend (Java / Spring Boot)

### Architecture
- Strict layers: `Controller → Service → Repository`. Controllers contain no business logic; repositories contain no business logic.
- Services are interfaces with `impl` classes. Cross-cutting rules (status transitions, priority, routing) live only in services.
- **Never expose entities in API responses.** Use request/response DTOs and mappers (MapStruct or manual).
- Constructor injection only (Lombok `@RequiredArgsConstructor`); no field `@Autowired`.

### Naming and style
- Packages lowercase; classes `PascalCase`; methods/fields `camelCase`; constants `UPPER_SNAKE`.
- Controllers `XController`, services `XService` / `XServiceImpl`, DTOs `XRequest` / `XResponse`.
- REST: nouns, plural, lowercase, kebab-case paths. Correct verbs and status codes (201 create, 204 no body, 400 validation, 401 unauthenticated, 403 forbidden, 404 missing, 409 conflict).
- Methods under ~30 lines; classes under ~300 lines.

### Validation and errors
- Validate every request with Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`, custom validators).
- One `GlobalExceptionHandler` (`@RestControllerAdvice`) returns the standard error JSON from the PRD. No stack traces to clients.
- Use custom exceptions: `ResourceNotFoundException`, `InvalidStatusTransitionException`, `AccessDeniedException`, `DuplicateResourceException`.

### Persistence
- `@Transactional` on service methods that write; `readOnly = true` for reads.
- Use pagination for every list endpoint. Add indexes listed in PRD section 7.
- Avoid N+1 (fetch joins / entity graphs). Use aggregate queries (`GROUP BY`) for analytics, not in-memory loops.
- Timestamps in UTC (`Instant` / `LocalDateTime` with UTC config). Use `@CreatedDate` / `@LastModifiedDate`.
- Schema changes via versioned SQL (Flyway recommended) or reviewed `schema.sql`; never rely on `ddl-auto=update` outside dev.

### Logging
- SLF4J only; no `System.out`. Log at INFO for business events, WARN for recoverable problems, ERROR for failures. Never log passwords, tokens, or full personal data.

---

## 3. Security (non-negotiable)
1. BCrypt for passwords (strength 10+). Never store or log plain text.
2. JWT: short-lived access token (15–30 min) + refresh token; secret from env; validate signature and expiry in a filter.
3. Every endpoint is secured by default; whitelist only `/api/v1/auth/**`, `/api/v1/public/**`, `/api/v1/bot/**`, static files.
4. Use method-level checks (`@PreAuthorize`) **and** ownership checks in services (a citizen can only read their own complaints; an officer only their department's).
5. File uploads: allow-list extensions and MIME types (jpg, png, webp, pdf), max 5 MB, max 5 files, random UUID file names, store outside web root, serve through an authorized controller.
6. Parameterized queries only (JPA/JPQL); never concatenate SQL.
7. CORS: explicit origins. Enable security headers (X-Content-Type-Options, X-Frame-Options, CSP where practical).
8. Rate limit login, registration, password reset and complaint creation.
9. Escape all user content when rendering in the frontend (`textContent`, not `innerHTML`, unless sanitized).
10. Public endpoints must never return emails, phones, or anonymous complainant identity.
11. Record sensitive actions (login failures, role changes, status changes, deletions) in `audit_logs`.

---

## 4. API Rules
- Base path `/api/v1`. JSON in and out. camelCase fields. ISO-8601 dates.
- Consistent envelope for lists: `{ content, page, size, totalElements, totalPages }`.
- Document every endpoint with springdoc annotations; keep Swagger accurate.
- Keep a Postman collection in `docs/postman/` and update it with every endpoint change.
- Backward-incompatible changes require a version bump or team agreement.

---

## 5. Frontend Rules

### Structure
- One HTML file per page; shared logic in `assets/js/core`, effects in `assets/js/ui`, page logic in `assets/js/pages`.
- Use ES modules. No global variables except through a single `window.CP` namespace if unavoidable.
- All HTTP calls go through `api.js` (adds JWT, handles 401 by redirecting to login, shows toast on errors).
- Route guards run on page load: redirect by role.

### CSS
- All colours, spacing, radius, shadows, durations come from tokens in `tokens.css` (see `design.md`). **No hardcoded hex values in components.**
- Naming: BEM-like (`cp-card`, `cp-card__title`, `cp-card--stat`). No inline styles except CSS variables set from JS.
- Mobile-first media queries. Use Bootstrap grid/utilities; override look with tokens, don't fight the framework.
- Dark theme via `[data-theme="dark"]` variables; every component must work in both themes.

### Animation and performance
- Animate only `transform` and `opacity` (and `filter` sparingly). Never animate `width/height/top/left` for effects.
- Use `will-change` only during an active animation. Keep durations within 150–600 ms (ambient loops excepted).
- Initialize heavy widgets (map, charts, tilt) lazily via `IntersectionObserver`.
- Always honour `prefers-reduced-motion`; disable tilt, parallax, confetti and magnetic effects for those users and for touch devices.
- Debounce search and `pointermove` handlers; use `requestAnimationFrame` for pointer-driven effects.

### Accessibility
- Semantic HTML, labels on every input, `alt` on images, `aria-label` on icon-only buttons.
- Visible focus states; full keyboard operability; modals trap focus and close on Esc.
- Colour is never the only indicator (status chips include text and icon).
- Announce toasts and chat messages with `aria-live`.

### Content and i18n
- No hardcoded UI strings in JS; use keys from `assets/i18n/*.json` (`data-i18n` attributes).
- Dates/numbers formatted with `Intl` using the user's locale.

---

## 6. Database Rules
- Table names plural snake_case; columns snake_case; PK `id` BIGINT auto-increment; FK `<entity>_id`.
- Every FK indexed; unique constraints for emails, public codes, upvote pairs, one feedback per complaint.
- Soft-disable users (`enabled=false`) instead of deleting; avoid hard deletes of complaints.
- Seed data (departments, categories, keyword rules, demo users) lives in `data.sql` and must be safe to re-run.

---

## 7. Git Workflow
- Branches: `main` (stable), `develop` (integration), feature branches `feature/<module>-<short-desc>`, fixes `fix/<desc>`.
- Commit messages (Conventional Commits): `feat(complaint): add duplicate detection`, `fix(auth): handle expired refresh token`, `docs: update PRD`, `style(ui): refine card hover`.
- Small, focused commits and PRs (aim under 400 lines). At least one teammate reviews each PR.
- Never commit directly to `main`. Never commit `.env`, uploads, `target/`, IDE folders.
- Rebase or merge `develop` into your branch before opening a PR; resolve conflicts locally.

---

## 8. Testing
- Unit tests for every service with business rules (status transitions, priority, classification, assignment, SLA).
- Controller tests with `MockMvc` for auth/permission cases (401/403/400/404/happy path).
- Repository tests for custom queries (`@DataJpaTest`).
- Target: 70%+ coverage on service layer; all critical flows have at least one integration test.
- Manual QA checklist per release: file complaint → route → work → resolve → feedback → analytics.
- Bug fix = add a failing test first when practical.

---

## 9. Definition of Done
A task is done only when:
- [ ] Code compiles, app starts, no new warnings of note
- [ ] Acceptance criteria in `PRD.md` met
- [ ] Validation, error handling and permission checks in place
- [ ] Tests written and passing
- [ ] UI works in light and dark theme, at 360 px and desktop widths, with reduced motion
- [ ] Swagger / Postman updated
- [ ] `task.md` ticked and `memory.md` updated if decisions changed
- [ ] PR reviewed and merged

---

## 10. Rules for AI Coding Assistants
1. Follow this file, `PRD.md` and `design.md` over your own preferences. If they conflict with the request, ask.
2. Do not invent endpoints, tables or enums; extend the documented ones and update docs in the same change.
3. Change only what the task needs; do not refactor unrelated files.
4. Never output secrets, real personal data, or hardcoded credentials.
5. Generate complete, runnable code with imports; include validation and error handling.
6. For UI: use existing tokens and components first; add new ones to `components.css` and document them in `design.md`.
7. State assumptions explicitly and list files changed at the end of each task.
8. If a requirement is ambiguous, ask a short question instead of guessing on security, data-model, or workflow rules.

---

## 11. DevOps, Legal and External Services
**Docker and CI**
- Dockerfile: multi-stage, pinned base image versions, non-root user, `HEALTHCHECK`, `.dockerignore`; no secrets in the image or build args.
- Compose reads `.env`; MySQL and app have healthchecks and the app waits for the database.
- CI runs on every push and PR: build, tests, JaCoCo artifact, compose up and smoke test. `main` must be green before merge; never skip or delete a failing test to get green.

**Deployment**
- Secrets only via host environment variables; `prod` profile uses `ddl-auto=validate`, HTTPS only, persistent storage for uploads.
- A demo instance may seed demo accounts, but their credentials come from environment variables, are documented in the README, and the site is clearly labelled as a demo.
- Every step needed to redeploy lives in `docs/DEPLOYMENT.md`.

**External services (vision API and similar)**
- Always behind an interface; key from environment; timeout and retry; failure never blocks the user flow.
- Send only what is needed (the single image), disclose it in the Privacy Policy, never log the key, mock the provider in tests.

**Legal pages**
- Write Privacy and Terms from the real data flows, mark them as a project draft, and update them in the same change whenever data collection changes. Consent is versioned.

**Duplicate prevention and QR**
- Duplicate checks run on the server; thresholds and rate limits come from system settings, not constants. Candidate lists never reveal another citizen's personal data.
- QR URLs use the configured `app.public-base-url`, never the request Host header.
