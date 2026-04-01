import React from 'react';
import { Link } from 'react-router-dom';

interface NavigationProps {
  userEmail: string;
  onLogout: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ userEmail, onLogout }) => {
  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'space-between', 
      alignItems: 'center', 
      marginBottom: '20px',
      padding: '15px',
      backgroundColor: '#f5f5f5',
      borderRadius: '5px'
    }}>
      <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
        <Link to="/habits" style={{ textDecoration: 'none', color: '#2196F3', fontWeight: 'bold' }}>
          Habits
        </Link>
        <Link to="/stats" style={{ textDecoration: 'none', color: '#2196F3', fontWeight: 'bold' }}>
          Statistics
        </Link>
      </div>
      <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
        <span style={{ color: '#666', fontSize: '14px' }}>{userEmail}</span>
        <button 
          onClick={onLogout} 
          style={{ 
            padding: '8px 16px', 
            cursor: 'pointer',
            backgroundColor: '#f44336',
            color: 'white',
            border: 'none',
            borderRadius: '3px'
          }}
        >
          Logout
        </button>
      </div>
    </div>
  );
};

export default Navigation;
