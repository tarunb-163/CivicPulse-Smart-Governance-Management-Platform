# CivicPulse — Design System and UI/UX Specification

**Design concept:** *"Aurora Glass"* — a calm, trustworthy civic platform with a futuristic feel: frosted-glass surfaces, soft aurora gradients, spotlight hover cards, and buttery motion. Serious enough for government use, delightful enough to be remembered.

**Stack constraint:** HTML5, CSS3, vanilla JavaScript, Bootstrap 5 (grid and utilities only, heavily themed), plus small libraries listed in section 11.

---

## 1. Design Principles

1. **Clarity first:** every animation explains something (state change, hierarchy, feedback). No motion for its own sake.
2. **Feels alive:** every interactive element responds to hover, focus and press within 100 ms.
3. **Smooth, never slow:** animate `transform` and `opacity` only; target 60 fps; durations 150–600 ms.
4. **Trust and accessibility:** high contrast, clear status colours, keyboard support, `prefers-reduced-motion` respected.
5. **One system:** all values come from CSS variables (tokens). No random hex codes or pixel values in components.

---

## 2. Design Tokens

### 2.1 Colours

```css
:root {
  /* Brand */
  --cp-primary-50:  #eef4ff;
  --cp-primary-100: #d9e6ff;
  --cp-primary-300: #8fb3ff;
  --cp-primary-500: #3b82f6;   /* main */
  --cp-primary-600: #2563eb;
  --cp-primary-700: #1d4ed8;
  --cp-accent:      #14b8a6;   /* teal: success / progress */
  --cp-violet:      #8b5cf6;   /* gradient partner */
  --cp-pink:        #ec4899;   /* highlight */

  /* Status */
  --cp-submitted:   #64748b;
  --cp-assigned:    #3b82f6;
  --cp-progress:    #f59e0b;
  --cp-resolved:    #10b981;
  --cp-closed:      #0d9488;
  --cp-rejected:    #ef4444;
  --cp-escalated:   #f43f5e;
  --cp-reopened:    #a855f7;
  --cp-merged:      #94a3b8;

  /* Surfaces (light) */
  --cp-bg:          #f5f8ff;
  --cp-surface:     rgba(255,255,255,.72);
  --cp-surface-solid:#ffffff;
  --cp-border:      rgba(15,23,42,.08);
  --cp-text:        #0f172a;
  --cp-text-muted:  #64748b;

  /* Gradients */
  --cp-grad-brand:  linear-gradient(135deg,#3b82f6 0%,#8b5cf6 60%,#ec4899 100%);
  --cp-grad-aurora: radial-gradient(60% 60% at 20% 20%,rgba(59,130,246,.35),transparent 60%),
                    radial-gradient(50% 50% at 80% 10%,rgba(139,92,246,.30),transparent 60%),
                    radial-gradient(50% 50% at 60% 90%,rgba(20,184,166,.25),transparent 60%);
}

[data-theme="dark"] {
  --cp-bg:          #060b18;
  --cp-surface:     rgba(17,24,39,.65);
  --cp-surface-solid:#0f172a;
  --cp-border:      rgba(255,255,255,.08);
  --cp-text:        #e5ecff;
  --cp-text-muted:  #94a3b8;
}
```

### 2.2 Typography
- **Headings:** `Plus Jakarta Sans` (700/800) — modern, friendly.
- **Body:** `Inter` (400/500/600).
- **Numbers / IDs:** `JetBrains Mono` for complaint codes.
- Scale (rem): 0.75, 0.875, 1, 1.125, 1.25, 1.5, 2, 2.75, 3.75 (hero, `clamp()` for fluid size).

### 2.3 Spacing, radius, shadow, blur
```css
:root {
  --sp-1:4px; --sp-2:8px; --sp-3:12px; --sp-4:16px; --sp-5:24px; --sp-6:32px; --sp-7:48px; --sp-8:72px;
  --r-sm:10px; --r-md:16px; --r-lg:24px; --r-pill:999px;
  --shadow-1:0 2px 8px rgba(15,23,42,.06);
  --shadow-2:0 10px 30px rgba(15,23,42,.10);
  --shadow-3:0 24px 60px rgba(59,130,246,.22);
  --blur:saturate(160%) blur(18px);
}
```

### 2.4 Motion tokens
```css
:root {
  --ease-out:    cubic-bezier(.22,1,.36,1);     /* default entrance */
  --ease-spring: cubic-bezier(.34,1.56,.64,1);  /* playful pop */
  --ease-inout:  cubic-bezier(.65,0,.35,1);
  --t-fast:150ms; --t-base:300ms; --t-slow:600ms;
}
```

---

## 3. Layout and Navigation

