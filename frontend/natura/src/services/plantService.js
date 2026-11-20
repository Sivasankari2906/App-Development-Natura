import { axiosConfig } from './axiosConfig';

export const getPlants = (params) => {
  return axiosConfig.get('/plants', { params });
};

export const getPlantById = (id) => {
  return axiosConfig.get(`/plants/${id}`);
};

export const addPlant = (plant) => {
  return axiosConfig.post('/plants', plant);
};

export const createPlant = (plant) => {
  return axiosConfig.post('/plants', plant);
};

export const updatePlant = (id, plant) => {
  return axiosConfig.put(`/plants/${id}`, plant);
};

export const deletePlant = (id) => {
  return axiosConfig.delete(`/plants/${id}`);
};
