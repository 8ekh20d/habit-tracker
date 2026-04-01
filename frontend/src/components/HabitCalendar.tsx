import React from 'react';
import { theme } from '../styles/theme';

interface HabitCalendarProps {
  habitRecords: Array<{ date: string; habitId: number }>;
  habitId: number;
}

const HabitCalendar: React.FC<HabitCalendarProps> = ({ habitRecords, habitId }) => {
  // Get last 30 days
  const getLast30Days = () => {
    const days = [];
    const today = new Date();
    for (let i = 29; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      days.push(date.toISOString().split('T')[0]);
    }
    return days;
  };

  const last30Days = getLast30Days();
  
  const isCompleted = (date: string) => {
    return habitRecords.some(
      (record) => record.habitId === habitId && record.date === date
    );
  };

  const getDayName = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { weekday: 'short' })[0];
  };

  return (
    <div style={{ marginTop: theme.spacing.md }}>
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(32px, 1fr))',
        gap: theme.spacing.xs,
        maxWidth: '100%'
      }}>
        {last30Days.map((date) => {
          const completed = isCompleted(date);
          const isToday = date === new Date().toISOString().split('T')[0];
          
          return (
            <div
              key={date}
              title={date}
              style={{
                width: '32px',
                height: '32px',
                borderRadius: theme.borderRadius.sm,
                backgroundColor: completed 
                  ? theme.colors.success 
                  : theme.colors.gray[200],
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '10px',
                fontWeight: isToday ? 'bold' : 'normal',
                color: completed ? theme.colors.white : theme.colors.gray[500],
                border: isToday ? `2px solid ${theme.colors.primary}` : 'none',
                transition: `all ${theme.transitions.fast}`,
                cursor: 'pointer',
              }}
            >
              {getDayName(date)}
            </div>
          );
        })}
      </div>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        marginTop: theme.spacing.sm,
        fontSize: '12px',
        color: theme.colors.gray[500]
      }}>
        <span>30 days ago</span>
        <span>Today</span>
      </div>
    </div>
  );
};

export default HabitCalendar;
