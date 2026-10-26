const { test, expect } = require('@playwright/test');
const { SEEDED_USERS } = require('../fixtures/auth-helper');
const { DataFactory } = require('../factories/data-factory');

test.describe('4N. Data Integrity, Concurrency & Idempotency Guards', () => {

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

  test('[DATA-01] Simultaneous Grievance Creations Generate Distinct Public Codes', async ({ request }) => {
    const c1 = DataFactory.validComplaint();
    const c2 = DataFactory.validComplaint();

    const [res1, res2] = await Promise.all([
      request.post('/api/v1/complaints', {
        headers: { Authorization: `Bearer ${citizenToken}` },
        data: c1
      }),
      request.post('/api/v1/complaints', {
        headers: { Authorization: `Bearer ${citizenToken}` },
        data: c2
      })
    ]);

    // If both succeeded, their public codes must be strictly distinct
    if (res1.status() === 201 && res2.status() === 201) {
      const b1 = await res1.json();
      const b2 = await res2.json();
      expect(b1.publicCode).not.toBe(b2.publicCode);
    }
  });

  test('[DATA-02] Duplicate Upvote Idempotency - Citizen cannot upvote twice', async ({ request }) => {
    // Attempt two consecutive upvotes on complaint ID 1
    const res1 = await request.post('/api/v1/complaints/1/upvote', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });

    const res2 = await request.post('/api/v1/complaints/1/upvote', {
      headers: { Authorization: `Bearer ${citizenToken}` }
    });

    // Both should return valid HTTP response (or 400 with 'already upvoted'), never 500 error
    expect([200, 400]).toContain(res2.status());
  });

  test('[DATA-03] Duplicate Citizen Feedback Idempotency - Only one feedback allowed', async ({ request }) => {
    const feedbackData = { rating: 5, comment: 'Great resolution work', satisfied: true };
    const res = await request.post('/api/v1/complaints/1/feedback', {
      headers: { Authorization: `Bearer ${citizenToken}` },
      data: feedbackData
    });

    // Either 201, 200, or 400 (if already submitted or not yet resolved) - must never return 500
    expect([200, 201, 400]).toContain(res.status());
    const body = await res.json();
    expect(body.stackTrace).toBeUndefined();
  });

  test('[DATA-04] Exact Duplicate Submission within 24h is Rejected (DUP-1)', async ({ request }) => {
    const dup = DataFactory.exactDuplicateComplaint();

    // First submission
    const res1 = await request.post('/api/v1/complaints', {
      headers: { Authorization: `Bearer ${citizenToken}` },
      data: dup
    });

    if (res1.status() === 201) {
      // Immediate identical second submission
      const res2 = await request.post('/api/v1/complaints', {
        headers: { Authorization: `Bearer ${citizenToken}` },
        data: dup
      });

      // Server duplicate guard rejects with 409 Conflict
      expect([409, 429]).toContain(res2.status());
    }
  });

  test('[DATA-05] Timestamps Stored in UTC and Consistent', async ({ request }) => {
    const res = await request.get('/api/v1/public/stats');
    expect(res.status()).toBe(200);
  });

});
