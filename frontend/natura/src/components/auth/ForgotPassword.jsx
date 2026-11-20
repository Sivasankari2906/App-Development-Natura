import React, { useState } from "react";
import { forgotPassword } from "../../services/authService";
import "../../styles/auth.css";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await forgotPassword(email);
      setMessage("If that email is registered, a password reset link has been sent.");
    } catch (err) {
      setMessage(err.message);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-image" />

        <div className="auth-form">
          <h2>Forgot Password</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                placeholder="Enter your registered email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <button type="submit">Send Reset Link</button>
          </form>

          {message && <p style={{ marginTop: "10px" }}>{message}</p>}
          <p>
            <a href="/login">Back to Login</a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
