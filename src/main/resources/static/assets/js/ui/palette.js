import { getRole, isAuthenticated } from '../core/auth.js';

export function initCommandPalette() {
  const modal = document.createElement('div');
  modal.id = 'cpPaletteModal';
  modal.className = 'cp-modal-backdrop';
  modal.innerHTML = `
    <div class="cp-modal-dialog" style="max-width: 520px; padding: 16px;">
      <div class="d-flex align-items-center gap-2 border-bottom pb-2 mb-3">
        <i class="bi bi-search text-muted fs-5"></i>
        <input type="text" id="cpPaletteInput" class="form-control border-0 shadow-none bg-transparent" placeholder="Type a command, page or complaint code..." autocomplete="off">
        <span class="badge bg-secondary-subtle text-muted">ESC</span>
      </div>
      <div id="cpPaletteResults" class="list-group list-group-flush" style="max-height: 320px; overflow-y: auto;">
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  const input = modal.querySelector('#cpPaletteInput');
  const resultsContainer = modal.querySelector('#cpPaletteResults');

  const defaultItems = [
    { title: 'Track Complaint', icon: 'bi-search', url: '/track.html', group: 'Navigation' },
    { title: 'Public Transparency Dashboard', icon: 'bi-graph-up', url: '/transparency.html', group: 'Navigation' },
    { title: 'Toggle Dark / Light Theme', icon: 'bi-moon-stars', action: 'toggle-theme', group: 'Actions' },
    { title: 'File New Complaint', icon: 'bi-plus-circle', url: '/citizen/new-complaint.html', group: 'Citizen' },
    { title: 'My Grievances', icon: 'bi-collection', url: '/citizen/my-complaints.html', group: 'Citizen' },
    { title: 'Officer Queue', icon: 'bi-inbox', url: '/officer/queue.html', group: 'Officer' },
    { title: 'Officer Kanban Board', icon: 'bi-kanban', url: '/officer/board.html', group: 'Officer' },
    { title: 'Admin Analytics', icon: 'bi-speedometer2', url: '/admin/dashboard.html', group: 'Admin' },
    { title: 'Admin Departments', icon: 'bi-building', url: '/admin/departments.html', group: 'Admin' },
    { title: 'Admin Keyword Rules', icon: 'bi-cpu', url: '/admin/rules.html', group: 'Admin' }
  ];

  function renderList(query = '') {
    const q = query.toLowerCase().trim();
    const filtered = defaultItems.filter(item => item.title.toLowerCase().includes(q));

    if (filtered.length === 0) {
      if (q.startsWith('cp-')) {
        resultsContainer.innerHTML = `
          <a href="/track.html?code=${encodeURIComponent(q.toUpperCase())}" class="list-group-item list-group-item-action border-0 rounded-3 mb-1 d-flex align-items-center gap-3">
            <i class="bi bi-box-arrow-up-right text-primary fs-5"></i>
            <div>
              <div class="fw-semibold">Track code: ${q.toUpperCase()}</div>
              <small class="text-muted">Jump directly to public tracking</small>
            </div>
          </a>
        `;
        return;
      }
      resultsContainer.innerHTML = `<div class="p-4 text-center text-muted">No commands or pages found</div>`;
      return;
    }

    resultsContainer.innerHTML = filtered.map(item => `
      <a href="${item.url || '#'}" data-action="${item.action || ''}" class="list-group-item list-group-item-action border-0 rounded-3 mb-1 d-flex align-items-center gap-3 cp-palette-item">
        <i class="bi ${item.icon} text-primary fs-5"></i>
        <div>
          <div class="fw-semibold">${item.title}</div>
          <small class="text-muted">${item.group}</small>
        </div>
      </a>
    `).join('');

    resultsContainer.querySelectorAll('.cp-palette-item').forEach(el => {
      el.addEventListener('click', (e) => {
        const action = el.getAttribute('data-action');
        if (action === 'toggle-theme') {
          e.preventDefault();
          const current = document.documentElement.getAttribute('data-theme') || 'light';
          document.documentElement.setAttribute('data-theme', current === 'dark' ? 'light' : 'dark');
          localStorage.setItem('cp_theme', current === 'dark' ? 'light' : 'dark');
          closePalette();
        } else {
          closePalette();
        }
      });
    });
  }

  function openPalette() {
    modal.classList.add('show');
    input.value = '';
    renderList();
    setTimeout(() => input.focus(), 50);
  }

  function closePalette() {
    modal.classList.remove('show');
  }

  input.addEventListener('input', (e) => renderList(e.target.value));

  modal.addEventListener('click', (e) => {
    if (e.target === modal) closePalette();
  });

  window.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      if (modal.classList.contains('show')) {
        closePalette();
      } else {
        openPalette();
      }
    }
    if (e.key === 'Escape' && modal.classList.contains('show')) {
      closePalette();
    }
  });

  document.querySelectorAll('.cp-open-palette').forEach(btn => {
    btn.addEventListener('click', openPalette);
  });
}

if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', initCommandPalette);
}
