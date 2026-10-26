import { apiRequest } from '../core/api.js';
import { escapeHtml } from '../core/utils.js';

export function initCivicBot() {
  if (document.getElementById('cpBotPanel')) return;

  const trigger = document.createElement('button');
  trigger.id = 'botTrigger';
  trigger.className = 'cp-bot-trigger';
  trigger.setAttribute('type', 'button');
  trigger.setAttribute('aria-expanded', 'false');
  trigger.setAttribute('aria-controls', 'cpBotPanel');
  trigger.setAttribute('aria-label', 'Open Virtual Assistant');
  trigger.innerHTML = `<i class="bi bi-headset"></i> <span>Virtual Assistant</span>`;

  const panel = document.createElement('div');
  panel.id = 'cpBotPanel';
  panel.className = 'cp-bot-panel';
  panel.setAttribute('role', 'region');
  panel.setAttribute('aria-label', 'Virtual Assistant Dialog');
  panel.innerHTML = `
    <div class="cp-bot-header">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-shield-check fs-5"></i>
        <div>
          <h2 class="h6 mb-0 fw-bold text-white">Virtual Assistant</h2>
          <small style="opacity: 0.9; font-size: 0.75rem;">CivicPulse Citizen Helpdesk</small>
        </div>
      </div>
      <button type="button" class="btn btn-sm text-white border-0 p-1" id="cpBotClose" aria-label="Close Assistant"><i class="bi bi-x-lg"></i></button>
    </div>
    <div class="cp-bot-messages" id="cpBotMessages" aria-live="polite" aria-atomic="false">
      <div class="cp-bot-bubble-bot">
        Namaste! I am the <strong>CivicPulse Virtual Assistant</strong>. I can assist you with lodging grievances, checking complaint status, department information, and operating hours.
      </div>
    </div>
    <div class="p-2 border-top bg-light" id="cpBotChips" style="display: flex; gap: 6px; flex-wrap: wrap;">
      <button type="button" class="btn btn-sm cp-btn-secondary cp-btn-sm cp-bot-chip" data-msg="How do I file a complaint?">How to lodge</button>
      <button type="button" class="btn btn-sm cp-btn-secondary cp-btn-sm cp-bot-chip" data-msg="Track CP-2026-000001">Track Sample</button>
      <button type="button" class="btn btn-sm cp-btn-secondary cp-btn-sm cp-bot-chip" data-msg="What are your working hours?">Office timings</button>
      <button type="button" class="btn btn-sm cp-btn-secondary cp-btn-sm cp-bot-chip" data-msg="What departments are available?">Departments</button>
    </div>
    <form id="cpBotForm" class="p-2 border-top d-flex gap-2 bg-white">
      <input type="text" id="cpBotInput" class="form-control form-control-sm" placeholder="Type your query (e.g. Track CP-...)" autocomplete="off" aria-label="Assistant question">
      <button type="submit" class="btn btn-sm cp-btn-primary" aria-label="Send Message"><i class="bi bi-send-fill"></i></button>
    </form>
  `;

  document.body.appendChild(trigger);
  document.body.appendChild(panel);

  const messages = panel.querySelector('#cpBotMessages');
  const form = panel.querySelector('#cpBotForm');
  const input = panel.querySelector('#cpBotInput');
  const closeBtn = panel.querySelector('#cpBotClose');
  const chips = panel.querySelector('#cpBotChips');

  trigger.addEventListener('click', () => {
    const isShowing = panel.classList.toggle('show');
    trigger.setAttribute('aria-expanded', isShowing ? 'true' : 'false');
    if (isShowing) {
      input.focus();
    }
  });

  closeBtn.addEventListener('click', () => {
    panel.classList.remove('show');
    trigger.setAttribute('aria-expanded', 'false');
    trigger.focus();
  });

  chips.addEventListener('click', (e) => {
    const targetBtn = e.target.closest('.cp-bot-chip');
    if (targetBtn) {
      const msg = targetBtn.getAttribute('data-msg');
      if (msg) sendUserMessage(msg);
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
    typing.className = 'cp-bot-bubble-bot text-muted';
    typing.innerHTML = `<span class="spinner-border spinner-border-sm me-1" role="status"></span> Assistant is processing your request...`;
    messages.appendChild(typing);
    messages.scrollTop = messages.scrollHeight;

    try {
      const res = await apiRequest('/bot/message', {
        method: 'POST',
        body: JSON.stringify({ message: text })
      });

      typing.remove();
      appendMessage(res.reply || 'Information received.', 'bot');

      // Update chips if suggestions provided
      if (res.quickReplies && res.quickReplies.length > 0) {
        chips.innerHTML = res.quickReplies.map(qr => `
          <button type="button" class="btn btn-sm cp-btn-secondary cp-btn-sm cp-bot-chip" data-msg="${escapeHtml(qr)}">${escapeHtml(qr)}</button>
        `).join('');
      }
    } catch {
      typing.remove();
      appendMessage('Sorry, the citizen helpdesk service is currently unreachable. Please try again later or contact the civic helpline.', 'bot');
    }
  }

  function appendMessage(text, sender) {
    const msg = document.createElement('div');
    msg.className = sender === 'user' ? 'cp-bot-bubble-user' : 'cp-bot-bubble-bot';
    msg.innerHTML = escapeHtml(text).replace(/\n/g, '<br>');
    messages.appendChild(msg);
    messages.scrollTop = messages.scrollHeight;
  }
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCivicBot);
  } else {
    initCivicBot();
  }
}
