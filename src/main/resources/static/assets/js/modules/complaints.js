import { apiRequest } from '../core/api.js';
import { showToast } from '../core/toast.js';
import { escapeHtml } from '../core/utils.js';

export async function classifyText(text) {
  if (!text || text.trim().length < 5) return null;
  return await apiRequest('/public/classify', {
    method: 'POST',
    body: JSON.stringify({ text }),
    suppressToast: true
  });
}

export async function checkDuplicates(lat, lng, text) {
  return await apiRequest('/complaints/check-duplicates', {
    method: 'POST',
    body: JSON.stringify({ lat, lng, text }),
    suppressToast: true
  });
}

export async function submitComplaint(formData) {
  return await apiRequest('/complaints', {
    method: 'POST',
    body: formData
  });
}

export async function supportComplaint(complaintId) {
  const res = await apiRequest(`/complaints/${complaintId}/support`, {
    method: 'POST'
  });
  showToast(res.message || 'Supported complaint successfully', 'success');
  return res;
}

export async function upvoteComplaint(complaintId) {
  const res = await apiRequest(`/complaints/${complaintId}/upvote`, {
    method: 'POST'
  });
  showToast('Upvote recorded', 'success');
  return res;
}

export async function addComment(complaintId, text, isInternal = false) {
  return await apiRequest(`/complaints/${complaintId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ comment: text, isInternal })
  });
}

export async function submitFeedback(complaintId, rating, comment, satisfied) {
  return await apiRequest(`/complaints/${complaintId}/feedback`, {
    method: 'POST',
    body: JSON.stringify({ rating, comment, satisfied })
  });
}

export async function reopenComplaint(complaintId, reason) {
  return await apiRequest(`/complaints/${complaintId}/reopen`, {
    method: 'POST',
    body: JSON.stringify({ reason })
  });
}

export async function downloadReceipt(complaintId, code) {
  try {
    const blob = await apiRequest(`/complaints/${complaintId}/receipt.pdf`);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `CivicPulse-${code}-Receipt.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
    showToast('Receipt downloaded', 'success');
  } catch (err) {
    showToast('Failed to download PDF receipt', 'error');
  }
}

export function triggerConfetti() {
  if (typeof confetti === 'function') {
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 }
    });
  }
}

export function showDuplicateDialog(candidates = [], onSupport, onSubmitAnyway) {
  const existing = document.getElementById('cpDuplicateModal');
  if (existing) existing.remove();

  const modal = document.createElement('div');
  modal.id = 'cpDuplicateModal';
  modal.className = 'cp-modal-backdrop show';
  modal.innerHTML = `
    <div class="cp-modal-dialog p-4" style="max-width: 620px;">
      <div class="d-flex align-items-center gap-2 text-warning mb-2">
        <i class="bi bi-exclamation-triangle-fill fs-3"></i>
        <h5 class="fw-bold mb-0 text-warning">Similar Complaints Found Nearby</h5>
      </div>
      <p class="text-muted small mb-3">
        A similar issue was already reported within 300 meters. Supporting an existing issue increases its municipal priority score and avoids duplicate queues.
      </p>

      <div class="list-group mb-3" style="max-height: 280px; overflow-y: auto;">
        ${candidates.map(c => `
          <div class="list-group-item list-group-item-action border rounded-3 p-3 mb-2 cp-dup-candidate">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <span class="cp-mono fw-bold text-primary">${escapeHtml(c.code)}</span>
              <span class="badge bg-secondary-subtle text-dark">${Math.round(c.distanceMeters || 0)}m away</span>
            </div>
            <h6 class="fw-semibold mb-1">${escapeHtml(c.title)}</h6>
            <div class="d-flex justify-content-between align-items-center mt-2">
              <span class="badge bg-info-subtle text-info">${escapeHtml(c.status)}</span>
              <button type="button" class="btn btn-sm btn-primary rounded-pill cp-support-btn" data-id="${c.id}">
                <i class="bi bi-heart-fill me-1"></i>Support this instead
              </button>
            </div>
          </div>
        `).join('')}
      </div>

      <div class="d-flex justify-content-between align-items-center pt-2 border-top">
        <button type="button" class="btn btn-ghost text-muted cp-cancel-btn">Cancel</button>
        <button type="button" class="btn btn-outline-danger rounded-pill cp-submit-anyway-btn">
          My issue is different, submit anyway
        </button>
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  modal.querySelector('.cp-cancel-btn').addEventListener('click', () => modal.remove());

  modal.querySelectorAll('.cp-support-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      modal.remove();
      if (onSupport) onSupport(id);
    });
  });

  modal.querySelector('.cp-submit-anyway-btn').addEventListener('click', () => {
    modal.remove();
    if (onSubmitAnyway) onSubmitAnyway();
  });
}
