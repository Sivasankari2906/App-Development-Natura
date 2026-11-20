import React, { useState, useEffect, useCallback } from 'react';
import {
  getCareTasks,
  generateSchedule,
  completeCareTask,
  rescheduleTask,
  getCareStatistics,
} from '../../services/careScheduleService';
import "../../styles/careScheduler.css";
import "../../styles/professional-components.css";

const CareTasks = ({ plantId }) => {
  const [careTasks, setCareTasks] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchCareTasks = useCallback(async () => {
    try {
      setLoading(true);
      const [tasks, statistics] = await Promise.all([
        getCareTasks(plantId),
        getCareStatistics(),
      ]);
      setCareTasks(tasks.data);
      setStats(statistics.data);
      setError('');
    } catch (err) {
      console.error('Error fetching care tasks:', err);
      setCareTasks([]);
      setError('Could not load care tasks.');
    } finally {
      setLoading(false);
    }
  }, [plantId]);

  useEffect(() => {
    if (plantId) {
      fetchCareTasks();
    }
  }, [plantId, fetchCareTasks]);

  const run = async (action, failureMessage) => {
    try {
      await action();
      await fetchCareTasks();
    } catch (err) {
      setError(err.response?.data?.message || failureMessage);
    }
  };

  const handleGenerate = () =>
    run(() => generateSchedule(plantId), 'Could not generate the schedule.');

  const handleComplete = (id) =>
    run(() => completeCareTask(id), 'Could not mark the task complete.');

  const handleReschedule = (id) => {
    const input = window.prompt('New date (YYYY-MM-DD):');
    if (!input) return;
    if (!/^\d{4}-\d{2}-\d{2}$/.test(input.trim())) {
      setError('Enter the date as YYYY-MM-DD.');
      return;
    }
    run(() => rescheduleTask(id, `${input.trim()}T09:00:00`), 'Could not reschedule the task.');
  };

  const isOverdue = (task) =>
    task.status === 'PENDING' && new Date(task.scheduledDate) < new Date();

  if (loading) return <p>Loading care tasks...</p>;

  return (
    <div className="care-tasks-container">
      {stats && (
        <p className="care-stats" data-testid="care-stats">
          {stats.total} tasks | {stats.pending} pending | {stats.completed} completed |{' '}
          {stats.overdue} overdue | {stats.completionRate}% completion
        </p>
      )}
      {error && <p className="error" role="alert">{error}</p>}
      <button className="btn-generate" onClick={handleGenerate}>
        Generate 30-day schedule
      </button>
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
                <div className={`task-status status-${(isOverdue(task) ? 'overdue' : task.status)?.toLowerCase()}`}>
                  {isOverdue(task) ? 'OVERDUE' : task.status}
                </div>
              </div>
              <div className="task-content">
                <h4>{task.notes}</h4>
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
                      {new Date(task.scheduledDate).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              </div>
              {task.status === 'PENDING' && (
                <div className="task-actions">
                  <button className="btn-complete" onClick={() => handleComplete(task.id)}>
                    Mark Complete
                  </button>
                  <button className="btn-reschedule" onClick={() => handleReschedule(task.id)}>
                    Reschedule
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CareTasks;
