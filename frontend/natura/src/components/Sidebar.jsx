import React from 'react';
import '../styles/styles.css';
import { useAuth } from '../contexts/AuthContext';

const Sidebar = ({ items, selectedItem, onItemSelect, onSettingsClick }) => {
  const { user, logout } = useAuth();

  const getRoleBasedSidebarItems = (userRole) => {
    switch (userRole) {
      case 'ADMIN':
        return ['Dashboard', 'All Plants', 'Users', 'System Analytics', 'Reports', 'Settings'];
      case 'PLANT_CARE_SPECIALIST':
        return ['Dashboard', 'Client Plants', 'Consultations', 'Care Plans', 'Knowledge Base', 'Reports'];
      case 'PREMIUM_PLANT_OWNER':
        return ['Dashboard', 'My Plants', 'Care Schedule', 'Environment Data', 'Health Records', 'Analytics'];
      case 'STANDARD_PLANT_OWNER':
        return ['Dashboard', 'My Plants', 'Care Tasks', 'Basic Health'];
      case 'COMMUNITY_MEMBER':
        return ['Dashboard', 'Community', 'Plant Library', 'Tips & Guides'];
      case 'GUEST':
        return ['Browse Plants', 'Plant Care Tips', 'Join Community'];
      default:
        return ['Plants', 'Care Tasks', 'Environment Data', 'Health Records'];
    }
  };

  const sidebarItems = items || getRoleBasedSidebarItems(user?.role);

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to logout?')) {
      logout();
    }
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <strong>NATURA</strong>
        <div className="sidebar-subtitle">Plant Care System</div>
      </div>
      <nav className="sidebar-menu">
        {sidebarItems.map(item => (
          <div
            key={item}
            className={`sidebar-item ${selectedItem === item ? 'selected' : ''}`}
            onClick={() => onItemSelect(item)}
          >
            {item}
          </div>
        ))}
      </nav>
      <div className="sidebar-footer">
        <div className="user-info">
          <div className="user-name">{user ? user.username : 'Guest'}</div>
          {user?.role && (
            <div className="user-role">{user.role.replace('_', ' ')}</div>
          )}
        </div>
        <div className="sidebar-actions">
          <button className="settings-btn" onClick={onSettingsClick}>
            Settings
          </button>
          {user && (
            <button className="logout-btn" onClick={handleLogout}>
              Logout
            </button>
          )}
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;