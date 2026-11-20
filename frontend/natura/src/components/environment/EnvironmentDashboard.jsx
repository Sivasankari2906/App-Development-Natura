import React, { useState, useEffect } from 'react';
import { getCurrentEnvironmentData } from '../../services/environmentService';
import "../../styles/environmentDashboard.css";
import "../../styles/professional-components.css";

const EnvironmentDashboard = ({ plantId }) => {
  const [environmentData, setEnvironmentData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (plantId) {
      getCurrentEnvironmentData(plantId)
        .then((response) => {
          setEnvironmentData(response.data);
          setLoading(false);
        })
        .catch((error) => {
          console.error('Error fetching environment data:', error);
          setLoading(false);
        });
    }
  }, [plantId]);

  if (loading) {
    return <p>Loading environment data...</p>;
  }

  if (!environmentData) {
    return <p>No environment data available for this plant.</p>;
  }

  return (
    <div className="environment-dashboard">
      <div className="metrics-grid">
        <div className="metric-card temperature">
          <div className="metric-icon">🌡️</div>
          <div className="metric-content">
            <div className="metric-value">{environmentData.temperatureCelsius}°C</div>
            <div className="metric-label">Temperature</div>
            <div className="metric-status good">Optimal</div>
          </div>
        </div>
        
        <div className="metric-card humidity">
          <div className="metric-icon">💧</div>
          <div className="metric-content">
            <div className="metric-value">{environmentData.humidityPercentage}%</div>
            <div className="metric-label">Humidity</div>
            <div className="metric-status good">Good</div>
          </div>
        </div>
        
        <div className="metric-card light">
          <div className="metric-icon">☀️</div>
          <div className="metric-content">
            <div className="metric-value">{environmentData.lightLevelLux}</div>
            <div className="metric-label">Light (lux)</div>
            <div className="metric-status warning">Moderate</div>
          </div>
        </div>
        
        <div className="metric-card soil">
          <div className="metric-icon">🌱</div>
          <div className="metric-content">
            <div className="metric-value">{environmentData.soilMoisturePercentage}%</div>
            <div className="metric-label">Soil Moisture</div>
            <div className="metric-status good">Healthy</div>
          </div>
        </div>
        
        <div className="metric-card ph">
          <div className="metric-icon">🧪</div>
          <div className="metric-content">
            <div className="metric-value">{environmentData.phLevel}</div>
            <div className="metric-label">pH Level</div>
            <div className="metric-status good">Balanced</div>
          </div>
        </div>
      </div>
      
      <div className="data-info">
        <div className="info-item">
          <span className="info-label">Last Updated:</span>
          <span className="info-value">{new Date(environmentData.recordedDate).toLocaleString()}</span>
        </div>
        <div className="info-item">
          <span className="info-label">Data Source:</span>
          <span className="info-value">{environmentData.dataSource}</span>
        </div>
      </div>
    </div>
  );
};

export default EnvironmentDashboard;
