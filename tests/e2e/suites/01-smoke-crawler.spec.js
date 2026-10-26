// @ts-check
const { test, expect } = require('@playwright/test');
const { loginAs } = require('../fixtures/auth-helper');

const ALL_PAGES = [
  { path: '/index.html', title: 'CivicPulse', auth: false },
  { path: '/login.html', title: 'Login', auth: false },
  { path: '/register.html', title: 'Register', auth: false },
  { path: '/forgot-password.html', title: 'Forgot Password', auth: false },
  { path: '/reset-password.html', title: 'Reset Password', auth: false },
  { path: '/track.html', title: 'Track', auth: false },
  { path: '/transparency.html', title: 'Transparency', auth: false },
  { path: '/privacy.html', title: 'Privacy', auth: false },
  { path: '/terms.html', title: 'Terms', auth: false },
  { path: '/accessibility.html', title: 'Accessibility', auth: false },
  { path: '/screen-reader.html', title: 'Screen Reader', auth: false },
  { path: '/sitemap.html', title: 'Sitemap', auth: false },
  { path: '/help.html', title: 'Help', auth: false },
  { path: '/contact.html', title: 'Contact', auth: false },
  { path: '/404.html', title: '404', auth: false },
  { path: '/403.html', title: '403', auth: false },
  { path: '/500.html', title: '500', auth: false },

  // Citizen
  { path: '/citizen/dashboard.html', title: 'Dashboard', auth: true, role: 'citizen' },
  { path: '/citizen/new-complaint.html', title: 'Lodge', auth: true, role: 'citizen' },
  { path: '/citizen/my-complaints.html', title: 'My Grievances', auth: true, role: 'citizen' },
  { path: '/citizen/profile.html', title: 'Profile', auth: true, role: 'citizen' },

  // Officer
  { path: '/officer/dashboard.html', title: 'Officer Dashboard', auth: true, role: 'officer' },
  { path: '/officer/queue.html', title: 'Queue', auth: true, role: 'officer' },
  { path: '/officer/board.html', title: 'Board', auth: true, role: 'officer' },

  // Admin
  { path: '/admin/dashboard.html', title: 'Admin Dashboard', auth: true, role: 'admin' },
  { path: '/admin/departments.html', title: 'Departments', auth: true, role: 'admin' },
  { path: '/admin/categories.html', title: 'Categories', auth: true, role: 'admin' },
  { path: '/admin/rules.html', title: 'Rules', auth: true, role: 'admin' },
  { path: '/admin/users.html', title: 'Users', auth: true, role: 'admin' },
  { path: '/admin/sla.html', title: 'SLA', auth: true, role: 'admin' },
  { path: '/admin/audit.html', title: 'Audit', auth: true, role: 'admin' },
  { path: '/admin/settings.html', title: 'Settings', auth: true, role: 'admin' }
];

test.describe('4A. Every Page Smoke & Layout Verification', () => {

  for (const p of ALL_PAGES) {
    test(`[SMK] Page ${p.path} loads cleanly without errors or old effects`, async ({ page }) => {
      const consoleErrors = [];
      const failedRequests = [];

      page.on('console', msg => {
        if (msg.type() === 'error') {
          consoleErrors.push(msg.text());
        }
      });

      page.on('requestfailed', req => {
        // Exclude optional external maps tile errors if offline
        if (!req.url().includes('tile.openstreetmap.org')) {
          failedRequests.push(`${req.url()} (${req.failure()?.errorText})`);
        }
      });

      if (p.auth) {
        await loginAs(page, p.role);
      }

      const response = await page.goto(p.path, { waitUntil: 'domcontentloaded' });
      expect(response?.status()).toBe(200);

      // Verify HTML title and single h1
      const title = await page.title();
      expect(title.length).toBeGreaterThan(3);

      const h1Count = await page.locator('h1').count();
      expect(h1Count, `Expected single <h1> on ${p.path}`).toBe(1);

      // Verify HTML5 Landmarks
      expect(await page.locator('header, [role="banner"]').count()).toBeGreaterThanOrEqual(1);
      expect(await page.locator('main, [role="main"], #main-content').count()).toBeGreaterThanOrEqual(1);
      expect(await page.locator('footer, [role="contentinfo"]').count()).toBeGreaterThanOrEqual(1);

      // Verify Skip Link
      const skipLink = page.locator('.cp-skip-link');
      expect(await skipLink.count()).toBe(1);
      expect(await skipLink.getAttribute('href')).toBe('#main-content');

      // Verify Government Disclaimer
      const pageText = await page.textContent('body');
      expect(pageText).toContain('Demonstration project');
      expect(pageText).toContain('CivicPulse');
      expect(pageText).toContain('developed by Tarun B');

      // Verify NO old-theme legacy classes
      const forbiddenClasses = ['aurora', 'glass-card', 'tilt-card', 'spotlight', 'magnetic'];
      for (const fc of forbiddenClasses) {
        const count = await page.locator(`.${fc}`).count();
        expect(count, `Found forbidden old-theme class: .${fc} on ${p.path}`).toBe(0);
      }

      // Verify zero console errors & broken network requests
      const realErrors = consoleErrors.filter(e => !e.includes('favicon') && !e.includes('Map container'));
      expect(realErrors, `Console errors on ${p.path}`).toHaveLength(0);
      expect(failedRequests, `Failed network requests on ${p.path}`).toHaveLength(0);
    });
  }

  test('[SMK-CRAWL] Full Internal Link Crawler traverses entire portal without 4xx/5xx', async ({ page }) => {
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    const links = await page.$$eval('a[href]', anchors => {
      return anchors
        .map(a => a.getAttribute('href'))
        .filter(h => h && !h.startsWith('http') && !h.startsWith('#') && !h.startsWith('tel:') && !h.startsWith('mailto:'))
        .map(h => h.split('?')[0]);
    });

    const uniqueLinks = Array.from(new Set(links));
    expect(uniqueLinks.length).toBeGreaterThan(10);

    for (const link of uniqueLinks) {
      const resp = await page.goto(link, { waitUntil: 'domcontentloaded' });
      expect(resp?.status(), `Link ${link} failed with status ${resp?.status()}`).toBe(200);
    }
  });
});
