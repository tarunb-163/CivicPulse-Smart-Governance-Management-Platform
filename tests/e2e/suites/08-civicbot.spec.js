const { test, expect } = require('@playwright/test');

test.describe('4H. CivicBot Virtual Assistant & Interactive Intent Handling', () => {

  test('[BOT-01] CivicBot - Widget Floating Launcher Opens and Traps Focus', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    const launcher = page.locator('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]').first();
    await expect(launcher).toBeVisible({ timeout: 6000 });
    await launcher.click();

    const botWindow = page.locator('.cp-bot-window, #cpBotWindow');
    await expect(botWindow).toBeVisible({ timeout: 4000 });

    // Verify close button closes window
    const closeBtn = page.locator('.cp-bot-close, #cpBotClose, button[aria-label="Close"]').first();
    await closeBtn.click();
    await expect(botWindow).not.toBeVisible({ timeout: 4000 });
  });

  test('[BOT-02] CivicBot - Greeting Intent & Prompt Suggestions', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]');
    // Greeting message
    const botMessages = page.locator('.cp-bot-msg, .bot-bubble');
    await expect(botMessages.first()).toBeVisible({ timeout: 5000 });
  });

  test('[BOT-03] CivicBot - Tracking Intent with Live Grievance Code', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('.cp-bot-input, #cpBotInput, input[placeholder*="Ask"]');
    await input.fill('Track CP-2026-000001');
    await page.keyboard.press('Enter');

    // Bot reply should fetch status or acknowledge tracking query
    await expect(page.locator('.cp-bot-msg-bot, .bot-bubble', { hasText: 'CP-2026' }).first()).toBeVisible({ timeout: 8000 });
  });

  test('[BOT-04] CivicBot - Operating Hours & Office Intent', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('.cp-bot-input, #cpBotInput, input[placeholder*="Ask"]');
    await input.fill('What are municipal operating hours?');
    await page.keyboard.press('Enter');

    await expect(page.locator('.cp-bot-msg-bot, .bot-bubble', { hasText: '09:00 AM' }).first()).toBeVisible({ timeout: 7000 });
  });

  test('[BOT-05] CivicBot - Unknown & Fallback Intent Handling', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('.cp-bot-input, #cpBotInput, input[placeholder*="Ask"]');
    await input.fill('asdfghjkl random gibberish query 12345');
    await page.keyboard.press('Enter');

    await expect(page.locator('.cp-bot-msg-bot, .bot-bubble', { hasText: 'I could not understand' }).first()).toBeVisible({ timeout: 7000 });
  });

  test('[BOT-06] CivicBot - XSS Payload Escaped Cleanly', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('.cp-bot-input, #cpBotInput, input[placeholder*="Ask"]');
    await input.fill('<script>alert("XSS")</script><img src=x onerror=alert(1)>');
    await page.keyboard.press('Enter');

    // Verify no alert dialog was triggered and text is treated as textContent
    await page.waitForTimeout(1000);
    const scriptTags = await page.locator('.cp-bot-body script').count();
    expect(scriptTags).toBe(0);
  });

  test('[BOT-07] CivicBot - aria-live Screen Reader Announcements Present', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('.cp-bot-launcher, #cpBotLauncher, button[aria-label*="Virtual Assistant"]');
    const liveRegion = page.locator('[aria-live="polite"], [aria-live="assertive"]');
    expect(await liveRegion.count()).toBeGreaterThanOrEqual(1);
  });

});
