import { useState, useEffect, useCallback } from 'react';
import * as plantService from '../services/plantService';
import { useAuth } from '../contexts/AuthContext';

export const usePlants = () => {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();

  const fetchPlants = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await plantService.getPlants();
      const fetchedPlants = Array.isArray(response.data) ? response.data : response.data.content || [];
      setPlants(fetchedPlants);
    } catch (err) {
      console.error('Error fetching plants:', err);
      setError(err.message || 'Failed to fetch plants');
      setPlants([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const addPlant = useCallback(async (plantData) => {
    try {
      const response = await plantService.createPlant(plantData);
      setPlants(prev => [...prev, response.data]);
      return response.data;
    } catch (err) {
      setError(err.message || 'Failed to add plant');
      throw err;
    }
  }, []);

  const updatePlant = useCallback(async (id, plantData) => {
    try {
      const response = await plantService.updatePlant(id, plantData);
      setPlants(prev => prev.map(plant => plant.id === id ? response.data : plant));
      return response.data;
    } catch (err) {
      setError(err.message || 'Failed to update plant');
      throw err;
    }
  }, []);

  const deletePlant = useCallback(async (id) => {
    try {
      await plantService.deletePlant(id);
      setPlants(prev => prev.filter(plant => plant.id !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete plant');
      throw err;
    }
  }, []);

  useEffect(() => {
    if (user) {
      fetchPlants();
    }
  }, [user, fetchPlants]);

  return { 
    plants, 
    loading, 
    error, 
    refetch: fetchPlants,
    addPlant,
    updatePlant,
    deletePlant
  };
};