import React from 'react';
import { theme } from '../styles/theme';

interface HabitCalendarProps {
  habitRecords: Array<{ date: string; habitId: number }>;
  habitId: number;
}

const HabitCalendar: React.FC<HabitCalendarProps> = ({ habitRecords, habitId }) => {
  const isCompleted = (date: string) => {
    return habitRecords.some(
      (record) => record.habitId === habitId && record.date === date
    );
  };

  // Get calendar data for last 5 weeks (35 days to ensure we show complete weeks)
  const getCalendarDays = () => {
    const days = [];
    const today = new Date();
    
    // Go back 34 days to get ~5 weeks of data
    for (let i = 34; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      days.push({
        dateStr: date.toISOString().split('T')[0],
        date: date,
        dayOfWeek: date.getDay(), // 0 = Sunday, 1 = Monday, etc.
        dateNum: date.getDate(),
      });
    }
    
    // Find the first Monday (or start of week)
    const firstDayIndex = days.findIndex(d => d.dayOfWeek === 1); // Monday
    if (firstDayIndex > 0) {
      days.splice(0, firstDayIndex);
    }
    
    // Organize into weeks (7 days each)
    const weeks = [];
    for (let i = 0; i < days.length; i += 7) {
      weeks.push(days.slice(i, i + 7));
    }
    
    return weeks;
  };

  const weeks = getCalendarDays();
  const dayHeaders = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const isWeekend = (day: string) => day === 'Sat' || day === 'Sun';

  return (
    <div style={{ marginTop: theme.spacing.md, maxWidth: '400px' }}>
      {/* Day headers */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(7, 1fr)',
        gap: theme.spacing.xs,
        marginBottom: theme.spacing.xs,
      }}>
        {dayHeaders.map((day) => (
          <div
            key={day}
            style={{
              textAlign: 'center',
              fontSize: '11px',
              fontWeight: '600',
              color: isWeekend(day) ? '#ef4444' : theme.colors.gray[600],
              padding: '4px',
            }}
          >
            {day}
          </div>
        ))}
      </div>

      {/* Calendar grid */}
      {weeks.map((week, weekIndex) => (
        <div
          key={weekIndex}
          style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(7, 1fr)',
            gap: theme.spacing.xs,
            marginBottom: theme.spacing.xs,
          }}
        >
          {week.map((day) => {
            const completed = isCompleted(day.dateStr);
            const isToday = day.dateStr === new Date().toISOString().split('T')[0];
            const isWeekendDay = day.dayOfWeek === 0 || day.dayOfWeek === 6; // Sunday or Saturday
            
            return (
              <div
                key={day.dateStr}
                title={day.date.toLocaleDateString('en-US', { 
                  weekday: 'short', 
                  month: 'short', 
                  day: 'numeric',
                  year: 'numeric'
                })}
                style={{
                  aspectRatio: '1',
                  borderRadius: theme.borderRadius.sm,
                  backgroundColor: completed 
                    ? theme.colors.success 
                    : theme.colors.gray[200],
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '13px',
                  fontWeight: isToday ? '700' : '500',
                  color: completed 
                    ? theme.colors.white 
                    : isWeekendDay 
                      ? '#ef4444' 
                      : theme.colors.gray[700],
                  border: isToday ? `2px solid ${theme.colors.primary}` : 'none',
                  transition: `all ${theme.transitions.fast}`,
                  cursor: 'pointer',
                  boxShadow: completed ? theme.shadows.sm : 'none',
                }}
              >
                {day.dateNum}
              </div>
            );
          })}
        </div>
      ))}
    </div>
  );
};

export default HabitCalendar;
