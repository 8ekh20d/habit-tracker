import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { theme } from '../styles/theme';

const Signup: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
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

    setIsLoading(true);

    try {
      const response = await signup(email, password);
      setMessage(response.message);
      setIsSuccess(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Signup failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleContinueToLogin = () => {
    navigate('/login');
  };

  if (isSuccess) {
    return (
      <div style={{ 
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: theme.spacing.md,
      }}>
        <div style={{ 
          maxWidth: '500px',
          width: '100%',
          backgroundColor: theme.colors.white,
          borderRadius: theme.borderRadius.xl,
          boxShadow: theme.shadows.xl,
          padding: theme.spacing.xxl,
          textAlign: 'center',
        }}>
          <div style={{ fontSize: '64px', marginBottom: theme.spacing.lg }}>✅</div>
          <h2 style={{ 
            fontSize: '28px',
            fontWeight: '700',
            color: theme.colors.success,
            marginBottom: theme.spacing.md,
          }}>
            Account Created!
          </h2>
          <p style={{ 
            color: theme.colors.gray[600],
            marginBottom: theme.spacing.md,
            fontSize: '16px',
            lineHeight: '1.6',
          }}>
            {message}
          </p>
          <p style={{ 
            color: theme.colors.gray[600],
            marginBottom: theme.spacing.xl,
            fontSize: '14px',
          }}>
            Please check your email to verify your account before logging in.
          </p>
          <button
            onClick={handleContinueToLogin}
            style={{
              padding: '14px 32px',
              background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
              color: theme.colors.white,
              border: 'none',
              borderRadius: theme.borderRadius.md,
              fontSize: '16px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: `all ${theme.transitions.normal}`,
            }}
            onMouseOver={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
            onMouseOut={(e) => e.currentTarget.style.transform = 'translateY(0)'}
          >
            Continue to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{ 
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: theme.spacing.md,
    }}>
      <div style={{ 
        maxWidth: '420px',
        width: '100%',
        backgroundColor: theme.colors.white,
        borderRadius: theme.borderRadius.xl,
        boxShadow: theme.shadows.xl,
        padding: theme.spacing.xxl,
      }}>
        <div style={{ textAlign: 'center', marginBottom: theme.spacing.xl }}>
          <h1 style={{ 
            fontSize: '32px',
            fontWeight: '700',
            background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            marginBottom: theme.spacing.sm,
          }}>
            Create Account
          </h1>
          <p style={{ color: theme.colors.gray[600], fontSize: '14px' }}>
            Start building better habits today
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: theme.spacing.lg }}>
            <label style={{ 
              display: 'block',
              marginBottom: theme.spacing.sm,
              color: theme.colors.gray[700],
              fontSize: '14px',
              fontWeight: '500',
            }}>
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@example.com"
              style={{ 
                width: '100%',
                padding: '12px 16px',
                border: `2px solid ${theme.colors.gray[200]}`,
                borderRadius: theme.borderRadius.md,
                fontSize: '14px',
                transition: `all ${theme.transitions.fast}`,
                outline: 'none',
              }}
              onFocus={(e) => e.target.style.borderColor = theme.colors.primary}
              onBlur={(e) => e.target.style.borderColor = theme.colors.gray[200]}
            />
          </div>

          <div style={{ marginBottom: theme.spacing.lg }}>
            <label style={{ 
              display: 'block',
              marginBottom: theme.spacing.sm,
              color: theme.colors.gray[700],
              fontSize: '14px',
              fontWeight: '500',
            }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
              placeholder="••••••••"
              style={{ 
                width: '100%',
                padding: '12px 16px',
                border: `2px solid ${theme.colors.gray[200]}`,
                borderRadius: theme.borderRadius.md,
                fontSize: '14px',
                transition: `all ${theme.transitions.fast}`,
                outline: 'none',
              }}
              onFocus={(e) => e.target.style.borderColor = theme.colors.primary}
              onBlur={(e) => e.target.style.borderColor = theme.colors.gray[200]}
            />
            <small style={{ color: theme.colors.gray[500], fontSize: '12px', marginTop: theme.spacing.xs, display: 'block' }}>
              Minimum 8 characters
            </small>
          </div>

          {error && (
            <div style={{ 
              padding: theme.spacing.md,
              backgroundColor: theme.colors.dangerLight,
              borderRadius: theme.borderRadius.md,
              marginBottom: theme.spacing.lg,
              display: 'flex',
              alignItems: 'center',
              gap: theme.spacing.sm,
            }}>
              <span style={{ fontSize: '18px' }}>⚠️</span>
              <span style={{ color: theme.colors.danger, fontSize: '14px' }}>{error}</span>
            </div>
          )}

          <button 
            type="submit"
            disabled={isLoading}
            style={{ 
              width: '100%',
              padding: '14px',
              background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
              color: theme.colors.white,
              border: 'none',
              borderRadius: theme.borderRadius.md,
              fontSize: '16px',
              fontWeight: '600',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: `all ${theme.transitions.normal}`,
              opacity: isLoading ? 0.7 : 1,
            }}
            onMouseOver={(e) => !isLoading && (e.currentTarget.style.transform = 'translateY(-2px)')}
            onMouseOut={(e) => e.currentTarget.style.transform = 'translateY(0)'}
          >
            {isLoading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <div style={{ 
          marginTop: theme.spacing.lg,
          textAlign: 'center',
          fontSize: '14px',
          color: theme.colors.gray[600],
        }}>
          Already have an account?{' '}
          <Link 
            to="/login"
            style={{ 
              color: theme.colors.primary,
              textDecoration: 'none',
              fontWeight: '600',
            }}
          >
            Login
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Signup;
