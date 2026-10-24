# CivicPulse — Task Plan

Legend: `[ ]` todo · `[~]` in progress · `[x]` done · **(MVP)** must ship · **(WOW)** differentiator · **(LATER)** stretch  
Estimates are for a team of 4–5 working in parallel. Suggested total: **9–11 weeks** (includes Phase 8).

---

## Phase 0 — Setup (Week 1)
- [x] T0.1 Create GitHub repo, branches (`main`, `develop`), `.gitignore`, README **(MVP)**
- [x] T0.2 Generate Spring Boot project (Maven) with dependencies from `memory.md` **(MVP)**
- [x] T0.3 Configure `application.yml` profiles, env vars, MySQL connection **(MVP)**
- [x] T0.4 Create database schema + seed (`departments`, `categories`, `keyword_rules`, demo users) **(MVP)**
- [x] T0.5 Package structure, `GlobalExceptionHandler`, standard error JSON, CORS, Swagger **(MVP)**
- [x] T0.6 Frontend scaffold: folder structure, `tokens.css`, `base.css`, Bootstrap theme override, fonts **(MVP)**
- [x] T0.7 `api.js`, `auth.js`, `toast.js`, `theme.js` core modules **(MVP)**
- [x] T0.8 Postman collection skeleton; CI (GitHub Actions: build + test) **(WOW)**

## Phase 1 — Authentication and Users (Week 1–2)
- [x] T1.1 `User` entity, repository, roles enum **(MVP)**
- [x] T1.2 Register endpoint with validation + BCrypt **(MVP)**
- [x] T1.3 Login endpoint, `JwtService`, `JwtAuthFilter`, refresh token **(MVP)**
- [x] T1.4 `SecurityConfig` with RBAC rules and `@PreAuthorize` **(MVP)**
- [x] T1.5 Forgot/reset password with emailed token **(MVP)**
- [x] T1.6 Email verification on signup **(WOW)**
- [x] T1.7 Profile endpoints (view, edit, change password, preferences) **(MVP)**
- [x] T1.8 Login rate limiting and lockout **(WOW)**
- [x] T1.9 Frontend: Login, Register, Forgot/Reset pages with split-layout animation, password strength meter **(MVP)**
- [x] T1.10 Frontend: role-based redirect + route guards **(MVP)**
- [x] T1.11 Tests: auth service, 401/403 cases **(MVP)**

## Phase 2 — Complaint Core (Week 2–4)
- [x] T2.1 Entities: `Complaint`, `Attachment`, `StatusHistory`, `Category`, `Department` **(MVP)**
- [x] T2.2 `CodeGenerator` for public code `CP-YYYY-######` **(MVP)**
- [x] T2.3 File storage service with validation (type, size, count, random name) **(MVP)**
- [x] T2.4 `POST /complaints` multipart create + history entry SUBMITTED **(MVP)**
- [x] T2.5 `ClassifierService` (keyword rules) + `POST /complaints/classify` **(MVP)**
- [x] T2.6 Routing: category → department; fallback General **(MVP)**
- [x] T2.7 Priority score function **(WOW)**
- [x] T2.8 Auto-assignment to least-loaded officer **(WOW)**
- [x] T2.9 Duplicate detection (`findSimilar`: 300 m radius + text similarity) **(WOW)**
- [x] T2.10 `GET /complaints/mine`, `GET /complaints/{id}` with ownership checks **(MVP)**
- [x] T2.11 Anonymous complaint mode **(WOW)**
- [x] T2.12 PDF receipt generation **(MVP)**
- [x] T2.13 Frontend: File Complaint wizard (4 steps, animated stepper) **(MVP)**
- [x] T2.14 Frontend: Leaflet map picker, geolocation, draggable pin, address fill **(WOW)**
- [x] T2.15 Frontend: live category suggestion chip while typing (debounced) **(WOW)**
- [x] T2.16 Frontend: drag-drop uploader with previews **(MVP)**
- [x] T2.17 Frontend: similar-complaints panel + "support this instead" **(WOW)**
- [x] T2.18 Frontend: draft autosave, confetti + animated receipt on success **(WOW)**
- [x] T2.19 Frontend: My Complaints (cards/table toggle, filters, skeletons) **(MVP)**
- [x] T2.20 Tests: classifier, priority, routing, validation **(MVP)**

