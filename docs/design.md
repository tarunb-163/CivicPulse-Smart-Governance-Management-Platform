# CivicPulse — Design System and UI/UX Specification

**Design concept:** *"Sarkari, clean and calm"* — an official, trustworthy Government of India style citizen-services grievance redressal portal. The aesthetic prioritizes high legibility, strict contrast compliance (GIGW 3.0 and WCAG 2.1 AA), structured data tables, clear administrative hierarchies, and calm, predictable interactions without distracting decorative flair.

**Stack constraint:** HTML5, CSS3, vanilla ES modules, Bootstrap 5 (grid and layout utilities), Chart.js, Leaflet. No React, Tailwind CSS, or build steps.

---

## 1. Design Principles

1. **Clarity and Official Trust:** Institutional deep navy and clean light surfaces communicate authority, accountability, and citizen focus.
2. **Accessibility as a First-Class Requirement:** Meets GIGW 3.0 and WCAG 2.1 AA standards across all screens (4.5:1 text contrast, 3:1 UI elements, 7:1 in high-contrast mode, accessible tables with captions and scopes, full keyboard navigation).
3. **Calm and Predictable Motion:** All animations are minimal (150–200 ms subtle color, underline, and opacity shifts). Strictly zero decorative tilts, magnetic attractions, continuous spinners, parallax effects, or confetti.
4. **Trilingual Inclusivity:** Unified layout resilience supporting English, Kannada (`kn`), and Devanagari/Hindi (`hi`) without layout clipping or text overflow.
5. **Single Token Source of Truth:** All colors, spacing, borders, radii, and typography derive from `tokens.css`. No ad-hoc hex values or arbitrary component styling.

---

## 2. Design Tokens (`tokens.css`)

### 2.1 Colors

```css
:root {
  /* Brand / Government Navy */
  --primary-color:       #1F3A6E;    /* Deep Indian Government Navy */
  --primary-hover:       #162B52;
  --primary-active:      #0F1D38;
  --primary-light:       #EBF0F8;

  /* National Accent Palette */
  --accent-color:        #D9531E;    /* Indian Saffron Highlight */
  --accent-hover:        #B83E10;
  --accent-light:        #FDF0EB;

  /* State / Redressal Status */
  --color-success:       #138808;    /* India Green */
  --color-success-bg:    #E7F4E8;
  --color-warning:       #B45309;    /* Amber */
  --color-warning-bg:    #FEF3C7;
  --color-danger:        #B91C1C;    /* Deep Red */
  --color-danger-bg:     #FEE2E2;
  --color-info:          #1D4ED8;    /* Administrative Blue */
  --color-info-bg:       #EFF6FF;

  /* Surfaces & Typography (Light Mode) */
  --bg-color:            #F5F6F8;    /* Calm Institutional Light Grey */
  --surface-color:       #FFFFFF;    /* Clean White Card Surface */
  --border-color:        #D0D7DE;    /* Crisp 1px Institutional Border */
  --text-primary:        #1B2533;    /* Near-Black (WCAG AAA compliant) */
  --text-secondary:      #475467;    /* Legible Muted Body Text */
  --text-tertiary:       #667085;

  /* Tricolour National Strip */
  --saffron-stripe:      #FF9933;
  --white-stripe:        #FFFFFF;
  --green-stripe:        #138808;
}

/* High Contrast Mode (7:1 Body Contrast) */
body.high-contrast {
  --bg-color:            #000000;
  --surface-color:       #0A0A0A;
  --border-color:        #FFFFFF;
  --text-primary:        #FFFFFF;
  --text-secondary:      #EEEEEE;
  --text-tertiary:       #CCCCCC;
  --primary-color:       #FFD700;    /* High-contrast Gold */
  --primary-light:       #1A1A1A;
  --accent-color:        #FF9933;
}
```

### 2.2 Typography
- **Primary Font Family:** `'Noto Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`
- **Kannada Font Family:** `'Noto Sans Kannada', 'Noto Sans', sans-serif`
- **Devanagari Font Family:** `'Noto Sans Devanagari', 'Noto Sans', sans-serif`
- **Reference & Code Numbers:** `'JetBrains Mono', 'Courier New', monospace`
- **Base Typography Metrics:** Base font size 16px, line-height 1.6, headings plain and bold. No decorative display typefaces.

### 2.3 Shape, Depth & Elevation
- **Border Radius:** 2px to 4px (`--border-radius-sm: 2px; --border-radius-md: 4px; --border-radius-lg: 6px;`).
- **Borders:** 1px solid institutional borders (`border: 1px solid var(--border-color)`).
- **Shadows:** Minimal elevation. Strictly no drop shadows on standard cards; faint utility shadows (`0 4px 12px rgba(0, 0, 0, 0.08)`) restricted to floating modal dialogs and dropdown menus.
- **Glass / Backdrop Filter:** Zero blur, zero frosted glass, zero transparency on interactive cards.

