/**
 * Authentication and Role Fixtures Helper
 */

const CREDENTIALS = {
  citizen: {
    email: 'citizen@civicpulse.test',
    password: 'Password@123',
    role: 'CITIZEN',
    name: 'Rohan Deshmukh',
    landingUrl: '/citizen/dashboard.html'
  },
  citizenB: {
    email: 'citizen2@civicpulse.test',
    password: 'Password@123',
    role: 'CITIZEN',
    name: 'Priya Varma',
    landingUrl: '/citizen/dashboard.html'
  },
  citizenC: {
    email: 'citizen3@civicpulse.test',
    password: 'Password@123',
    role: 'CITIZEN',
    name: 'Kiran Kumar',
    landingUrl: '/citizen/dashboard.html'
  },
  officer: {
    email: 'officer.roads@civicpulse.test',
    password: 'Password@123',
    role: 'OFFICER',
    department: 'ROADS',
    name: 'Manoj Kumar',
    landingUrl: '/officer/dashboard.html'
  },
  otherOfficer: {
    email: 'officer.swm@civicpulse.test',
    password: 'Password@123',
    role: 'OFFICER',
    department: 'SWM',
    name: 'Girish Gouda',
    landingUrl: '/officer/dashboard.html'
  },
  deptHead: {
    email: 'head.roads@civicpulse.test',
    password: 'Password@123',
    role: 'DEPT_HEAD',
    department: 'ROADS',
    name: 'Suresh Patil',
    landingUrl: '/officer/dashboard.html'
  },
  admin: {
    email: 'admin@civicpulse.test',
    password: 'Password@123',
    role: 'ADMIN',
    name: 'Rajesh Sharma',
    landingUrl: '/admin/dashboard.html'
  }
};

/**
 * Log into the portal as a given role and wait for landing page redirect
 * @param {import('@playwright/test').Page} page
 * @param {'citizen' | 'citizenB' | 'officer' | 'otherOfficer' | 'deptHead' | 'admin' | 'visitor'} role
 */
async function loginAs(page, role) {
  if (role === 'visitor') {
    await page.goto('/index.html', { waitUntil: 'domcontentloaded' });
    await page.evaluate(() => localStorage.clear());
    return;
  }

  const cred = CREDENTIALS[role];
  if (!cred) throw new Error(`Unknown test role: ${role}`);

  await page.goto('/login.html', { waitUntil: 'domcontentloaded' });
  await page.evaluate(() => localStorage.clear());

  await page.fill('#email', cred.email);
  await page.fill('#password', cred.password);
  await page.click('button[type="submit"]');

  // Wait for redirect to expected landing page
  await page.waitForURL(`**${cred.landingUrl}*`, { timeout: 10000, waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(500);
}

/**
 * Perform in-browser logout
 * @param {import('@playwright/test').Page} page
 */
async function logout(page) {
  const logoutBtn = await page.$('#logoutBtn, #menuLogout, [data-action="logout"]');
  if (logoutBtn && await logoutBtn.isVisible()) {
    await logoutBtn.click();
    await page.waitForURL('**/login.html*', { timeout: 8000 });
  } else {
    await page.evaluate(() => localStorage.clear());
    await page.goto('/login.html', { waitUntil: 'domcontentloaded' });
  }
}

module.exports = {
  CREDENTIALS,
  SEEDED_USERS: CREDENTIALS,
  loginAs,
  logout
};
