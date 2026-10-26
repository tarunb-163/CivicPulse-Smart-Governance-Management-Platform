const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';
const OUTPUT_DIR = path.resolve(__dirname, '../docs/screenshots/govt-ui');
const AXE_PATH = path.resolve(__dirname, '../node_modules/axe-core/axe.min.js');

if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

const axeScript = fs.readFileSync(AXE_PATH, 'utf8');

const auditReport = {
  timestamp: new Date().toISOString(),
  pagesTested: 0,
  axeScores: {},
  overflowChecks: [],
  screenshotsCaptured: []
};

async function runAxe(page, pageName) {
  try {
    await page.evaluate(axeScript);
    const results = await page.evaluate(async () => {
      // Exclude third party elements if any (e.g. leaflet attribution or external tiles)
      return await window.axe.run(document, {
        runOnly: {
          type: 'tag',
          values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'best-practice']
        }
      });
    });
    const critical = results.violations.filter(v => v.impact === 'critical');
    const serious = results.violations.filter(v => v.impact === 'serious');
    const moderate = results.violations.filter(v => v.impact === 'moderate');
    const minor = results.violations.filter(v => v.impact === 'minor');

    auditReport.axeScores[pageName] = {
      criticalCount: critical.length,
      seriousCount: serious.length,
      moderateCount: moderate.length,
      minorCount: minor.length,
      passes: results.passes.length,
      violations: results.violations.map(v => ({ id: v.id, impact: v.impact, description: v.description, nodes: v.nodes.length }))
    };

    console.log(`  [Axe-Core] ${pageName}: ${critical.length} Critical, ${serious.length} Serious, ${moderate.length} Moderate, ${minor.length} Minor, ${results.passes.length} Passes`);
    return { critical, serious, moderate, minor };
  } catch (err) {
    console.error(`  [Axe-Core Error] ${pageName}:`, err.message);
    auditReport.axeScores[pageName] = { error: err.message };
    return null;
  }
}

async function checkOverflow(page, label) {
  const info = await page.evaluate(() => {
    return {
      scrollWidth: document.documentElement.scrollWidth,
      innerWidth: window.innerWidth,
      hasOverflow: document.documentElement.scrollWidth > window.innerWidth
    };
  });
  auditReport.overflowChecks.push({ label, ...info });
  if (info.hasOverflow) {
    console.warn(`  [OVERFLOW WARNING] ${label}: scrollWidth (${info.scrollWidth}) > innerWidth (${info.innerWidth})`);
  } else {
    console.log(`  [OK NO-OVERFLOW] ${label}: ${info.scrollWidth} <= ${info.innerWidth}`);
  }
  return !info.hasOverflow;
}

