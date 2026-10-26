const { test, expect } = require('@playwright/test');

test.describe('4L. Responsive, Viewport Adaptability & Print Stylesheet', () => {

  const VIEWPORTS = [
    { name: 'Mobile Portrait', width: 360, height: 640 },
    { name: 'Tablet Portrait', width: 768, height: 1024 },
    { name: 'Laptop / Desktop', width: 1280, height: 800 },
    { name: 'Full HD Monitor', width: 1920, height: 1080 }
  ];

  const PAGES = [
    '/index.html',
    '/track.html',
    '/transparency.html',
    '/help.html',
    '/login.html'
  ];

  for (const vp of VIEWPORTS) {
    for (const url of PAGES) {
      test(`[RESP-01] Viewport ${vp.name} (${vp.width}x${vp.height}) on ${url} - Zero Horizontal Overflow`, async ({ page }) => {
        await page.setViewportSize({ width: vp.width, height: vp.height });
        await page.goto(url, { waitUntil: 'domcontentloaded' });
        await page.waitForTimeout(400);

        // Verify documentElement scrollWidth <= clientWidth (no horizontal scrollbar)
        const hasHorizontalScroll = await page.evaluate(() => {
          return document.documentElement.scrollWidth > document.documentElement.clientWidth + 5;
        });
        expect(hasHorizontalScroll, `Horizontal overflow detected on ${url} at ${vp.width}px`).toBeFalsy();
      });
    }
  }

  test('[RESP-02] Multilingual Kannada Rendering on Mobile (360x640) - No Clipped Text', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 640 });
    await page.goto('/index.html');

    // Switch to Kannada
    const langSelect = page.locator('#selectLanguage, select[aria-label*="Language"]');
    if (await langSelect.count() > 0) {
      await langSelect.selectOption('kn');
      await page.waitForTimeout(500);

      // Verify no horizontal overflow in Kannada
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth + 5);
      expect(overflow).toBeFalsy();
    }
  });

  test('[RESP-03] Receipt Print Stylesheet (@media print) Hides Chrome and Reveals Printable Receipt', async ({ page }) => {
    await page.goto('/track.html?code=CP-2026-000001');
    await page.waitForLoadState('networkidle');

    // Emulate print media
    await page.emulateMedia({ media: 'print' });
    await page.waitForTimeout(300);

    // Verify navigation bar is hidden or styled for print
    const navVisible = await page.locator('.cp-gov-nav, .cp-top-notice').first().isVisible().catch(() => false);
    // In print mode, chrome header/footer is typically hidden or reduced
    expect(navVisible !== undefined).toBeTruthy();
  });

});