## Phase 3 — Officer Workflow and SLA (Week 4–6)
- [x] T3.1 `ComplaintService.changeStatus` with transition validation, history, audit **(MVP)**
- [x] T3.2 Officer queue endpoint with filters, search, pagination **(MVP)**
- [x] T3.3 Status update + remark; proof upload **(MVP)**
- [x] T3.4 Internal notes; reassignment with reason **(MVP)**
- [x] T3.5 Reject with reason; citizen reopen (7-day window) **(MVP)**
- [x] T3.6 SLA policy + `sla_due_at` calculation per category **(WOW)**
- [x] T3.7 `SlaScheduler` (every 15 min): flag overdue and escalate to head then admin **(WOW)**
- [x] T3.8 `AutoCloseScheduler` (resolved → closed after 7 days) **(WOW)**
- [x] T3.9 Comments thread (citizen ↔ officer) **(WOW)**
- [x] T3.10 Upvote ("Me too") endpoint with unique constraint **(WOW)**
- [x] T3.11 Officer performance stats endpoint **(WOW)**
- [x] T3.12 Frontend: Officer dashboard + queue table with quick actions **(MVP)**
- [x] T3.13 Frontend: Kanban board with SortableJS and keyboard "move to" menu **(WOW)**
- [x] T3.14 Frontend: complaint workspace drawer (status, remark, proof, notes, reassign) **(MVP)**
- [x] T3.15 Frontend: SLA ring component and countdown **(WOW)**
- [x] T3.16 Frontend: Complaint detail page with animated timeline, gallery lightbox, comments **(MVP)**
- [x] T3.17 Tests: transition matrix, ownership/permission, scheduler logic **(MVP)**

## Phase 4 — Notifications, Tracking, Feedback (Week 5–6)
- [x] T4.1 `Notification` entity + service; create on submit/assign/status/comment **(MVP)**
- [x] T4.2 Notification endpoints (list, unread count, mark read, read all) **(MVP)**
- [x] T4.3 Email notifications (async, templates) **(MVP)**
- [x] T4.4 Preference toggles per user **(WOW)**
- [x] T4.5 Feedback endpoint (once, only after resolved) + department rating update **(MVP)**
- [x] T4.6 Public tracking endpoint by code (+ verification) **(MVP)**
- [x] T4.7 Citizen impact stats and badges **(WOW)**
- [x] T4.8 Frontend: bell dropdown, unread badge wiggle, polling every 15 s, live toasts **(MVP)**
- [x] T4.9 Frontend: feedback modal with animated stars **(MVP)**
- [x] T4.10 Frontend: public Track page with timeline result **(MVP)**
- [x] T4.11 Frontend: citizen dashboard (stats, recent, activity, badges) **(MVP)**

## Phase 5 — Analytics and Admin (Week 6–8)
- [x] T5.1 Aggregate queries: summary, trend, by-category, by-department, avg resolution time **(MVP)**
- [x] T5.2 Leaderboard (resolution rate, avg time, rating) **(MVP)**
- [x] T5.3 Heatmap data endpoint **(WOW)**
- [x] T5.4 Hotspot prediction (moving average vs last week, 1.5× rule) **(WOW)**
- [x] T5.5 CSV and PDF export **(MVP)**
- [x] T5.6 Public stats endpoint for Transparency Dashboard **(WOW)**
- [x] T5.7 Admin CRUD: departments, categories **(MVP)**
- [x] T5.8 Admin: keyword rules CRUD + test endpoint **(WOW)**
- [x] T5.9 Admin: users management, officer creation **(MVP)**
- [x] T5.10 Admin: SLA policies, announcements, settings **(WOW)**
- [x] T5.11 Audit log endpoint + viewer **(MVP)**
- [x] T5.12 Frontend: Admin dashboard (KPI counters, Chart.js charts, filters, leaderboard, heatmap, prediction cards) **(MVP)**
- [x] T5.13 Frontend: admin management pages (table + drawer-form pattern) **(MVP)**
- [x] T5.14 Frontend: Transparency Dashboard (public) **(WOW)**
- [x] T5.15 Tests: analytics queries, export, permissions **(MVP)**

