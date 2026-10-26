const { test, expect } = require('@playwright/test');
const { SEEDED_USERS } = require('../fixtures/auth-helper');

test.describe('4M. Performance, Query Latencies & Core Web Vitals', () => {

  let citizenToken = '';

  test.beforeAll(async ({ request }) => {
    const citRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.citizen.email, password: SEEDED_USERS.citizen.password }
    });
    if (citRes.ok()) {
      const json = await citRes.json();
      citizenToken = json.token || json.accessToken;
    }
  });

  test('[PERF-01] Public Stats Endpoint Latency is under 500ms', async ({ request }) => {
    const start = Date.now();
    const res = await request.get('/api/v1/public/stats');
    const duration = Date.now() - start;

    expect(res.status()).toBe(200);
    expect(duration).toBeLessThan(500);
  });

  test('[PERF-02] Citizen My Complaints List Endpoint Latency is under 500ms', async ({ request }) => {
    const start = Date.now();
    const res = await request.get('/api/v1/complaints/mine?page=0&size=10', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    const duration = Date.now() - start;

    expect(res.status()).toBe(200);
    expect(duration).toBeLessThan(500);
  });

  test('[PERF-03] Departments List Endpoint Latency is under 500ms', async ({ request }) => {
    const start = Date.now();
    const res = await request.get('/api/v1/public/departments');
    const duration = Date.now() - start;

    expect(res.status()).toBe(200);
    expect(duration).toBeLessThan(500);
  });

  test('[PERF-04] Landing Page DOM Content Loaded is fast', async ({ page }) => {
    const start = Date.now();
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    const duration = Date.now() - start;

    expect(duration).toBeLessThan(3500);
  });

  test('[PERF-05] Public Tracking Query Latency is under 500ms', async ({ request }) => {
    const start = Date.now();
    const res = await request.get('/api/v1/public/track/CP-2026-000001');
    const duration = Date.now() - start;

    expect(res.status()).toBe(200);
    expect(duration).toBeLessThan(500);
  });

});
