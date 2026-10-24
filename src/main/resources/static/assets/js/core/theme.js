export function initTheme() {
  const saved = localStorage.getItem('cp_theme');
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  const theme = saved || (prefersDark ? 'dark' : 'light');

  setTheme(theme);

  // Setup click listeners on all theme toggle buttons
  document.querySelectorAll('.cp-theme-toggle').forEach(btn => {
    btn.addEventListener('click', toggleTheme);
  });
}

export function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('cp_theme', theme);
  updateThemeIcons(theme);
}

export function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'light';
  const next = current === 'dark' ? 'light' : 'dark';
  setTheme(next);
}

function updateThemeIcons(theme) {
  document.querySelectorAll('.cp-theme-toggle i').forEach(icon => {
    if (theme === 'dark') {
      icon.className = 'bi bi-sun-fill text-warning';
    } else {
      icon.className = 'bi bi-moon-stars-fill text-primary';
    }
  });
}

// Auto-run on script load
if (typeof document !== 'undefined') {
  initTheme();
}
