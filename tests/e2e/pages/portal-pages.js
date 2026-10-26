/**
 * Page Objects for Portal Workflows
 */

const { BasePage } = require('./base-page');

class LandingPage extends BasePage {
  constructor(page) {
    super(page);
    this.lodgeGrievanceBtn = page.locator('a[href*="new-complaint"]');
    this.trackGrievanceBtn = page.locator('a[href*="track.html"]');
    this.quickTrackInput = page.locator('#quickTrackCode');
    this.quickTrackSubmit = page.locator('#btnQuickTrack');
    this.ticker = page.locator('#announcementsTicker');
    this.tickerToggle = page.locator('#tickerToggle');
    this.faqAccordion = page.locator('#faqAccordion, .cp-accordion');
  }

  async goto() {
    await this.page.goto('/index.html', { waitUntil: 'domcontentloaded' });
  }
}

class LoginPage extends BasePage {
  constructor(page) {
    super(page);
    this.emailInput = page.locator('#email');
    this.passwordInput = page.locator('#password');
    this.submitBtn = page.locator('button[type="submit"]');
    this.togglePasswordBtn = page.locator('#togglePassword');
    this.errorMessage = page.locator('.alert-danger, #loginError');
  }

  async goto() {
    await this.page.goto('/login.html', { waitUntil: 'domcontentloaded' });
  }

  async login(email, password) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitBtn.click();
  }
}

class RegisterPage extends BasePage {
  constructor(page) {
    super(page);
    this.nameInput = page.locator('#name');
    this.emailInput = page.locator('#email');
    this.phoneInput = page.locator('#phone');
    this.passwordInput = page.locator('#password');
    this.consentCheckbox = page.locator('#consentCheck');
    this.submitBtn = page.locator('button[type="submit"]');
    this.alertBox = page.locator('.alert');
  }

  async goto() {
    await this.page.goto('/register.html', { waitUntil: 'domcontentloaded' });
  }
}

class NewComplaintPage extends BasePage {
  constructor(page) {
    super(page);
    this.titleInput = page.locator('#complaintTitle');
    this.descInput = page.locator('#complaintDesc');
    this.categorySelect = page.locator('#categorySelect');
    this.urgencySelect = page.locator('#urgencySelect');
    this.addressInput = page.locator('#complaintAddress');
    this.latInput = page.locator('#complaintLat');
    this.lngInput = page.locator('#complaintLng');
    this.anonymousSwitch = page.locator('#anonymousSwitch');
    this.fileInput = page.locator('#fileInput');

    // Step Nav
    this.btnStep1Next = page.locator('#btnStep1Next');
    this.btnStep2Next = page.locator('#btnStep2Next');
    this.btnStep3Next = page.locator('#btnStep3Next');
    this.btnSubmit = page.locator('#btnSubmitGrievance');

    // Suggestion chips
    this.classifierChip = page.locator('#textClassifierChipContainer');

    // Duplicate Dialog
    this.duplicateModal = page.locator('#duplicateModal');
    this.btnSupportExisting = page.locator('#btnSupportExisting');
    this.btnConfirmForceSubmit = page.locator('#btnConfirmSubmit');

    // Success Card
    this.stepSuccess = page.locator('#stepSuccess');
    this.successCode = page.locator('#successCode');
    this.btnDownloadReceipt = page.locator('#btnDownloadReceipt');
  }

  async goto() {
    await this.page.goto('/citizen/new-complaint.html', { waitUntil: 'domcontentloaded' });
  }

  async fillStep1(title, description, category = '') {
    await this.titleInput.fill(title);
    await this.descInput.fill(description);
    if (category) {
      await this.categorySelect.selectOption({ label: category });
    }
    await this.btnStep1Next.click();
    await this.page.waitForTimeout(400);
  }

  async fillStep2(address = 'PB Road, Davanagere', lat = '14.4644', lng = '75.9218') {
    await this.addressInput.fill(address);
    await this.latInput.fill(lat);
    await this.lngInput.fill(lng);
    await this.btnStep2Next.click();
    await this.page.waitForTimeout(400);
  }

  async fillStep3(images = [], anonymous = false) {
    if (images && images.length > 0) {
      await this.fileInput.setInputFiles(images);
      await this.page.waitForTimeout(600);
    }
    await this.btnStep3Next.click();
    await this.page.waitForTimeout(400);
  }
}

class MyComplaintsPage extends BasePage {
  constructor(page) {
    super(page);
    this.table = page.locator('table.cp-table');
    this.statusFilter = page.locator('#statusFilter');
    this.searchInput = page.locator('#searchInput');
    this.rows = page.locator('table.cp-table tbody tr');
    this.pagination = page.locator('.pagination');
  }

