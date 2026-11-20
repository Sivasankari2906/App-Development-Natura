import React, { useState, useEffect, useCallback } from 'react';
import { getCareTasks } from '../../services/careScheduleService';
import "../../styles/careScheduler.css";
import "../../styles/professional-components.css";

const CareTasks = ({ plantId }) => {
  const [careTasks, setCareTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchCareTasks = useCallback(async () => {
    try {
      setLoading(true);
      const response = await getCareTasks(plantId);
      setCareTasks(response.data);
    } catch (error) {
      console.error('Error fetching care tasks:', error);
      setCareTasks([]);
    } finally {
      setLoading(false);
    }
  }, [plantId]);

  useEffect(() => {
    if (plantId) {
      fetchCareTasks();
    }
  }, [plantId, fetchCareTasks]);

  if (loading) return <p>Loading care tasks...</p>;

  return (
    <div className="care-tasks-container">
      {careTasks.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📅</div>
          <h3>No Care Tasks</h3>
          <p>No scheduled care tasks for this plant yet.</p>
        </div>
      ) : (
        <div className="tasks-grid">
          {careTasks.map(task => (
            <div key={task.id} className={`task-card priority-${task.priority?.toLowerCase()}`}>
              <div className="task-header">
                <div className="task-type">{task.taskType}</div>
                <div className={`task-status status-${task.status?.toLowerCase()}`}>
                  {task.status}
                </div>
              </div>
              <div className="task-content">
                <h4>{task.description}</h4>
                <div className="task-meta">
                  <div className="meta-item">
                    <span className="meta-label">Priority:</span>
                    <span className={`priority-badge priority-${task.priority?.toLowerCase()}`}>
                      {task.priority}
                    </span>
                  </div>
                  <div className="meta-item">
                    <span className="meta-label">Due:</span>
                    <span className="due-date">
                      {new Date(task.dueDate).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              </div>
              <div className="task-actions">
                <button className="btn-complete">Mark Complete</button>
                <button className="btn-reschedule">Reschedule</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CareTasks;