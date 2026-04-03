import { useAuthStore } from '../../stores/authStore'

const Home = () => {
  const user = useAuthStore((s) => s.user)

  return (
    <div className="flex min-h-screen items-center justify-center p-6">
      {user ? (
        <article className="w-full max-w-sm overflow-hidden rounded-xl border border-neutral-200 bg-white shadow-sm dark:border-neutral-700 dark:bg-neutral-900">
          <div className="flex flex-col items-center gap-4 p-6">
            <img
              src={user.image}
              alt=""
              className="h-24 w-24 rounded-full border border-neutral-200 object-cover dark:border-neutral-600"
            />
            <div className="text-center">
              <h1 className="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                {user.firstName} {user.lastName}
              </h1>
              <p className="text-sm text-neutral-500 dark:text-neutral-400">@{user.username}</p>
            </div>
            <dl className="w-full space-y-2 text-sm">
              <div className="flex justify-between gap-4 border-t border-neutral-100 pt-3 dark:border-neutral-800">
                <dt className="text-neutral-500 dark:text-neutral-400">Email</dt>
                <dd className="truncate text-right font-medium text-neutral-900 dark:text-neutral-100">
                  {user.email}
                </dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-neutral-500 dark:text-neutral-400">Gender</dt>
                <dd className="text-right font-medium capitalize text-neutral-900 dark:text-neutral-100">
                  {user.gender}
                </dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-neutral-500 dark:text-neutral-400">User ID</dt>
                <dd className="font-mono text-right text-neutral-900 dark:text-neutral-100">{user.id}</dd>
              </div>
            </dl>
          </div>
        </article>
      ) : (
        <p className="text-lg text-neutral-500 dark:text-neutral-400">No user profile.</p>
      )}
    </div>
  )
}

export default Home
