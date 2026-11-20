import { axiosConfig } from './axiosConfig';

export const getUsers = (page = 0, size = 10, role, status, search) => {
  const params = new URLSearchParams();
  params.append('page', page);
  params.append('size', size);
  if (role) params.append('role', role);
  if (status) params.append('status', status);
  if (search) params.append('search', search);
  return axiosConfig.get(`/admin/users?${params}`);
};

export const getUserStats = () => {
  return axiosConfig.get('/admin/users/stats');
};

export const getSystemAnalytics = () => {
  return axiosConfig.get('/admin/system/analytics');
};

export const getSystemPerformance = () => {
  return axiosConfig.get('/admin/system/performance');
};

export const getAvailableReports = () => {
  return axiosConfig.get('/admin/reports/list');
};

export const updateUserStatus = (userId, status) => {
  return axiosConfig.put(`/admin/users/${userId}/status?status=${status}`);
};

export const deleteUser = (userId) => {
  return axiosConfig.delete(`/admin/users/${userId}`);
};