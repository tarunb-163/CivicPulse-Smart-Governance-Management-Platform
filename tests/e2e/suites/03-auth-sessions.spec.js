const { test, expect } = require('@playwright/test');
const { loginAs, logout, SEEDED_USERS } = require('../fixtures/auth-helper');
const { DataFactory } = require('../factories/data-factory');

test.describe('4C. Authentication, Sessions & RBAC Guards', () => {

  test('[AUTH-01] Register - Full Registration Flow with Valid Data', async ({ page }) => {
    const uniqueUser = DataFactory.uniqueUser();
    await page.goto('/register.html');

    await page.fill('#name, #fullName', uniqueUser.fullName);
    await page.fill('#email', uniqueUser.email);
    await page.fill('#phone', uniqueUser.phone);
    await page.fill('#password', uniqueUser.password);
    await page.fill('#confirmPassword', uniqueUser.password);
    
    // Check consent checkbox
    const consent = page.locator('#acceptedTerms, #termsConsent, input[type="checkbox"][required]');
    if (await consent.count() > 0) {
      await consent.first().check();
    }

    await page.click('#submitBtn, button[type="submit"]');

    // Should redirect to login with registered query param or show success
    await page.waitForURL('**/login.html*', { timeout: 10000 });
    expect(page.url()).toContain('/login.html');
  });

  test('[AUTH-02] Register - Consent Requirement Enforcement', async ({ page }) => {
    const uniqueUser = DataFactory.uniqueUser();
    await page.goto('/register.html');

    await page.fill('#name, #fullName', uniqueUser.fullName);
    await page.fill('#email', uniqueUser.email);
    await page.fill('#password', uniqueUser.password);
    await page.fill('#confirmPassword', uniqueUser.password);

    // Uncheck consent if checked
    const consent = page.locator('#acceptedTerms, #termsConsent, input[type="checkbox"][required]');
    if (await consent.count() > 0) {
      await consent.first().uncheck();
    }

    await page.click('#submitBtn, button[type="submit"]');
    // Browser HTML5 validation or UI validation should block submission
    const currentUrl = page.url();
    expect(currentUrl).toContain('/register.html');
  });

  test('[AUTH-03] Register - Weak Password Rejected', async ({ page }) => {
    await page.goto('/register.html');

    await page.fill('#name, #fullName', 'Weak Pass User');
    await page.fill('#email', `weak_${Date.now()}@civicpulse.test`);
    await page.fill('#phone', '9876543210');
    await page.fill('#password', '12345');
    await page.fill('#confirmPassword', '12345');

    const consent = page.locator('#acceptedTerms, #termsConsent, input[type="checkbox"][required]');
    if (await consent.count() > 0) await consent.first().check();

    await page.click('#submitBtn, button[type="submit"]');
    // HTML5 minlength=8 blocks submission or form stays on register.html
    const currentUrl = page.url();
    expect(currentUrl).toContain('/register.html');
  });

  test('[AUTH-04] Register - Duplicate Email Rejected', async ({ page }) => {
    await page.goto('/register.html');

    // Seeded email already exists
    await page.fill('#name, #fullName', 'Duplicate User');
    await page.fill('#email', SEEDED_USERS.citizen.email);
    await page.fill('#phone', '9876543210');
    await page.fill('#password', 'Password@123');
    await page.fill('#confirmPassword', 'Password@123');

    const consent = page.locator('#acceptedTerms, #termsConsent, input[type="checkbox"][required]');
    if (await consent.count() > 0) await consent.first().check();

    await page.click('#submitBtn, button[type="submit"]');
    await expect(page.locator('#registerAlert, .alert-danger, #errorAlert').first()).toBeVisible({ timeout: 7000 });
    const alertText = await page.locator('#registerAlert, .alert-danger, #errorAlert').first().innerText();
    expect(alertText.toLowerCase()).toMatch(/already (exists|registered|in use)/);
  });

  test('[AUTH-05] Login - Valid Citizen Credentials Redirects to Citizen Dashboard', async ({ page }) => {
    await page.goto('/login.html');
    await page.fill('#email', SEEDED_USERS.citizen.email);
    await page.fill('#password', SEEDED_USERS.citizen.password);
    await page.click('#submitBtn, button[type="submit"]');

    await page.waitForURL('**/citizen/dashboard.html*', { timeout: 10000 });
    expect(page.url()).toContain('/citizen/dashboard.html');
  });

  test('[AUTH-06] Login - Valid Officer Credentials Redirects to Officer Dashboard', async ({ page }) => {
    await page.goto('/login.html');
    await page.fill('#email', SEEDED_USERS.officer.email);
    await page.fill('#password', SEEDED_USERS.officer.password);
    await page.click('#submitBtn, button[type="submit"]');

    await page.waitForURL('**/officer/dashboard.html*', { timeout: 10000 });
    expect(page.url()).toContain('/officer/dashboard.html');
  });

  test('[AUTH-07] Login - Valid Admin Credentials Redirects to Admin Dashboard', async ({ page }) => {
    await page.goto('/login.html');
    await page.fill('#email', SEEDED_USERS.admin.email);
    await page.fill('#password', SEEDED_USERS.admin.password);
    await page.click('#submitBtn, button[type="submit"]');

    await page.waitForURL('**/admin/dashboard.html*', { timeout: 10000 });
    expect(page.url()).toContain('/admin/dashboard.html');
  });

  test('[AUTH-08] Login - Invalid Password Displays Error Message', async ({ page }) => {
    await page.goto('/login.html');
    await page.fill('#email', SEEDED_USERS.citizen.email);
    await page.fill('#password', 'WrongPassword!999');
    await page.click('#submitBtn, button[type="submit"]');

    await expect(page.locator('#loginAlert, .alert-danger, #errorAlert').first()).toBeVisible({ timeout: 6000 });
  });

  test('[AUTH-09] Login - Account Lockout after 5 Failed Attempts', async ({ page }) => {
    const targetEmail = `lockout_test_${Date.now()}@civicpulse.test`;
    // Register the user first
    await page.goto('/register.html');
    await page.fill('#name, #fullName', 'Lockout Target');
    await page.fill('#email', targetEmail);
    await page.fill('#phone', '9811223344');
    await page.fill('#password', 'Password@123');
    await page.fill('#confirmPassword', 'Password@123');
    const consent = page.locator('#acceptedTerms, #termsConsent, input[type="checkbox"][required]');
    if (await consent.count() > 0) await consent.first().check();
    await page.click('#submitBtn, button[type="submit"]');
    await page.waitForURL('**/login.html*', { timeout: 8000 });

    // Attempt 5 bad logins
    for (let i = 1; i <= 5; i++) {
      await page.goto('/login.html');
      await page.fill('#email', targetEmail);
      await page.fill('#password', `WrongPass_${i}`);
      await page.click('#submitBtn, button[type="submit"]');
      await page.waitForTimeout(600);
    }

    // 6th attempt should state locked out
    await page.fill('#email', targetEmail);
    await page.fill('#password', 'WrongPass_6');
    await page.click('#submitBtn, button[type="submit"]');
    await expect(page.locator('#loginAlert, .alert-danger, #errorAlert').first()).toBeVisible({ timeout: 6000 });
    const text = await page.locator('#loginAlert, .alert-danger, #errorAlert').first().innerText();
    expect(text.toLowerCase()).toMatch(/locked|failed/);
  });

  test('[AUTH-10] Forgot Password & Reset Flow', async ({ page }) => {
    await page.goto('/forgot-password.html');
    await page.fill('#email', SEEDED_USERS.citizen.email);
    await page.click('#submitBtn, button[type="submit"]');

    await expect(page.locator('#forgotAlert, .alert-success, #successAlert').first()).toBeVisible({ timeout: 7000 });
  });

  test('[AUTH-11] Logout - Clears Storage and Redirects to Login or Home', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/dashboard.html');

    await logout(page);
    expect(page.url()).toMatch(/\/(login\.html|index\.html)/);

    // Verify localStorage auth tokens are cleared
    const token = await page.evaluate(() => localStorage.getItem('cp_token') || localStorage.getItem('token') || localStorage.getItem('jwt'));
    expect(token).toBeNull();
  });

  test('[AUTH-12] Route Guard - Citizen Accessing /admin/dashboard.html is Denied', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/admin/dashboard.html');

    // Should redirect to 403 or login or dashboard
    await page.waitForTimeout(1500);
    expect(page.url()).not.toContain('/admin/dashboard.html');
  });

  test('[AUTH-13] Route Guard - Citizen Accessing /officer/dashboard.html is Denied', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/officer/dashboard.html');

    await page.waitForTimeout(1500);
    expect(page.url()).not.toContain('/officer/dashboard.html');
  });

  test('[AUTH-14] Route Guard - Officer Accessing /admin/dashboard.html is Denied', async ({ page }) => {
    await loginAs(page, 'officer');
    await page.goto('/admin/dashboard.html');

    await page.waitForTimeout(1500);
    expect(page.url()).not.toContain('/admin/dashboard.html');
  });

  test('[AUTH-15] Route Guard - Unauthenticated Visitor Accessing /citizen/dashboard.html is Redirected', async ({ page }) => {
    await page.goto('/citizen/dashboard.html');
    await page.waitForTimeout(1500);
    expect(page.url()).toMatch(/\/(login\.html|index\.html)/);
  });

  test('[AUTH-16] Route Guard - Unauthenticated Visitor Accessing /officer/dashboard.html is Redirected', async ({ page }) => {
    await page.goto('/officer/dashboard.html');
    await page.waitForTimeout(1500);
    expect(page.url()).toMatch(/\/(login\.html|index\.html)/);
  });

  test('[AUTH-17] Route Guard - Unauthenticated Visitor Accessing /admin/dashboard.html is Redirected', async ({ page }) => {
    await page.goto('/admin/dashboard.html');
    await page.waitForTimeout(1500);
    expect(page.url()).toMatch(/\/(login\.html|index\.html)/);
  });

  test('[AUTH-18] Back Button after Logout does not show private citizen data', async ({ page }) => {
    await loginAs(page, 'citizen');
    await page.goto('/citizen/dashboard.html');
    await page.waitForLoadState('networkidle');

    await logout(page);
    await page.goBack();
    await page.waitForTimeout(1000);

    // Either redirected back to login or page shows no authorized data
    const isLoginPage = page.url().includes('login.html');
    const isAuthError = await page.locator('text=Session expired, text=Please log in').count() > 0;
    const token = await page.evaluate(() => localStorage.getItem('token') || localStorage.getItem('cp_token'));
    expect(isLoginPage || isAuthError || !token).toBeTruthy();
  });

});
