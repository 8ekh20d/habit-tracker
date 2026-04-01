import axiosInstance from './axios';

export interface Habit {
  id: number;
  name: string;
  frequencyType: string;
  createdAt: string;
}

export interface HabitRecord {
  habitId: number;
  date: string;
  status: string;
}

export const getHabits = async (): Promise<Habit[]> => {
  const response = await axiosInstance.get('/habits');
  return response.data;
};

export const createHabit = async (name: string): Promise<Habit> => {
  const response = await axiosInstance.post('/habits', { name });
  return response.data;
};

export const updateHabit = async (id: number, name: string): Promise<Habit> => {
  const response = await axiosInstance.patch(`/habits/${id}`, { name });
  return response.data;
};

export const deleteHabit = async (id: number): Promise<void> => {
  await axiosInstance.delete(`/habits/${id}`);
};

export const checkHabit = async (id: number, date: string): Promise<HabitRecord> => {
  const response = await axiosInstance.post(`/habits/${id}/check`, { date });
  return response.data;
};

export const getHabitRecords = async (): Promise<HabitRecord[]> => {
  const response = await axiosInstance.get('/habits/records');
  return response.data;
};
