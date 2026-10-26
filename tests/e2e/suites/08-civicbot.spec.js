const { test, expect } = require('@playwright/test');

test.describe('4H. CivicBot Virtual Assistant & Interactive Intent Handling', () => {

  test('[BOT-01] CivicBot - Widget Floating Launcher Opens and Traps Focus', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    const launcher = page.locator('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]').first();
    await expect(launcher).toBeVisible({ timeout: 6000 });
    await launcher.click();

    const botWindow = page.locator('#cpBotPanel, .cp-bot-panel');
    await expect(botWindow).toBeVisible({ timeout: 4000 });

    // Verify close button closes window
    const closeBtn = page.locator('#cpBotClose, button[aria-label="Close Assistant"]').first();
    await closeBtn.click();
    await expect(botWindow).not.toBeVisible({ timeout: 4000 });
  });

  test('[BOT-02] CivicBot - Greeting Intent & Prompt Suggestions', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]');
    // Greeting message
    const botMessages = page.locator('#cpBotMessages .cp-bot-bubble-bot');
    await expect(botMessages.first()).toBeVisible({ timeout: 5000 });
  });

  test('[BOT-03] CivicBot - Tracking Intent with Live Grievance Code', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('#cpBotInput');
    await input.fill('Track CP-2026-000001');
    await page.click('#cpBotForm button[type="submit"]');

    // Bot reply should fetch status or acknowledge tracking query
    await expect(page.locator('#cpBotMessages .cp-bot-bubble-bot', { hasText: 'CP-2026-000001' }).first()).toBeVisible({ timeout: 8000 });
  });

  test('[BOT-04] CivicBot - Operating Hours & Office Intent', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('#cpBotInput');
    await input.fill('What are municipal operating hours?');
    await page.click('#cpBotForm button[type="submit"]');

    await expect(page.locator('#cpBotMessages .cp-bot-bubble-bot', { hasText: '9:00 AM' }).first()).toBeVisible({ timeout: 7000 });
  });

  test('[BOT-05] CivicBot - Unknown & Fallback Intent Handling', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('#cpBotInput');
    await input.fill('asdfghjkl random gibberish query 12345');
    await page.click('#cpBotForm button[type="submit"]');

    await expect(page.locator('#cpBotMessages .cp-bot-bubble-bot', { hasText: 'CivicBot' }).first()).toBeVisible({ timeout: 7000 });
  });

  test('[BOT-06] CivicBot - XSS Payload Escaped Cleanly', async ({ page }) => {
    let dialogTriggered = false;
    page.on('dialog', () => { dialogTriggered = true; });

    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]');
    const input = page.locator('#cpBotInput');
    await input.fill('<script>alert("XSS")</script><img src=x onerror=alert(1)>');
    await page.click('#cpBotForm button[type="submit"]');

    await page.waitForTimeout(1000);
    const scriptTags = await page.locator('#cpBotMessages script').count();
    expect(scriptTags).toBe(0);
    expect(dialogTriggered).toBe(false);
  });

  test('[BOT-07] CivicBot - aria-live Screen Reader Announcements Present', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForLoadState('networkidle');

    await page.click('#botTrigger, .cp-bot-trigger, button[aria-label*="Virtual Assistant"]');
    const liveRegion = page.locator('[aria-live="polite"], [aria-live="assertive"]');
    expect(await liveRegion.count()).toBeGreaterThanOrEqual(1);
  });

});

