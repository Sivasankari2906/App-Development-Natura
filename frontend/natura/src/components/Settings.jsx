import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/modal.css';
import '../styles/settings.css';
import '../styles/professional-settings.css';

const Settings = ({ isOpen, onClose }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({});

  useEffect(() => {
    if (isOpen) {
      fetchUserData();
    }
  }, [isOpen]);

  const fetchUserData = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("accessToken");
      const response = await axios.get('http://localhost:8080/api/auth/me', {
        headers: {
          Authorization: `Bearer ${token}`,
        }
      });
      setUser(response.data);
      setFormData(response.data);
    } catch (error) {
      console.error('Error fetching user data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSave = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      await axios.put('http://localhost:8080/api/auth/me', formData, {
        headers: {
          Authorization: `Bearer ${token}`,
        }
      });
      setUser(formData);
      setEditing(false);
      alert('Profile updated successfully!');
    } catch (error) {
      alert('Failed to update profile');
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="settings-modal">
        <div className="settings-header">
          <h2>Account Settings</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading your settings...</p>
          </div>
        ) : (
          <div className="settings-content">
            {editing ? (
              <form onSubmit={(e) => { e.preventDefault(); handleSave(); }} className="settings-form">
                <div className="settings-section">
                  <h3>Profile Information</h3>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Username</label>
                      <input
                        type="text"
                        name="username"
                        value={formData.username || ''}
                        onChange={handleChange}
                        className="form-input"
                      />
                    </div>
                    <div className="form-group">
                      <label>Email Address</label>
                      <input
                        type="email"
                        name="email"
                        value={formData.email || ''}
                        onChange={handleChange}
                        className="form-input"
                      />
                    </div>
                  </div>
                </div>
                
                <div className="settings-section">
                  <h3>Security</h3>
                  <div className="form-group">
                    <label>New Password</label>
                    <input
                      type="password"
                      name="password"
                      placeholder="Leave blank to keep current password"
                      value={formData.password || ''}
                      onChange={handleChange}
                      className="form-input"
                    />
                  </div>
                </div>
                
                <div className="settings-section">
                  <h3>Gardening Profile</h3>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Location</label>
                      <input
                        type="text"
                        name="location"
                        placeholder="e.g., New York, NY"
                        value={formData.location || ''}
                        onChange={handleChange}
                        className="form-input"
                      />
                    </div>
                    <div className="form-group">
                      <label>Gardening Experience</label>
                      <select
                        name="gardeningExp"
                        value={formData.gardeningExp || ''}
                        onChange={handleChange}
                        className="form-input"
                      >
                        <option value="">Select experience level</option>
                        <option value="Beginner">Beginner (0-1 years)</option>
                        <option value="Intermediate">Intermediate (2-5 years)</option>
                        <option value="Advanced">Advanced (5+ years)</option>
                        <option value="Expert">Expert (10+ years)</option>
                      </select>
                    </div>
                  </div>
                </div>
                
                <div className="settings-actions">
                  <button type="button" className="btn-cancel" onClick={() => setEditing(false)}>Cancel</button>
                  <button type="submit" className="btn-save">Save Changes</button>
                </div>
              </form>
            ) : (
              <div className="settings-view">
                <div className="settings-section">
                  <div className="section-header">
                    <h3>Profile Information</h3>
                    <button className="btn-edit" onClick={() => setEditing(true)}>Edit Profile</button>
                  </div>
                  <div className="info-grid">
                    <div className="info-item">
                      <span className="info-label">Username</span>
                      <span className="info-value">{user?.username}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Email</span>
                      <span className="info-value">{user?.email}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Role</span>
                      <span className="role-badge">{user?.role}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Member Since</span>
                      <span className="info-value">{user?.createdDate ? new Date(user.createdDate).toLocaleDateString() : 'N/A'}</span>
                    </div>
                  </div>
                </div>
                
                <div className="settings-section">
                  <h3>Gardening Profile</h3>
                  <div className="info-grid">
                    <div className="info-item">
                      <span className="info-label">Location</span>
                      <span className="info-value">{user?.location || 'Not specified'}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Experience Level</span>
                      <span className="experience-badge">{user?.gardeningExp || 'Not specified'}</span>
                    </div>
                  </div>
                </div>
                
                <div className="settings-section">
                  <h3>Preferences</h3>
                  <div className="preferences-grid">
                    <div className="preference-item">
                      <label className="toggle-label">
                        <input type="checkbox" defaultChecked />
                        <span className="toggle-slider"></span>
                        Email notifications
                      </label>
                    </div>
                    <div className="preference-item">
                      <label className="toggle-label">
                        <input type="checkbox" defaultChecked />
                        <span className="toggle-slider"></span>
                        Care reminders
                      </label>
                    </div>
                    <div className="preference-item">
                      <label className="toggle-label">
                        <input type="checkbox" />
                        <span className="toggle-slider"></span>
                        Weekly reports
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Settings;