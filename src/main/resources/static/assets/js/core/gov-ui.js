/**
 * Government Portal Shared UI Interactions (GIGW 3.0 / WCAG 2.1 AA)
 * - Ticker Play/Pause control
 * - Mobile navigation menu drawer
 * - Accessible keyboard navigation & focus traps
 */

export function initGovUi() {
  // 1. Ticker Pause/Play Control
  const tickerBtn = document.getElementById('tickerToggle') || document.getElementById('tickerPauseBtn');
  const tickerTrack = document.getElementById('tickerText') || document.querySelector('.cp-ticker-text') || document.querySelector('.cp-ticker-track');
  if (tickerBtn && tickerTrack) {
    let isPaused = false;
    tickerBtn.addEventListener('click', () => {
      isPaused = !isPaused;
      if (isPaused) {
        tickerTrack.style.animationPlayState = 'paused';
        tickerTrack.classList.add('paused');
        tickerBtn.innerHTML = '<i class="bi bi-play-fill"></i> Play';
        tickerBtn.setAttribute('aria-label', 'Play Announcements');
      } else {
        tickerTrack.style.animationPlayState = 'running';
        tickerTrack.classList.remove('paused');
        tickerBtn.innerHTML = '<i class="bi bi-pause-fill"></i> Pause';
        tickerBtn.setAttribute('aria-label', 'Pause Announcements');
      }
    });

    // Automatically pause if reduced motion is preferred
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      tickerTrack.style.animationPlayState = 'paused';
      tickerTrack.classList.add('paused');
      tickerBtn.innerHTML = '<i class="bi bi-play-fill"></i> Play';
      tickerBtn.setAttribute('aria-label', 'Play Announcements');
    }
  }

  // 2. Mobile Main Navigation Toggle
  const navToggle = document.querySelector('.cp-nav-mobile-toggle');
  const navMenu = document.querySelector('.cp-nav-menu');
  if (navToggle && navMenu) {
    navToggle.addEventListener('click', () => {
      const isExpanded = navToggle.getAttribute('aria-expanded') === 'true';
      navToggle.setAttribute('aria-expanded', !isExpanded);
      navMenu.classList.toggle('show');
    });
  }

  // 3. Portal Sidebar Mobile Toggle
  const sidebarToggle = document.querySelector('.cp-sidebar-toggle');
  const sidebar = document.querySelector('.cp-sidebar');
  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener('click', () => {
      sidebar.classList.toggle('show');
    });
  }

  // 4. Global Search Redirect (if search input exists in header)
  const headerSearch = document.getElementById('headerSearchInput');
  if (headerSearch) {
    headerSearch.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        const query = headerSearch.value.trim();
        if (query.startsWith('CP-')) {
          window.location.href = `/track.html?code=${encodeURIComponent(query)}`;
        } else if (query) {
          window.location.href = `/help.html?q=${encodeURIComponent(query)}`;
        }
      }
    });
  }

  // 5. Accordion Accessibility
  document.querySelectorAll('.cp-accordion-button').forEach(btn => {
    btn.addEventListener('click', () => {
      const isExpanded = btn.getAttribute('aria-expanded') === 'true';
      btn.setAttribute('aria-expanded', !isExpanded);
      const targetId = btn.getAttribute('data-bs-target') || btn.getAttribute('aria-controls');
      if (targetId) {
        const body = document.querySelector(targetId);
        if (body) {
          body.style.display = isExpanded ? 'none' : 'block';
        }
      }
    });
  });
}

// Auto-run when DOM is ready
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initGovUi);
  } else {
    initGovUi();
  }
}
