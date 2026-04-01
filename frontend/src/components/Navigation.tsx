import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { theme } from '../styles/theme';

interface NavigationProps {
  userEmail: string;
  onLogout: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ userEmail, onLogout }) => {
  const location = useLocation();
  
  const isActive = (path: string) => location.pathname === path;
  
  return (
    <div style={{ 
      backgroundColor: theme.colors.white,
      boxShadow: theme.shadows.sm,
      position: 'sticky',
      top: 0,
      zIndex: 1000,
    }}>
      <div style={{ 
        maxWidth: '1200px',
        margin: '0 auto',
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        padding: `${theme.spacing.lg} ${theme.spacing.xl}`,
      }}>
        <div style={{ display: 'flex', gap: theme.spacing.xl, alignItems: 'center' }}>
          <h2 style={{ 
            margin: 0,
            fontSize: '24px',
            fontWeight: '700',
            background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}>
            HabitTracker
          </h2>
          <div style={{ display: 'flex', gap: theme.spacing.md }}>
            <Link 
              to="/habits" 
              style={{ 
                textDecoration: 'none',
                padding: `${theme.spacing.sm} ${theme.spacing.md}`,
                borderRadius: theme.borderRadius.md,
                fontWeight: '600',
                fontSize: '14px',
                color: isActive('/habits') ? theme.colors.primary : theme.colors.gray[600],
                backgroundColor: isActive('/habits') ? theme.colors.gray[100] : 'transparent',
                transition: `all ${theme.transitions.fast}`,
              }}
            >
              Habits
            </Link>
            <Link 
              to="/stats" 
              style={{ 
                textDecoration: 'none',
                padding: `${theme.spacing.sm} ${theme.spacing.md}`,
                borderRadius: theme.borderRadius.md,
                fontWeight: '600',
                fontSize: '14px',
                color: isActive('/stats') ? theme.colors.primary : theme.colors.gray[600],
                backgroundColor: isActive('/stats') ? theme.colors.gray[100] : 'transparent',
                transition: `all ${theme.transitions.fast}`,
              }}
            >
              Statistics
            </Link>
          </div>
        </div>
        <div style={{ display: 'flex', gap: theme.spacing.md, alignItems: 'center' }}>
          <div style={{ 
            padding: `${theme.spacing.sm} ${theme.spacing.md}`,
            backgroundColor: theme.colors.gray[100],
            borderRadius: theme.borderRadius.md,
            fontSize: '13px',
            color: theme.colors.gray[700],
            fontWeight: '500',
          }}>
            {userEmail}
          </div>
          <button 
            onClick={onLogout} 
            style={{ 
              padding: `${theme.spacing.sm} ${theme.spacing.md}`,
              cursor: 'pointer',
              backgroundColor: theme.colors.danger,
              color: theme.colors.white,
              border: 'none',
              borderRadius: theme.borderRadius.md,
              fontSize: '14px',
              fontWeight: '600',
              transition: `all ${theme.transitions.fast}`,
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#dc2626'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = theme.colors.danger}
          >
            Logout
          </button>
        </div>
      </div>
    </div>
  );
};

export default Navigation;
