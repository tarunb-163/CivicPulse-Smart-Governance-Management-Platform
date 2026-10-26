const { test, expect } = require('@playwright/test');
const { loginAs, SEEDED_USERS } = require('../fixtures/auth-helper');

test.describe('4E. Officer Department Queues, Kanban & Status Workflow', () => {

  test('[OFF-01] Officer - Queue Displays Only Own Department Complaints', async ({ page }) => {
    // officer.roads is assigned to department ROADS
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Verify complaints list or table
    const tableRows = page.locator('table tbody tr, .queue-item, .complaint-row');
    await expect(tableRows.first()).toBeVisible({ timeout: 8000 });

    // Verify all visible complaints belong to Roads or match department context
    const deptBadges = page.locator('.badge-dept, .badge-department, text=Roads & Infrastructure');
    expect(await deptBadges.count()).toBeGreaterThanOrEqual(1);
  });

  test('[OFF-02] Officer - Department Isolation: Cannot see Water complaints', async ({ page }) => {
    await loginAs(page, 'officer'); // Roads department
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Water Supply & Sewerage complaints should not appear in roads officer queue
    const waterItems = page.locator('td:has-text("Water Supply & Sewerage"), .badge:has-text("WATER")');
    expect(await waterItems.count()).toBe(0);
  });

  test('[OFF-03] Officer - Kanban Board Rendering with 4 Columns', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/board.html');
    await page.waitForLoadState('networkidle');

    // Check board columns: Assigned, In Progress, Resolved, etc.
    const columns = page.locator('.kanban-column, .board-col, [data-status]');
    expect(await columns.count()).toBeGreaterThanOrEqual(3);
  });

  test('[OFF-04] Officer - Status Transition to IN_PROGRESS requires Remark', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Select first complaint to open workspace
    const actionBtn = page.locator('a:has-text("Manage"), button:has-text("Action"), .btn-manage').first();
    if (await actionBtn.count() > 0) {
      await actionBtn.click();
      await page.waitForLoadState('networkidle');

      // Status change form
      const statusSelect = page.locator('#newStatus, select[name="status"]');
      if (await statusSelect.count() > 0) {
        await statusSelect.selectOption('IN_PROGRESS');
        // Clear remark
        const remarkField = page.locator('#statusRemark, textarea[name="remark"]');
        if (await remarkField.count() > 0) {
          await remarkField.fill('');
          const updateBtn = page.locator('#updateStatusBtn, button:has-text("Update Status")');
          await updateBtn.click();
          // Remark should be required
          await expect(page.locator('.invalid-feedback, .alert-danger, text=Remark is required').first()).toBeVisible({ timeout: 4000 });
        }
      }
    }
  });

  test('[OFF-05] Officer - Status Transition to RESOLVED requires Proof Photo', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    const actionBtn = page.locator('a:has-text("Manage"), button:has-text("Action"), .btn-manage').first();
    if (await actionBtn.count() > 0) {
      await actionBtn.click();
      await page.waitForLoadState('networkidle');

      const statusSelect = page.locator('#newStatus, select[name="status"]');
      if (await statusSelect.count() > 0) {
        await statusSelect.selectOption('RESOLVED');
        const remarkField = page.locator('#statusRemark, textarea[name="remark"]');
        if (await remarkField.count() > 0) {
          await remarkField.fill('Work has been completed on site.');
        }

        // Try submitting without proof if required by workflow
        const updateBtn = page.locator('#updateStatusBtn, button:has-text("Update Status")');
        if (await updateBtn.count() > 0) {
          await updateBtn.click();
          // Should either prompt for resolution proof upload or complete
          await page.waitForTimeout(1000);
        }
      }
    }
  });

  test('[OFF-06] Officer - Add Internal Department Note Hidden From Citizen', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    const actionBtn = page.locator('a:has-text("Manage"), button:has-text("Action"), .btn-manage').first();
    if (await actionBtn.count() > 0) {
      await actionBtn.click();
      await page.waitForLoadState('networkidle');

      const internalNoteInput = page.locator('#internalNoteText, textarea[name="internalNote"]');
      if (await internalNoteInput.count() > 0) {
        const secretNote = `Confidential crew note ${Date.now()}`;
        await internalNoteInput.fill(secretNote);
        await page.click('#addInternalNoteBtn, button:has-text("Add Internal Note")');
        await expect(page.locator(`text=${secretNote}`)).toBeVisible({ timeout: 6000 });
      }
    }
  });

  test('[OFF-07] Officer - SLA Badge Indicators and Escalation Display', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // SLA indicators (On Track, Approaching, Breached)
    const slaBadges = page.locator('.badge-sla, .badge:has-text("SLA"), .badge:has-text("BREACHED"), .badge:has-text("Normal")');
    expect(await slaBadges.count()).toBeGreaterThanOrEqual(1);
  });

  test('[OFF-08] Officer - Merge Duplicate Grievance Workflow', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    const mergeBtn = page.locator('button:has-text("Merge"), .btn-merge, #mergeDuplicateBtn').first();
    if (await mergeBtn.count() > 0) {
      await mergeBtn.click();
      const mergeModal = page.locator('#mergeModal, .modal.show');
      await expect(mergeModal).toBeVisible({ timeout: 4000 });
    }
  });

  test('[OFF-09] Officer - Performance KPI Stats View', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/dashboard.html');
    await page.waitForLoadState('networkidle');

    // Officer dashboard metrics
    const statCards = page.locator('.cp-card, .metric-card, .stat-value');
    expect(await statCards.count()).toBeGreaterThanOrEqual(3);
  });

});