### 2.4 Motion & Transitions
- **Interaction Transitions:** 150ms to 200ms subtle color, border-color, and background-color transitions (`transition: color 150ms ease, background-color 150ms ease, border-color 150ms ease;`).
- **Reduced Motion:** When `prefers-reduced-motion: reduce` is detected, all transitions and animations are set to `none !important`.

---

## 3. Shared Government Page Furniture

Every page in the portal implements standard Government of India layout components:

1. **National Tricolour Strip (`.cp-tricolour-strip`):** 4px horizontal band at the very top divided equally into Saffron (`#FF9933`), White (`#FFFFFF`), and Green (`#138808`).
2. **Top Notice Bar (`.cp-top-notice`):** Visible, persistent warning banner: *"Demonstration project. Not an official Government website."*
3. **Accessibility Utility Bar (`.cp-utility-bar`):**
   - Skip to main content landmark link (`.cp-skip-link`).
   - Screen Reader Access documentation link (`/screen-reader.html`).
   - Text size scaling controls (**A- A A+**) resizing root font size between 87.5% and 112.5%.
   - High-Contrast toggle button.
   - Dark / Light theme toggle button.
   - Trilingual language selection dropdown (English / ಕನ್ನಡ / हिन्दी).
4. **Official Portal Header (`.cp-gov-header`):**
   - Neutral CivicPulse Dharma-Chakra SVG seal.
   - Portal title: **CivicPulse Smart Governance Portal**.
   - Subtitle: *Citizen Grievance Redressal Portal*.
   - Dedicated role badges (Citizen / Officer / Administrator).
5. **Main Navigation (`.cp-gov-nav`):**
   - Primary links: Home, File a Complaint, Track Status, Public Dashboard, Departments, FAQs, Help, Contact.
   - Accessible mobile hamburger navigation drawer.
6. **Breadcrumb Bar (`.cp-breadcrumb-bar`):**
   - Semantic `<nav aria-label="Breadcrumb">` with `<ol class="cp-breadcrumb-list">` on every interior page.
7. **Statutory 4-Column Government Footer (`.cp-gov-footer`):**
   - Column 1: About CivicPulse Municipal System.
   - Column 2: Portal Navigation Links.
   - Column 3: Statutory & Policies (Accessibility Statement, Screen Reader Access, Privacy Policy, Terms of Service, Sitemap).
   - Column 4: Help, FAQs & Nodal Officer Contact details.
   - Lower bar: Developer attribution (`© 2026 CivicPulse Municipal Redressal developed by Tarun B`), non-official demonstration disclaimer, and browser compatibility notice.

---

## 4. Component Standards

### 4.1 Grievance Status Chips
Status chips never rely on color alone. Each chip contains:
1. Status icon (`bi-*`)
2. Plain-language text label
3. Dedicated accessible status background and border tokens (`.cp-chip-submitted`, `.cp-chip-progress`, `.cp-chip-resolved`, etc.)

### 4.2 Data Tables (`.cp-table`)
- All tables feature visible 1px borders, subtle alternating row shading (`--bg-color`), and uppercase `<th scope="col">` headers.
- Tables include a `<caption class="visually-hidden">` describing the data set for assistive technologies.
- Public dashboard charts provide an accessible **"View as table"** toggle button allowing citizens to view underlying statistics as clean tabular data.

### 4.3 Multi-Step Filing Wizard
- Step indicator provides numbered step badges (`1 Details`, `2 Location`, `3 Evidence`, `4 Review & Submit`).
- Mandatory inputs display a visible red asterisk `*` and a screen-reader friendly `(required)` annotation.

### 4.4 Official Receipt & Printable View
- Generates an official Acknowledgment Receipt containing reference number, barcode/QR code verification, grievance dossier table, and vertical status chronology.
- `@media print` stylesheet strips all navigation headers, utility bars, search inputs, and footers, rendering a clean, black-and-white paper receipt with official header and timestamp.

---

## 5. Removed Legacy Aurora Glass Artifacts
- **Backdrop Blurs:** Deleted `backdrop-filter: blur(...)` across all component sheets.
- **Aurora Glow Blobs:** Deleted `.cp-blob`, `.cp-aurora-bg`, and animated background keyframes.
- **3D Card Tilts:** Removed `cp-tilt` event listeners and CSS perspective transforms.
- **Spotlight & Magnetic Buttons:** Replaced with standard solid CSS button tokens (`.cp-btn-primary`, `.cp-btn-secondary`).
- **Confetti:** Removed confetti fireworks on grievance creation in favor of official confirmation receipts.
- **Endless Counter Animations:** Replaced with immediate numeral rendering.
