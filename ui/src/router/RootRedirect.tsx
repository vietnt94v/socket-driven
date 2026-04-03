import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'

export const RootRedirect = () => {
  const token = useAuthStore((s) => s.token)
  return <Navigate to={token ? '/home' : '/login'} replace />
}
