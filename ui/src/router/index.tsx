import { Suspense } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { AppRoutes } from './routes'

const RouteFallback = () => (
  <div className="flex min-h-screen items-center justify-center p-6 text-neutral-500">Loading…</div>
)

export const AppRouter = () => (
  <BrowserRouter>
    <Suspense fallback={<RouteFallback />}>
      <AppRoutes />
    </Suspense>
  </BrowserRouter>
)
