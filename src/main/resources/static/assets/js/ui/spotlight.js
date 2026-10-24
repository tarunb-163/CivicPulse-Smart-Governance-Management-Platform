export function initSpotlight() {
  if ('ontouchstart' in window) return; // Disable on touch devices

  document.querySelectorAll('.cp-card, .cp-spotlight').forEach(card => {
    if (card._hasSpotlight) return;
    card._hasSpotlight = true;

    card.addEventListener('pointermove', e => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      card.style.setProperty('--mx', `${x}px`);
      card.style.setProperty('--my', `${y}px`);
    });
  });
}

// Auto-run on load
if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', initSpotlight);
}
