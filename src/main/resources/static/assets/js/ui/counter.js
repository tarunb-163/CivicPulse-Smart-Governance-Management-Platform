export function animateCounter(element, target, duration = 250) {
  const isFloat = String(target).includes('.');
  const numTarget = parseFloat(target) || 0;

  // Immediately display value if reduced motion is preferred
  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    element.textContent = isFloat ? numTarget.toFixed(1) : numTarget.toLocaleString();
    return;
  }

  const start = 0;
  const startTime = performance.now();

  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const ease = 1 - Math.pow(1 - progress, 2);
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
  }, { threshold: 0.1 });

  document.querySelectorAll('[data-counter]').forEach(el => observer.observe(el));
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCounters);
  } else {
    initCounters();
  }
}
