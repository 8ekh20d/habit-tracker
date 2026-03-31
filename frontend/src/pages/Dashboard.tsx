import React from 'react';
import { useNavigate } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div style={{ maxWidth: '800px', margin: '50px auto', padding: '20px' }}>
      <h2>Dashboard</h2>
      <p>Welcome to Habit Tracker!</p>
      <button onClick={handleLogout} style={{ padding: '10px 20px', cursor: 'pointer', marginTop: '20px' }}>
        Logout
      </button>
    </div>
  );
};

export default Dashboard;
