import { apiRequest } from '../core/api.js';
import { showToast } from '../core/toast.js';

export async function fetchDepartments() {
  return await apiRequest('/public/departments');
}

export async function createDepartment(data) {
  return await apiRequest('/admin/departments', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export async function updateDepartment(id, data) {
  return await apiRequest(`/admin/departments/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
}

export async function deleteDepartment(id) {
  return await apiRequest(`/admin/departments/${id}`, {
    method: 'DELETE'
  });
}

export async function fetchCategories() {
  return await apiRequest('/public/categories');
}

export async function createCategory(data) {
  return await apiRequest('/admin/categories', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export async function updateCategory(id, data) {
  return await apiRequest(`/admin/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
}

export async function deleteCategory(id) {
  return await apiRequest(`/admin/categories/${id}`, {
    method: 'DELETE'
  });
}

export async function fetchKeywordRules() {
  return await apiRequest('/admin/rules');
}

export async function createKeywordRule(data) {
  return await apiRequest('/admin/rules', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export async function updateKeywordRule(id, data) {
  return await apiRequest(`/admin/rules/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
}

export async function deleteKeywordRule(id) {
  return await apiRequest(`/admin/rules/${id}`, {
    method: 'DELETE'
  });
}

export async function testClassifier(text) {
  return await apiRequest('/admin/rules/test', {
    method: 'POST',
    body: JSON.stringify({ text })
  });
}

export async function fetchUsers(params = {}) {
  const query = new URLSearchParams(params).toString();
  return await apiRequest(`/admin/users?${query}`);
}

export async function createOfficer(data) {
  return await apiRequest('/admin/officers', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export async function toggleUserStatus(userId, enabled) {
  return await apiRequest(`/admin/users/${userId}/status?enabled=${enabled}`, {
    method: 'PATCH'
  });
}
export const toggleUserActive = toggleUserStatus;

export async function changeUserRole(userId, role, departmentId) {
  let url = `/admin/users/${userId}/role?role=${role}`;
  if (departmentId) url += `&departmentId=${departmentId}`;
  return await apiRequest(url, {
    method: 'PATCH'
  });
}

export async function triggerSlaScheduler() {
  const res = await apiRequest('/admin/sla/trigger-scheduler', {
    method: 'POST'
  });
  showToast(res.message || 'SLA escalation check completed', 'success');
  return res;
}

export async function fetchAuditLogs(params = {}) {
  const query = new URLSearchParams(params).toString();
  return await apiRequest(`/admin/audit-logs?${query}`);
}

export async function fetchSettings() {
  return await apiRequest('/admin/settings');
}

export async function updateSetting(key, value, description) {
  const val = (value && typeof value === 'object') ? value.value : value;
  const desc = (value && typeof value === 'object') ? value.description : description;
  return await apiRequest('/admin/settings', {
    method: 'PUT',
    body: JSON.stringify({ key, value: val, settingKey: key, settingValue: val, description: desc })
  });
}

export async function fetchDevOutbox() {
  return await apiRequest('/admin/dev-outbox');
}
