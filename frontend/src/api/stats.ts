import axiosInstance from './axios';

export interface HabitStats {
  habitId: number;
  habitName: string;
  currentStreak: number;
  totalCompletions: number;
}

export interface StatsResponse {
  habits: HabitStats[];
}

export const getStats = async (): Promise<StatsResponse> => {
  const response = await axiosInstance.get('/stats');
  return response.data;
};
