import { apiRequest } from '../core/api.js';
import { escapeHtml } from '../core/utils.js';

export async function requestImageClassification(file, containerElement, onCategorySelected) {
  if (!file || !file.type.startsWith('image/')) return;

  containerElement.innerHTML = `
    <div class="d-inline-flex align-items-center gap-2 p-2 rounded-pill bg-primary-subtle text-primary border border-primary-subtle animate__animated animate__fadeIn">
      <span class="spinner-border spinner-border-sm" role="status"></span>
      <small style="font-size: 0.8rem;">Analyzing photo with Vision AI...</small>
    </div>
  `;

  try {
    const formData = new FormData();
    formData.append('file', file);

    const res = await apiRequest('/complaints/classify-image', {
      method: 'POST',
      body: formData,
      suppressToast: true
    });

    if (res && res.available && res.suggestedCategoryName) {
      containerElement.innerHTML = `
        <div class="d-inline-flex align-items-center gap-2 p-2 px-3 rounded-pill bg-success-subtle text-success border border-success-subtle shadow-sm animate__animated animate__fadeIn">
          <i class="bi bi-magic text-success"></i>
          <span style="font-size: 0.85rem;">Detected: <strong>${escapeHtml(res.suggestedCategoryName)}</strong> (${Math.round((res.confidence || 0.8) * 100)}%)</span>
          <button type="button" class="btn btn-sm btn-success rounded-pill py-0 px-2 cp-accept-btn" style="font-size: 0.75rem;">Apply</button>
        </div>
      `;

      const acceptBtn = containerElement.querySelector('.cp-accept-btn');
      acceptBtn.addEventListener('click', () => {
        if (onCategorySelected) {
          onCategorySelected(res.suggestedCategoryId, res.suggestedCategoryName);
        }
        containerElement.innerHTML = `
          <div class="text-success small"><i class="bi bi-check-circle-fill me-1"></i>Applied AI Category: <strong>${escapeHtml(res.suggestedCategoryName)}</strong></div>
        `;
      });
    } else {
      // Gracefully clear placeholder if vision is not configured or no match
      containerElement.innerHTML = '';
    }
  } catch {
    containerElement.innerHTML = '';
  }
}
