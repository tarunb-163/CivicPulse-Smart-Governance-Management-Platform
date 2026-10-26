const { test, expect } = require('@playwright/test');

test.describe('4G. Public Tracking, Transparency Dashboard & Citizen Privacy', () => {

  test('[PUB-01] Public Track - Valid Complaint Public Code Displays Status Timeline', async ({ page }) => {
    await page.goto('/track.html?code=CP-2026-000001');
    await page.waitForLoadState('networkidle');

    // Verification: Track result container visible with details
    await expect(page.locator('#trackResult')).toBeVisible({ timeout: 8000 });
    await expect(page.locator('#resCode')).toHaveText('CP-2026-000001');
  });

  test('[PUB-02] Public Track - Invalid Code Displays Friendly Error', async ({ page }) => {
    await page.goto('/track.html');
    await page.fill('#trackCodeInput', 'CP-9999-999999');
    await page.click('#trackForm button[type="submit"]');

    await expect(page.locator('#trackNotFound')).toBeVisible({ timeout: 7000 });
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

    const inputVal = await page.inputValue('#trackCodeInput');
    expect(inputVal).toContain('CP-2026-000002');
  });

  test('[PUB-05] Public Dashboard - High-Level Municipal KPIs Rendered', async ({ page }) => {
    await page.goto('/transparency.html');
    await page.waitForLoadState('networkidle');

    // Total Grievances, Resolution Rate, Avg Hours
    const stats = page.locator('.cp-stat-value, #statTotal, #statResolved');
    expect(await stats.count()).toBeGreaterThanOrEqual(2);
  });

  test('[PUB-06] Public Dashboard - Department Breakdown Table Present', async ({ page }) => {
    await page.goto('/transparency.html');
    await page.waitForLoadState('networkidle');

    // Verify department leaderboard items render
    const leaderboardItems = page.locator('#leaderboardList .list-group-item');
    await expect(leaderboardItems.first()).toBeVisible({ timeout: 6000 });

    // Click toggle button to open accessible table view alternative
    await page.click('#toggleCatTableBtn');
    await expect(page.locator('#catTableFallback table')).toBeVisible({ timeout: 6000 });
  });

  test('[PUB-07] Public FAQ & Help Accordions Expand and Collapse', async ({ page }) => {
    await page.goto('/help.html');
    await page.waitForLoadState('networkidle');

    const faqBtn = page.locator('#faqH2 button');
    await expect(faqBtn).toBeVisible({ timeout: 5000 });
    await faqBtn.click();
    await expect(faqBtn).toHaveAttribute('aria-expanded', 'true', { timeout: 5000 });
  });

  test('[PUB-08] Public Contact Directory Displays Municipal Office Details', async ({ page }) => {
    await page.goto('/contact.html');
    await page.waitForLoadState('networkidle');

    const contactCards = page.locator('.cp-card, table.cp-table');
    expect(await contactCards.count()).toBeGreaterThanOrEqual(1);
  });

});

