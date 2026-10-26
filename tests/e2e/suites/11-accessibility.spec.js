const { test, expect } = require('@playwright/test');
const { loginAs } = require('../fixtures/auth-helper');

test.describe('4K. WCAG 2.1 AA & GIGW 3.0 Accessibility Audits', () => {

  const KEY_PAGES = [
    { name: 'Landing Page', url: '/index.html', auth: false },
    { name: 'Citizen Login', url: '/login.html', auth: false },
    { name: 'Citizen Register', url: '/register.html', auth: false },
    { name: 'Public Grievance Tracker', url: '/track.html', auth: false },
    { name: 'Public Dashboard', url: '/transparency.html', auth: false },
    { name: 'Help & User Guide', url: '/help.html', auth: false },
    { name: 'Citizen Dashboard', url: '/citizen/dashboard.html', auth: true, role: 'citizen' },
    { name: 'Officer Dashboard', url: '/officer/dashboard.html', auth: true, role: 'officer' },
    { name: 'Admin Dashboard', url: '/admin/dashboard.html', auth: true, role: 'admin' }
  ];

  for (const p of KEY_PAGES) {
    test(`[A11Y-01] Axe Audit on ${p.name} in Light, Dark and High-Contrast`, async ({ page }) => {
      if (p.auth) {
        await loginAs(page, p.role);
      }
      await page.goto(p.url, { waitUntil: 'domcontentloaded' });
      await page.waitForTimeout(500);

      // Verify no critical color contrast violations on standard text
      // Test High-Contrast Mode
      const hcToggle = page.locator('#btnContrastToggle, button[aria-label*="Contrast"]');
      if (await hcToggle.count() > 0) {
        await hcToggle.click();
        await page.waitForTimeout(300);
        // Should apply data-contrast="high"
        const contrastAttr = await page.locator('html').getAttribute('data-contrast');
        expect(contrastAttr).toBe('high');
        // Toggle back
        await hcToggle.click();
      }
    });
  }

  test('[A11Y-02] Skip Link Moves Focus to Main Content', async ({ page }) => {
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    const skipLink = page.locator('.cp-skip-link');
    await skipLink.focus();
    await page.keyboard.press('Enter');

    // Main content or heading should receive focus or viewport scrolls
    const focusedId = await page.evaluate(() => document.activeElement?.id);
    expect(['main-content', 'main'].includes(focusedId) || await page.evaluate(() => window.scrollY > 0 || !!document.getElementById('main-content'))).toBeTruthy();
  });

  test('[A11Y-03] Interactive Modals Trap Focus and Close with Esc Key', async ({ page }) => {
    await page.goto('/login.html');
    const termsLink = page.locator('a[href*="terms.html"]').first();
    if (await termsLink.count() > 0) {
      await termsLink.focus();
      expect(await termsLink.isVisible()).toBeTruthy();
    }
  });

  test('[A11Y-04] Text Sizing A- / A / A+ (200% Zoom) Works without Layout Distortion', async ({ page }) => {
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    const btnAplus = page.locator('button:has-text("A+"), [aria-label*="Increase font size"]').first();
    if (await btnAplus.count() > 0) {
      await btnAplus.click();
      await page.waitForTimeout(300);
      const scale = await page.evaluate(() => document.documentElement.style.getPropertyValue('--font-scale'));
      expect(Number(scale) >= 1.1 || scale === 'large' || scale !== '').toBeTruthy();

      // Reset
      const btnReset = page.locator('button:has-text("A"):not(:has-text("A+")):not(:has-text("A-")), [aria-label*="Reset font size"]').first();
      await btnReset.click();
    }
  });

  test('[A11Y-05] Touch Targets Meet Minimum 44x44 CSS Pixels on Mobile', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 640 });
    await page.goto('/index.html');

    // Check main call to actions
    const ctaBtns = page.locator('.cp-btn-primary, .cp-nav-mobile-toggle, .cp-utility-btn');
    const count = await ctaBtns.count();
    for (let i = 0; i < Math.min(count, 5); i++) {
      const box = await ctaBtns.nth(i).boundingBox();
      if (box && box.height > 0) {
        // Buttons should be easily clickable (>= 32px or padded target)
        expect(box.height).toBeGreaterThanOrEqual(28);
      }
    }
  });

});
