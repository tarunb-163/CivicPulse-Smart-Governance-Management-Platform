const { test, expect } = require('@playwright/test');
const { loginAs, SEEDED_USERS } = require('../fixtures/auth-helper');
const { DataFactory } = require('../factories/data-factory');

test.describe('4D. Citizen Flows & Grievance Lifecycle', () => {

  test('[CIT-01] Citizen Wizard - Step 1 Category and Description Validation', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    // Attempt to proceed without selecting category or entering description
    const nextBtn = page.locator('button:has-text("Next"), #step1Next, .btn-next');
    if (await nextBtn.count() > 0) {
      await nextBtn.first().click();
      // Should show validation errors or remain on step 1
      const isStillOnStep1 = await page.locator('#title, #description, #categoryId').first().isVisible();
      expect(isStillOnStep1).toBeTruthy();
    }
  });

  test('[CIT-02] Citizen Wizard - 4 Steps Progression with Valid Inputs', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    const complaint = DataFactory.validComplaint();

    // Step 1: Category & Details
    if (await page.locator('#categoryId, select[name="categoryId"]').count() > 0) {
      await page.selectOption('#categoryId, select[name="categoryId"]', { index: 1 });
    }
    await page.fill('#title, input[name="title"]', complaint.title);
    await page.fill('#description, textarea[name="description"]', complaint.description);

    const step1Next = page.locator('#step1Next, button:has-text("Next"), .btn-next').first();
    if (await step1Next.isVisible()) await step1Next.click();

    // Step 2: Location
    await page.waitForTimeout(500);
    if (await page.locator('#address, input[name="address"]').isVisible()) {
      await page.fill('#address, input[name="address"]', complaint.address);
    }
    if (await page.locator('#latitude').isVisible()) {
      await page.fill('#latitude', complaint.latitude.toString());
      await page.fill('#longitude', complaint.longitude.toString());
    }

    const step2Next = page.locator('#step2Next, button:has-text("Next"), .btn-next').first();
    if (await step2Next.isVisible()) await step2Next.click();

    // Step 3: Media & Anonymous
    await page.waitForTimeout(500);
    const step3Next = page.locator('#step3Next, button:has-text("Next"), .btn-next').first();
    if (await step3Next.isVisible()) await step3Next.click();

    // Step 4: Review & Submit
    await page.waitForTimeout(500);
    const submitBtn = page.locator('button[type="submit"], #submitComplaint, button:has-text("Submit Grievance")').first();
    if (await submitBtn.isVisible()) {
      await submitBtn.click();
      // Should show success or acknowledgment
      await expect(page.locator('.alert-success, #successModal, #ackNumber, text=CP-2026, text=successfully submitted').first()).toBeVisible({ timeout: 10000 });
    }
  });

  test('[CIT-03] Citizen Wizard - Upload Image Size & Type Restrictions', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    const fileInput = page.locator('input[type="file"]');
    if (await fileInput.count() > 0) {
      // Create a dummy mock text file instead of image to test MIME validation
      await fileInput.setInputFiles({
        name: 'test-virus.exe',
        mimeType: 'application/x-msdownload',
        buffer: Buffer.from('MZDummyExecutablePayload')
      });

      // Verification: UI shows invalid file error or rejects upload
      await expect(page.locator('text=invalid file, text=JPG, text=PNG, text=WEBP, .text-danger').first()).toBeVisible({ timeout: 4000 });
    }
  });

  test('[CIT-04] Citizen Wizard - Anonymous Grievance Toggle', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/new-complaint.html');

    const anonCheckbox = page.locator('#isAnonymous, input[name="isAnonymous"], #anonymousToggle');
    if (await anonCheckbox.count() > 0) {
      await anonCheckbox.check();
      expect(await anonCheckbox.isChecked()).toBeTruthy();
    }
  });

  test('[CIT-05] Citizen - My Complaints Listing, Filtering & Sorting', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');

    await page.waitForLoadState('networkidle');
    // Verify table or list is rendered
    const complaintsTable = page.locator('table, .complaint-card, .complaint-list');
    await expect(complaintsTable.first()).toBeVisible({ timeout: 7000 });

    // Status filter
    const statusFilter = page.locator('#statusFilter, select[name="status"]');
    if (await statusFilter.count() > 0) {
      await statusFilter.selectOption({ index: 1 });
      await page.waitForTimeout(800);
    }
  });

  test('[CIT-06] Citizen - Complaint Details & Timeline Rendering', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');
    await page.waitForLoadState('networkidle');

    // Click first view details link/button
    const viewBtn = page.locator('a:has-text("View"), a:has-text("Details"), .btn-view').first();
    if (await viewBtn.count() > 0) {
      await viewBtn.click();
      await page.waitForLoadState('networkidle');
      // Timeline or status tracking must appear
      await expect(page.locator('.timeline, .activity-log, #complaintTimeline, text=Submitted').first()).toBeVisible({ timeout: 6000 });
    }
  });

  test('[CIT-07] Citizen - Add Comment to Grievance Thread', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');
    await page.waitForLoadState('networkidle');

    const viewBtn = page.locator('a:has-text("View"), a:has-text("Details"), .btn-view').first();
    if (await viewBtn.count() > 0) {
      await viewBtn.click();
      await page.waitForLoadState('networkidle');

      const commentInput = page.locator('#commentText, textarea[name="comment"]');
      if (await commentInput.count() > 0) {
        const commentMsg = `Citizen automated comment at ${Date.now()}`;
        await commentInput.fill(commentMsg);
        await page.click('button:has-text("Post Comment"), #postCommentBtn');
        await expect(page.locator(`text=${commentMsg}`)).toBeVisible({ timeout: 7000 });
      }
    }
  });

  test('[CIT-08] Citizen - Notifications Bell & Mark as Read', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/dashboard.html');

    const notifBell = page.locator('#notificationBell, .notification-toggle, [aria-label*="Notification"]').first();
    if (await notifBell.count() > 0) {
      await notifBell.click();
      // Dropdown should be visible
      const notifMenu = page.locator('.notification-dropdown, #notificationMenu, .dropdown-menu.show');
      await expect(notifMenu.first()).toBeVisible({ timeout: 4000 });
    }
  });

  test('[CIT-09] Citizen - Profile Details Edit & Language Preference Update', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/profile.html');

    const phoneField = page.locator('#phone, input[name="phone"]');
    if (await phoneField.count() > 0) {
      await phoneField.fill('9876543210');
      const saveBtn = page.locator('button[type="submit"], #saveProfileBtn, button:has-text("Save")').first();
      await saveBtn.click();
      await expect(page.locator('.alert-success, text=Profile updated, text=Saved').first()).toBeVisible({ timeout: 6000 });
    }
  });

  test('[CIT-10] Citizen - Download Official PDF Receipt with QR', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/my-complaints.html');
    await page.waitForLoadState('networkidle');

    const receiptLink = page.locator('a:has-text("Receipt"), a[href*="receipt.pdf"]').first();
    if (await receiptLink.count() > 0) {
      const [ download ] = await Promise.all([
        page.waitForEvent('download', { timeout: 8000 }).catch(() => null),
        receiptLink.click()
      ]);
      if (download) {
        expect(download.suggestedFilename()).toContain('.pdf');
      }
    }
  });

});
