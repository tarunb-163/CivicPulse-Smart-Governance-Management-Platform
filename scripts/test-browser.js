const { chromium } = require('playwright');

(async () => {
  try {
    const browser = await chromium.launch({
      executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
      headless: true
    });
    const page = await browser.newPage();
    await page.goto('http://localhost:8080/index.html');
    const title = await page.title();
    console.log('SUCCESS: Page title is:', title);
    await browser.close();
  } catch (err) {
    console.error('ERROR launching browser:', err);
    process.exit(1);
  }
})();
