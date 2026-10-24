import { apiRequest } from '../core/api.js';
import { showToast } from '../core/toast.js';
import { escapeHtml, formatDate, getStatusChip, renderPriorityBadge, renderSlaRing } from '../core/utils.js';

export async function fetchOfficerQueue(params = {}) {
  const query = new URLSearchParams(params).toString();
  return await apiRequest(`/officer/complaints?${query}`);
}

export async function fetchOfficerKanban() {
  return await apiRequest('/officer/kanban');
}

export async function updateStatus(complaintId, status, remark, proofFile = null) {
  // If proof file is attached, upload multipart or post status
  if (proofFile) {
    const formData = new FormData();
    formData.append('status', status);
    formData.append('remark', remark);
    formData.append('proof', proofFile);
    return await apiRequest(`/officer/complaints/${complaintId}/status`, {
      method: 'POST',
      body: formData
    });
  }

  return await apiRequest(`/officer/complaints/${complaintId}/status`, {
    method: 'POST',
    body: JSON.stringify({ status, remark })
  });
}

export async function reassignComplaint(complaintId, targetOfficerId, reason) {
  return await apiRequest(`/officer/complaints/${complaintId}/reassign`, {
    method: 'POST',
    body: JSON.stringify({ targetOfficerId, reason })
  });
}

export async function rejectComplaint(complaintId, reason) {
  return await apiRequest(`/officer/complaints/${complaintId}/reject`, {
    method: 'POST',
    body: JSON.stringify({ reason })
  });
}

export async function mergeComplaint(complaintId, parentComplaintId, reason) {
  return await apiRequest(`/officer/complaints/${complaintId}/merge`, {
    method: 'POST',
    body: JSON.stringify({ parentComplaintId, reason })
  });
}

export function openOfficerWorkspaceDrawer(complaint, onActionSuccess) {
  let drawer = document.getElementById('cpOfficerDrawer');
  if (!drawer) {
    drawer = document.createElement('div');
    drawer.id = 'cpOfficerDrawer';
    drawer.className = 'cp-drawer';
    document.body.appendChild(drawer);
  }

  drawer.innerHTML = `
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom bg-surface">
      <div>
        <span class="cp-mono fw-bold text-primary">${escapeHtml(complaint.code)}</span>
        <div class="small text-muted">${escapeHtml(complaint.categoryName || 'General')}</div>
      </div>
      <button type="button" class="btn btn-sm btn-close" id="cpDrawerClose"></button>
    </div>

    <div class="p-4 flex-grow-1 overflow-y-auto">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div>${getStatusChip(complaint.status)}</div>
        <div>${renderSlaRing(complaint.slaDueAt, complaint.status)}</div>
      </div>

      <h5 class="fw-bold mb-2">${escapeHtml(complaint.title)}</h5>
      <p class="text-muted small mb-3">${escapeHtml(complaint.description)}</p>

      <div class="p-3 rounded-3 bg-light mb-3">
        <div class="small text-muted mb-1"><i class="bi bi-geo-alt me-1"></i>Location</div>
        <div class="small fw-semibold">${escapeHtml(complaint.address || 'Coordinates marked')}</div>
      </div>

      <!-- Action Form -->
      <h6 class="fw-bold mt-4 mb-2">Update Workflow Status</h6>
      <form id="cpDrawerStatusForm">
        <div class="mb-3">
          <label class="form-label small fw-semibold">Next Status</label>
          <select id="cpDrawerNextStatus" class="form-select form-select-sm" required>
            <option value="">Select transition...</option>
            ${complaint.status === 'SUBMITTED' || complaint.status === 'ASSIGNED' ? `
              <option value="IN_PROGRESS">IN_PROGRESS (Begin Work)</option>
              <option value="REJECTED">REJECTED (Invalid / Out of scope)</option>
            ` : ''}
            ${complaint.status === 'IN_PROGRESS' || complaint.status === 'REOPENED' ? `
              <option value="RESOLVED">RESOLVED (Redressal complete)</option>
              <option value="REJECTED">REJECTED (Cannot complete)</option>
            ` : ''}
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label small fw-semibold">Remark / Resolution Note *</label>
          <textarea id="cpDrawerRemark" class="form-control form-control-sm" rows="3" placeholder="Provide clear municipal action taken..." required></textarea>
        </div>

        <div class="mb-3">
          <label class="form-label small fw-semibold">Resolution Proof (Photo)</label>
          <input type="file" id="cpDrawerProof" class="form-control form-control-sm" accept="image/*">
        </div>

        <button type="submit" class="btn btn-primary btn-sm w-100 rounded-pill mb-2">Submit Status Update</button>
      </form>

      <hr class="my-4">

      <!-- Additional Actions: Reassign & Merge -->
      <div class="d-grid gap-2">
        <button type="button" class="btn btn-outline-secondary btn-sm rounded-pill" id="cpDrawerMergeBtn">
          <i class="bi bi-intersect me-1"></i>Merge into Parent Complaint
        </button>
      </div>
    </div>
  `;

  drawer.classList.add('show');

  drawer.querySelector('#cpDrawerClose').addEventListener('click', () => drawer.classList.remove('show'));

  const form = drawer.querySelector('#cpDrawerStatusForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const status = drawer.querySelector('#cpDrawerNextStatus').value;
    const remark = drawer.querySelector('#cpDrawerRemark').value.trim();
    const proofFile = drawer.querySelector('#cpDrawerProof').files[0];

    try {
      await updateStatus(complaint.id, status, remark, proofFile);
      showToast('Status updated successfully', 'success');
      drawer.classList.remove('show');
      if (onActionSuccess) onActionSuccess();
    } catch (err) {
      // toast shown by api
    }
  });

  drawer.querySelector('#cpDrawerMergeBtn').addEventListener('click', async () => {
    const parentCode = prompt('Enter parent Complaint Code to merge into (e.g. CP-2026-000001):');
    if (!parentCode) return;
    const reason = prompt('Reason for merging:') || 'Duplicate issue merged by officer';

    try {
      // Find parent ID by code
      const parent = await apiRequest(`/public/complaints/code/${encodeURIComponent(parentCode.trim())}`);
      await mergeComplaint(complaint.id, parent.id, reason);
      showToast('Complaint merged successfully', 'success');
      drawer.classList.remove('show');
      if (onActionSuccess) onActionSuccess();
    } catch (err) {
      showToast(err.message || 'Failed to merge complaint', 'error');
    }
  });
}
