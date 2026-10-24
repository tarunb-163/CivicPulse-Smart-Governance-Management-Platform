import { apiRequest } from './api.js';

export function getToken() {
  return localStorage.getItem('cp_token');
}

export function getUser() {
  const userStr = localStorage.getItem('cp_user');
  if (!userStr) return null;
  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
}

export function getRole() {
  const user = getUser();
  return user ? user.role : null;
}

export function isAuthenticated() {
  return !!getToken();
}

export function hasRole(role) {
  const currentRole = getRole();
  return currentRole === role;
}

export async function login(email, password) {
  const data = await apiRequest('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
  });

  if (data && data.accessToken) {
    localStorage.setItem('cp_token', data.accessToken);
    if (data.refreshToken) {
      localStorage.setItem('cp_refresh_token', data.refreshToken);
    }
    if (data.user) {
      localStorage.setItem('cp_user', JSON.stringify(data.user));
    }
    return data;
  }
  throw new Error('Invalid authentication response');
}

export async function register(citizenData) {
  return await apiRequest('/auth/register', {
    method: 'POST',
    body: JSON.stringify(citizenData)
  });
}

export function logout() {
  localStorage.removeItem('cp_token');
  localStorage.removeItem('cp_refresh_token');
  localStorage.removeItem('cp_user');
  window.location.href = '/login.html';
}

export function getHomeRouteForRole(role) {
  switch (role) {
    case 'ADMIN':
      return '/admin/dashboard.html';
    case 'OFFICER':
    case 'DEPT_HEAD':
      return '/officer/dashboard.html';
    case 'CITIZEN':
    default:
      return '/citizen/dashboard.html';
  }
}
