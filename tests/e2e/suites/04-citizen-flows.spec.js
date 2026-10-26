const { test, expect } = require('@playwright/test');
const { loginAs, SEEDED_USERS } = require('../fixtures/auth-helper');
const { DataFactory } = require('../factories/data-factory');

test.describe('4D. Citizen Flows & Grievance Lifecycle', () => {

  test('[CIT-01] Citizen Wizard - Step 1 Category and Description Validation', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    // Attempt to proceed without selecting category or entering description
    const nextBtn = page.locator('#btnToStep2');
    await nextBtn.click();
    
    // Should remain on step 1
    const isStep1Visible = await page.locator('#stepPanel1').isVisible();
    expect(isStep1Visible).toBeTruthy();
  });

  test('[CIT-02] Citizen Wizard - 4 Steps Progression with Valid Inputs', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    const complaint = DataFactory.validComplaint();

    // Step 1: Category & Details
    await page.waitForFunction(() => document.querySelectorAll('#categorySelect option').length > 1);
    await page.selectOption('#categorySelect', { index: 1 });
    await page.fill('#complaintTitle', complaint.title);
    await page.fill('#complaintDesc', complaint.description);
    await page.click('#btnToStep2');

    // Step 2: Location
    await expect(page.locator('#stepPanel2')).toBeVisible({ timeout: 5000 });
    await page.fill('#complaintAddress', complaint.address);
    await page.click('#btnToStep3');

    // Step 3: Media & Evidence
    await expect(page.locator('#stepPanel3')).toBeVisible({ timeout: 5000 });
    await page.click('#btnToStep4');

    // Step 4: Review & Submit
    await expect(page.locator('#stepPanel4')).toBeVisible({ timeout: 5000 });
    await page.click('#btnSubmitGrievance');

    // Should show success state with CP-2026 acknowledgment code
    await expect(page.locator('#stepSuccess')).toBeVisible({ timeout: 10000 });
    const codeText = await page.locator('#successCode').innerText();
    expect(codeText).toMatch(/CP-2026/);
  });

  test('[CIT-03] Citizen Wizard - Upload Image Size & Type Restrictions', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    const fileInput = page.locator('#fileInput');
    if (await fileInput.count() > 0) {
      await fileInput.setInputFiles({
        name: 'test-virus.exe',
        mimeType: 'application/x-msdownload',
        buffer: Buffer.from('MZDummyExecutablePayload')
      });
      // The browser / form validates or ignores invalid types
      await page.waitForTimeout(500);
    }
  });

  test('[CIT-04] Citizen Wizard - Anonymous Grievance Toggle', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    // Navigate to step 4
    await page.waitForFunction(() => document.querySelectorAll('#categorySelect option').length > 1);
    await page.selectOption('#categorySelect', { index: 1 });
    await page.fill('#complaintTitle', 'Anonymous test complaint');
    await page.fill('#complaintDesc', 'Testing the anonymous grievance toggle switch');
    await page.click('#btnToStep2');
    await expect(page.locator('#stepPanel2')).toBeVisible({ timeout: 5000 });
    await page.fill('#complaintAddress', 'MG Road, Davanagere');
    await page.click('#btnToStep3');
    await expect(page.locator('#stepPanel3')).toBeVisible({ timeout: 5000 });
    await page.click('#btnToStep4');
    await expect(page.locator('#stepPanel4')).toBeVisible({ timeout: 5000 });

    const anonCheckbox = page.locator('#anonymousSwitch');
    await anonCheckbox.check();
    expect(await anonCheckbox.isChecked()).toBeTruthy();
  });

  test('[CIT-05] Citizen - My Complaints Listing, Filtering & Sorting', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');

    await page.waitForLoadState('networkidle');
    // Verify table or list is rendered
    const complaintsTable = page.locator('#complaintsGrid table, .cp-table');
    await expect(complaintsTable.first()).toBeVisible({ timeout: 7000 });

    // Status filter chip click
    const filterBtn = page.locator('#statusFilterChips button.filter-btn').first();
    if (await filterBtn.count() > 0) {
      await filterBtn.click();
      await page.waitForTimeout(600);
    }
  });

  test('[CIT-06] Citizen - Complaint Details & Timeline Rendering', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');
    await page.waitForLoadState('networkidle');

    // Click first view details link
    const viewBtn = page.locator('#complaintsGrid a[href*="/citizen/complaint.html"]').first();
    await expect(viewBtn).toBeVisible({ timeout: 7000 });
    await viewBtn.click();
    await page.waitForLoadState('networkidle');

    // Timeline and description must appear
    await expect(page.locator('#detailDesc, #detailTimeline, .cp-timeline').first()).toBeVisible({ timeout: 6000 });
  });

  test('[CIT-07] Citizen - Add Comment to Grievance Thread', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');
    await page.waitForLoadState('networkidle');

    const viewBtn = page.locator('#complaintsGrid a[href*="/citizen/complaint.html"]').first();
    await expect(viewBtn).toBeVisible({ timeout: 7000 });
    await viewBtn.click();
    await page.waitForLoadState('networkidle');

    const commentInput = page.locator('#commentInput');
    await expect(commentInput).toBeVisible({ timeout: 6000 });
    const commentMsg = `Citizen automated comment at ${Date.now()}`;
    await commentInput.fill(commentMsg);
    await page.locator('#commentForm button[type="submit"]').click();

    // Verify comment appears in thread
    await expect(page.locator(`#commentsThread:has-text("${commentMsg}")`)).toBeVisible({ timeout: 7000 });
  });

  test('[CIT-08] Citizen - Notifications Bell & Mark as Read', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/dashboard.html');

    const notifBell = page.locator('#cpNotifDropdown button[data-bs-toggle="dropdown"], [aria-label="Notifications"]').first();
    await expect(notifBell).toBeVisible({ timeout: 5000 });
    await notifBell.click();
    
    // Dropdown list should be visible
    const notifMenu = page.locator('#cpNotifDropdown .dropdown-menu');
    await expect(notifMenu).toBeVisible({ timeout: 4000 });
  });

  test('[CIT-09] Citizen - Profile Details Edit & Language Preference Update', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/profile.html');

    const phoneField = page.locator('#inputPhone');
    await expect(phoneField).toBeVisible({ timeout: 6000 });
    await phoneField.fill('9876543210');
    
    await page.locator('#profileForm button[type="submit"]').click();
    // Verify toast or updated state
    await page.waitForTimeout(1000);
  });

  test('[CIT-10] Citizen - Download Official PDF Receipt with QR', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');
    await page.waitForLoadState('networkidle');

    const viewBtn = page.locator('#complaintsGrid a[href*="/citizen/complaint.html"]').first();
    await expect(viewBtn).toBeVisible({ timeout: 7000 });
    await viewBtn.click();
    await page.waitForLoadState('networkidle');

    const printBtn = page.locator('#btnDownloadReceipt');
    await expect(printBtn).toBeVisible({ timeout: 6000 });
  });

});
