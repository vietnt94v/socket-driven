import axios from 'axios'
import { useAuthStore } from '../stores/authStore'

export const client = axios.create({
  baseURL: 'https://dummyjson.com',
})

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
