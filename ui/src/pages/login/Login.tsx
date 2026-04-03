import { useState, type SubmitEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { login } from '../../apis'
import { useAuthStore } from '../../stores/authStore'

const Login = () => {
  const navigate = useNavigate()
  const token = useAuthStore((s) => s.token)
  const setSession = useAuthStore((s) => s.setSession)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  if (token) {
    return <Navigate to="/home" replace />
  }

  const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError(null)
    setPending(true)
    try {
      const { accessToken, user } = await login(username, password)
      setSession(accessToken, user)
      navigate('/home', { replace: true })
    } catch {
      setError('Login failed. Check username and password.')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-6">
      <form
        className="flex w-full max-w-sm flex-col gap-4 rounded-lg border border-neutral-200 p-6 dark:border-neutral-700"
        onSubmit={handleSubmit}
      >
        <h1 className="text-center text-xl font-medium text-neutral-900 dark:text-neutral-100">
          Login
        </h1>
        <label className="flex flex-col gap-1 text-left text-sm">
          <span className="text-neutral-600 dark:text-neutral-400">Username</span>
          <input
            className="rounded border border-neutral-300 px-3 py-2 dark:border-neutral-600 dark:bg-neutral-900"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            required
          />
        </label>
        <label className="flex flex-col gap-1 text-left text-sm">
          <span className="text-neutral-600 dark:text-neutral-400">Password</span>
          <input
            type="password"
            className="rounded border border-neutral-300 px-3 py-2 dark:border-neutral-600 dark:bg-neutral-900"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        {error ? (
          <p className="text-center text-sm text-red-600 dark:text-red-400" role="alert">
            {error}
          </p>
        ) : null}
        <button
          type="submit"
          disabled={pending}
          className="rounded bg-violet-600 px-4 py-2 font-medium text-white disabled:opacity-50"
        >
          {pending ? 'Signing in…' : 'Sign in'}
        </button>
        <p className="text-center text-xs text-neutral-500">
          DummyJSON test user: emilys / emilyspass
        </p>
      </form>
    </div>
  )
}

export default Login