  async goto() {
    await this.page.goto('/citizen/my-complaints.html', { waitUntil: 'domcontentloaded' });
  }
}

class TrackPage extends BasePage {
  constructor(page) {
    super(page);
    this.codeInput = page.locator('#trackCodeInput');
    this.trackBtn = page.locator('#btnTrack');
    this.resultContainer = page.locator('#trackResult');
    this.timeline = page.locator('.cp-timeline');
    this.qrBox = page.locator('#qrCodeBox');
    this.printBtn = page.locator('#btnPrintReceipt');
  }

  async goto(code = '') {
    const url = code ? `/track.html?code=${encodeURIComponent(code)}` : '/track.html';
    await this.page.goto(url, { waitUntil: 'domcontentloaded' });
  }
}

class OfficerBoardPage extends BasePage {
  constructor(page) {
    super(page);
    this.kanbanBoard = page.locator('.cp-kanban, #kanbanBoard');
    this.assignedLane = page.locator('[data-status="ASSIGNED"]');
    this.inProgressLane = page.locator('[data-status="IN_PROGRESS"]');
    this.resolvedLane = page.locator('[data-status="RESOLVED"]');
  }

  async goto() {
    await this.page.goto('/officer/board.html', { waitUntil: 'domcontentloaded' });
  }
}

class OfficerQueuePage extends BasePage {
  constructor(page) {
    super(page);
    this.table = page.locator('table.cp-table');
    this.filterStatus = page.locator('#filterStatus');
    this.filterPriority = page.locator('#filterPriority');
    this.searchInput = page.locator('#searchQueue');
    this.rows = page.locator('table.cp-table tbody tr');
  }

  async goto() {
    await this.page.goto('/officer/queue.html', { waitUntil: 'domcontentloaded' });
  }
}

class OfficerWorkspacePage extends BasePage {
  constructor(page) {
    super(page);
    this.statusSelect = page.locator('#statusSelect');
    this.remarkInput = page.locator('#statusRemark');
    this.btnUpdateStatus = page.locator('#btnUpdateStatus');
    this.proofInput = page.locator('#proofFileInput');
    this.btnUploadProof = page.locator('#btnUploadProof');
    this.noteInput = page.locator('#noteText');
    this.btnSaveNote = page.locator('#btnSaveNote');
    this.timeline = page.locator('.cp-timeline');
  }

  async goto(id) {
    await this.page.goto(`/officer/workspace.html?id=${id}`, { waitUntil: 'domcontentloaded' });
  }
}

class AdminDashboardPage extends BasePage {
  constructor(page) {
    super(page);
    this.statTotal = page.locator('#statTotal');
    this.statPending = page.locator('#statPending');
    this.statResolved = page.locator('#statResolved');
    this.statSlaBreached = page.locator('#statSlaBreached');
    this.deptFilter = page.locator('#deptFilter');
    this.btnExportCsv = page.locator('#btnExportCsv');
    this.btnExportPdf = page.locator('#btnExportPdf');
  }

  async goto() {
    await this.page.goto('/admin/dashboard.html', { waitUntil: 'domcontentloaded' });
  }
}

class CivicBotWidget {
  constructor(page) {
    this.page = page;
    this.trigger = page.locator('#botTrigger, .bot-trigger, [data-bot-trigger]');
    this.drawer = page.locator('#botDrawer, .cp-bot-drawer');
    this.closeBtn = page.locator('#botClose, .cp-bot-close');
    this.input = page.locator('#botInput, input[type="text"]', { has: page.locator('visible') });
    this.sendBtn = page.locator('#botSend, button[type="submit"]');
    this.messagesContainer = page.locator('#botMessages, .cp-bot-messages');
  }

  async open() {
    await this.trigger.click();
    await this.page.waitForTimeout(400);
  }

  async close() {
    await this.closeBtn.click();
    await this.page.waitForTimeout(300);
  }

  async sendMessage(text) {
    const inp = this.drawer.locator('input');
    await inp.fill(text);
    await inp.press('Enter');
    await this.page.waitForTimeout(600);
  }
}

module.exports = {
  LandingPage,
  LoginPage,
  RegisterPage,
  NewComplaintPage,
  MyComplaintsPage,
  TrackPage,
  OfficerBoardPage,
  OfficerQueuePage,
  OfficerWorkspacePage,
  AdminDashboardPage,
  CivicBotWidget
};
