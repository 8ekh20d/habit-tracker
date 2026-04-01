import axiosInstance from './axios';

export const signup = async (email: string, password: string) => {
  const response = await axiosInstance.post('/auth/signup', { email, password });
  return response.data;
};

export const login = async (email: string, password: string) => {
  const response = await axiosInstance.post('/auth/login', { email, password });
  return response.data;
};

export const verifyEmail = async (token: string) => {
  const response = await axiosInstance.post(`/auth/verify-email?token=${token}`);
  return response.data;
};
