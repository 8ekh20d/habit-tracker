import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const signup = async (email: string, password: string) => {
  const response = await axios.post(`${API_URL}/auth/signup`, { email, password });
  return response.data;
};

export const login = async (email: string, password: string) => {
  const response = await axios.post(`${API_URL}/auth/login`, { email, password });
  return response.data;
};

export const verifyEmail = async (token: string) => {
  const response = await axios.post(`${API_URL}/auth/verify-email?token=${token}`);
  return response.data;
};
