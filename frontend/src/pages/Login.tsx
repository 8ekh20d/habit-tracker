import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { theme } from '../styles/theme';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(email, password);
      navigate('/habits');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Login failed';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

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
            Welcome Back
          </h1>
          <p style={{ color: theme.colors.gray[600], fontSize: '14px' }}>
            Sign in to continue your habit journey
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
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div style={{ 
          marginTop: theme.spacing.lg,
          textAlign: 'center',
          fontSize: '14px',
          color: theme.colors.gray[600],
        }}>
          Don't have an account?{' '}
          <Link 
            to="/signup"
            style={{ 
              color: theme.colors.primary,
              textDecoration: 'none',
              fontWeight: '600',
            }}
          >
            Sign Up
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
