import { apiRequest } from '../core/api.js';
import { escapeHtml, formatRelativeTime } from '../core/utils.js';

export async function initNotifications() {
  const badge = document.getElementById('cpNotifBadge');
  const listContainer = document.getElementById('cpNotifList');
  const readAllBtn = document.getElementById('cpNotifReadAll');

  if (!badge) return;

  async function updateBadge() {
    try {
      const res = await apiRequest('/notifications/unread-count', { suppressToast: true });
      const count = res.count || 0;
      if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.classList.remove('d-none');
        badge.classList.add('cp-bell-wiggle');
        setTimeout(() => badge.classList.remove('cp-bell-wiggle'), 700);
      } else {
        badge.classList.add('d-none');
      }
    } catch {
      // ignore
    }
  }

  async function loadNotifications() {
    if (!listContainer) return;
    listContainer.innerHTML = `<div class="p-3 text-center text-muted"><span class="spinner-border spinner-border-sm me-2"></span>Loading...</div>`;

    try {
      const res = await apiRequest('/notifications?size=15');
      const items = res.content || [];

      if (items.length === 0) {
        listContainer.innerHTML = `<div class="p-4 text-center text-muted small">No notifications yet</div>`;
        return;
      }

      listContainer.innerHTML = items.map(n => `
        <div class="p-2 border-bottom d-flex align-items-start gap-2 ${n.read ? 'opacity-75' : 'bg-primary-subtle rounded-2 mb-1'} cp-notif-row" data-id="${n.id}">
          <i class="bi ${n.type === 'SLA_ALERT' ? 'bi-exclamation-triangle-fill text-danger' : 'bi-bell-fill text-primary'} mt-1"></i>
          <div class="flex-grow-1">
            <div class="fw-semibold small">${escapeHtml(n.title)}</div>
            <div class="text-muted" style="font-size: 0.8rem;">${escapeHtml(n.message)}</div>
            <small class="text-secondary" style="font-size: 0.72rem;">${formatRelativeTime(n.createdAt)}</small>
          </div>
          ${!n.read ? `<button class="btn btn-sm text-primary p-0 cp-mark-read" title="Mark read" data-id="${n.id}"><i class="bi bi-check-lg"></i></button>` : ''}
        </div>
      `).join('');

      listContainer.querySelectorAll('.cp-mark-read').forEach(btn => {
        btn.addEventListener('click', async (e) => {
          e.stopPropagation();
          const id = btn.getAttribute('data-id');
          await apiRequest(`/notifications/${id}/read`, { method: 'PATCH' });
          btn.closest('.cp-notif-row').classList.remove('bg-primary-subtle');
          btn.remove();
          updateBadge();
        });
      });
    } catch {
      listContainer.innerHTML = `<div class="p-3 text-center text-danger small">Failed to load alerts</div>`;
    }
  }

  if (readAllBtn) {
    readAllBtn.addEventListener('click', async () => {
      await apiRequest('/notifications/read-all', { method: 'PATCH' });
      await loadNotifications();
      updateBadge();
    });
  }

  // Hook into dropdown toggle
  const notifDropdown = document.getElementById('cpNotifDropdown');
  if (notifDropdown) {
    notifDropdown.addEventListener('show.bs.dropdown', loadNotifications);
  }

  updateBadge();
  setInterval(updateBadge, 45000); // Check every 45s
}

if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', initNotifications);
}
