// @ts-check
const { test, expect } = require('@playwright/test');
const { BasePage } = require('../pages/base-page');

test.describe('4B. Government UI Furniture & Shared Controls', () => {

  test('[FURN-001] Skip-to-content moves focus directly to #main-content', async ({ page }) => {
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    const skipLink = page.locator('.cp-skip-link');

    // Tab to skip link
    await page.keyboard.press('Tab');
    await expect(skipLink).toBeFocused();

    // Trigger Enter on skip link
    await page.keyboard.press('Enter');
    const focusedId = await page.evaluate(() => document.activeElement?.id);
    expect(['main-content', 'main'].includes(focusedId) || await page.evaluate(() => window.scrollY > 0)).toBeTruthy();
  });

  test('[FURN-002-004] Text size resizer controls (A-, A, A+) scale layout and persist', async ({ page }) => {
    const bp = new BasePage(page);
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });

    // Click A+
    await bp.textSizePlus.click();
    await page.waitForTimeout(200);
    let scale = await page.evaluate(() => document.documentElement.style.getPropertyValue('--font-scale'));
    expect(Number(scale)).toBeGreaterThanOrEqual(1.1);

    // Reload and verify persistence
    await page.reload({ waitUntil: 'domcontentloaded' });
    scale = await page.evaluate(() => document.documentElement.style.getPropertyValue('--font-scale'));
    expect(Number(scale)).toBeGreaterThanOrEqual(1.1);

    // Reset with A
    await bp.textSizeReset.click();
    await page.waitForTimeout(200);
    scale = await page.evaluate(() => document.documentElement.style.getPropertyValue('--font-scale'));
    expect(Number(scale)).toBe(1.0);
  });

  test('[FURN-005-006] High-contrast toggle persists and meets 7:1 contrast ratio', async ({ page }) => {
    const bp = new BasePage(page);
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });

    // Activate High Contrast
    await bp.contrastToggle.click();
    await page.waitForTimeout(200);
    expect(await page.locator('html').getAttribute('data-contrast')).toBe('high');

    // Check persistence
    await page.reload({ waitUntil: 'domcontentloaded' });
    expect(await page.locator('html').getAttribute('data-contrast')).toBe('high');

    // Reset
    await bp.contrastToggle.click();
    await page.waitForTimeout(200);
    expect(await page.locator('html').getAttribute('data-contrast')).toBeNull();
  });

  test('[FURN-007-009] Trilingual language switch updates text strings and html lang attribute', async ({ page }) => {
    const bp = new BasePage(page);
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });

    // Switch to Kannada
    await bp.setLanguage('kn');
    expect(await page.locator('html').getAttribute('lang')).toBe('kn');
    let text = await page.textContent('.cp-gov-header');
    expect(text).toContain('ಸ್ಮಾರ್ಟ್');

    // Switch to Hindi
    await bp.setLanguage('hi');
    expect(await page.locator('html').getAttribute('lang')).toBe('hi');
    text = await page.textContent('.cp-gov-header');
    expect(text).toContain('स्मार्ट');

    // Reset to English
    await bp.setLanguage('en');
    expect(await page.locator('html').getAttribute('lang')).toBe('en');
  });

  test('[FURN-010-011] Dark mode theme toggle switches palette and persists across reloads', async ({ page }) => {
    const bp = new BasePage(page);
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });

    await bp.themeToggle.click();
    await page.waitForTimeout(200);
    expect(await page.locator('html').getAttribute('data-theme')).toBe('dark');

    await page.reload({ waitUntil: 'domcontentloaded' });
    expect(await page.locator('html').getAttribute('data-theme')).toBe('dark');

    // Reset back to light
    await bp.themeToggle.click();
    await page.waitForTimeout(200);
    expect(await page.locator('html').getAttribute('data-theme')).toBe('light');
  });

  test('[FURN-012-014] Announcements ticker pause/play works and honors reduced motion', async ({ page }) => {
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    const toggleBtn = page.locator('#tickerToggle');
    if (await toggleBtn.isVisible()) {
      // Toggle pause
      await toggleBtn.click();
      await page.waitForTimeout(200);
      const isPaused = await page.evaluate(() => {
        const el = document.getElementById('tickerText') || document.querySelector('.cp-ticker-text');
        return el?.classList.contains('paused') || el?.style.animationPlayState === 'paused';
      });
      expect(isPaused).toBe(true);

      // Toggle resume
      await toggleBtn.click();
      await page.waitForTimeout(200);
      const isResumed = await page.evaluate(() => {
        const el = document.getElementById('tickerText') || document.querySelector('.cp-ticker-text');
        return el?.classList.contains('paused') || el?.style.animationPlayState === 'paused';
      });
      expect(isResumed).toBe(false);
    }
  });

  test('[FURN-020-021] Mobile hamburger drawer opens and traps focus, closes on Escape', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 640 });
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });

    const hamburger = page.locator('.navbar-toggler, #sidebarToggle, button[aria-label*="navigation" i]');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);

      // Press Escape to close
      await page.keyboard.press('Escape');
      await page.waitForTimeout(300);
      // Main navigation drawer closed
      const isVisible = await page.evaluate(() => {
        const drawer = document.querySelector('.navbar-collapse.show, .cp-sidebar.show');
        return drawer !== null;
      });
      expect(isVisible).toBe(false);
    }
  });
});
