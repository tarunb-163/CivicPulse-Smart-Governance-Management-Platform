export function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatRelativeTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const now = new Date();
  const diffSec = Math.floor((now - d) / 1000);

  if (diffSec < 60) return 'Just now';
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}h ago`;
  const diffDays = Math.floor(diffHour / 24);
  if (diffDays < 30) return `${diffDays}d ago`;
  return formatDate(dateStr);
}

export function getStatusChip(status) {
  const norm = (status || '').toUpperCase();
  const cls = `cp-chip cp-chip-${norm.toLowerCase()}`;
  return `<span class="${cls}">${escapeHtml(norm.replace('_', ' '))}</span>`;
}

export function calculateSlaRemaining(slaDueAt) {
  if (!slaDueAt) return { text: 'No SLA', isOverdue: false, percent: 100 };
  const due = new Date(slaDueAt).getTime();
  const now = Date.now();
  const diffMs = due - now;

  if (diffMs <= 0) {
    const overdueHrs = Math.abs(Math.floor(diffMs / (1000 * 60 * 60)));
    return { text: `Overdue ${overdueHrs}h`, isOverdue: true, percent: 0 };
  }

  const hours = Math.floor(diffMs / (1000 * 60 * 60));
  const days = Math.floor(hours / 24);
  const text = days > 0 ? `${days}d ${hours % 24}h left` : `${hours}h left`;
  
  // Estimate percentage based on standard 48h SLA window
  const pct = Math.min(100, Math.max(5, Math.round((diffMs / (48 * 3600 * 1000)) * 100)));
  return { text, isOverdue: false, percent: pct };
}

export function renderSlaRing(slaDueAt, status) {
  if (status === 'RESOLVED' || status === 'CLOSED' || status === 'REJECTED' || status === 'MERGED') {
    return `<span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-check2-all me-1"></i>Completed</span>`;
  }

  const sla = calculateSlaRemaining(slaDueAt);
  const strokeColor = sla.isOverdue ? '#ef4444' : (sla.percent > 40 ? '#10b981' : '#f59e0b');
  const circumference = 2 * Math.PI * 18;
  const offset = circumference - (sla.percent / 100) * circumference;

  return `
    <div class="cp-sla-ring" title="${escapeHtml(sla.text)}">
      <svg viewBox="0 0 44 44">
        <circle class="cp-sla-ring-bg" cx="22" cy="22" r="18" />
        <circle class="cp-sla-ring-progress" cx="22" cy="22" r="18"
          stroke="${strokeColor}"
          stroke-dasharray="${circumference}"
          stroke-dashoffset="${offset}" />
      </svg>
      <span class="cp-sla-ring-text" style="color: ${strokeColor};">${sla.isOverdue ? '!' : `${sla.percent}%`}</span>
    </div>
  `;
}

export function renderPriorityBadge(score) {
  const s = Number(score) || 0;
  if (s >= 50) {
    return `<span class="badge bg-danger text-white"><i class="bi bi-fire me-1"></i>Urgent (${s})</span>`;
  } else if (s >= 30) {
    return `<span class="badge bg-warning text-dark"><i class="bi bi-exclamation-triangle me-1"></i>High (${s})</span>`;
  } else if (s >= 15) {
    return `<span class="badge bg-info text-dark">Medium (${s})</span>`;
  }
  return `<span class="badge bg-secondary text-white">Low (${s})</span>`;
}

export function renderRatingStars(rating) {
  const r = Math.round(Number(rating) || 0);
  let html = '';
  for (let i = 1; i <= 5; i++) {
    if (i <= r) {
      html += '<i class="bi bi-star-fill text-warning me-1"></i>';
    } else {
      html += '<i class="bi bi-star text-muted me-1"></i>';
    }
  }
  return html;
}
