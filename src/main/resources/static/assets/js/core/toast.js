export function showToast(message, type = 'info', duration = 4000) {
  let container = document.getElementById('cpToastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'cpToastContainer';
    container.className = 'cp-toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `cp-toast cp-toast-${type}`;

  const iconMap = {
    success: 'bi-check-circle-fill text-success',
    error: 'bi-exclamation-triangle-fill text-danger',
    warn: 'bi-exclamation-circle-fill text-warning',
    info: 'bi-info-circle-fill text-primary'
  };

  const icon = iconMap[type] || iconMap.info;

  toast.innerHTML = `
    <i class="bi ${icon} fs-5"></i>
    <div class="flex-grow-1" style="font-size: 0.9rem; font-weight: 500;">${escapeHtml(message)}</div>
    <button type="button" class="btn-close btn-close-sm" style="font-size: 0.75rem;" aria-label="Close"></button>
    <div class="cp-toast-progress" style="animation-duration: ${duration}ms;"></div>
  `;

  const closeBtn = toast.querySelector('.btn-close');
  closeBtn.addEventListener('click', () => removeToast(toast));

  container.appendChild(toast);

  const timeoutId = setTimeout(() => {
    removeToast(toast);
  }, duration);

  toast.addEventListener('mouseenter', () => {
    clearTimeout(timeoutId);
    const bar = toast.querySelector('.cp-toast-progress');
    if (bar) bar.style.animationPlayState = 'paused';
  });

  toast.addEventListener('mouseleave', () => {
    setTimeout(() => removeToast(toast), 1500);
  });
}

function removeToast(toast) {
  toast.style.transition = 'opacity 0.25s ease, transform 0.25s ease';
  toast.style.opacity = '0';
  toast.style.transform = 'translateX(100%)';
  setTimeout(() => {
    if (toast.parentElement) toast.remove();
  }, 250);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}
