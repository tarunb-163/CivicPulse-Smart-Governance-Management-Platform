import { apiRequest } from '../core/api.js';
import { escapeHtml } from '../core/utils.js';

export function initCivicBot() {
  if (document.getElementById('cpBotOrb')) return;

  const orb = document.createElement('div');
  orb.id = 'cpBotOrb';
  orb.className = 'cp-bot-orb';
  orb.title = 'Ask CivicBot';
  orb.innerHTML = `<i class="bi bi-robot"></i>`;

  const panel = document.createElement('div');
  panel.id = 'cpBotPanel';
  panel.className = 'cp-bot-panel';
  panel.innerHTML = `
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom bg-primary text-white">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-robot fs-5"></i>
        <div>
          <h6 class="mb-0 fw-bold">CivicBot Assistant</h6>
          <small style="opacity: 0.85; font-size: 0.75rem;">AI Citizen Support</small>
        </div>
      </div>
      <button type="button" class="btn btn-sm text-white border-0" id="cpBotClose"><i class="bi bi-x-lg"></i></button>
    </div>
    <div class="cp-bot-messages" id="cpBotMessages">
      <div class="cp-bot-msg cp-bot-msg-bot">
        Hello! I am <strong>CivicBot</strong>. I can assist you with filing grievances, tracking complaint status by code, department contact numbers, and municipal FAQs.
      </div>
    </div>
    <div class="cp-bot-chips" id="cpBotChips">
      <span class="cp-bot-chip" data-msg="How do I file a complaint?">How to file</span>
      <span class="cp-bot-chip" data-msg="Track CP-2026-000001">Track Sample</span>
      <span class="cp-bot-chip" data-msg="What are your working hours?">Working hours</span>
      <span class="cp-bot-chip" data-msg="What departments are available?">Departments</span>
    </div>
    <form id="cpBotForm" class="p-2 border-top d-flex gap-2">
      <input type="text" id="cpBotInput" class="form-control form-control-sm rounded-pill" placeholder="Ask a question..." autocomplete="off">
      <button type="submit" class="btn btn-sm btn-primary rounded-circle cp-btn-icon" style="width: 34px; height: 34px;"><i class="bi bi-send-fill"></i></button>
    </form>
  `;

  document.body.appendChild(orb);
  document.body.appendChild(panel);

  const messages = panel.querySelector('#cpBotMessages');
  const form = panel.querySelector('#cpBotForm');
  const input = panel.querySelector('#cpBotInput');
  const closeBtn = panel.querySelector('#cpBotClose');
  const chips = panel.querySelector('#cpBotChips');

  orb.addEventListener('click', () => {
    panel.classList.toggle('show');
    if (panel.classList.contains('show')) {
      input.focus();
    }
  });

  closeBtn.addEventListener('click', () => panel.classList.remove('show'));

  chips.addEventListener('click', (e) => {
    if (e.target.classList.contains('cp-bot-chip')) {
      const msg = e.target.getAttribute('data-msg');
      sendUserMessage(msg);
    }
  });

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const txt = input.value.trim();
    if (txt) {
      sendUserMessage(txt);
      input.value = '';
    }
  });

  async function sendUserMessage(text) {
    appendMessage(text, 'user');

    // Add typing indicator
    const typing = document.createElement('div');
    typing.className = 'cp-bot-msg cp-bot-msg-bot text-muted';
    typing.innerHTML = `<span class="spinner-grow spinner-grow-sm me-1" role="status"></span> CivicBot is thinking...`;
    messages.appendChild(typing);
    messages.scrollTop = messages.scrollHeight;

    try {
      const res = await apiRequest('/bot/message', {
        method: 'POST',
        body: JSON.stringify({ message: text })
      });

      typing.remove();
      appendMessage(res.reply || 'I am sorry, I could not understand that request.', 'bot');

      // Update chips if suggestions provided
      if (res.quickReplies && res.quickReplies.length > 0) {
        chips.innerHTML = res.quickReplies.map(qr => `
          <span class="cp-bot-chip" data-msg="${escapeHtml(qr)}">${escapeHtml(qr)}</span>
        `).join('');
      }
    } catch {
      typing.remove();
      appendMessage('Sorry, I am having trouble connecting to the civic knowledge service. Please try again later.', 'bot');
    }
  }

  function appendMessage(text, sender) {
    const msg = document.createElement('div');
    msg.className = `cp-bot-msg cp-bot-msg-${sender}`;
    msg.innerHTML = escapeHtml(text).replace(/\n/g, '<br>');
    messages.appendChild(msg);
    messages.scrollTop = messages.scrollHeight;
  }
}

if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', initCivicBot);
}