## Phase 6 — Experience Polish (Week 7–9, overlaps Phase 5)
- [x] T6.1 Aurora background, glass surfaces, final tokens for light/dark **(MVP)**
- [x] T6.2 Spotlight hover cards, 3D tilt, magnetic buttons, ripple **(WOW)**
- [x] T6.3 Scroll reveal + stagger, animated counters **(WOW)**
- [x] T6.4 Page transitions (View Transitions API + fallback) **(WOW)**
- [x] T6.5 Landing page: hero with live track box, floating stat cards, how-it-works, features, FAQ **(MVP)**
- [x] T6.6 Theme toggle with morph animation, persisted preference **(WOW)**
- [x] T6.7 Command palette (Ctrl+K) **(WOW)**
- [x] T6.8 CivicBot backend (intents, track-by-ID) **(WOW)**
- [x] T6.9 CivicBot widget UI (orb, chat panel, typing dots, chips) **(WOW)**
- [x] T6.10 i18n: English, Kannada, Hindi JSON + language switcher **(WOW)**
- [x] T6.11 Skeletons, empty states, error pages (404/403/500) with illustrations **(MVP)**
- [x] T6.12 Sidebar collapse, bottom nav on mobile, responsive pass **(MVP)**
- [x] T6.13 PWA manifest + icons **(WOW)**
- [x] T6.14 Accessibility pass (keyboard, ARIA, contrast, reduced-motion) **(MVP)**
- [x] T6.15 Performance pass (lazy init, image optimisation, Lighthouse 85+/90+) **(MVP)**

## Phase 7 — Testing, Docs, Release (Week 9–10)
- [x] T7.1 End-to-end manual test of full lifecycle with all three roles **(MVP)**
- [x] T7.2 Security review checklist (rules.md section 3) **(MVP)**
- [x] T7.3 Fix bugs, cross-browser and mobile testing **(MVP)**
- [x] T7.4 Realistic demo data seeder (e.g., 200 complaints across departments and weeks) **(WOW)**
- [x] T7.5 Finalise Swagger docs and Postman collection **(MVP)**
- [x] T7.6 README with setup, screenshots, architecture diagram **(MVP)**
- [x] T7.7 Deployment is done in Phase 8 (T8.27–T8.28); here only re-verify the live site after the final fixes **(MVP)**
- [x] T7.8 Demo script and recorded walkthrough; final presentation updates **(MVP)**

## Phase 8 — Additional Features and DevOps (start after Phase 2, finish before Phase 7 sign-off)

### 8A. Complaint duplication prevention (DUP-1 to DUP-7)
- [x] T8.1 Migration: `content_hash`, `duplicate_of_id`, `image_suggestion` on complaints; `sha256`, `phash` on attachments; `complaint_followers` table; add `MERGED` status and transitions **(MVP)**
- [x] T8.2 `normalizeContent` + exact-duplicate guard (409 `DUPLICATE_EXACT`, 24 h window) **(MVP)**
- [x] T8.3 Server-side similar-complaint guard: 409 `DUPLICATE_SUSPECTED` with candidates, `confirmNotDuplicate` flag **(MVP)**
- [x] T8.4 `POST /complaints/{id}/support` (follow + upvote, idempotent) and follower notifications **(MVP)**
- [x] T8.5 Image hashing (SHA-256 + perceptual hash) and near-match flagging on upload **(WOW)**
- [x] T8.6 Officer merge endpoint, admin unmerge, follower/upvote transfer without double counting, notifications, audit **(MVP)**
- [x] T8.7 Per-user rate limits (hourly/daily) with `Retry-After` **(MVP)**
- [x] T8.8 Admin system settings for radius, similarity threshold, hash window, rate limits **(WOW)**
- [x] T8.9 Frontend: duplicate dialog in wizard (candidate cards, "Support this instead", "Submit anyway" with confirmation), exact-duplicate message with link, MERGED badge, merge UI in officer workspace **(MVP)**
- [x] T8.10 Tests: `ContentNormalizerTest`, `DuplicateGuardTest`, `ImageHashTest`, `MergeServiceTest`, `RateLimitTest`; smoke steps S24–S29; UI checks U16, U18 **(MVP)**

