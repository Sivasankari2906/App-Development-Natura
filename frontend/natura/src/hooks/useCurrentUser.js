import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import * as authService from '../services/authService';

export const useCurrentUser = () => {
  const { user, token } = useAuth();
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      if (token) {
        try {
          const response = await authService.getCurrentUser();
          setCurrentUser(response.data);
          setError(null);
        } catch (err) {
          setError(err.message);
          setCurrentUser(user);
        }
      } else {
        setCurrentUser(user);
      }
      setLoading(false);
    };

    fetchCurrentUser();
  }, [user, token]);

  return { currentUser, loading, error };
};