import { useAuthStore } from '../../stores/authStore';
import HomeUserListSection from './HomeUserListSection';

const Home = () => {
  const user = useAuthStore((s) => s.user);

  return (
    <div className="min-h-screen">
      <div className="p-6 lg:pr-80">
        {user ? (
          <h1 className="text-2xl font-semibold text-neutral-900 dark:text-neutral-100">
            Hello {user.firstName} {user.lastName}!
          </h1>
        ) : (
          <p className="text-lg text-neutral-500 dark:text-neutral-400">
            No user profile.
          </p>
        )}
      </div>

      <HomeUserListSection
        excludeUserId={user?.id}
        className="mx-4 mb-4 h-[min(50vh,28rem)] rounded-xl lg:fixed lg:inset-y-0 lg:right-0 lg:mx-0 lg:mb-0 lg:h-screen lg:w-80 lg:max-w-none lg:rounded-none lg:border-l lg:border-neutral-800"
      />
    </div>
  );
};

export default Home;
