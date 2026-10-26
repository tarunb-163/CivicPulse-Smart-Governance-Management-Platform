/**
 * Base Page Object covering shared Government Furniture across all 28 pages
 */

class BasePage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.skipLink = page.locator('.cp-skip-link');
    this.mainContent = page.locator('#main-content');
    this.topNotice = page.locator('.cp-top-notice');
    this.tricolourStrip = page.locator('.cp-tricolour-strip');
    this.textSizeMinus = page.locator('button[data-size="-"]');
    this.textSizeReset = page.locator('button[data-size="0"]');
    this.textSizePlus = page.locator('button[data-size="+"]');
    this.contrastToggle = page.locator('.cp-contrast-btn:not(.cp-theme-toggle), #contrastToggle, [aria-label*="Contrast"]').first();
    this.themeToggle = page.locator('.cp-theme-toggle').first();
    this.langSelect = page.locator('#langSelect, .cp-lang-select').first();
    this.botTrigger = page.locator('#botTrigger, .bot-trigger, [data-bot-trigger]').first();
    this.footer = page.locator('footer.cp-gov-footer');
    this.footerAttribution = page.locator('.cp-footer-bottom');
  }

  async setLanguage(lang) {
    const select = this.page.locator('#langSelect, .cp-lang-select').first();
    if (await select.count() > 0) {
      await select.selectOption(lang);
      await this.page.waitForTimeout(500);
    }
  }

  async setHighContrast(enable = true) {
    const isHigh = (await this.page.locator('html').getAttribute('data-contrast')) === 'high';
    if ((enable && !isHigh) || (!enable && isHigh)) {
      if (await this.contrastToggle.count() > 0) {
        await this.contrastToggle.click();
        await this.page.waitForTimeout(300);
      }
    }
  }

  async setTheme(theme = 'dark') {
    await this.page.evaluate((t) => {
      document.documentElement.setAttribute('data-theme', t);
      localStorage.setItem('theme', t);
    }, theme);
    await this.page.waitForTimeout(200);
  }

  async hasHorizontalScroll() {
    return await this.page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth);
  }
}

module.exports = { BasePage };
