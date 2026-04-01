import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getStats, HabitStats } from '../api/stats';
import Navigation from '../components/Navigation';
import { getEmailFromToken } from '../utils/jwt';

const Stats: React.FC = () => {
  const [stats, setStats] = useState<HabitStats[]>([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { logout, token } = useAuth();
  const userEmail = getEmailFromToken(token);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const data = await getStats();
      setStats(data.habits);
    } catch (err: any) {
      setError('Failed to load statistics');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ maxWidth: '800px', margin: '50px auto', padding: '20px' }}>
      <Navigation userEmail={userEmail} onLogout={handleLogout} />
      
      <h2 style={{ marginBottom: '20px' }}>Habit Statistics</h2>

      {error && <p style={{ color: 'red', marginBottom: '15px' }}>{error}</p>}

      <div>
        {stats.length === 0 ? (
          <p>No statistics available. Start tracking your habits!</p>
        ) : (
          <div style={{ display: 'grid', gap: '15px' }}>
            {stats.map((stat) => (
              <div
                key={stat.habitId}
                style={{
                  border: '1px solid #ddd',
                  padding: '20px',
                  borderRadius: '5px',
                  backgroundColor: '#f9f9f9',
                }}
              >
                <h3 style={{ margin: '0 0 15px 0' }}>{stat.habitName}</h3>
                <div style={{ display: 'flex', gap: '30px' }}>
                  <div>
                    <p style={{ margin: '0', fontSize: '14px', color: '#666' }}>Current Streak</p>
                    <p style={{ margin: '5px 0 0 0', fontSize: '32px', fontWeight: 'bold', color: '#4CAF50' }}>
                      {stat.currentStreak > 0 && '🔥 '}
                      {stat.currentStreak} days
                    </p>
                  </div>
                  <div>
                    <p style={{ margin: '0', fontSize: '14px', color: '#666' }}>Total Completions</p>
                    <p style={{ margin: '5px 0 0 0', fontSize: '32px', fontWeight: 'bold', color: '#2196F3' }}>
                      {stat.totalCompletions}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Stats;
