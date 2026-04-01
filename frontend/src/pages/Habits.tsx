import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getHabits, createHabit, updateHabit, deleteHabit, checkHabit, getHabitRecords, Habit, HabitRecord } from '../api/habits';
import Navigation from '../components/Navigation';
import { getEmailFromToken } from '../utils/jwt';

const Habits: React.FC = () => {
  const [habits, setHabits] = useState<Habit[]>([]);
  const [habitRecords, setHabitRecords] = useState<HabitRecord[]>([]);
  const [newHabitName, setNewHabitName] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const { logout, token } = useAuth();
  const userEmail = getEmailFromToken(token);

  // Auto-clear messages after 5 seconds
  useEffect(() => {
    if (message || error) {
      const timer = setTimeout(() => {
        setMessage('');
        setError('');
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [message, error]);

  useEffect(() => {
    loadHabits();
    loadHabitRecords();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadHabits = async () => {
    try {
      const data = await getHabits();
      setHabits(data);
    } catch (err: any) {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError('Failed to load habits');
      }
    }
  };

  const loadHabitRecords = async () => {
    try {
      const data = await getHabitRecords();
      setHabitRecords(data);
    } catch (err: any) {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      }
      // Silently fail for records - not critical
    }
  };

  const isHabitCompletedForDate = (habitId: number, date: string): boolean => {
    return habitRecords.some(
      (record) => record.habitId === habitId && record.date === date
    );
  };

  const getTodayString = (): string => {
    return new Date().toISOString().split('T')[0];
  };

  const handleCreateHabit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!newHabitName.trim()) {
      setError('Habit name cannot be empty');
      return;
    }

    if (newHabitName.length > 100) {
      setError('Habit name must not exceed 100 characters');
      return;
    }

    try {
      await createHabit(newHabitName);
      setNewHabitName('');
      setMessage('Habit created successfully');
      loadHabits();
    } catch (err: any) {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError(err.response?.data?.message || 'Failed to create habit');
      }
    }
  };

  const handleUpdateHabit = async (id: number) => {
    setError('');
    setMessage('');

    if (!editingName.trim()) {
      setError('Habit name cannot be empty');
      return;
    }

    if (editingName.length > 100) {
      setError('Habit name must not exceed 100 characters');
      return;
    }

    try {
      await updateHabit(id, editingName);
      setEditingId(null);
      setEditingName('');
      setMessage('Habit updated successfully');
      loadHabits();
    } catch (err: any) {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError(err.response?.data?.message || 'Failed to update habit');
      }
    }
  };

  const handleDeleteHabit = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this habit? All records will be deleted.')) {
      return;
    }

    setError('');
    setMessage('');

    try {
      await deleteHabit(id);
      setMessage('Habit deleted successfully');
      loadHabits();
      loadHabitRecords();
    } catch (err: any) {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError(err.response?.data?.message || 'Failed to delete habit');
      }
    }
  };

  const handleCheckHabit = async (id: number) => {
    setError('');
    setMessage('');

    const targetDate = getTodayString();

    try {
      await checkHabit(id, targetDate);
      setMessage('Habit marked as done for today');
      loadHabitRecords();
    } catch (err: any) {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError(err.response?.data?.message || 'Failed to check habit');
      }
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const today = getTodayString();

  return (
    <div style={{ maxWidth: '800px', margin: '50px auto', padding: '20px' }}>
      <Navigation userEmail={userEmail} onLogout={handleLogout} />
      
      <h2 style={{ marginBottom: '20px' }}>My Habits</h2>

      {message && <p style={{ color: 'green', marginBottom: '15px' }}>{message}</p>}
      {error && <p style={{ color: 'red', marginBottom: '15px' }}>{error}</p>}

      <form onSubmit={handleCreateHabit} style={{ marginBottom: '30px' }}>
        <div style={{ display: 'flex', gap: '10px' }}>
          <input
            type="text"
            value={newHabitName}
            onChange={(e) => setNewHabitName(e.target.value)}
            placeholder="Enter new habit name"
            style={{ flex: 1, padding: '8px' }}
            maxLength={100}
          />
          <button type="submit" style={{ padding: '8px 20px', cursor: 'pointer' }}>
            Add Habit
          </button>
        </div>
      </form>

      <div>
        {habits.length === 0 ? (
          <p>No habits yet. Create your first habit above!</p>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {habits.map((habit) => {
              const isCompletedToday = isHabitCompletedForDate(habit.id, today);
              
              return (
                <li
                  key={habit.id}
                  style={{
                    border: '1px solid #ddd',
                    padding: '15px',
                    marginBottom: '10px',
                    borderRadius: '5px',
                    backgroundColor: isCompletedToday ? '#e8f5e9' : 'white',
                  }}
                >
                  {editingId === habit.id ? (
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                      <input
                        type="text"
                        value={editingName}
                        onChange={(e) => setEditingName(e.target.value)}
                        style={{ flex: 1, padding: '8px' }}
                        maxLength={100}
                      />
                      <button
                        onClick={() => handleUpdateHabit(habit.id)}
                        style={{ padding: '8px 16px', cursor: 'pointer' }}
                      >
                        Save
                      </button>
                      <button
                        onClick={() => {
                          setEditingId(null);
                          setEditingName('');
                        }}
                        style={{ padding: '8px 16px', cursor: 'pointer' }}
                      >
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h3 style={{ margin: '0 0 5px 0' }}>
                            {habit.name}
                            {isCompletedToday && (
                              <span style={{ marginLeft: '10px', color: '#4CAF50', fontSize: '18px' }}>
                                ✓
                              </span>
                            )}
                          </h3>
                          <small style={{ color: '#666' }}>
                            Created: {new Date(habit.createdAt).toLocaleDateString()}
                          </small>
                        </div>
                        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                          <button
                            onClick={() => handleCheckHabit(habit.id)}
                            style={{
                              padding: '8px 16px',
                              cursor: 'pointer',
                              backgroundColor: isCompletedToday ? '#81C784' : '#4CAF50',
                              color: 'white',
                              border: 'none',
                              borderRadius: '3px',
                            }}
                            disabled={isCompletedToday}
                          >
                            {isCompletedToday ? '✓ Done Today' : 'Mark Done Today'}
                          </button>
                          <button
                            onClick={() => {
                              setEditingId(habit.id);
                              setEditingName(habit.name);
                            }}
                            style={{ padding: '8px 16px', cursor: 'pointer' }}
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteHabit(habit.id)}
                            style={{
                              padding: '8px 16px',
                              cursor: 'pointer',
                              backgroundColor: '#f44336',
                              color: 'white',
                              border: 'none',
                              borderRadius: '3px',
                            }}
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Habits;
