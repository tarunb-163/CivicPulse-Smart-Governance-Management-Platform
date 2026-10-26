/**
 * Government Portal Accessibility & Theme Controls
 * Manages: Theme (Light/Dark), High-Contrast Mode (7:1), Text Resizing (A- / A / A+)
 */

export function initTheme() {
  let theme = 'light';
  try {
    const saved = localStorage.getItem('cp_theme') || localStorage.getItem('theme');
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    theme = saved || (prefersDark ? 'dark' : 'light');
  } catch (e) {
    console.warn('localStorage not accessible for theme', e);
  }
  setTheme(theme);

  // Restore High Contrast
  try {
    const savedContrast = localStorage.getItem('cp_contrast');
    if (savedContrast === 'high') {
      document.documentElement.setAttribute('data-contrast', 'high');
    }
  } catch (e) {}

  // Restore Text Scale
  try {
    const savedScale = localStorage.getItem('cp_text_size');
    if (savedScale) {
      setTextScale(parseFloat(savedScale));
    }
  } catch (e) {}

  // Setup click listeners on all theme toggle buttons
  document.querySelectorAll('.cp-theme-toggle').forEach(btn => {
    btn.removeEventListener('click', toggleTheme);
    btn.addEventListener('click', toggleTheme);
  });

  // Setup contrast toggle buttons
  document.querySelectorAll('.cp-contrast-btn, .cp-contrast-toggle').forEach(btn => {
    btn.addEventListener('click', toggleContrast);
  });

  // Setup text resizing buttons
  document.querySelectorAll('.cp-text-size-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const action = e.currentTarget.getAttribute('data-size') || e.currentTarget.textContent.trim();
      if (action.includes('-')) {
        setTextScale(0.9);
      } else if (action.includes('+')) {
        setTextScale(1.15);
      } else {
        setTextScale(1.0);
      }
    });
  });
}

export function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  try {
    localStorage.setItem('cp_theme', theme);
    localStorage.setItem('theme', theme);
  } catch (e) {}
  updateThemeIcons(theme);
}

export function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'light';
  const next = current === 'dark' ? 'light' : 'dark';
  setTheme(next);
}

export function toggleContrast() {
  const current = document.documentElement.getAttribute('data-contrast');
  const next = current === 'high' ? 'normal' : 'high';
  if (next === 'high') {
    document.documentElement.setAttribute('data-contrast', 'high');
  } else {
    document.documentElement.removeAttribute('data-contrast');
  }
  try {
    localStorage.setItem('cp_contrast', next);
  } catch (e) {}
}

export function setTextScale(scale) {
  document.documentElement.style.setProperty('--font-scale', scale);
  try {
    localStorage.setItem('cp_text_size', scale.toString());
  } catch (e) {}

  document.querySelectorAll('.cp-text-size-btn').forEach(btn => {
    const text = btn.textContent.trim();
    if (scale < 1.0 && text.includes('-')) {
      btn.classList.add('active');
    } else if (scale > 1.0 && text.includes('+')) {
      btn.classList.add('active');
    } else if (scale === 1.0 && text === 'A') {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });
}

function updateThemeIcons(theme) {
  document.querySelectorAll('.cp-theme-toggle i').forEach(icon => {
    if (theme === 'dark') {
      icon.className = 'bi bi-sun-fill text-warning';
    } else {
      icon.className = 'bi bi-moon-fill text-primary';
    }
  });
}

// Auto-run on script load
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTheme);
  } else {
    initTheme();
  }
}
