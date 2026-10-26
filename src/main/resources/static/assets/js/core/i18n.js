let currentLang = 'en';
try {
  currentLang = localStorage.getItem('cp_lang') || 'en';
} catch (e) {}

let translations = {};

export async function initI18n() {
  document.documentElement.setAttribute('lang', currentLang);
  await loadTranslations(currentLang);
  applyTranslations();

  document.querySelectorAll('.cp-lang-select').forEach(select => {
    select.value = currentLang;
    select.addEventListener('change', async (e) => {
      await switchLanguage(e.target.value);
    });
  });

  document.querySelectorAll('[data-lang-switch]').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const targetLang = e.currentTarget.getAttribute('data-lang-switch');
      if (targetLang) {
        await switchLanguage(targetLang);
      }
    });
  });
}

export async function switchLanguage(lang) {
  currentLang = lang;
  try {
    localStorage.setItem('cp_lang', lang);
  } catch (e) {}
  document.documentElement.setAttribute('lang', lang);
  await loadTranslations(lang);
  applyTranslations();

  // Update any language selectors on page
  document.querySelectorAll('.cp-lang-select').forEach(s => {
    s.value = lang;
  });
}

export async function loadTranslations(lang) {
  try {
    const res = await fetch(`/assets/i18n/${lang}.json`);
    if (res.ok) {
      translations = await res.json();
    } else {
      console.warn(`Could not load translations for ${lang}`);
    }
  } catch (err) {
    console.warn(`Failed loading i18n file for ${lang}`, err);
  }
}

export function t(key, defaultValue = '') {
  return translations[key] || defaultValue || key;
}

export function applyTranslations() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (translations[key]) {
      if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
        el.placeholder = translations[key];
      } else {
        el.textContent = translations[key];
      }
    }
  });
}

// Auto-run if document is available
if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initI18n);
  } else {
    initI18n();
  }
}
