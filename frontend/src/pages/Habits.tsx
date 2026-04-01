import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getHabits, createHabit, updateHabit, deleteHabit, checkHabit, Habit } from '../api/habits';

const Habits: React.FC = () => {
  const [habits, setHabits] = useState<Habit[]>([]);
  const [newHabitName, setNewHabitName] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();
  const { logout } = useAuth();

  useEffect(() => {
    loadHabits();
  }, []);

  const loadHabits = async () => {
    try {
      const data = await getHabits();
      setHabits(data);
    } catch (err: any) {
      setError('Failed to load habits');
    }
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
      setError(err.response?.data?.message || 'Failed to create habit');
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
      setError(err.response?.data?.message || 'Failed to update habit');
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
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete habit');
    }
  };

  const handleCheckHabit = async (id: number) => {
    setError('');
    setMessage('');

    const today = new Date().toISOString().split('T')[0];

    try {
      await checkHabit(id, today);
      setMessage('Habit marked as done for today');
      loadHabits();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to check habit');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ maxWidth: '800px', margin: '50px auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>My Habits</h2>
        <div>
          <Link to="/stats" style={{ marginRight: '15px' }}>View Stats</Link>
          <button onClick={handleLogout} style={{ padding: '8px 16px', cursor: 'pointer' }}>
            Logout
          </button>
        </div>
      </div>

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
            {habits.map((habit) => (
              <li
                key={habit.id}
                style={{
                  border: '1px solid #ddd',
                  padding: '15px',
                  marginBottom: '10px',
                  borderRadius: '5px',
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
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <h3 style={{ margin: '0 0 5px 0' }}>{habit.name}</h3>
                      <small style={{ color: '#666' }}>
                        Created: {new Date(habit.createdAt).toLocaleDateString()}
                      </small>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button
                        onClick={() => handleCheckHabit(habit.id)}
                        style={{ padding: '8px 16px', cursor: 'pointer', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '3px' }}
                      >
                        ✓ Done Today
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
                        style={{ padding: '8px 16px', cursor: 'pointer', backgroundColor: '#f44336', color: 'white', border: 'none', borderRadius: '3px' }}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Habits;
