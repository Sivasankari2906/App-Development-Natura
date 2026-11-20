import { axiosConfig } from "./axiosConfig";

export const getCareTasks = (plantId, date, status) => {
  const params = new URLSearchParams();
  if (plantId) params.append('plantId', plantId);
  if (date) params.append('date', date);
  if (status) params.append('status', status);
  return axiosConfig.get(`/care-tasks?${params}`);
};

export const createCareTask = (task) => {
  return axiosConfig.post('/care-tasks', task);
};

export const getCareTaskById = (id) => {
  return axiosConfig.get(`/care-tasks/${id}`);
};

export const updateCareTask = (id, task) => {
  return axiosConfig.put(`/care-tasks/${id}`, task);
};

export const deleteCareTask = (id) => {
  return axiosConfig.delete(`/care-tasks/${id}`);
};

export const completeCareTask = (id) => {
  return axiosConfig.put(`/care-tasks/${id}/complete`);
};

export const getUpcomingTasks = () => {
  return axiosConfig.get('/care-tasks/upcoming');
};

export const getOverdueTasks = () => {
  return axiosConfig.get('/care-tasks/overdue');
};

export const bulkCompleteTasks = (ids) => {
  return axiosConfig.post('/care-tasks/bulk-complete', ids);
};

export const getTaskCalendar = (month, year) => {
  return axiosConfig.get(`/care-tasks/calendar/${month}/${year}`);
};

export const generateSchedule = (plantId) => {
  return axiosConfig.post(`/care-tasks/generate-schedule?plantId=${plantId}`);
};

export const rescheduleTask = (id, newDate) => {
  return axiosConfig.put(`/care-tasks/${id}/reschedule?newDate=${newDate}`);
};
