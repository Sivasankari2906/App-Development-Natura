import { axiosConfig } from "./axiosConfig";

export const getHealthRecords = (plantId) => {
  return axiosConfig.get(`/health-records?plantId=${plantId}`);
};

export const createHealthRecord = (record) => {
  return axiosConfig.post('/health-records', record);
};

export const updateHealthRecord = (id, record) => {
  return axiosConfig.put(`/health-records/${id}`, record);
};

export const getHealthTrends = (plantId) => {
  return axiosConfig.get(`/health-records/trends/${plantId}`);
};
