import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getHabits, createHabit, updateHabit, deleteHabit, checkHabit, getHabitRecords, Habit, HabitRecord } from '../api/habits';
import Navigation from '../components/Navigation';
import HabitCalendar from '../components/HabitCalendar';
import { getEmailFromToken } from '../utils/jwt';
import { theme } from '../styles/theme';

const HabitsModern: React.FC = () => {
  const [habits, setHabits] = useState<Habit[]>([]);
  const [habitRecords, setHabitRecords] = useState<HabitRecord[]>([]);
  const [newHabitName, setNewHabitName] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [collapsedHabits, setCollapsedHabits] = useState<Set<number>>(new Set());
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

  const calculateStreak = (habitId: number): number => {
    const records = habitRecords
      .filter(r => r.habitId === habitId)
      .map(r => r.date)
      .sort((a, b) => b.localeCompare(a)); // Sort descending (newest first)

    if (records.length === 0) return 0;

    let streak = 0;
    const today = new Date();
    let currentDate = new Date(today);

    // Check if today is completed
    const todayStr = today.toISOString().split('T')[0];
    if (!records.includes(todayStr)) {
      // If today is not completed, start from yesterday
      currentDate.setDate(currentDate.getDate() - 1);
    }

    // Count consecutive days backwards
    while (true) {
      const dateStr = currentDate.toISOString().split('T')[0];
      if (records.includes(dateStr)) {
        streak++;
        currentDate.setDate(currentDate.getDate() - 1);
      } else {
        break;
      }
    }

    return streak;
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

  const toggleCollapse = (habitId: number) => {
    setCollapsedHabits(prev => {
      const newSet = new Set(prev);
      if (newSet.has(habitId)) {
        newSet.delete(habitId);
      } else {
        newSet.add(habitId);
      }
      return newSet;
    });
  };

  const today = getTodayString();

  return (
    <div style={{ minHeight: '100vh', backgroundColor: theme.colors.gray[50] }}>
      <Navigation userEmail={userEmail} onLogout={handleLogout} />
      
      <div style={{ maxWidth: '900px', margin: '0 auto', padding: theme.spacing.xl }}>
        <div style={{ marginBottom: theme.spacing.xl }}>
          <h1 style={{ 
            fontSize: '32px',
            fontWeight: '700',
            color: theme.colors.gray[900],
            marginBottom: theme.spacing.sm,
          }}>
            My Habits
          </h1>
          <p style={{ color: theme.colors.gray[600], fontSize: '16px' }}>
            Track your daily habits and build lasting routines
          </p>
        </div>

        {message && (
          <div style={{ 
            padding: theme.spacing.md,
            backgroundColor: theme.colors.successLight,
            borderRadius: theme.borderRadius.md,
            marginBottom: theme.spacing.lg,
            display: 'flex',
            alignItems: 'center',
            gap: theme.spacing.sm,
            boxShadow: theme.shadows.sm,
          }}>
            <span style={{ fontSize: '20px' }}>✅</span>
            <span style={{ color: theme.colors.success, fontSize: '14px', fontWeight: '500' }}>{message}</span>
          </div>
        )}

        {error && (
          <div style={{ 
            padding: theme.spacing.md,
            backgroundColor: theme.colors.dangerLight,
            borderRadius: theme.borderRadius.md,
            marginBottom: theme.spacing.lg,
            display: 'flex',
            alignItems: 'center',
            gap: theme.spacing.sm,
            boxShadow: theme.shadows.sm,
          }}>
            <span style={{ fontSize: '20px' }}>⚠️</span>
            <span style={{ color: theme.colors.danger, fontSize: '14px', fontWeight: '500' }}>{error}</span>
          </div>
        )}

        <div style={{ 
          backgroundColor: theme.colors.white,
          borderRadius: theme.borderRadius.lg,
          padding: theme.spacing.xl,
          marginBottom: theme.spacing.xl,
          boxShadow: theme.shadows.md,
        }}>
          <h2 style={{ 
            fontSize: '18px',
            fontWeight: '600',
            color: theme.colors.gray[900],
            marginBottom: theme.spacing.md,
          }}>
            Create New Habit
          </h2>
          <form onSubmit={handleCreateHabit} style={{ display: 'flex', gap: theme.spacing.md }}>
            <input
              type="text"
              value={newHabitName}
              onChange={(e) => setNewHabitName(e.target.value)}
              placeholder="Enter habit name (e.g., Morning Exercise)"
              maxLength={100}
              style={{ 
                flex: 1,
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
            <button 
              type="submit"
              style={{ 
                padding: '12px 32px',
                background: `linear-gradient(135deg, ${theme.colors.primary} 0%, ${theme.colors.secondary} 100%)`,
                color: theme.colors.white,
                border: 'none',
                borderRadius: theme.borderRadius.md,
                fontSize: '14px',
                fontWeight: '600',
                cursor: 'pointer',
                transition: `all ${theme.transitions.normal}`,
                whiteSpace: 'nowrap',
              }}
              onMouseOver={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
              onMouseOut={(e) => e.currentTarget.style.transform = 'translateY(0)'}
            >
              Add Habit
            </button>
          </form>
        </div>

        {habits.length === 0 ? (
          <div style={{ 
            backgroundColor: theme.colors.white,
            borderRadius: theme.borderRadius.lg,
            padding: theme.spacing.xxl,
            textAlign: 'center',
            boxShadow: theme.shadows.md,
          }}>
            <div style={{ fontSize: '64px', marginBottom: theme.spacing.md }}>🎯</div>
            <h3 style={{ 
              fontSize: '20px',
              fontWeight: '600',
              color: theme.colors.gray[900],
              marginBottom: theme.spacing.sm,
            }}>
              No habits yet
            </h3>
            <p style={{ color: theme.colors.gray[600], fontSize: '14px' }}>
              Create your first habit above to start building better routines!
            </p>
          </div>
        ) : (
          <div style={{ display: 'grid', gap: theme.spacing.lg }}>
            {habits.map((habit) => {
              const isCompletedToday = isHabitCompletedForDate(habit.id, today);
              const currentStreak = calculateStreak(habit.id);
              const isCollapsed = collapsedHabits.has(habit.id);
              
              return (
                <div
                  key={habit.id}
                  style={{
                    backgroundColor: theme.colors.white,
                    borderRadius: theme.borderRadius.lg,
                    padding: theme.spacing.xl,
                    boxShadow: theme.shadows.md,
                    border: isCompletedToday ? `2px solid ${theme.colors.success}` : `2px solid ${theme.colors.gray[100]}`,
                    transition: `all ${theme.transitions.normal}`,
                  }}
                >
                  {editingId === habit.id ? (
                    <div style={{ display: 'flex', gap: theme.spacing.md, alignItems: 'center' }}>
                      <input
                        type="text"
                        value={editingName}
                        onChange={(e) => setEditingName(e.target.value)}
                        maxLength={100}
                        style={{ 
                          flex: 1,
                          padding: '12px 16px',
                          border: `2px solid ${theme.colors.gray[200]}`,
                          borderRadius: theme.borderRadius.md,
                          fontSize: '14px',
                        }}
                      />
                      <button
                        onClick={() => handleUpdateHabit(habit.id)}
                        style={{ 
                          padding: '10px 20px',
                          backgroundColor: theme.colors.success,
                          color: theme.colors.white,
                          border: 'none',
                          borderRadius: theme.borderRadius.md,
                          fontSize: '14px',
                          fontWeight: '600',
                          cursor: 'pointer',
                        }}
                      >
                        Save
                      </button>
                      <button
                        onClick={() => {
                          setEditingId(null);
                          setEditingName('');
                        }}
                        style={{ 
                          padding: '10px 20px',
                          backgroundColor: theme.colors.gray[200],
                          color: theme.colors.gray[700],
                          border: 'none',
                          borderRadius: theme.borderRadius.md,
                          fontSize: '14px',
                          fontWeight: '600',
                          cursor: 'pointer',
                        }}
                      >
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <>
                      {/* Header row with collapse button always on right */}
                      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: isCollapsed ? 0 : theme.spacing.lg }}>
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: theme.spacing.md, marginBottom: theme.spacing.sm, flexWrap: 'wrap' }}>
                            <h3 style={{ 
                              fontSize: '20px',
                              fontWeight: '600',
                              color: theme.colors.gray[900],
                              margin: 0,
                            }}>
                              {habit.name}
                            </h3>
                            {currentStreak > 0 && (
                              <span style={{ 
                                padding: '4px 12px',
                                backgroundColor: theme.colors.warningLight,
                                color: theme.colors.warning,
                                borderRadius: theme.borderRadius.full,
                                fontSize: '13px',
                                fontWeight: '700',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '4px',
                              }}>
                                🔥 {currentStreak} day{currentStreak !== 1 ? 's' : ''}
                              </span>
                            )}
                            {isCompletedToday && (
                              <span style={{ 
                                padding: '4px 12px',
                                backgroundColor: theme.colors.successLight,
                                color: theme.colors.success,
                                borderRadius: theme.borderRadius.full,
                                fontSize: '12px',
                                fontWeight: '600',
                              }}>
                                ✓ Done Today
                              </span>
                            )}
                          </div>
                          <p style={{ 
                            color: theme.colors.gray[500],
                            fontSize: '13px',
                            margin: 0,
                          }}>
                            Created {new Date(habit.createdAt).toLocaleDateString('en-US', { 
                              month: 'short', 
                              day: 'numeric', 
                              year: 'numeric' 
                            })}
                          </p>
                        </div>
                        
                        {/* Collapse button - always stays on right */}
                        <button
                          onClick={() => toggleCollapse(habit.id)}
                          style={{
                            padding: '8px',
                            backgroundColor: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '40px',
                            color: theme.colors.gray[400],
                            transition: `all ${theme.transitions.fast}`,
                            flexShrink: 0,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            marginLeft: theme.spacing.md,
                          }}
                          title={isCollapsed ? 'Expand' : 'Collapse'}
                        >
                          {isCollapsed ? '▾' : '▴'}
                        </button>
                      </div>

                      {/* Content area - only shown when expanded */}
                      {!isCollapsed && (
                        <div style={{ display: 'flex', gap: theme.spacing.xl, alignItems: 'flex-start' }}>
                          {/* Left side - Action buttons */}
                          <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{ display: 'flex', gap: theme.spacing.sm, flexWrap: 'wrap' }}>
                              <button
                                onClick={() => handleCheckHabit(habit.id)}
                                disabled={isCompletedToday}
                                style={{
                                  padding: '10px 20px',
                                  backgroundColor: isCompletedToday ? theme.colors.gray[200] : theme.colors.success,
                                  color: isCompletedToday ? theme.colors.gray[500] : theme.colors.white,
                                  border: 'none',
                                  borderRadius: theme.borderRadius.md,
                                  fontSize: '14px',
                                  fontWeight: '600',
                                  cursor: isCompletedToday ? 'not-allowed' : 'pointer',
                                  transition: `all ${theme.transitions.fast}`,
                                }}
                              >
                                {isCompletedToday ? '✓ Completed' : 'Mark Done'}
                              </button>
                              <button
                                onClick={() => {
                                  setEditingId(habit.id);
                                  setEditingName(habit.name);
                                }}
                                style={{ 
                                  padding: '10px 20px',
                                  backgroundColor: theme.colors.gray[100],
                                  color: theme.colors.gray[700],
                                  border: 'none',
                                  borderRadius: theme.borderRadius.md,
                                  fontSize: '14px',
                                  fontWeight: '600',
                                  cursor: 'pointer',
                                }}
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteHabit(habit.id)}
                                style={{
                                  padding: '10px 20px',
                                  backgroundColor: theme.colors.dangerLight,
                                  color: theme.colors.danger,
                                  border: 'none',
                                  borderRadius: theme.borderRadius.md,
                                  fontSize: '14px',
                                  fontWeight: '600',
                                  cursor: 'pointer',
                                }}
                              >
                                Delete
                              </button>
                            </div>
                          </div>
                          
                          {/* Right side - Calendar */}
                          <div style={{ 
                            padding: theme.spacing.lg,
                            backgroundColor: theme.colors.gray[50],
                            borderRadius: theme.borderRadius.md,
                          }}>
                            <h4 style={{ 
                              fontSize: '14px',
                              fontWeight: '600',
                              color: theme.colors.gray[700],
                              marginBottom: theme.spacing.sm,
                              marginTop: 0,
                            }}>
                              Last 5 Weeks
                            </h4>
                            <HabitCalendar habitRecords={habitRecords} habitId={habit.id} />
                          </div>
                        </div>
                      )}
                    </>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default HabitsModern;
