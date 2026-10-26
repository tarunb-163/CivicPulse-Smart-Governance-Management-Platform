const { test, expect } = require('@playwright/test');
const { SEEDED_USERS } = require('../fixtures/auth-helper');

test.describe('4I. API Contract, Negative Testing & Envelope Validation', () => {

  let citizenToken = '';
  let officerToken = '';
  let adminToken = '';

  test.beforeAll(async ({ request }) => {
    // Acquire citizen JWT
    const citRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.citizen.email, password: SEEDED_USERS.citizen.password }
    });
    if (citRes.ok()) {
      const citJson = await citRes.json();
      citizenToken = citJson.token || citJson.accessToken;
    }

    // Acquire officer JWT
    const offRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.officer.email, password: SEEDED_USERS.officer.password }
    });
    if (offRes.ok()) {
      const offJson = await offRes.json();
      officerToken = offJson.token || offJson.accessToken;
    }

    // Acquire admin JWT
    const admRes = await request.post('/api/v1/auth/login', {
      data: { email: SEEDED_USERS.admin.email, password: SEEDED_USERS.admin.password }
    });
    if (admRes.ok()) {
      const admJson = await admRes.json();
      adminToken = admJson.token || admJson.accessToken;
    }
  });

  test('[API-01] GET /api/v1/public/stats - Contract & Format Validation', async ({ request }) => {
    const res = await request.get('/api/v1/public/stats');
    expect(res.status()).toBe(200);
    const body = await res.json();

    expect(body).toHaveProperty('totalGrievances');
    expect(body).toHaveProperty('resolvedGrievances');
    expect(body).toHaveProperty('resolutionRatePercentage');
    expect(body).toHaveProperty('departmentStats');
    expect(Array.isArray(body.departmentStats)).toBeTruthy();
  });

  test('[API-02] GET /api/v1/public/departments - Array Envelope', async ({ request }) => {
    const res = await request.get('/api/v1/public/departments');
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
    expect(body.length).toBeGreaterThanOrEqual(5);
    expect(body[0]).toHaveProperty('id');
    expect(body[0]).toHaveProperty('name');
  });

  test('[API-03] GET /api/v1/public/categories - Array Envelope', async ({ request }) => {
    const res = await request.get('/api/v1/public/categories');
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBeTruthy();
    expect(body.length).toBeGreaterThanOrEqual(5);
    expect(body[0]).toHaveProperty('id');
    expect(body[0]).toHaveProperty('name');
  });

  test('[API-04] GET /api/v1/public/track/{code} - Valid Code Contract', async ({ request }) => {
    const res = await request.get('/api/v1/public/track/CP-2026-000001');
    expect(res.status()).toBe(200);
    const body = await res.json();

    expect(body).toHaveProperty('publicCode', 'CP-2026-000001');
    expect(body).toHaveProperty('status');
    expect(body).toHaveProperty('title');
    // Anti-leak check (must be null or undefined, never exposing real email/phone)
    expect(body.citizenEmail).toBeFalsy();
    expect(body.citizenPhone).toBeFalsy();
  });

  test('[API-05] GET /api/v1/public/track/{code} - Non-existent Code returns 404', async ({ request }) => {
    const res = await request.get('/api/v1/public/track/CP-9999-999999');
    expect(res.status()).toBe(404);
    const body = await res.json();
    expect(body).toHaveProperty('status', 404);
    expect(body).toHaveProperty('error');
    expect(body.stackTrace).toBeUndefined();
  });

  test('[API-06] POST /api/v1/complaints - Unauthenticated returns 401', async ({ request }) => {
    const res = await request.post('/api/v1/complaints', {
      data: { title: 'Test without auth', description: 'desc', categoryId: 1 }
    });
    expect(res.status()).toBe(401);
    const body = await res.json();
    expect(body).toHaveProperty('status', 401);
  });

  test('[API-07] GET /api/v1/admin/users - Citizen Role returns 403 Forbidden', async ({ request }) => {
    const res = await request.get('/api/v1/admin/users', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect(res.status()).toBe(403);
    const body = await res.json();
    expect(body).toHaveProperty('status', 403);
    expect(body.stackTrace).toBeUndefined();
  });

  test('[API-08] GET /api/v1/officer/stats - Citizen Role returns 403 Forbidden', async ({ request }) => {
    const res = await request.get('/api/v1/officer/stats', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect(res.status()).toBe(403);
  });

  test('[API-09] GET /api/v1/complaints/mine - PagedResponse Envelope', async ({ request }) => {
    const res = await request.get('/api/v1/complaints/mine?page=0&size=5', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();

    expect(body).toHaveProperty('content');
    expect(body).toHaveProperty('page');
    expect(body).toHaveProperty('size');
    expect(body).toHaveProperty('totalElements');
    expect(body).toHaveProperty('totalPages');
  });

  test('[API-10] Method Not Allowed (405) - Unsupported HTTP Method on Endpoint', async ({ request }) => {
    // Notifications read endpoint only supports PATCH
    const res = await request.delete('/api/v1/notifications/1/read', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });
    expect(res.status()).toBe(405);
    const body = await res.json();
    expect(body).toHaveProperty('status', 405);
  });

  test('[API-11] Unsupported Media Type (415) - Sending XML to JSON API', async ({ request }) => {
    const res = await request.post('/api/v1/auth/login', {
      headers: { 'Content-Type': 'application/xml' },
      data: '<login><email>admin@test.com</email></login>'
    });
    expect([400, 415]).toContain(res.status());
  });

  test('[API-12] Tampered JWT Signature returns 401 Unauthorized', async ({ request }) => {
    const tampered = citizenToken.slice(0, -6) + 'abcdef';
    const res = await request.get('/api/v1/complaints/mine', {
      headers: { Authorization: `Bearer ${tampered}` }
    });
    expect(res.status()).toBe(401);
  });

  test('[API-13] Unknown API Path returns 404 with Standard Error Envelope', async ({ request }) => {
    const res = await request.get('/api/v1/public/non-existent-endpoint-path');
    expect(res.status()).toBe(404);
    const body = await res.json();
    expect(body).toHaveProperty('status', 404);
    expect(body.stackTrace).toBeUndefined();
  });

});
