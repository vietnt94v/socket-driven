import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const baseURL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081';

export const client = axios.create({
  baseURL,
});

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
