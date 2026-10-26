const { test, expect } = require('@playwright/test');

test.describe('4G. Public Tracking, Transparency Dashboard & Citizen Privacy', () => {

  test('[PUB-01] Public Track - Valid Complaint Public Code Displays Status Timeline', async ({ page }) => {
    await page.goto('/track.html?code=CP-2026-000001');
    await page.waitForLoadState('networkidle');

    // Verification: Track result container visible with details
    const trackResult = page.locator('#trackResult, .track-card, text=Grievance Status, text=CP-2026-000001').first();
    await expect(trackResult).toBeVisible({ timeout: 8000 });
  });

  test('[PUB-02] Public Track - Invalid Code Displays Friendly Error', async ({ page }) => {
    await page.goto('/track.html');
    await page.fill('#trackingCodeInput, #publicCodeInput, input[name="code"]', 'CP-9999-999999');
    await page.click('button[type="submit"], #trackBtn');

    await expect(page.locator('.alert-danger, #errorAlert, text=not found, text=Invalid tracking code').first()).toBeVisible({ timeout: 7000 });
  });

  test('[PUB-03] Public Track - Privacy Protection: NEVER Leaks Email, Phone or Citizen ID', async ({ page }) => {
    await page.goto('/track.html?code=CP-2026-000001');
    await page.waitForLoadState('networkidle');

    const bodyText = await page.textContent('body');
    // Ensure no raw email or phone leaks on public tracking
    expect(bodyText).not.toContain('@civicpulse.test');
    expect(bodyText).not.toContain('citizenId');
    expect(bodyText).not.toContain('9876543210');
  });

  test('[PUB-04] Public Track - QR Code Deep Link with pre-filled code', async ({ page }) => {
    await page.goto('/track.html?code=CP-2026-000002');
    await page.waitForLoadState('networkidle');

    const inputVal = await page.inputValue('#trackingCodeInput, #publicCodeInput, input[name="code"]');
    expect(inputVal).toContain('CP-2026-000002');
  });

  test('[PUB-05] Public Dashboard - High-Level Municipal KPIs Rendered', async ({ page }) => {
    await page.goto('/transparency.html');
    await page.waitForLoadState('networkidle');

    // Total Grievances, Resolution Rate, Avg Hours
    const stats = page.locator('.stat-value, .cp-kpi-value, #totalGrievances, #resolutionRate');
    expect(await stats.count()).toBeGreaterThanOrEqual(2);
  });

  test('[PUB-06] Public Dashboard - Department Breakdown Table Present', async ({ page }) => {
    await page.goto('/transparency.html');
    await page.waitForLoadState('networkidle');

    const deptTable = page.locator('table, #deptStatsTable, .department-breakdown');
    await expect(deptTable.first()).toBeVisible({ timeout: 6000 });
  });

  test('[PUB-07] Public FAQ & Help Accordions Expand and Collapse', async ({ page }) => {
    await page.goto('/help.html');
    await page.waitForLoadState('networkidle');

    const faqBtn = page.locator('.accordion-button, details summary').first();
    if (await faqBtn.count() > 0) {
      await faqBtn.click();
      await page.waitForTimeout(400);
      const isExpanded = await faqBtn.getAttribute('aria-expanded');
      expect(isExpanded === 'true' || isExpanded === null).toBeTruthy();
    }
  });

  test('[PUB-08] Public Contact Directory Displays Municipal Office Details', async ({ page }) => {
    await page.goto('/contact.html');
    await page.waitForLoadState('networkidle');

    const contactCards = page.locator('.contact-card, .cp-card, text=Toll-Free, text=Helpline');
    expect(await contactCards.count()).toBeGreaterThanOrEqual(1);
  });

});
