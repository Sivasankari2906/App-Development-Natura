import React, { useState, useEffect } from 'react';
import { getHealthRecords } from '../../services/healthMonitorService';
import "../../styles/healthMonitor.css";
import "../../styles/professional-components.css";

const HealthMonitor = ({ plantId }) => {
  const [healthRecords, setHealthRecords] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (plantId) {
      getHealthRecords(plantId)
        .then((response) => {
          setHealthRecords(response.data);
          setLoading(false);
        })
        .catch((error) => {
          console.error('Error fetching health records:', error);
          setLoading(false);
        });
    }
  }, [plantId]);

  if (loading) {
    return <p>Loading health records...</p>;
  }

  if (!healthRecords || healthRecords.length === 0) {
    return <p>No health records available for this plant.</p>;
  }

  return (
    <div className="health-monitor">
      {healthRecords.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🌿</div>
          <h3>No Health Records</h3>
          <p>No health assessments recorded for this plant yet.</p>
          <button className="btn-primary">Add Health Record</button>
        </div>
      ) : (
        <div className="records-timeline">
          {healthRecords.map((record) => (
            <div key={record.id} className="record-card">
              <div className="record-header">
                <div className="record-date">
                  {new Date(record.assessmentDate).toLocaleDateString()}
                </div>
                <div className={`health-status status-${record.overallHealth?.toLowerCase()}`}>
                  {record.overallHealth}
                </div>
              </div>
              
              <div className="record-content">
                <div className="health-overview">
                  <div className="health-metric">
                    <span className="metric-label">Overall Health:</span>
                    <span className={`health-badge health-${record.overallHealth?.toLowerCase()}`}>
                      {record.overallHealth}
                    </span>
                  </div>
                  <div className="health-metric">
                    <span className="metric-label">Recovery Status:</span>
                    <span className={`recovery-badge recovery-${record.recoveryStatus?.toLowerCase()}`}>
                      {record.recoveryStatus}
                    </span>
                  </div>
                </div>
                
                {record.notes && (
                  <div className="record-notes">
                    <h4>Assessment Notes</h4>
                    <p>{record.notes}</p>
                  </div>
                )}
                
                <div className="record-footer">
                  {record.followUpDate && (
                    <div className="follow-up">
                      <span className="follow-up-label">Follow-up:</span>
                      <span className="follow-up-date">
                        {new Date(record.followUpDate).toLocaleDateString()}
                      </span>
                    </div>
                  )}
                  <div className="record-actions">
                    <button className="btn-edit">Edit</button>
                    <button className="btn-view">View Details</button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default HealthMonitor;
