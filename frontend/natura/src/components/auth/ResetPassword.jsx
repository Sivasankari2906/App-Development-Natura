import React, { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { resetPassword } from "../../services/authService";
import "../../styles/auth.css";

const PASSWORD_RULE =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [message, setMessage] = useState("");
  const [done, setDone] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!PASSWORD_RULE.test(password)) {
      setMessage(
        "Password must be 8+ chars, include uppercase, lowercase, number & special char"
      );
      return;
    }
    if (password !== confirm) {
      setMessage("Passwords do not match.");
      return;
    }
    try {
      setMessage(await resetPassword(token, password));
      setDone(true);
    } catch (err) {
      setMessage(err.message);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-image" />
        <div className="auth-form">
          <h2>Reset Password</h2>
          {!token ? (
            <p>This reset link is missing its token. Request a new one.</p>
          ) : (
            !done && (
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label>New password</label>
                  <input
                    type="password"
                    placeholder="New password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Confirm password</label>
                  <input
                    type="password"
                    placeholder="Confirm new password"
                    value={confirm}
                    onChange={(e) => setConfirm(e.target.value)}
                    required
                  />
                </div>
                <button type="submit">Reset Password</button>
              </form>
            )
          )}
          {message && <p style={{ marginTop: "10px" }}>{message}</p>}
          <p>
            <a href="/login">{done ? "Go to Login" : "Back to Login"}</a>
            {!token && (
              <>
                {" | "}
                <a href="/forgot-password">Request new link</a>
              </>
            )}
          </p>
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
