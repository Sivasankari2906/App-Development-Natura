import { axiosConfig } from "./axiosConfig";

export const getEnvironmentData = (plantId, locationId, fromDate) => {
  const params = new URLSearchParams();
  if (plantId) params.append('plantId', plantId);
  if (locationId) params.append('locationId', locationId);
  if (fromDate) params.append('fromDate', fromDate);
  return axiosConfig.get(`/environment?${params}`);
};

export const getCurrentEnvironmentData = (plantId) => {
  return axiosConfig.get(`/environment/${plantId}/current`);
};

export const getEnvironmentTrends = (plantId, from, to) => {
  return axiosConfig.get(`/environment/${plantId}/trends?from=${from}&to=${to}`);
};

export const addManualEnvironmentData = (data) => {
  return axiosConfig.post('/environment/manual-entry', data);
};

export const addSensorData = (data) => {
  return axiosConfig.post('/environment/sensor-data', data);
};