### 8B. QR codes (QR-1 to QR-3)
- [x] T8.11 Add ZXing dependency, config `app.public-base-url`, `QrService` **(MVP)**
- [x] T8.12 `GET /public/qr/{code}.png`; embed the QR in the PDF receipt **(MVP)**
- [x] T8.13 Frontend: QR card on success, detail and track pages with Download PNG and Print stylesheet **(MVP)**
- [x] T8.14 `track.html?code=` deep link: prefill and auto-load result **(MVP)**
- [x] T8.15 Tests: `QrServiceTest` (decode PNG and PDF QR back to the URL); smoke S30; UI check U14 **(MVP)**

### 8C. Photo-based category suggestion (IMG-1 to IMG-4)
- [x] T8.16 `ImageClassifierService` interface + vision-provider implementation (REST call, env-configured provider/key, timeout, retry, allowed-category prompt) **(MVP)**
- [x] T8.17 `POST /complaints/classify-image` with file validation and graceful fallback (`available:false`) **(MVP)**
- [x] T8.18 Combine image and text suggestions (agreement raises confidence, conflict returns both) **(MVP)**
- [x] T8.19 Frontend: suggestion chip after first photo, accept/change, never auto-applied **(MVP)**
- [x] T8.20 Tests with a mock HTTP server (`ImageClassifierServiceTest`, `ClassificationMergeTest`); live check when a real key is available; smoke S31; UI check U15 **(MVP)**

### 8D. Privacy and Terms (LEG-1 to LEG-4)
- [x] T8.21 Write `privacy.html` and `terms.html` from the real implemented data flows (include photos, location, third-party image processing, map tiles); add footer links on every page and links beside register/submit forms **(MVP)**
- [x] T8.22 Consent checkbox on register; backend requires `acceptedTerms`, stores consent version and timestamp; smoke S32; UI check U17 **(MVP)**

### 8E. Docker, CI and live deployment (OPS-1 to OPS-5)
- [x] T8.23 Multi-stage `Dockerfile`, non-root user, `HEALTHCHECK` **(MVP)**
- [x] T8.24 `docker-compose.yml` (app + MySQL, volumes for DB and uploads, `.env`, health-based start order); verify `docker compose up --build` from clean volumes **(MVP)**
- [x] T8.25 Actuator health/readiness; restrict other actuator endpoints; smoke S33 **(MVP)**
- [x] T8.26 GitHub Actions `ci.yml`: build, test, JaCoCo artifact, compose up + smoke test; README badge **(MVP)**
- [x] T8.27 Deployment: choose host, provision hosted MySQL, `prod` profile, environment secrets, HTTPS, persistent uploads storage, `docs/DEPLOYMENT.md` with exact steps **(MVP)**
- [x] T8.28 Run `scripts/smoke-test.sh <live-url>`; add live URL, CI badge and demo credentials to README **(MVP)**

---

## Stretch / Future (LATER)
- [ ] Voice input for complaint description
- [ ] SSE/WebSocket real-time notifications
- [ ] SMS provider integration
- [ ] ML/AI classifier and duplicate detection (replace `ClassifierService`)
- [ ] LLM-powered CivicBot
- [ ] Android/iOS app or full PWA offline mode
- [ ] Government portal integrations

---

## Weekly Checkpoints
| Week | Demo-able outcome | Status |
|---|---|---|
| 1 | Project runs, DB ready, login works | ✅ COMPLETE |
| 2 | Citizen can register/login and see dashboard shell | ✅ COMPLETE |
| 3–4 | Citizen files complaint with images and map; auto-routed | ✅ COMPLETE |
| 5–6 | Officer works the complaint; citizen sees timeline and notifications | ✅ COMPLETE |
| 7–8 | Admin analytics, exports, transparency page | ✅ COMPLETE |
| 9 | Polish, CivicBot, themes, i18n | ✅ COMPLETE |
| 10–11 | Phase 8 finished: Docker, CI green, legal pages, QR, image suggestion, duplicate prevention, live URL; final testing, docs, demo | ✅ COMPLETE |
