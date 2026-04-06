import { useEffect, useState } from 'react';
import { fetchUsers, type DirectoryUser } from '../../apis';

const storyRing = (id: string) =>
  id.split('').reduce((a, c) => a + c.charCodeAt(0), 0) % 5 === 0;

const EditFabIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    width="22"
    height="22"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden
  >
    <path d="M12 20h9" />
    <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z" />
  </svg>
);

type HomeUserListSectionProps = {
  excludeUserId?: string;
  className?: string;
};

const HomeUserListSection = ({
  excludeUserId,
  className = '',
}: HomeUserListSectionProps) => {
  const [users, setUsers] = useState<DirectoryUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchUsers()
      .then((list) => {
        if (!cancelled) setUsers(list);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load users.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const visibleUsers = excludeUserId
    ? users.filter((u) => u.id !== excludeUserId)
    : users;

  return (
    <section
      className={`relative flex min-h-0 flex-col overflow-hidden bg-[#242526] ${className}`}
      aria-label="Users"
    >
      <div className="min-h-0 flex-1 overflow-y-auto py-2">
        {loading ? (
          <p className="px-4 py-6 text-center text-sm text-neutral-400">
            Loading users…
          </p>
        ) : error ? (
          <p className="px-4 py-6 text-center text-sm text-red-400">{error}</p>
        ) : visibleUsers.length === 0 ? (
          <p className="px-4 py-6 text-center text-sm text-neutral-400">
            No users to show.
          </p>
        ) : (
          <ul className="flex flex-col">
            {visibleUsers.map((u) => {
              const avatar = (
                <div className="relative h-12 w-12 shrink-0">
                  <img
                    src={u.image}
                    alt=""
                    className="h-12 w-12 rounded-full object-cover"
                  />
                  <span
                    className="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-[#242526] bg-emerald-500"
                    aria-hidden
                  />
                </div>
              );
              return (
                <li key={u.id}>
                  <div className="flex items-center gap-3 px-3 py-2.5 hover:bg-white/5">
                    {storyRing(u.id) ? (
                      <div className="shrink-0 rounded-full bg-gradient-to-tr from-sky-500 to-blue-600 p-0.5">
                        <div className="rounded-full bg-[#242526] p-0.5">
                          {avatar}
                        </div>
                      </div>
                    ) : (
                      avatar
                    )}
                    <span className="font-semibold text-white">
                      {u.firstName} {u.lastName}
                    </span>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>
      <button
        type="button"
        className="absolute bottom-4 right-4 flex h-12 w-12 items-center justify-center rounded-full bg-[#3a3b3c] text-white shadow-lg hover:bg-[#4e4f50]"
        aria-label="Edit"
      >
        <EditFabIcon />
      </button>
    </section>
  );
};

export default HomeUserListSection;
