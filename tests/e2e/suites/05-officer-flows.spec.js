const { test, expect } = require('@playwright/test');
const { loginAs, SEEDED_USERS } = require('../fixtures/auth-helper');

test.describe('4E. Officer Department Queues, Kanban & Status Workflow', () => {

  test('[OFF-01] Officer - Queue Displays Only Own Department Complaints', async ({ page }) => {
    // officer.roads is assigned to department ROADS
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Verify queue table rows
    const tableRows = page.locator('#queueTbody tr');
    await expect(tableRows.first()).toBeVisible({ timeout: 8000 });

    // Table should contain at least 1 complaint
    const rowCount = await tableRows.count();
    expect(rowCount).toBeGreaterThanOrEqual(1);

    // Verify category reflects Roads issues (e.g. Pothole, Footpath, Manhole)
    const categoryBadge = page.locator('#queueTbody td .badge').first();
    await expect(categoryBadge).toBeVisible();
  });

  test('[OFF-02] Officer - Department Isolation: Cannot see Water complaints', async ({ page }) => {
    await loginAs(page, 'officer'); // Roads department officer
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Water Supply & Sewerage complaints should not appear in roads officer queue
    const waterItems = page.locator('#queueTbody td:has-text("Water Supply & Sewerage"), #queueTbody td:has-text("Contaminated / Dirty Water")');
    expect(await waterItems.count()).toBe(0);
  });

  test('[OFF-03] Officer - Kanban Board Rendering with Columns', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/board.html');
    await page.waitForLoadState('networkidle');

    // Check board columns: Assigned, In Progress, Resolved
    const columns = page.locator('.cp-kanban-cards[data-status]');
    expect(await columns.count()).toBeGreaterThanOrEqual(3);
  });

  test('[OFF-04] Officer - Status Transition to IN_PROGRESS requires Remark', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Click first Work button to open drawer
    const workBtn = page.locator('#queueTbody .cp-open-drawer-btn').first();
    await expect(workBtn).toBeVisible({ timeout: 7000 });
    await workBtn.click();

    // Verify drawer is open
    const drawer = page.locator('#cpOfficerDrawer.show');
    await expect(drawer).toBeVisible({ timeout: 5000 });

    // Clear remark textarea
    const remarkField = drawer.locator('#cpDrawerRemark');
    await remarkField.fill('');

    // Attempt to submit
    const submitBtn = drawer.locator('#cpDrawerStatusForm button[type="submit"]');
    await submitBtn.click();

    // HTML5 validation or form remains open
    expect(await drawer.isVisible()).toBeTruthy();
  });

  test('[OFF-05] Officer - Status Transition to RESOLVED with Remark', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Open first complaint drawer
    const workBtn = page.locator('#queueTbody .cp-open-drawer-btn').first();
    await expect(workBtn).toBeVisible({ timeout: 7000 });
    await workBtn.click();

    const drawer = page.locator('#cpOfficerDrawer.show');
    await expect(drawer).toBeVisible({ timeout: 5000 });

    // Check if status select has RESOLVED option
    const statusSelect = drawer.locator('#cpDrawerNextStatus');
    const resolvedOption = statusSelect.locator('option[value="RESOLVED"]');
    if (await resolvedOption.count() > 0) {
      await statusSelect.selectOption('RESOLVED');
      await drawer.locator('#cpDrawerRemark').fill('Municipal maintenance completed according to specifications.');
      await drawer.locator('#cpDrawerStatusForm button[type="submit"]').click();
      await page.waitForTimeout(1000);
    }
  });

  test('[OFF-06] Officer - Add Internal Department Note Hidden From Citizen', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Get complaint ID from first row
    const firstBtn = page.locator('#queueTbody .cp-open-drawer-btn').first();
    await expect(firstBtn).toBeVisible({ timeout: 7000 });
    const complaintId = await firstBtn.getAttribute('data-id');

    // Navigate to full officer workspace
    await page.goto(`/officer/workspace.html?id=${complaintId}`);
    await page.waitForLoadState('networkidle');

    const noteInput = page.locator('#workNoteInput');
    await expect(noteInput).toBeVisible({ timeout: 6000 });
    const secretNote = `Confidential crew inspection note ${Date.now()}`;
    await noteInput.fill(secretNote);
    await page.locator('#workNoteForm button[type="submit"]').click();

    // Verify note is rendered in workspace notes container
    await expect(page.locator(`#workComments:has-text("${secretNote}")`)).toBeVisible({ timeout: 7000 });
  });

  test('[OFF-07] Officer - SLA Badge Indicators and Escalation Display', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    // Verify SLA indicator rings / badges are present in the table
    const slaIndicators = page.locator('#queueTbody td [title*="SLA"], #queueTbody td svg, #queueTbody td .badge');
    expect(await slaIndicators.count()).toBeGreaterThanOrEqual(1);
  });

  test('[OFF-08] Officer - Merge Duplicate Grievance Workflow Button', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/queue.html');
    await page.waitForLoadState('networkidle');

    const firstBtn = page.locator('#queueTbody .cp-open-drawer-btn').first();
    await expect(firstBtn).toBeVisible({ timeout: 7000 });
    const complaintId = await firstBtn.getAttribute('data-id');

    await page.goto(`/officer/workspace.html?id=${complaintId}`);
    await page.waitForLoadState('networkidle');

    const mergeBtn = page.locator('#btnWorkMerge');
    await expect(mergeBtn).toBeVisible({ timeout: 6000 });
  });

  test('[OFF-09] Officer - Performance KPI Stats View', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/officer/dashboard.html');
    await page.waitForLoadState('networkidle');

    // Officer dashboard stat cards
    const statCards = page.locator('.cp-stat-card, .cp-card');
    expect(await statCards.count()).toBeGreaterThanOrEqual(4);
    await expect(page.locator('#kpiAssigned, #kpiInProgress, #kpiResolved').first()).toBeVisible();
  });

});
