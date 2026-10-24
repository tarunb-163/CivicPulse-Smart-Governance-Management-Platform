import { isAuthenticated, getRole, getHomeRouteForRole } from './auth.js';

export function checkRouteGuard(allowedRoles = []) {
  const path = window.location.pathname;
  const isAuth = isAuthenticated();
  const currentRole = getRole();

  // Public auth pages (login/register)
  if (path.endsWith('/login.html') || path.endsWith('/register.html')) {
    if (isAuth && currentRole) {
      window.location.replace(getHomeRouteForRole(currentRole));
      return false;
    }
    return true;
  }

  // Protected pages
  if (path.includes('/citizen/') || path.includes('/officer/') || path.includes('/admin/')) {
    if (!isAuth) {
      window.location.replace(`/login.html?redirect=${encodeURIComponent(path)}`);
      return false;
    }

    if (allowedRoles.length > 0 && !allowedRoles.includes(currentRole)) {
      window.location.replace('/403.html');
      return false;
    }
  }

  return true;
}
