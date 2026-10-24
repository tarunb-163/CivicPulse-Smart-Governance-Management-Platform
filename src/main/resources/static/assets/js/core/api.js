import { showToast } from './toast.js';

const API_BASE = '/api/v1';

export async function apiRequest(endpoint, options = {}) {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
  
  const token = localStorage.getItem('cp_token');
  const headers = options.headers ? { ...options.headers } : {};

  // If not FormData, default to application/json
  if (!(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers
  };

  try {
    let response = await fetch(url, config);

    // If 401 Unauthorized, try refresh token once
    if (response.status === 401 && !options._retry && localStorage.getItem('cp_refresh_token')) {
      options._retry = true;
      const refreshed = await attemptTokenRefresh();
      if (refreshed) {
        headers['Authorization'] = `Bearer ${localStorage.getItem('cp_token')}`;
        response = await fetch(url, { ...options, headers });
      } else {
        localStorage.removeItem('cp_token');
        localStorage.removeItem('cp_refresh_token');
        localStorage.removeItem('cp_user');
        if (!window.location.pathname.includes('/login.html')) {
          window.location.href = '/login.html?expired=true';
        }
        return Promise.reject(new Error('Session expired'));
      }
    }

    // Handle responses
    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type') || '';
    let data;
    if (contentType.includes('application/json')) {
      data = await response.json();
    } else if (contentType.includes('application/pdf') || contentType.includes('image/')) {
      data = await response.blob();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      const errorMsg = (data && data.message) ? data.message : (response.statusText || 'An error occurred');
      
      // Do not show global toast if suppressed or 409 conflict handled by caller
      if (!options.suppressToast && response.status !== 409) {
        showToast(errorMsg, 'error');
      }

      const error = new Error(errorMsg);
      error.status = response.status;
      error.data = data;
      return Promise.reject(error);
    }

    return data;
  } catch (err) {
    if (!options.suppressToast && err.message !== 'Session expired') {
      showToast(err.message || 'Network communication failure', 'error');
    }
    throw err;
  }
}

async function attemptTokenRefresh() {
  const refreshToken = localStorage.getItem('cp_refresh_token');
  if (!refreshToken) return false;

  try {
    const res = await fetch(`${API_BASE}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });

    if (res.ok) {
      const data = await res.json();
      localStorage.setItem('cp_token', data.accessToken);
      if (data.refreshToken) {
        localStorage.setItem('cp_refresh_token', data.refreshToken);
      }
      return true;
    }
    return false;
  } catch {
    return false;
  }
}
