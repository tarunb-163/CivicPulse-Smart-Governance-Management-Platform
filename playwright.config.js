// @ts-check
const { defineConfig, devices } = require('@playwright/test');
const path = require('path');

const RESULTS_DIR = path.resolve(__dirname, 'docs/test-results');

module.exports = defineConfig({
  testDir: './tests/e2e',
  timeout: 60000,
  expect: {
    timeout: 10000
  },
  fullyParallel: false, // Run test suites sequentially to ensure deterministic DB and session isolation
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: [
    ['list'],
    ['html', { outputFolder: path.join(RESULTS_DIR, 'playwright-html'), open: 'never' }],
    ['junit', { outputFile: path.join(RESULTS_DIR, 'playwright-junit.xml') }],
    ['json', { outputFile: path.join(RESULTS_DIR, 'playwright-results.json') }]
  ],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:8080',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    viewport: { width: 1280, height: 800 },
    actionTimeout: 15000,
    navigationTimeout: 30000
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome']
      }
    }
  ]
});