async function captureEvidence() {
  console.log('======================================================================');
  console.log('CivicPulse Government Portal UI Evidence Capture & A11y Audit');
  console.log(`Target: ${BASE_URL}`);
  console.log(`Output: ${OUTPUT_DIR}`);
  console.log('======================================================================\n');

  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    headless: true
  });

  const context = await browser.newContext({
    viewport: { width: 1280, height: 800 }
  });
  const page = await context.newPage();

  // 1. PUBLIC PAGES
  const publicPages = [
    { url: '/index.html', name: 'home' },
    { url: '/login.html', name: 'login' },
    { url: '/register.html', name: 'register' },
    { url: '/track.html?code=CP-2026-000001', name: 'track-receipt' },
    { url: '/transparency.html', name: 'transparency' },
    { url: '/privacy.html', name: 'privacy' },
    { url: '/terms.html', name: 'terms' },
    { url: '/accessibility.html', name: 'accessibility' },
    { url: '/screen-reader.html', name: 'screen-reader' },
    { url: '/sitemap.html', name: 'sitemap' },
    { url: '/help.html', name: 'help' },
    { url: '/contact.html', name: 'contact' },
    { url: '/404.html', name: '404' }
  ];

  console.log('\n--- 1. Auditing & Capturing Public Pages ---');
  for (const p of publicPages) {
    await page.goto(`${BASE_URL}${p.url}`, { waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(600);
    auditReport.pagesTested++;

    // 1280px Desktop Light
    const img1280Light = `${p.name}-1280-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, img1280Light), fullPage: false });
    auditReport.screenshotsCaptured.push(img1280Light);

    // Run Axe on primary public pages
    if (['home', 'login', 'register', 'track-receipt', 'accessibility'].includes(p.name)) {
      await runAxe(page, p.name);
    }

    // High Contrast Mode at 1280px
    await page.evaluate(() => {
      document.documentElement.setAttribute('data-contrast', 'high');
      localStorage.setItem('contrast', 'high');
    });
    await page.waitForTimeout(200);
    const img1280HC = `${p.name}-1280-highcontrast.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, img1280HC), fullPage: false });
    auditReport.screenshotsCaptured.push(img1280HC);

    // Reset contrast
    await page.evaluate(() => {
      document.documentElement.removeAttribute('data-contrast');
      localStorage.removeItem('contrast');
    });

    // Mobile 360px Light
    await page.setViewportSize({ width: 360, height: 740 });
    await page.waitForTimeout(200);
    const img360Light = `${p.name}-360-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, img360Light), fullPage: false });
    auditReport.screenshotsCaptured.push(img360Light);
    await checkOverflow(page, `${p.name} (360px Mobile)`);

    // Reset viewport to 1280px
    await page.setViewportSize({ width: 1280, height: 800 });
  }

  // 2. MULTILINGUAL CHECKS (Kannada & Hindi)
  console.log('\n--- 2. Auditing Multilingual Rendering (Kannada & Hindi) ---');
  for (const lang of ['kn', 'hi']) {
    for (const p of [{ url: '/index.html', name: 'home' }, { url: '/track.html?code=CP-2026-000001', name: 'track-receipt' }, { url: '/login.html', name: 'login' }]) {
      await page.goto(`${BASE_URL}${p.url}`, { waitUntil: 'domcontentloaded' });
      await page.evaluate(async (l) => {
        localStorage.setItem('lang', l);
        document.documentElement.setAttribute('lang', l);
        // Dispatch custom or call i18n
        if (window.applyTranslations) await window.applyTranslations(l);
      }, lang);
      await page.waitForTimeout(600);

      // 1280px
      const imgDesktopLang = `${p.name}-1280-${lang}.png`;
      await page.screenshot({ path: path.join(OUTPUT_DIR, imgDesktopLang), fullPage: false });
      auditReport.screenshotsCaptured.push(imgDesktopLang);

      // 360px mobile
      await page.setViewportSize({ width: 360, height: 740 });
      await page.waitForTimeout(300);
      const imgMobileLang = `${p.name}-360-${lang}.png`;
      await page.screenshot({ path: path.join(OUTPUT_DIR, imgMobileLang), fullPage: false });
      auditReport.screenshotsCaptured.push(imgMobileLang);
      await checkOverflow(page, `${p.name} (360px Mobile - ${lang.toUpperCase()})`);

      // Reset
      await page.setViewportSize({ width: 1280, height: 800 });
    }
  }

  // Reset language
  await page.evaluate(() => {
    localStorage.setItem('lang', 'en');
    document.documentElement.setAttribute('lang', 'en');
  });

  // 3. CITIZEN PORTAL (Logged in)
  console.log('\n--- 3. Auditing Citizen Portal ---');
  await page.evaluate(() => localStorage.clear());
  await page.goto(`${BASE_URL}/login.html`, { waitUntil: 'domcontentloaded' });
  await page.fill('#email', 'citizen@civicpulse.test');
  await page.fill('#password', 'Password@123');
  await page.click('button[type="submit"]');
  try {
    await page.waitForURL('**/citizen/**', { timeout: 6000 });
  } catch (e) {
    await page.waitForTimeout(2000);
  }

  const citizenPages = [
    { url: '/citizen/dashboard.html', name: 'citizen-dashboard' },
    { url: '/citizen/new-complaint.html', name: 'citizen-new-complaint' },
    { url: '/citizen/my-complaints.html', name: 'citizen-my-complaints' },
    { url: '/citizen/profile.html', name: 'citizen-profile' }
  ];

  for (const cp of citizenPages) {
    await page.goto(`${BASE_URL}${cp.url}`, { waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(800);
    auditReport.pagesTested++;

    const imgDesktop = `${cp.name}-1280-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, imgDesktop), fullPage: false });
    auditReport.screenshotsCaptured.push(imgDesktop);

    if (cp.name === 'citizen-new-complaint') {
      await runAxe(page, cp.name);
    }

    // High Contrast
    await page.evaluate(() => document.documentElement.setAttribute('data-contrast', 'high'));
    await page.waitForTimeout(200);
    const imgHC = `${cp.name}-1280-highcontrast.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, imgHC), fullPage: false });
    auditReport.screenshotsCaptured.push(imgHC);
    await page.evaluate(() => document.documentElement.removeAttribute('data-contrast'));

    // Mobile 360px
    await page.setViewportSize({ width: 360, height: 740 });
    await page.waitForTimeout(200);
    const imgMobile = `${cp.name}-360-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, imgMobile), fullPage: false });
    auditReport.screenshotsCaptured.push(imgMobile);
    await checkOverflow(page, `${cp.name} (360px)`);

    await page.setViewportSize({ width: 1280, height: 800 });
  }

  // 4. OFFICER PORTAL
  console.log('\n--- 4. Auditing Officer Portal ---');
  await page.evaluate(() => localStorage.clear());
  await page.goto(`${BASE_URL}/login.html`, { waitUntil: 'domcontentloaded' });
  await page.fill('#email', 'officer.roads@civicpulse.test');
  await page.fill('#password', 'Password@123');
  await page.click('button[type="submit"]');
  try {
    await page.waitForURL('**/officer/**', { timeout: 6000 });
  } catch (e) {
    await page.waitForTimeout(2000);
  }

  const officerPages = [
    { url: '/officer/dashboard.html', name: 'officer-dashboard' },
    { url: '/officer/queue.html', name: 'officer-queue' },
    { url: '/officer/board.html', name: 'officer-kanban-board' }
  ];

  for (const op of officerPages) {
    await page.goto(`${BASE_URL}${op.url}`, { waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(800);
    auditReport.pagesTested++;

    const imgDesktop = `${op.name}-1280-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, imgDesktop), fullPage: false });
    auditReport.screenshotsCaptured.push(imgDesktop);

    // Mobile 360px
    await page.setViewportSize({ width: 360, height: 740 });
    await page.waitForTimeout(200);
    const imgMobile = `${op.name}-360-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, imgMobile), fullPage: false });
    auditReport.screenshotsCaptured.push(imgMobile);
    await checkOverflow(page, `${op.name} (360px)`);

    await page.setViewportSize({ width: 1280, height: 800 });
  }

  // 5. ADMIN CONTROL CENTER
  console.log('\n--- 5. Auditing Admin Control Center ---');
  await page.evaluate(() => localStorage.clear());
  await page.goto(`${BASE_URL}/login.html`, { waitUntil: 'domcontentloaded' });
  await page.fill('#email', 'admin@civicpulse.test');
  await page.fill('#password', 'Password@123');
  await page.click('button[type="submit"]');
  try {
    await page.waitForURL('**/admin/**', { timeout: 6000 });
  } catch (e) {
    await page.waitForTimeout(2000);
  }

  const adminPages = [
    { url: '/admin/dashboard.html', name: 'admin-dashboard' },
    { url: '/admin/departments.html', name: 'admin-departments' },
    { url: '/admin/categories.html', name: 'admin-categories' },
    { url: '/admin/rules.html', name: 'admin-rules' },
    { url: '/admin/users.html', name: 'admin-users' },
    { url: '/admin/sla.html', name: 'admin-sla' },
    { url: '/admin/audit.html', name: 'admin-audit' },
    { url: '/admin/settings.html', name: 'admin-settings' }
  ];

  for (const ap of adminPages) {
    await page.goto(`${BASE_URL}${ap.url}`, { waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(800);
    auditReport.pagesTested++;

    const imgDesktop = `${ap.name}-1280-light.png`;
    await page.screenshot({ path: path.join(OUTPUT_DIR, imgDesktop), fullPage: false });
    auditReport.screenshotsCaptured.push(imgDesktop);

    if (ap.name === 'admin-dashboard') {
      await runAxe(page, ap.name);
    }
  }

  // 6. PRINT PREVIEW RECEIPT VERIFICATION
  console.log('\n--- 6. Verifying Print Preview for Official Receipt ---');
  await page.goto(`${BASE_URL}/track.html?code=CP-2026-000001`, { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(800);
  await page.emulateMedia({ media: 'print' });
  const printImg = 'official-receipt-print-preview.png';
  await page.screenshot({ path: path.join(OUTPUT_DIR, printImg), fullPage: true });
  auditReport.screenshotsCaptured.push(printImg);
  console.log(`  [Print Receipt Captured] ${printImg}`);
  await page.emulateMedia({ media: 'screen' });

  // 7. CIVICBOT ASSISTANT
  console.log('\n--- 7. CivicBot Floating Assistant Verification ---');
  await page.goto(`${BASE_URL}/index.html`, { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(500);
  const botBtn = await page.$('#botTrigger, .bot-trigger, [data-bot-trigger]');
  if (botBtn) {
    await botBtn.click();
    await page.waitForTimeout(500);
    const botImg = 'civicbot-drawer-opened.png';
    await page.screenshot({ path: path.join(OUTPUT_DIR, botImg), fullPage: false });
    auditReport.screenshotsCaptured.push(botImg);
  }

  await browser.close();

  // Save audit data
  const dataPath = path.join(OUTPUT_DIR, 'audit-data.json');
  fs.writeFileSync(dataPath, JSON.stringify(auditReport, null, 2), 'utf8');

  console.log('\n======================================================================');
  console.log(`EVIDENCE CAPTURE COMPLETE`);
  console.log(`Total Pages Tested: ${auditReport.pagesTested}`);
  console.log(`Screenshots Captured: ${auditReport.screenshotsCaptured.length}`);
  console.log(`Overflow Checks Passed: ${auditReport.overflowChecks.filter(c => !c.hasOverflow).length}/${auditReport.overflowChecks.length}`);
  console.log(`Report JSON: ${dataPath}`);
  console.log('======================================================================\n');
}

captureEvidence().catch(err => {
  console.error('Evidence Capture Failed:', err);
  process.exit(1);
});