- **Public pages:** floating glass navbar that shrinks and gains blur on scroll; animated underline on active link; theme toggle and language switch.
- **App pages (citizen/officer/admin):** collapsible glass **sidebar** (icon-only mode with tooltips) + top bar with search (Ctrl+K), notification bell, avatar menu.
- **Background:** fixed aurora gradient layer with 2–3 slowly drifting blurred blobs (CSS keyframes, 30–40 s loop) and a subtle noise overlay.
- **Grid:** Bootstrap 12-col, container max 1280 px, 16 px mobile gutter. Mobile: sidebar becomes a bottom tab bar.
- **Page transitions:** fade + 12 px slide-up on enter (`View Transitions API` where supported, CSS fallback).

---

## 4. Signature Interactions (what makes it unique)

### 4.1 Spotlight hover cards
Cards have a radial glow that follows the cursor and lights the border.

```css
.cp-card{
  position:relative; border-radius:var(--r-lg); padding:var(--sp-5);
  background:var(--cp-surface); backdrop-filter:var(--blur);
  border:1px solid var(--cp-border); box-shadow:var(--shadow-1);
  transition:transform var(--t-base) var(--ease-out), box-shadow var(--t-base) var(--ease-out);
  overflow:hidden;
}
.cp-card::before{ /* spotlight */
  content:""; position:absolute; inset:0; pointer-events:none; opacity:0;
  background:radial-gradient(420px circle at var(--mx,50%) var(--my,50%),rgba(59,130,246,.18),transparent 45%);
  transition:opacity var(--t-base);
}
.cp-card:hover{ transform:translateY(-6px); box-shadow:var(--shadow-3); }
.cp-card:hover::before{ opacity:1; }
```
```js
document.querySelectorAll('.cp-card').forEach(c=>{
  c.addEventListener('pointermove',e=>{
    const r=c.getBoundingClientRect();
    c.style.setProperty('--mx',`${e.clientX-r.left}px`);
    c.style.setProperty('--my',`${e.clientY-r.top}px`);
  });
});
```

### 4.2 3D tilt cards (feature cards, stat cards)
Use `vanilla-tilt` (max 8°, glare on, scale 1.02) or a 20-line custom `pointermove` handler setting `rotateX/rotateY`. Disable on touch and reduced-motion.

### 4.3 Magnetic buttons
Primary CTA buttons shift up to 8 px toward the cursor, spring back on leave. Ripple effect on click. Gradient background position slides on hover.

### 4.4 Scroll reveal and stagger
Elements with `data-reveal` fade/slide in using `IntersectionObserver`; children with `data-stagger` animate with 60 ms delay steps. (AOS or GSAP ScrollTrigger acceptable.)

### 4.5 Animated counters and charts
KPI numbers count up from 0 in 1.2 s on first view (ease-out). Chart.js charts animate in with `easing: 'easeOutQuart'`, 900 ms; legend hover highlights the matching slice.

### 4.6 Animated status timeline
Vertical timeline: a line draws downward as it enters the viewport, each node pops with spring easing, current step has a pulsing ring.

### 4.7 Kanban drag and drop
Cards lift (scale 1.04, shadow-3, slight rotate 1.5°) while dragged; target column highlights with dashed glow; drop settles with spring animation. (SortableJS.)

### 4.8 Hero section
Large gradient headline with animated gradient text, floating glass "live stats" cards that bob gently (parallax on mouse move), animated city/skyline SVG or Lottie, and a "Track your complaint" input with inline result preview.

### 4.9 Command palette (Ctrl+K)
Glass modal, fuzzy search over pages and the user's complaints, keyboard navigation, scale-in animation.

### 4.10 CivicBot widget
Floating orb bottom-right with idle pulse; opens as a glass chat panel (scale+fade from the orb), typing-dots animation, quick-reply chips, message bubbles slide in.

### 4.11 Micro-interactions list
- Input focus: label floats, border glows with brand gradient.
- Checkbox/toggle: spring animation; theme toggle morphs sun to moon.
- Success: confetti burst (canvas-confetti) after complaint submit and after feedback.
- Toasts slide in from top-right with progress bar.
- Skeleton shimmer while loading lists; empty states with soft floating illustration.
- File drop zone: border animates on drag-over, thumbnails pop in with a stagger.
- Status chips: subtle gradient + dot that pulses for IN_PROGRESS.
- Sidebar items: icon nudges, pill background slides to hovered item (shared-element feel).
- Table rows: hover raises row with soft background, action buttons fade in.
- Number badges on bell wiggle once when a new notification arrives.
- Custom cursor ring on desktop landing page (optional, toggle off in settings).
- Smooth scrolling (Lenis) on public pages only.

---

## 5. Component Library

