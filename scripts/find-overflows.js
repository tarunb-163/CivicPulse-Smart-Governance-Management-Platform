const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    headless: true
  });
  const page = await browser.newPage();
  await page.setViewportSize({ width: 360, height: 740 });

  // Login
  await page.goto('http://localhost:8080/login.html');
  await page.fill('#email', 'citizen@civicpulse.test');
  await page.fill('#password', 'Password@123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/citizen/dashboard.html');
  await page.waitForTimeout(1000);

  const wideElements = await page.evaluate(() => {
    return Array.from(document.querySelectorAll('*'))
      .filter(el => el.getBoundingClientRect().right > window.innerWidth || el.scrollWidth > window.innerWidth)
      .map(el => ({
        tag: el.tagName,
        id: el.id,
        className: (el.className || '').toString().slice(0, 50),
        offsetWidth: el.offsetWidth,
        scrollWidth: el.scrollWidth,
        rectRight: el.getBoundingClientRect().right
      }))
      .filter(el => el.offsetWidth > 360 || el.scrollWidth > 360)
      .slice(0, 15);
  });

  console.log('WIDE ELEMENTS ON CITIZEN DASHBOARD:');
  console.log(JSON.stringify(wideElements, null, 2));

  // Check Track Receipt
  await page.goto('http://localhost:8080/track.html?code=CP-2026-000001');
  await page.waitForTimeout(800);
  const trackWide = await page.evaluate(() => {
    return Array.from(document.querySelectorAll('*'))
      .filter(el => el.scrollWidth > 360)
      .map(el => ({
        tag: el.tagName,
        id: el.id,
        className: (el.className || '').toString().slice(0, 50),
        scrollWidth: el.scrollWidth
      }))
      .slice(0, 10);
  });

  console.log('WIDE ELEMENTS ON TRACK:');
  console.log(JSON.stringify(trackWide, null, 2));

  await browser.close();
})();
