import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";

// Auth
import LoginForm from "./components/auth/LoginForm";
import RegisterForm from "./components/auth/RegsiterForm";

// Plants
import PlantList from "./components/plants/PlantList";

// Care Tasks
import CareTasks from "./components/care/CareTasks";

// Environment
import EnvironmentDashboard from "./components/environment/EnvironmentDashboard";

// Health
import HealthMonitor from "./components/health/HealthMonitor";
import ForgotPassword from "./components/auth/ForgotPassword";
import Dashboard from "./components/Dashboard";

// Simple protected route wrapper
const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem("accessToken");
  return token ? children : <Navigate to="/login" />;
};

const App = () => {
  return (
    <AuthProvider>
      <Router>
        <Routes>
        {/* Auth */}
        <Route path="/login" element={<LoginForm />} />
        <Route path="/register" element={<RegisterForm />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* Dashboard → protected */}
        <Route
          path="/dashboard"
          element={
            <Dashboard/>
          }
        />

        {/* Plants */}
        <Route
          path="/plants"
          element={
            <PrivateRoute>
              <PlantList />
            </PrivateRoute>
          }
        />

        {/* Care Tasks */}
        <Route
          path="/care-tasks"
          element={
            <PrivateRoute>
              <CareTasks />
            </PrivateRoute>
          }
        />

        {/* Environment (example: plantId=1) */}
        <Route
          path="/environment/:plantId"
          element={
            <PrivateRoute>
              <EnvironmentDashboard plantId={1} />
            </PrivateRoute>
          }
        />

        {/* Health Records (example: plantId=1) */}
        <Route
          path="/health/:plantId"
          element={
            <PrivateRoute>
              <HealthMonitor plantId={1} />
            </PrivateRoute>
          }
        />

        {/* Default → redirect */}
        <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
