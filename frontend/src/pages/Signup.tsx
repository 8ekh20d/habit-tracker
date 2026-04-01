import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Signup: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);
  const navigate = useNavigate();
  const { signup } = useAuth();

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    // Validate email format
    if (!validateEmail(email)) {
      setError('Invalid email format');
      return;
    }

    // Validate password length
    if (password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    try {
      const response = await signup(email, password);
      setMessage(response.message);
      setIsSuccess(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Signup failed');
    }
  };

  const handleContinueToLogin = () => {
    navigate('/login');
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
      <h2>Sign Up</h2>
      {isSuccess ? (
        <div style={{ 
          padding: '20px', 
          backgroundColor: '#e8f5e9', 
          borderRadius: '8px',
          textAlign: 'center',
          marginBottom: '20px'
        }}>
          <div style={{ fontSize: '48px', marginBottom: '10px' }}>✅</div>
          <h3 style={{ color: '#4CAF50', marginBottom: '15px' }}>Account Created Successfully!</h3>
          <p style={{ marginBottom: '20px', color: '#666' }}>{message}</p>
          <p style={{ marginBottom: '20px', color: '#666' }}>
            Please check your email to verify your account before logging in.
          </p>
          <button
            onClick={handleContinueToLogin}
            style={{
              padding: '10px 30px',
              cursor: 'pointer',
              backgroundColor: '#4CAF50',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '16px'
            }}
          >
            Continue to Login
          </button>
        </div>
      ) : (
        <>
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '15px' }}>
              <label>Email:</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                style={{ width: '100%', padding: '8px', marginTop: '5px' }}
              />
            </div>
            <div style={{ marginBottom: '15px' }}>
              <label>Password:</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={8}
                style={{ width: '100%', padding: '8px', marginTop: '5px' }}
              />
              <small style={{ color: '#666' }}>Minimum 8 characters</small>
            </div>
            <button type="submit" style={{ padding: '10px 20px', cursor: 'pointer' }}>
              Sign Up
            </button>
          </form>
          {error && <p style={{ color: 'red', marginTop: '15px' }}>{error}</p>}
          <p style={{ marginTop: '15px' }}>
            Already have an account? <Link to="/login">Login</Link>
          </p>
        </>
      )}
    </div>
  );
};

export default Signup;
