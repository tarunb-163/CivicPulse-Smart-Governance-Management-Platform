let currentLang = localStorage.getItem('cp_lang') || 'en';
let translations = {};

export async function initI18n() {
  await loadTranslations(currentLang);
  applyTranslations();

  document.querySelectorAll('.cp-lang-select').forEach(select => {
    select.value = currentLang;
    select.addEventListener('change', async (e) => {
      await switchLanguage(e.target.value);
    });
  });
}

export async function switchLanguage(lang) {
  currentLang = lang;
  localStorage.setItem('cp_lang', lang);
  await loadTranslations(lang);
  applyTranslations();
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
