import React, { useState, useEffect } from 'react';
import "../styles/styles.css";
import "../styles/role-dashboards.css";
import "../styles/admin-components.css";
import PlantCard from './plants/PlantCard';
import * as plantService from '../services/plantService';
import EnvironmentDashboard from './environment/EnvironmentDashboard';
import HealthMonitor from './health/HealthMonitor';
import AddPlantModal from './plants/AddPlantModal';
import EditPlantModal from './plants/EditPlantModal';
import DeleteConfirmModal from './plants/DeleteConfirmModal';
import CareTasks from './care/CareTasks';
import Settings from './Settings';
import { useAuth } from '../contexts/AuthContext';
import * as adminService from '../services/adminService';

const Dashboard = () => {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedSidebar, setSelectedSidebar] = useState('Plants');

  const [selectedPlantId, setSelectedPlantId] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedPlant, setSelectedPlant] = useState(null);
  const [showSettings, setShowSettings] = useState(false);
  
  // Admin data states
  const [users, setUsers] = useState([]);
  const [userStats, setUserStats] = useState({});
  const [usersLoading, setUsersLoading] = useState(false);
  const [analytics, setAnalytics] = useState({});
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [usersPerPage] = useState(10);
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [searchFilter, setSearchFilter] = useState('');

  const fetchPlants = () => {
    setLoading(true);
    plantService.getPlants()
      .then(res => {
        const fetchedPlants = Array.isArray(res.data) ? res.data : res.data.content || [];
        setPlants(fetchedPlants);
        setLoading(false);
      })
      .catch(() => {
        setPlants([]);
        setLoading(false);
      });
  };



  const { user, logout } = useAuth();

  useEffect(() => {
    fetchPlants();
  }, []);

  useEffect(() => {
    // Load user stats for admin dashboard
    if (user?.role === 'ADMIN') {
      const fetchUserStats = async () => {
        try {
          const statsResponse = await adminService.getUserStats();
          setUserStats(statsResponse.data);
        } catch (error) {
          console.error('Error fetching user stats:', error);
        }
      };
      fetchUserStats();
    }
  }, [user?.role]);

  const fetchUsersForPage = async (page = 1, role = roleFilter, status = statusFilter, search = searchFilter) => {
    if (user?.role !== 'ADMIN') return;
    
    setUsersLoading(true);
    try {
      console.log('Fetching users for page:', page, 'with filters:', { role, status, search });
      const usersResponse = await adminService.getUsers(page - 1, usersPerPage, role, status, search);
      console.log('Users response:', usersResponse.data);
      const responseData = usersResponse.data;
      
      if (responseData.content && responseData.totalElements) {
        setUsers(responseData.content);
        window.totalUserCount = responseData.totalElements;
      } else {
        const fetchedUsers = responseData || [];
        setUsers(fetchedUsers);
        window.totalUserCount = fetchedUsers.length;
      }
      console.log('Total users from backend:', window.totalUserCount);
    } catch (error) {
      console.error('Error fetching user data:', error);
      setUsers([]);
    } finally {
      setUsersLoading(false);
    }
  };

  const handleFilterChange = (filterType, value) => {
    setCurrentPage(1); // Reset to first page when filtering
    
    if (filterType === 'role') {
      setRoleFilter(value);
      fetchUsersForPage(1, value, statusFilter, searchFilter);
    } else if (filterType === 'status') {
      setStatusFilter(value);
      fetchUsersForPage(1, roleFilter, value, searchFilter);
    } else if (filterType === 'search') {
      setSearchFilter(value);
      fetchUsersForPage(1, roleFilter, statusFilter, value);
    }
  };

  useEffect(() => {
    if (selectedSidebar === 'Users' && user?.role === 'ADMIN') {
      setCurrentPage(1);
      fetchUsersForPage(1);
    }
    
    if (selectedSidebar === 'System Analytics' && user?.role === 'ADMIN') {
      setAnalyticsLoading(true);
      const fetchAnalytics = async () => {
        try {
          const response = await adminService.getSystemAnalytics();
          setAnalytics(response.data);
        } catch (error) {
          console.error('Error fetching analytics:', error);
        } finally {
          setAnalyticsLoading(false);
        }
      };
      fetchAnalytics();
    }
  }, [selectedSidebar, user?.role]);

  const handleEdit = (plant) => {
    setSelectedPlant(plant);
    setShowEditModal(true);
  };

  const handleDeleteClick = (plant) => {
    setSelectedPlant(plant);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      await plantService.deletePlant(selectedPlant.id);
      alert('Plant deleted successfully');
      fetchPlants();
      setShowDeleteModal(false);
      setSelectedPlant(null);
    } catch (error) {
      alert('Failed to delete plant');
    }
  };

  const getRoleBasedSidebarItems = (userRole) => {
    switch (userRole) {
      case 'ADMIN':
        return ['Dashboard', 'All Plants', 'Users', 'System Analytics', 'Reports', 'Settings'];
      case 'PLANT_CARE_SPECIALIST':
        return ['Dashboard', 'Client Plants', 'Consultations', 'Care Plans', 'Knowledge Base', 'Reports'];
      case 'PREMIUM_PLANT_OWNER':
        return ['Dashboard', 'My Plants', 'Care Schedule', 'Environment Data', 'Health Records', 'Analytics'];
      case 'STANDARD_PLANT_OWNER':
        return ['Dashboard', 'My Plants', 'Care Tasks', 'Environment Data', 'Basic Health'];
      case 'COMMUNITY_MEMBER':
        return ['Dashboard', 'Community', 'Plant Library', 'Tips & Guides'];
      case 'GUEST':
        return ['Browse Plants', 'Plant Care Tips', 'Join Community'];
      default:
        return ['Plants', 'Care Tasks', 'Environment Data', 'Health Records'];
    }
  };

  const sidebarItems = getRoleBasedSidebarItems(user?.role);

  const handleEnvironmentData = (plantId) => {
    setSelectedPlantId(plantId);
    setSelectedSidebar('Environment Data');
  };

  const handleHealthRecord = (plantId) => {
    setSelectedPlantId(plantId);
    setSelectedSidebar('Health Records');
  };

  const handleCareTasks = (plantId) => {
    setSelectedPlantId(plantId);
    setSelectedSidebar('Care Tasks');
  };

  const renderRoleBasedContent = () => {
    const userRole = user?.role;
    
    // Common plant-related content
    const plantContent = (
      <>
        {loading ? (
          <p>Loading plants...</p>
        ) : (
          <div className="cards-container">
            {plants.map(plant => (
              <PlantCard
                key={plant.id}
                plant={plant}
                onDelete={() => handleDeleteClick(plant)}
                onEdit={() => handleEdit(plant)}
                onCareTasks={() => handleCareTasks(plant.id)}
                onEnvironmentData={() => handleEnvironmentData(plant.id)}
                onHealthRecord={() => handleHealthRecord(plant.id)}
              />
            ))}
          </div>
        )}
        <button
          className="add-btn"
          onClick={() => setShowAddModal(true)}
          aria-label="Add new plant"
        >
          +
        </button>
      </>
    );

    // Role-specific content rendering
    switch (selectedSidebar) {
      // Common sections
      case 'Plants':
      case 'My Plants':
      case 'All Plants':
      case 'Client Plants':
        return plantContent;
      
      case 'Care Tasks':
      case 'Care Schedule':
        return <CareTasks plantId={selectedPlantId} />;
      
      case 'Environment Data':
        return <EnvironmentDashboard plantId={selectedPlantId} />;
      
      case 'Health Records':
      case 'Basic Health':
        return <HealthMonitor plantId={selectedPlantId} />;
      
      // Role-specific dashboards
      case 'Dashboard':
        return renderDashboardByRole(userRole);
      
      // Admin-specific
      case 'Users':
        return renderUserManagement();
      
      case 'System Analytics':
        return renderSystemAnalytics();
      
      case 'Reports':
        return user?.role === 'ADMIN' ? renderReports() : renderSpecialistReports();
      
      case 'Settings':
        return renderAdminSettings();
      
      // Specialist-specific
      case 'Consultations':
        return (
          <div className="specialist-section">
            <h3>Client Consultations</h3>
            <div className="consultation-stats">
              <div className="stat-card">
                <h4>Today's Appointments</h4>
                <div className="stat-number">8</div>
              </div>
              <div className="stat-card">
                <h4>This Week</h4>
                <div className="stat-number">32</div>
              </div>
              <div className="stat-card">
                <h4>Completed</h4>
                <div className="stat-number">156</div>
              </div>
            </div>
            <div className="consultation-schedule">
              <h4>Today's Schedule</h4>
              <div className="appointment-list">
                <div className="appointment-item">
                  <div className="appointment-time">9:00 AM</div>
                  <div className="appointment-details">
                    <strong>Sarah Johnson</strong>
                    <span>Monstera leaf yellowing issue</span>
                  </div>
                  <div className="appointment-status pending">Pending</div>
                </div>
                <div className="appointment-item">
                  <div className="appointment-time">10:30 AM</div>
                  <div className="appointment-details">
                    <strong>Mike Chen</strong>
                    <span>Succulent care consultation</span>
                  </div>
                  <div className="appointment-status completed">Completed</div>
                </div>
                <div className="appointment-item">
                  <div className="appointment-time">2:00 PM</div>
                  <div className="appointment-details">
                    <strong>Emma Davis</strong>
                    <span>Indoor garden setup</span>
                  </div>
                  <div className="appointment-status upcoming">Upcoming</div>
                </div>
                <div className="appointment-item">
                  <div className="appointment-time">3:30 PM</div>
                  <div className="appointment-details">
                    <strong>Robert Wilson</strong>
                    <span>Plant disease diagnosis</span>
                  </div>
                  <div className="appointment-status upcoming">Upcoming</div>
                </div>
              </div>
            </div>
          </div>
        );
      
      case 'Care Plans':
        return (
          <div className="specialist-section">
            <h3>Care Plans</h3>
            <div className="care-plans-overview">
              <div className="stat-card">
                <h4>Active Plans</h4>
                <div className="stat-number">24</div>
              </div>
              <div className="stat-card">
                <h4>Completed</h4>
                <div className="stat-number">89</div>
              </div>
              <div className="stat-card">
                <h4>Success Rate</h4>
                <div className="stat-number">94%</div>
              </div>
            </div>
            <div className="care-plans-list">
              <h4>Recent Care Plans</h4>
              <div className="plan-grid">
                <div className="plan-card">
                  <div className="plan-header">
                    <h5>Tropical Plant Recovery</h5>
                    <span className="plan-status active">Active</span>
                  </div>
                  <p><strong>Client:</strong> Sarah Johnson</p>
                  <p><strong>Plant:</strong> Monstera Deliciosa</p>
                  <p><strong>Issue:</strong> Root rot treatment</p>
                  <div className="plan-progress">
                    <span>Progress: 60%</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '60%'}}></div>
                    </div>
                  </div>
                </div>
                <div className="plan-card">
                  <div className="plan-header">
                    <h5>Succulent Garden Setup</h5>
                    <span className="plan-status completed">Completed</span>
                  </div>
                  <p><strong>Client:</strong> Mike Chen</p>
                  <p><strong>Plants:</strong> Mixed Succulents</p>
                  <p><strong>Goal:</strong> Low-maintenance garden</p>
                  <div className="plan-progress">
                    <span>Progress: 100%</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '100%'}}></div>
                    </div>
                  </div>
                </div>
                <div className="plan-card">
                  <div className="plan-header">
                    <h5>Indoor Air Purification</h5>
                    <span className="plan-status active">Active</span>
                  </div>
                  <p><strong>Client:</strong> Emma Davis</p>
                  <p><strong>Plants:</strong> Snake Plant, Pothos</p>
                  <p><strong>Goal:</strong> Improve air quality</p>
                  <div className="plan-progress">
                    <span>Progress: 30%</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '30%'}}></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      
      case 'Knowledge Base':
        return (
          <div className="specialist-section">
            <h3>Knowledge Base</h3>
            <div className="knowledge-categories">
              <div className="category-section">
                <h4>Plant Diseases</h4>
                <div className="knowledge-items">
                  <div className="knowledge-item">
                    <strong>Root Rot</strong>
                    <p>Causes, symptoms, and treatment methods for root rot in houseplants</p>
                    <span className="knowledge-tag">Common Issue</span>
                  </div>
                  <div className="knowledge-item">
                    <strong>Leaf Spot Disease</strong>
                    <p>Identification and management of fungal leaf spot diseases</p>
                    <span className="knowledge-tag">Fungal</span>
                  </div>
                  <div className="knowledge-item">
                    <strong>Powdery Mildew</strong>
                    <p>Prevention and treatment of powdery mildew infections</p>
                    <span className="knowledge-tag">Fungal</span>
                  </div>
                </div>
              </div>
              <div className="category-section">
                <h4>Pest Control</h4>
                <div className="knowledge-items">
                  <div className="knowledge-item">
                    <strong>Spider Mites</strong>
                    <p>Detection and elimination of spider mite infestations</p>
                    <span className="knowledge-tag">Pest</span>
                  </div>
                  <div className="knowledge-item">
                    <strong>Aphids</strong>
                    <p>Natural and chemical methods for aphid control</p>
                    <span className="knowledge-tag">Pest</span>
                  </div>
                  <div className="knowledge-item">
                    <strong>Scale Insects</strong>
                    <p>Identification and treatment of scale insect problems</p>
                    <span className="knowledge-tag">Pest</span>
                  </div>
                </div>
              </div>
              <div className="category-section">
                <h4>Care Techniques</h4>
                <div className="knowledge-items">
                  <div className="knowledge-item">
                    <strong>Proper Watering</strong>
                    <p>Watering schedules and techniques for different plant types</p>
                    <span className="knowledge-tag">Technique</span>
                  </div>
                  <div className="knowledge-item">
                    <strong>Pruning Methods</strong>
                    <p>When and how to prune various houseplants for optimal health</p>
                    <span className="knowledge-tag">Technique</span>
                  </div>
                  <div className="knowledge-item">
                    <strong>Repotting Guide</strong>
                    <p>Step-by-step guide to repotting plants safely</p>
                    <span className="knowledge-tag">Technique</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      
      // Premium features
      case 'Analytics':
        return (
          <div className="premium-section">
            <h3>Plant Analytics</h3>
            <div className="analytics-overview">
              <div className="analytics-stats">
                <div className="stat-card">
                  <h4>Growth Rate</h4>
                  <div className="stat-number">+12%</div>
                  <span className="stat-trend positive">↗ This month</span>
                </div>
                <div className="stat-card">
                  <h4>Health Score</h4>
                  <div className="stat-number">87%</div>
                  <span className="stat-trend positive">↗ +5% from last month</span>
                </div>
                <div className="stat-card">
                  <h4>Care Efficiency</h4>
                  <div className="stat-number">94%</div>
                  <span className="stat-trend positive">↗ Excellent</span>
                </div>
                <div className="stat-card">
                  <h4>Water Usage</h4>
                  <div className="stat-number">2.3L</div>
                  <span className="stat-trend neutral">→ Weekly average</span>
                </div>
              </div>
            </div>
            
            <div className="analytics-charts">
              <div className="chart-section">
                <h4>Plant Health Trends</h4>
                <div className="chart-placeholder">
                  <div className="trend-line">
                    <div className="data-point" style={{left: '10%', bottom: '60%'}}></div>
                    <div className="data-point" style={{left: '25%', bottom: '65%'}}></div>
                    <div className="data-point" style={{left: '40%', bottom: '70%'}}></div>
                    <div className="data-point" style={{left: '55%', bottom: '75%'}}></div>
                    <div className="data-point" style={{left: '70%', bottom: '80%'}}></div>
                    <div className="data-point" style={{left: '85%', bottom: '87%'}}></div>
                  </div>
                  <div className="chart-labels">
                    <span>Jan</span><span>Feb</span><span>Mar</span><span>Apr</span><span>May</span><span>Jun</span>
                  </div>
                </div>
              </div>
              
              <div className="chart-section">
                <h4>Care Activity</h4>
                <div className="activity-bars">
                  <div className="activity-item">
                    <span>Watering</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '85%'}}></div>
                    </div>
                    <span>85%</span>
                  </div>
                  <div className="activity-item">
                    <span>Fertilizing</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '70%'}}></div>
                    </div>
                    <span>70%</span>
                  </div>
                  <div className="activity-item">
                    <span>Pruning</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '60%'}}></div>
                    </div>
                    <span>60%</span>
                  </div>
                  <div className="activity-item">
                    <span>Repotting</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: '40%'}}></div>
                    </div>
                    <span>40%</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="plant-insights">
              <h4>Plant Insights</h4>
              <div className="insights-grid">
                <div className="insight-card">
                  <h5>Best Performing Plant</h5>
                  <p>Monstera Deliciosa</p>
                  <span className="insight-metric">98% health score</span>
                </div>
                <div className="insight-card">
                  <h5>Needs Attention</h5>
                  <p>Snake Plant</p>
                  <span className="insight-metric warning">Overwatered</span>
                </div>
                <div className="insight-card">
                  <h5>Growth Champion</h5>
                  <p>Pothos</p>
                  <span className="insight-metric">+25% growth this month</span>
                </div>
                <div className="insight-card">
                  <h5>Care Streak</h5>
                  <p>Daily Care</p>
                  <span className="insight-metric">28 days</span>
                </div>
              </div>
            </div>
          </div>
        );
      
      // Community features
      case 'Community':
        return (
          <div className="community-section">
            <h3>Plant Community</h3>
            <div className="community-stats">
              <div className="stat-card">
                <h4>Active Members</h4>
                <div className="stat-number">2,847</div>
              </div>
              <div className="stat-card">
                <h4>Posts Today</h4>
                <div className="stat-number">156</div>
              </div>
              <div className="stat-card">
                <h4>Plant Photos</h4>
                <div className="stat-number">12,394</div>
              </div>
            </div>
            <div className="community-feed">
              <h4>Recent Community Posts</h4>
              <div className="post-item">
                <strong>Sarah M.</strong> shared a photo of her Monstera deliciosa
                <span className="post-time">2 hours ago</span>
              </div>
              <div className="post-item">
                <strong>Mike R.</strong> asked about yellowing leaves on snake plant
                <span className="post-time">4 hours ago</span>
              </div>
              <div className="post-item">
                <strong>Emma L.</strong> posted care tips for succulents
                <span className="post-time">6 hours ago</span>
              </div>
            </div>
          </div>
        );
      
      case 'Plant Library':
        return (
          <div className="community-section">
            <h3>Plant Library</h3>
            <div className="library-categories">
              <div className="category-grid">
                <div className="category-card">
                  <h4>Houseplants</h4>
                  <p>847 species</p>
                  <div className="popular-plants">
                    <span>Monstera</span>
                    <span>Pothos</span>
                    <span>Snake Plant</span>
                  </div>
                </div>
                <div className="category-card">
                  <h4>Succulents</h4>
                  <p>312 species</p>
                  <div className="popular-plants">
                    <span>Aloe Vera</span>
                    <span>Jade Plant</span>
                    <span>Echeveria</span>
                  </div>
                </div>
                <div className="category-card">
                  <h4>Outdoor Plants</h4>
                  <p>1,205 species</p>
                  <div className="popular-plants">
                    <span>Roses</span>
                    <span>Lavender</span>
                    <span>Tomatoes</span>
                  </div>
                </div>
                <div className="category-card">
                  <h4>Herbs</h4>
                  <p>89 species</p>
                  <div className="popular-plants">
                    <span>Basil</span>
                    <span>Mint</span>
                    <span>Rosemary</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      
      case 'Tips & Guides':
        return (
          <div className="community-section">
            <h3>Care Tips & Guides</h3>
            <div className="guides-grid">
              <div className="guide-category">
                <h4>Beginner Guides</h4>
                <div className="guide-list">
                  <div className="guide-item">
                    <span className="guide-title">Plant Care Basics</span>
                    <span className="guide-author">by Expert Team</span>
                  </div>
                  <div className="guide-item">
                    <span className="guide-title">Choosing Your First Plant</span>
                    <span className="guide-author">by Sarah Green</span>
                  </div>
                  <div className="guide-item">
                    <span className="guide-title">Watering 101</span>
                    <span className="guide-author">by Plant Guru</span>
                  </div>
                </div>
              </div>
              <div className="guide-category">
                <h4>Seasonal Care</h4>
                <div className="guide-list">
                  <div className="guide-item">
                    <span className="guide-title">Winter Plant Care</span>
                    <span className="guide-author">by Green Thumb</span>
                  </div>
                  <div className="guide-item">
                    <span className="guide-title">Spring Repotting Guide</span>
                    <span className="guide-author">by Plant Expert</span>
                  </div>
                  <div className="guide-item">
                    <span className="guide-title">Summer Watering Tips</span>
                    <span className="guide-author">by Garden Pro</span>
                  </div>
                </div>
              </div>
              <div className="guide-category">
                <h4>Problem Solving</h4>
                <div className="guide-list">
                  <div className="guide-item">
                    <span className="guide-title">Common Plant Diseases</span>
                    <span className="guide-author">by Dr. Plant</span>
                  </div>
                  <div className="guide-item">
                    <span className="guide-title">Pest Control Methods</span>
                    <span className="guide-author">by Eco Gardener</span>
                  </div>
                  <div className="guide-item">
                    <span className="guide-title">Yellowing Leaves Solutions</span>
                    <span className="guide-author">by Plant Clinic</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      
      // Guest features
      case 'Browse Plants':
        return <div className="guest-section"><h3>Browse Plants</h3><p>Explore our plant collection</p></div>;
      
      case 'Plant Care Tips':
        return <div className="guest-section"><h3>Plant Care Tips</h3><p>Free plant care advice and tips</p></div>;
      
      case 'Join Community':
        return <div className="guest-section"><h3>Join Our Community</h3><p>Sign up to start your plant journey</p></div>;
      
      default:
        return <p>Content for "{selectedSidebar}" will be shown here.</p>;
    }
  };

  const renderDashboardByRole = (role) => {
    switch (role) {
      case 'ADMIN':
        return (
          <div className="admin-dashboard">
            <div className="dashboard-stats">
              <div className="stat-card">
                <h3>Total Users</h3>
                <div className="stat-number">{userStats.totalUsers || 0}</div>
              </div>
              <div className="stat-card">
                <h3>Total Plants</h3>
                <div className="stat-number">{plants.length}</div>
              </div>
              <div className="stat-card">
                <h3>Active Sessions</h3>
                <div className="stat-number">{userStats.activeToday || 0}</div>
              </div>
              <div className="stat-card">
                <h3>Premium Users</h3>
                <div className="stat-number">{userStats.premiumUsers || 0}</div>
              </div>
            </div>
          </div>
        );
      
      case 'PLANT_CARE_SPECIALIST':
        return (
          <div className="specialist-dashboard">
            <div className="dashboard-stats">
              <div className="stat-card">
                <h3>Active Clients</h3>
                <div className="stat-number">24</div>
              </div>
              <div className="stat-card">
                <h3>Plants Under Care</h3>
                <div className="stat-number">156</div>
              </div>
              <div className="stat-card">
                <h3>Consultations Today</h3>
                <div className="stat-number">8</div>
              </div>
              <div className="stat-card">
                <h3>Success Rate</h3>
                <div className="stat-number">94%</div>
              </div>
            </div>
          </div>
        );
      
      case 'PREMIUM_PLANT_OWNER':
        return (
          <div className="premium-dashboard">
            <div className="dashboard-stats">
              <div className="stat-card">
                <h3>My Plants</h3>
                <div className="stat-number">{plants.length}</div>
              </div>
              <div className="stat-card">
                <h3>Healthy Plants</h3>
                <div className="stat-number">{Math.floor(plants.length * 0.85)}</div>
              </div>
              <div className="stat-card">
                <h3>Tasks Due</h3>
                <div className="stat-number">3</div>
              </div>
              <div className="stat-card">
                <h3>Growth Rate</h3>
                <div className="stat-number">+12%</div>
              </div>
            </div>
          </div>
        );
      
      default:
        return (
          <div className="standard-dashboard">
            <div className="dashboard-stats">
              <div className="stat-card">
                <h3>My Plants</h3>
                <div className="stat-number">{plants.length}</div>
              </div>
              <div className="stat-card">
                <h3>Care Tasks</h3>
                <div className="stat-number">2</div>
              </div>
              <div className="stat-card">
                <h3>Days Active</h3>
                <div className="stat-number">45</div>
              </div>
            </div>
          </div>
        );
    }
  };

  const renderUserManagement = () => {
    if (usersLoading) {
      return <div className="loading-state"><div className="spinner"></div><p>Loading users...</p></div>;
    }

    // Use total count from backend if available, otherwise use local array length
    const totalUserCount = window.totalUserCount || users.length;
    const totalPages = Math.ceil(totalUserCount / usersPerPage);
    
    // For display, use all available users (backend already paginated)
    const currentUsers = users;
    const indexOfFirstUser = (currentPage - 1) * usersPerPage;
    const indexOfLastUser = currentPage * usersPerPage;
    
    console.log('Pagination Debug:', {
      totalUsers: users.length,
      currentPage,
      usersPerPage,
      totalPages,
      indexOfFirstUser,
      indexOfLastUser,
      currentUsersCount: currentUsers.length,
      shouldShowNext: currentPage < totalPages,
      isNextDisabled: currentPage >= totalPages || totalPages <= 1
    });
    
    // Test calculation: 18 users, 10 per page = 2 pages
    if (users.length === 18) {
      console.log('Expected: 2 pages, Current calculation:', Math.ceil(18/10));
    }

    return (
      <div className="admin-users">
        <div className="admin-header">
          <h3>User Management</h3>
          <div className="admin-actions">
            <button className="btn-export">Export Users</button>
          </div>
        </div>
        
        <div className="users-stats">
          <div className="stat-card">
            <h4>Total Users</h4>
            <div className="stat-number">{userStats.totalUsers || 0}</div>
          </div>
          <div className="stat-card">
            <h4>Active Today</h4>
            <div className="stat-number">{userStats.activeToday || 0}</div>
          </div>
          <div className="stat-card">
            <h4>New This Month</h4>
            <div className="stat-number">{userStats.newThisMonth || 0}</div>
          </div>
          <div className="stat-card">
            <h4>Premium Users</h4>
            <div className="stat-number">{userStats.premiumUsers || 0}</div>
          </div>
        </div>

        <div className="users-table">
          <div className="table-header">
            <div className="table-filters">
              <select 
                className="filter-select" 
                value={roleFilter} 
                onChange={(e) => handleFilterChange('role', e.target.value)}
              >
                <option value="">All Roles</option>
                <option value="ADMIN">ADMIN</option>
                <option value="PLANT_CARE_SPECIALIST">PLANT_CARE_SPECIALIST</option>
                <option value="PREMIUM_PLANT_OWNER">PREMIUM_PLANT_OWNER</option>
                <option value="STANDARD_PLANT_OWNER">STANDARD_PLANT_OWNER</option>
                <option value="COMMUNITY_MEMBER">COMMUNITY_MEMBER</option>
                <option value="GUEST">GUEST</option>
              </select>
              <select 
                className="filter-select" 
                value={statusFilter} 
                onChange={(e) => handleFilterChange('status', e.target.value)}
              >
                <option value="">All Status</option>
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
              <input 
                type="text" 
                placeholder="Search users..." 
                className="search-input" 
                value={searchFilter}
                onChange={(e) => handleFilterChange('search', e.target.value)}
              />
            </div>
          </div>
          
          <table className="data-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Role</th>
                <th>Status</th>
                <th>Last Login</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {currentUsers.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{textAlign: 'center', padding: '20px'}}>
                    No users found in database
                  </td>
                </tr>
              ) : (
                currentUsers.map(user => (
                  <tr key={user.id}>
                    <td>
                      <div className="user-info">
                        <div className="user-avatar">{user.username?.charAt(0).toUpperCase()}</div>
                        <div>
                          <div className="user-name">{user.username}</div>
                          <div className="user-email">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td><span className={`role-badge role-${user.role?.toLowerCase()}`}>{user.role}</span></td>
                    <td><span className={`status-badge status-${user.isActive ? 'active' : 'inactive'}`}>{user.isActive ? 'Active' : 'Inactive'}</span></td>
                    <td>{user.lastLogin ? new Date(user.lastLogin).toLocaleDateString() : 'Never'}</td>
                    <td>
                      <div className="table-actions">
                        <button className="btn-edit">Edit</button>
                        <button className="btn-suspend" onClick={() => handleUserAction(user.id, 'suspend')}>Suspend</button>
                        <button className="btn-delete" onClick={() => handleUserAction(user.id, 'delete')}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
          
          {users.length > 0 && (
            <div className="pagination">
              <button 
                className="pagination-btn" 
                onClick={() => {
                  const prevPage = currentPage - 1;
                  console.log('Previous clicked, going to page:', prevPage);
                  setCurrentPage(prevPage);
                  fetchUsersForPage(prevPage);
                }}
                disabled={currentPage <= 1}
              >
                Previous
              </button>
              
              <div className="pagination-numbers">
                {Array.from({ length: Math.max(totalPages, 1) }, (_, i) => i + 1).map(page => (
                  <button
                    key={page}
                    className={`pagination-number ${currentPage === page ? 'active' : ''}`}
                    onClick={() => {
                      console.log('Page clicked:', page);
                      setCurrentPage(page);
                      fetchUsersForPage(page);
                    }}
                  >
                    {page}
                  </button>
                ))}
              </div>
              
              <div className="pagination-info">
                Page {currentPage} of {Math.max(totalPages, 1)} ({window.totalUserCount || users.length} total users)
              </div>
              
              <button 
                className="pagination-btn" 
                onClick={() => {
                  const nextPage = currentPage + 1;
                  console.log('Next clicked, going to page:', nextPage);
                  setCurrentPage(nextPage);
                  fetchUsersForPage(nextPage);
                }}
                disabled={currentPage >= totalPages}
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  const handleUserAction = async (userId, action) => {
    try {
      if (action === 'delete') {
        if (window.confirm('Are you sure you want to delete this user?')) {
          await adminService.deleteUser(userId);
          // Refresh user list
          window.location.reload();
        }
      } else if (action === 'suspend') {
        await adminService.updateUserStatus(userId, 'suspended');
        // Refresh user list
        window.location.reload();
      }
    } catch (error) {
      console.error('Error performing user action:', error);
      alert('Failed to perform action');
    }
  };

  const renderSystemAnalytics = () => {
    if (analyticsLoading) {
      return <div className="loading-state"><div className="spinner"></div><p>Loading analytics...</p></div>;
    }
    return (
      <div className="admin-analytics">
        <div className="admin-header">
          <h3>System Analytics</h3>
          <div className="admin-actions">
            <select className="time-filter">
              <option>Last 24 Hours</option>
              <option>Last 7 Days</option>
              <option>Last 30 Days</option>
              <option>Last 3 Months</option>
            </select>
            <button className="btn-refresh">Refresh</button>
          </div>
        </div>

        <div className="analytics-grid">
          <div className="analytics-card">
            <h4>System Performance</h4>
            <div className="performance-metrics">
              <div className="metric">
                <span className="metric-label">CPU Usage</span>
                <div className="progress-bar">
                  <div className="progress-fill" style={{width: `${analytics.performance?.cpuUsage || 0}%`}}></div>
                </div>
                <span className="metric-value">{Math.round(analytics.performance?.cpuUsage || 0)}%</span>
              </div>
              <div className="metric">
                <span className="metric-label">Memory Usage</span>
                <div className="progress-bar">
                  <div className="progress-fill" style={{width: `${analytics.performance?.memoryUsage || 0}%`}}></div>
                </div>
                <span className="metric-value">{Math.round(analytics.performance?.memoryUsage || 0)}%</span>
              </div>
              <div className="metric">
                <span className="metric-label">Disk Usage</span>
                <div className="progress-bar">
                  <div className="progress-fill" style={{width: `${analytics.performance?.diskUsage || 0}%`}}></div>
                </div>
                <span className="metric-value">{Math.round(analytics.performance?.diskUsage || 0)}%</span>
              </div>
            </div>
          </div>

          <div className="analytics-card">
            <h4>User Activity</h4>
            <div className="activity-stats">
              <div className="activity-item">
                <span className="activity-label">Active Sessions</span>
                <span className="activity-value">{analytics.activity?.activeSessions || 0}</span>
              </div>
              <div className="activity-item">
                <span className="activity-label">Page Views</span>
                <span className="activity-value">{analytics.activity?.pageViews?.toLocaleString() || 0}</span>
              </div>
              <div className="activity-item">
                <span className="activity-label">API Calls</span>
                <span className="activity-value">{analytics.activity?.apiCalls?.toLocaleString() || 0}</span>
              </div>
              <div className="activity-item">
                <span className="activity-label">Error Rate</span>
                <span className="activity-value error">{(analytics.activity?.errorRate || 0).toFixed(1)}%</span>
              </div>
            </div>
          </div>

          <div className="analytics-card">
            <h4>Database Statistics</h4>
            <div className="db-stats">
              <div className="db-item">
                <span className="db-label">Total Records</span>
                <span className="db-value">{analytics.database?.totalRecords?.toLocaleString() || 0}</span>
              </div>
              <div className="db-item">
                <span className="db-label">Query Performance</span>
                <span className="db-value good">{(analytics.database?.queryPerformance || 0).toFixed(1)}%</span>
              </div>
              <div className="db-item">
                <span className="db-label">Backup Status</span>
                <span className="db-value good">{analytics.database?.backupStatus || 'Unknown'}</span>
              </div>
            </div>
          </div>

          <div className="analytics-card">
            <h4>Security Metrics</h4>
            <div className="security-stats">
              <div className="security-item">
                <span className="security-label">Failed Logins</span>
                <span className="security-value warning">{analytics.security?.failedLogins || 0}</span>
              </div>
              <div className="security-item">
                <span className="security-label">Blocked IPs</span>
                <span className="security-value">{analytics.security?.blockedIPs || 0}</span>
              </div>
              <div className="security-item">
                <span className="security-label">SSL Status</span>
                <span className="security-value good">{analytics.security?.sslStatus || 'Unknown'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderSpecialistReports = () => {
    return (
      <div className="specialist-reports">
        <div className="admin-header">
          <h3>Care Reports</h3>
          <div className="admin-actions">
            <button className="btn-generate">Generate Report</button>
          </div>
        </div>

        <div className="reports-grid">
          <div className="report-category">
            <h4>Client Care Reports</h4>
            <div className="report-list">
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Plant Health Summary</span>
                  <span className="report-desc">Overall health status of client plants</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Care Plan Progress</span>
                  <span className="report-desc">Client care plan completion rates</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
            </div>
          </div>

          <div className="report-category">
            <h4>Treatment Reports</h4>
            <div className="report-list">
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Disease Treatment Outcomes</span>
                  <span className="report-desc">Success rates of plant treatments</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Recovery Statistics</span>
                  <span className="report-desc">Plant recovery time analysis</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
            </div>
          </div>

          <div className="report-category">
            <h4>Consultation Reports</h4>
            <div className="report-list">
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Monthly Consultations</span>
                  <span className="report-desc">Client consultation summary</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Client Satisfaction</span>
                  <span className="report-desc">Feedback and satisfaction metrics</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderReports = () => {
    return (
      <div className="admin-reports">
        <div className="admin-header">
          <h3>System Reports</h3>
          <div className="admin-actions">
            <button className="btn-schedule">Schedule Report</button>
            <button className="btn-generate">Generate Report</button>
          </div>
        </div>

        <div className="reports-grid">
          <div className="report-category">
            <h4>User Reports</h4>
            <div className="report-list">
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">User Activity Report</span>
                  <span className="report-desc">Detailed user engagement metrics</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Registration Trends</span>
                  <span className="report-desc">New user registration patterns</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
            </div>
          </div>

          <div className="report-category">
            <h4>System Reports</h4>
            <div className="report-list">
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Performance Analytics</span>
                  <span className="report-desc">System performance metrics</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Error Logs Summary</span>
                  <span className="report-desc">System errors and issues</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
            </div>
          </div>

          <div className="report-category">
            <h4>Business Reports</h4>
            <div className="report-list">
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Revenue Analytics</span>
                  <span className="report-desc">Financial performance metrics</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
              <div className="report-item">
                <div className="report-info">
                  <span className="report-name">Plant Care Statistics</span>
                  <span className="report-desc">Plant health and care metrics</span>
                </div>
                <div className="report-actions">
                  <button className="btn-view">View</button>
                  <button className="btn-download">Download</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderAdminSettings = () => {
    return (
      <div className="admin-settings">
        <div className="admin-header">
          <h3>System Settings</h3>
          <div className="admin-actions">
            <button className="btn-backup">Create Backup</button>
            <button className="btn-save-settings">Save Changes</button>
          </div>
        </div>

        <div className="settings-sections">
          <div className="settings-section">
            <h4>General Settings</h4>
            <div className="settings-form">
              <div className="form-group">
                <label>System Name</label>
                <input type="text" defaultValue="NATURA Plant Care System" className="form-input" />
              </div>
              <div className="form-group">
                <label>Maintenance Mode</label>
                <label className="toggle-switch">
                  <input type="checkbox" />
                  <span className="toggle-slider"></span>
                </label>
              </div>
              <div className="form-group">
                <label>Max Users</label>
                <input type="number" defaultValue="10000" className="form-input" />
              </div>
            </div>
          </div>

          <div className="settings-section">
            <h4>Security Settings</h4>
            <div className="settings-form">
              <div className="form-group">
                <label>Session Timeout (minutes)</label>
                <input type="number" defaultValue="30" className="form-input" />
              </div>
              <div className="form-group">
                <label>Password Policy</label>
                <select className="form-input">
                  <option>Standard</option>
                  <option>Strong</option>
                  <option>Very Strong</option>
                </select>
              </div>
              <div className="form-group">
                <label>Two-Factor Authentication</label>
                <label className="toggle-switch">
                  <input type="checkbox" defaultChecked />
                  <span className="toggle-slider"></span>
                </label>
              </div>
            </div>
          </div>

          <div className="settings-section">
            <h4>Email Settings</h4>
            <div className="settings-form">
              <div className="form-group">
                <label>SMTP Server</label>
                <input type="text" defaultValue="smtp.natura.com" className="form-input" />
              </div>
              <div className="form-group">
                <label>From Email</label>
                <input type="email" defaultValue="noreply@natura.com" className="form-input" />
              </div>
              <div className="form-group">
                <label>Email Notifications</label>
                <label className="toggle-switch">
                  <input type="checkbox" defaultChecked />
                  <span className="toggle-slider"></span>
                </label>
              </div>
            </div>
          </div>

          <div className="settings-section">
            <h4>Backup Settings</h4>
            <div className="settings-form">
              <div className="form-group">
                <label>Auto Backup</label>
                <label className="toggle-switch">
                  <input type="checkbox" defaultChecked />
                  <span className="toggle-slider"></span>
                </label>
              </div>
              <div className="form-group">
                <label>Backup Frequency</label>
                <select className="form-input">
                  <option>Daily</option>
                  <option>Weekly</option>
                  <option>Monthly</option>
                </select>
              </div>
              <div className="form-group">
                <label>Retention Period (days)</label>
                <input type="number" defaultValue="30" className="form-input" />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="dashboard-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <strong>NATURA</strong>
        </div>
        <nav className="sidebar-menu">
          {sidebarItems.map(item => (
            <div
              key={item}
              className={`sidebar-item ${selectedSidebar === item ? 'selected' : ''}`}
              onClick={() => setSelectedSidebar(item)}
            >
              {item}
            </div>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-name">{user ? user.username : 'Guest'}</div>
          <button className="settings-btn" onClick={() => setShowSettings(true)}>
           Settings 
          </button>
          <button className="logout-btn" onClick={logout}>
            Logout
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="main-content">
        <h2>{selectedSidebar}</h2>

        {/* Role-based Content Rendering */}
        {renderRoleBasedContent()}
      </main>
      
      <AddPlantModal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        onPlantAdded={fetchPlants}
      />
      
      <EditPlantModal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        onPlantUpdated={fetchPlants}
        plant={selectedPlant}
      />
      
      <DeleteConfirmModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={confirmDelete}
        plantName={selectedPlant?.nickname}
      />
      
      <Settings
        isOpen={showSettings}
        onClose={() => setShowSettings(false)}
      />
    </div>
  );
};

export default Dashboard;


