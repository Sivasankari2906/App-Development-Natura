import React, { useState } from "react";
import { register } from "../../services/authService";
import "../../styles/auth.css";

const RegisterForm = () => {
  const [userData, setUserData] = useState({
    username: "",
    email: "",
    password: "",
    role: "STANDARD_PLANT_OWNER",
    gardeningExperience: "BEGINNER",
    location: "",
    timezone: "",
  });

  const [success, setSuccess] = useState("");
  const [errors, setErrors] = useState({});

  const roles = [
    "ADMIN",
    "PLANT_CARE_SPECIALIST",
    "PREMIUM_PLANT_OWNER",
    "STANDARD_PLANT_OWNER",
    "COMMUNITY_MEMBER",
    "GUEST",
  ];

  const experiences = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"];

  const handleChange = (e) => {
    setUserData({ ...userData, [e.target.name]: e.target.value });
  };

  const validate = () => {
    const newErrors = {};

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(userData.email)) {
      newErrors.email = "Invalid email format";
    }

    // Password validation
    const passwordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(userData.password)) {
      newErrors.password =
        "Password must be 8+ chars, include uppercase, lowercase, number & special char";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await register(userData);
      setSuccess("Registration successful! Please login.");
    } catch (err) {
      setSuccess(err.message || "Registration failed.");
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        {/* Left image */}
        <div className="auth-image" />

        {/* Right form */}
        <div className="auth-form">
          <h2>Create Account</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <input
                type="text"
                name="username"
                placeholder="Username"
                value={userData.username}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={userData.email}
                onChange={handleChange}
                required
              />
              {errors.email && <p className="error">{errors.email}</p>}
            </div>

            <div className="form-group">
              <input
                type="password"
                name="password"
                placeholder="Password"
                value={userData.password}
                onChange={handleChange}
                required
              />
              {errors.password && <p className="error">{errors.password}</p>}
            </div>

            <div className="form-group">
              <input
                type="text"
                name="location"
                value={userData.location}
                onChange={handleChange}
                placeholder="Location (e.g., Chennai, India)"
              />
            </div>

            <div className="form-group">
              <input
                type="text"
                name="timezone"
                value={userData.timezone}
                onChange={handleChange}
                placeholder="Timezone (e.g., IST)"
              />
            </div>

            <div className="form-group">
              <select
                name="role"
                value={userData.role}
                onChange={handleChange}
              >
                {roles.map((role) => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <select
                name="gardeningExperience"
                value={userData.gardeningExperience}
                onChange={handleChange}
              >
                {experiences.map((exp) => (
                  <option key={exp} value={exp}>
                    {exp}
                  </option>
                ))}
              </select>
            </div>

            <button type="submit">Register</button>
          </form>

          {success && <p style={{ marginTop: "1px" }}>{success}</p>}
          <p>
            Already have an account? <a href="/login">Login</a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterForm;