| Component | Spec |
|---|---|
| **Button** | Primary (gradient), Secondary (glass), Ghost, Danger; sizes sm/md/lg; loading state with inline spinner; 44 px min touch height |
| **Input / Select / Textarea** | Floating label, 14 px radius, icon slot, validation shake animation on error |
| **Card** | Glass surface, spotlight hover, optional tilt; variants: stat, feature, complaint, department |
| **Stat card** | Icon in gradient circle, animated counter, delta chip (up/down), sparkline |
| **Complaint card** | Code (mono), title, category icon, status chip, priority flag, SLA ring, upvote button with pop animation, hover reveals quick actions |
| **Status chip** | Pill with dot, colours from status tokens |
| **SLA ring** | SVG circular progress: green over 50%, amber 20–50%, red under 20% / overdue pulsing |
| **Stepper (wizard)** | Animated progress line, step icons morph to check marks |
| **Timeline** | See 4.6 |
| **Modal / Drawer** | Backdrop blur, scale-in; drawer slides from right for complaint quick view |
| **Toast** | Success/info/warn/error, auto-dismiss 4 s, pause on hover |
| **Tabs** | Sliding underline indicator |
| **Table** | Sticky header, sortable, row hover, pagination, mobile card fallback |
| **Dropdown / Avatar menu** | Fade + translate 8 px |
| **Skeleton** | Shimmer gradient 1.4 s loop |
| **Badge / Tooltip / Progress / Rating stars** | Stars fill with spring pop on hover and select |
| **Map** | Leaflet, custom animated pin drop, cluster circles, heat layer |
| **Duplicate dialog** | Glass modal listing candidate complaint cards (code, title, distance, status, upvotes); primary "Support this instead", secondary "Submit anyway" that needs an extra confirm step; exact duplicates show an inline banner linking to the existing complaint |
| **Suggestion chip** | Small pill under the photo/description ("Looks like Roads · 82%") with Accept and Change actions; fades in, never auto-applies, hidden when there is no suggestion |
| **QR card** | Glass card with the QR image, complaint code (mono), Download PNG and Print buttons; print stylesheet shows only the QR and code |
| **Merged badge** | Status chip using `--cp-merged` with an arrow link to the parent complaint |
| **Consent checkbox** | Required checkbox with links to Terms and Privacy; shake and message when unticked on submit |
| **Footer** | Links to Privacy, Terms, Track and Transparency on every page; app pages use a slim footer |

---

## 6. Page Designs

### 6.1 Landing (`index.html`)
Sections: hero + live track box → animated stats strip (counters) → "How it works" 4-step horizontal cards (draw-line connector) → feature grid (tilt cards) → live transparency preview (mini chart + map) → department showcase carousel → testimonials/impact → FAQ accordion → CTA banner → footer.

### 6.2 Auth pages
Split layout: left animated aurora + illustration + rotating quotes; right glass form card. Password strength meter, show/hide toggle, role-aware redirect after login.

### 6.3 Citizen Dashboard
Greeting with time-based message, 4 stat cards, "Raise new complaint" big CTA card with glow, recent complaints list (complaint cards), activity feed, impact badges, announcements banner.

### 6.4 File Complaint wizard
Step 1 Details (title, description with live category suggestion chip, urgency slider with emoji) → Step 2 Location (map with draggable pin, address autofill, "similar complaints nearby" panel) → Step 3 Evidence (drag-drop zone, previews) → Step 4 Review and submit (summary card, anonymous toggle, submit with confetti and animated receipt card).

### 6.5 My Complaints
Search, filter chips, view switch (cards/table), infinite scroll or pagination, staggered card entrance.

### 6.6 Complaint Detail
Header with code, status chip, SLA ring; two columns: left timeline + comments chat; right info card (department, officer, location mini-map, attachments gallery with lightbox); sticky action bar (upvote, share, reopen, feedback). Feedback modal with animated star rating.

### 6.7 Officer Dashboard and Workspace
KPI strip, "Today's priorities" list sorted by priority score, Kanban board (toggle with table), workspace drawer with status dropdown, remark box, proof upload, internal notes, reassign dialog, SLA countdown.

### 6.8 Admin Dashboard
Date-range picker, animated KPI cards, line chart (trend), doughnut (categories), bar (departments), leaderboard with medal icons and animated bars, heatmap, hotspot prediction cards ("Likely spike: Water supply, Ward 7"), export buttons.

### 6.9 Admin management pages
Departments, categories, keyword rules (with live test box), users, SLA, announcements, audit logs, settings — consistent table + drawer-form pattern.

### 6.10 Transparency Dashboard (public)
Big counters, resolution rate ring, department leaderboard, anonymized heatmap, "resolved this week" ticker marquee.

### 6.11 Track Complaint (public)
Large input, result card with animated timeline, empty/error states with illustrations. Opening `track.html?code=CP-…` (for example from a scanned QR) prefills the input and loads the result automatically; the QR card sits beside the timeline.

