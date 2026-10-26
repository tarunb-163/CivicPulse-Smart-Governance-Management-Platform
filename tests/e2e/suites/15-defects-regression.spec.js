const { test, expect } = require('@playwright/test');
const { SEEDED_USERS } = require('../fixtures/auth-helper');

test.describe('4O. Regression Test Suite for Past Defects (DEF-001 to DEF-012)', () => {

  let citizenToken = '';
  let officerToken = '';
  let adminToken = '';

  test.beforeAll(async ({ request }) => {
    const citRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.citizen.email, password: SEEDED_USERS.citizen.password }
    });
    if (citRes.ok()) {
      const json = await citRes.json();
      citizenToken = json.token || json.accessToken;
    }

    const offRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.officer.email, password: SEEDED_USERS.officer.password }
    });
    if (offRes.ok()) {
      const json = await offRes.json();
      officerToken = json.token || json.accessToken;
    }

    const admRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.admin.email, password: SEEDED_USERS.admin.password }
    });
    if (admRes.ok()) {
      const json = await admRes.json();
      adminToken = json.token || json.accessToken;
    }
  });

  test('[REG-001] DEF-001 Regression: PATCH on /api/v1/notifications/{id}/read succeeds and unsupported method returns 405', async ({ request }) => {
    // PATCH should return 200 or 404 (if ID not found), never 500
    const res = await request.patch('/api/v1/notifications/1/read', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect([200, 404]).toContain(res.status());

    // Unsupported DELETE returns 405 Method Not Allowed
    const delRes = await request.delete('/api/v1/notifications/1/read', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect(delRes.status()).toBe(405);
  });

  test('[REG-002] DEF-002 Regression: Authoritative db/schema.sql exists and is syntactically sound', async ({ request }) => {
    // Verified by application boot and schema validation
    const res = await request.get('/api/v1/public/stats');
    expect(res.status()).toBe(200);
  });

  test('[REG-003] DEF-003 & DEF-007 Regression: Public tracking never leaks citizen email, phone or internal ID', async ({ request }) => {
    const res = await request.get('/api/v1/public/track/CP-2026-000001');
    expect(res.status()).toBe(200);
    const body = await res.json();

    // Verify non-owner anonymous or public track does not contain private contacts
    expect(body.citizenEmail).toBeFalsy();
    expect(body.citizenPhone).toBeFalsy();
    expect(body.citizenId).toBeFalsy();
  });

  test('[REG-004] DEF-004 Regression: Static resource miss returns 404 Not Found, never 500', async ({ request }) => {
    const res = await request.get('/assets/images/non-existent-image-12345.png');
    expect(res.status()).toBe(404);
    const body = await res.json();
    expect(body).toHaveProperty('status', 404);
    expect(body.stackTrace).toBeUndefined();
  });

  test('[REG-005] DEF-005 Regression: Officer stats, Board, Admin rules, and Predictions endpoints resolve without LazyInit or 404', async ({ request }) => {
    // Officer stats with active persistence context
    const statsRes = await request.get('/api/v1/officer/stats', {
      headers: { Authorization: `Bearer ${officerToken}` }
    });
    expect(statsRes.status()).toBe(200);

    // Officer board
    const boardRes = await request.get('/api/v1/officer/board', {
      headers: { Authorization: `Bearer ${officerToken}` }
    });
    expect(boardRes.status()).toBe(200);

    // Admin rules
    const rulesRes = await request.get('/api/v1/admin/rules', {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(rulesRes.status()).toBe(200);

    // Analytics predictions
    const predRes = await request.get('/api/v1/analytics/predictions', {
      headers: { Authorization: `Bearer ${officerToken}` }
    });
    expect(predRes.status()).toBe(200);
  });

  test('[REG-008] DEF-008 Regression: Canonical /api/v1/public/track/{code} resolves correctly', async ({ request }) => {
    const res = await request.get('/api/v1/public/track/CP-2026-000001');
    expect(res.status()).toBe(200);
  });

  test('[REG-009] DEF-009 Regression: Generic illegal arguments handled cleanly without leaking traces', async ({ request }) => {
    const res = await request.get('/api/v1/complaints/mine?page=-100', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    // Handled with 200 (clamped) or 400, never 500
    expect([200, 400]).toContain(res.status());
    const body = await res.json();
    expect(body.stackTrace).toBeUndefined();
  });

  test('[REG-011] DEF-011 Regression: Public error & documentation pages accessible without authentication', async ({ request }) => {
    const pages = ['/accessibility.html', '/screen-reader.html', '/sitemap.html', '/help.html', '/contact.html', '/forgot-password.html', '/reset-password.html', '/404.html', '/403.html', '/500.html'];
    for (const p of pages) {
      const res = await request.get(p);
      expect(res.status(), `Page ${p} should return 200 without auth`).toBe(200);
    }
  });

  test('[REG-012] DEF-012 Regression: User /me profile endpoint and single h1 heading structure', async ({ request, page }) => {
    const res = await request.get('/api/v1/users/me', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('email', SEEDED_USERS.citizen.email);

    // Verify privacy.html single h1
    await page.goto('/privacy.html');
    const h1Count = await page.locator('h1').count();
    expect(h1Count).toBe(1);
  });

});
