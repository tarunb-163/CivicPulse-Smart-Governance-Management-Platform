export function animateCounter(element, target, duration = 1200) {
  const start = 0;
  const startTime = performance.now();
  const isFloat = String(target).includes('.');
  const numTarget = parseFloat(target) || 0;

  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    // Ease out quart
    const ease = 1 - Math.pow(1 - progress, 4);
    const current = start + (numTarget - start) * ease;

    element.textContent = isFloat ? current.toFixed(1) : Math.round(current).toLocaleString();

    if (progress < 1) {
      requestAnimationFrame(update);
    } else {
      element.textContent = isFloat ? numTarget.toFixed(1) : numTarget.toLocaleString();
    }
  }

  requestAnimationFrame(update);
}

export function initCounters() {
  const observer = new IntersectionObserver((entries, obs) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const el = entry.target;
        const targetVal = el.getAttribute('data-counter');
        if (targetVal) {
          animateCounter(el, targetVal);
          obs.unobserve(el);
        }
      }
    });
  }, { threshold: 0.2 });

  document.querySelectorAll('[data-counter]').forEach(el => observer.observe(el));
}

if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', initCounters);
}
