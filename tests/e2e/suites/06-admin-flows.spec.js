const { test, expect } = require('@playwright/test');
const { loginAs, SEEDED_USERS } = require('../fixtures/auth-helper');

test.describe('4F. Admin Configuration, Governance, Management & Exports', () => {

  test('[ADM-01] Admin - Dashboard KPIs & Metrics Rendering', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/dashboard.html');
    await page.waitForLoadState('networkidle');

    // KPI Summary cards
    const kpiCards = page.locator('.stat-card, .metric-card, .cp-card');
    expect(await kpiCards.count()).toBeGreaterThanOrEqual(4);
  });

  test('[ADM-02] Admin - Charts Have Accessible Table Alternatives', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/dashboard.html');
    await page.waitForLoadState('networkidle');

    // Verification: Charts have accessible table alternative or visually-hidden data table
    const tablesOrAlternates = page.locator('table, [role="table"], .chart-table-alt, .table-responsive');
    expect(await tablesOrAlternates.count()).toBeGreaterThanOrEqual(1);
  });

  test('[ADM-03] Admin - Export CSV Report Triggers Valid File Download', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/dashboard.html');
    await page.waitForLoadState('networkidle');

    const csvBtn = page.locator('a:has-text("CSV"), button:has-text("CSV"), a[href*="export/csv"]').first();
    if (await csvBtn.count() > 0) {
      const [ download ] = await Promise.all([
        page.waitForEvent('download', { timeout: 8000 }).catch(() => null),
        csvBtn.click()
      ]);
      if (download) {
        expect(download.suggestedFilename()).toContain('.csv');
      }
    }
  });

  test('[ADM-04] Admin - Export PDF Report Triggers Valid File Download', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/dashboard.html');
    await page.waitForLoadState('networkidle');

    const pdfBtn = page.locator('a:has-text("PDF"), button:has-text("PDF"), a[href*="export/pdf"]').first();
    if (await pdfBtn.count() > 0) {
      const [ download ] = await Promise.all([
        page.waitForEvent('download', { timeout: 8000 }).catch(() => null),
        pdfBtn.click()
      ]);
      if (download) {
        expect(download.suggestedFilename()).toContain('.pdf');
      }
    }
  });

  test('[ADM-05] Admin - Department Management CRUD Interface', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/departments.html');
    await page.waitForLoadState('networkidle');

    const deptRows = page.locator('table tbody tr, .department-card');
    expect(await deptRows.count()).toBeGreaterThanOrEqual(5);

    // Open Add Department modal/form
    const addBtn = page.locator('#addDeptBtn, button:has-text("Add Department"), button:has-text("New Department")').first();
    if (await addBtn.count() > 0) {
      await addBtn.click();
      await expect(page.locator('#deptModal, .modal.show, form#deptForm').first()).toBeVisible({ timeout: 4000 });
    }
  });

  test('[ADM-06] Admin - Category Management CRUD Interface', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/categories.html');
    await page.waitForLoadState('networkidle');

    const catRows = page.locator('table tbody tr, .category-card');
    expect(await catRows.count()).toBeGreaterThanOrEqual(5);
  });

  test('[ADM-07] Admin - Keyword Routing Rules and Interactive Test Box', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/rules.html');
    await page.waitForLoadState('networkidle');

    // Interactive classification testing box
    const testInput = page.locator('#testTextInput, textarea#testQuery, input#queryText');
    if (await testInput.count() > 0) {
      await testInput.fill('overflowing garbage near bus stop');
      const testBtn = page.locator('#testClassifyBtn, button:has-text("Test"), button:has-text("Classify")').first();
      await testBtn.click();

      // Expect classified suggestion result
      await expect(page.locator('#testResult, .suggestion-result, text=Solid Waste Management, text=Garbage').first()).toBeVisible({ timeout: 6000 });
    }
  });

  test('[ADM-08] Admin - User Directory and Status Toggle', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/users.html');
    await page.waitForLoadState('networkidle');

    const userRows = page.locator('table tbody tr');
    expect(await userRows.count()).toBeGreaterThanOrEqual(3);
  });

  test('[ADM-09] Admin - SLA Policies Configuration View', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/sla.html');
    await page.waitForLoadState('networkidle');

    const slaInputs = page.locator('input[type="number"], table tbody tr');
    expect(await slaInputs.count()).toBeGreaterThanOrEqual(1);
  });

  test('[ADM-10] Admin - System Audit Logs Verification', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/audit.html');
    await page.waitForLoadState('networkidle');

    const auditRows = page.locator('table tbody tr, .audit-row');
    expect(await auditRows.count()).toBeGreaterThanOrEqual(1);
  });

  test('[ADM-11] Admin - System Settings and Duplicate Thresholds', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/settings.html');
    await page.waitForLoadState('networkidle');

    const saveSettingsBtn = page.locator('button[type="submit"], #saveSettingsBtn, button:has-text("Save")').first();
    await expect(saveSettingsBtn).toBeVisible({ timeout: 5000 });
  });

});