### 6.12 Duplicate prevention flow (inside the wizard)
When the server answers 409 `DUPLICATE_SUSPECTED`, a glass dialog slides up listing candidate complaints as compact complaint cards with a mini-map pin and distance. Primary action "Support this instead" (heart/pop animation, then toast and redirect to that complaint); secondary "This is different, submit anyway" opens a one-line confirmation before resending with `confirmNotDuplicate=true`. For 409 `DUPLICATE_EXACT` show an inline banner with a link to the existing complaint. A 429 shows a friendly "Please wait" message with the retry time. Files: `assets/js/modules/duplicates.js`.

### 6.13 Photo suggestion (wizard step 3)
After the first image is added, show a shimmering placeholder chip, then "Looks like Roads · 82%" with Accept / Change. If text and photo suggestions disagree, show both as selectable chips. No chip appears when no suggestion is available, and nothing blocks the next step. Files: `assets/js/modules/image-suggest.js`.

### 6.14 QR card
Shown on the submit-success screen next to the receipt, on the complaint detail sidebar and on the track result. Download PNG and Print buttons; print view contains only the QR, code and tracking URL. Files: `assets/js/modules/qr.js`.

### 6.15 Privacy, Terms and consent
`privacy.html` and `terms.html` use the public layout with a sticky table of contents, readable line length and both themes. Register page shows the consent checkbox above the submit button. Footer on every page links to both.

---

## 7. Responsive Rules
Breakpoints: 576 / 768 / 992 / 1200 / 1400. Mobile-first. Sidebar → bottom nav under 992 px. Tables → stacked cards under 768 px. Touch targets 44 px. Disable tilt/magnetic/cursor effects on touch devices. Hero floating cards hidden under 576 px.

---

## 8. Accessibility
- Colour contrast AA minimum (check glass surfaces against both themes).
- Visible focus ring: 3 px brand outline with offset, never removed.
- Semantic landmarks, ARIA labels on icon buttons, `aria-live` on toasts and bot messages.
- Keyboard: all cards, tabs, modals, drag-drop alternatives (move-to menu on Kanban cards).
- `@media (prefers-reduced-motion: reduce)` disables parallax, tilt, confetti, blob drift; keeps simple fades.
- Status is never colour-only: icon + text label included.

```css
@media (prefers-reduced-motion: reduce){
  *,*::before,*::after{animation-duration:.01ms!important;animation-iteration-count:1!important;transition-duration:.01ms!important;scroll-behavior:auto!important}
}
```

---

## 9. Iconography and Illustration
Bootstrap Icons or Lucide (consistent 1.5 px stroke). Category icons: road, trash, water drop, bulb, tree, building, shield, bus, etc. Illustrations: unDraw/Storyset in brand colours, or simple inline SVG. Lottie for success check, empty box, loading.

---

## 10. Frontend File Structure

```
src/main/resources/static/
├── index.html, login.html, register.html, track.html, transparency.html, privacy.html, terms.html
├── citizen/   dashboard.html, new-complaint.html, my-complaints.html, complaint.html, profile.html
├── officer/   dashboard.html, queue.html, board.html, workspace.html
├── admin/     dashboard.html, departments.html, categories.html, rules.html, users.html, sla.html, audit.html, settings.html
└── assets/
    ├── css/   tokens.css, base.css, components.css, animations.css, pages/*.css
    ├── js/
    │   ├── core/    api.js, auth.js, router-guard.js, theme.js, i18n.js, toast.js, utils.js
    │   ├── ui/      spotlight.js, tilt.js, magnetic.js, reveal.js, counter.js, palette.js, bot.js
    │   ├── modules/ complaints.js, officer.js, admin.js, charts.js, map.js, notifications.js
    │   └── pages/   (one file per page)
    ├── i18n/  en.json, kn.json, hi.json
    └── img/, lottie/
```

---

## 11. Libraries (CDN or npm-free)
Bootstrap 5.3, Bootstrap Icons, Chart.js 4, Leaflet + leaflet.heat + markercluster, SortableJS, GSAP (+ScrollTrigger) or AOS, Lenis, vanilla-tilt, canvas-confetti, Lottie-web, Flatpickr (date range), jsPDF (client receipts, optional). Self-host fonts from Google Fonts.

---

## 12. Performance Budget
- JS under 250 KB gzipped total for any single page; defer non-critical scripts.
- Images WebP, lazy loaded; hero illustration under 150 KB.
- Initialize heavy effects (map, charts, tilt) only when their section enters the viewport.
- Run Lighthouse before each release; fix regressions.

---

## 13. Design QA Checklist
- [ ] Every interactive element has hover, focus, active, disabled states
- [ ] Light and dark theme verified on all pages
- [ ] Reduced-motion mode verified
- [ ] 360 px, 768 px, 1440 px layouts verified
- [ ] No layout shift when skeletons swap to content
- [ ] Empty, loading, error states exist for every list and chart
- [ ] Status colours and chips consistent across pages
