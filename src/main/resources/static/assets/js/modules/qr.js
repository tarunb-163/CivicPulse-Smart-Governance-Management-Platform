import { escapeHtml } from '../core/utils.js';

export function renderQrCard(complaintCode, containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const qrUrl = `/api/v1/public/qr/${encodeURIComponent(complaintCode)}.png`;
  const trackUrl = `${window.location.origin}/track.html?code=${encodeURIComponent(complaintCode)}`;

  container.innerHTML = `
    <div class="cp-card text-center p-4">
      <h6 class="fw-bold mb-3"><i class="bi bi-qr-code-scan text-primary me-2"></i>Official QR Verification</h6>
      <div class="bg-white p-3 rounded-3 d-inline-block shadow-sm mb-3">
        <img src="${qrUrl}" alt="QR code for ${escapeHtml(complaintCode)}" class="img-fluid" style="width: 170px; height: 170px;">
      </div>
      <div class="cp-mono fw-bold fs-5 text-primary mb-1">${escapeHtml(complaintCode)}</div>
      <small class="text-muted d-block mb-3">Scan with mobile camera to view real-time status</small>
      <div class="d-flex justify-content-center gap-2">
        <a href="${qrUrl}" download="${complaintCode}-qr.png" class="btn btn-sm btn-outline-primary rounded-pill">
          <i class="bi bi-download me-1"></i>Download PNG
        </a>
        <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill" onclick="window.print()">
          <i class="bi bi-printer me-1"></i>Print QR
        </button>
      </div>
    </div>
  `;
}
