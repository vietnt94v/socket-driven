import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Avatar } from '../../components/Avatar';
import { fetchAllUsers } from '../../apis/users';
import type { DirectoryUser } from '../../apis/users';
import { useAuthStore } from '../../stores/authStore';

const CreateGroup = () => {
  const navigate = useNavigate();
  const myId = useAuthStore((s) => s.user?.id);
  const [users, setUsers] = useState<DirectoryUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [q, setQ] = useState('');
  const [selected, setSelected] = useState<Record<string, DirectoryUser>>({});

  useEffect(() => {
    void fetchAllUsers()
      .then((list) => {
        setUsers(myId ? list.filter((u) => u.id !== myId) : list);
      })
      .finally(() => setLoading(false));
  }, [myId]);

  const filtered = useMemo(() => {
    const t = q.trim().toLowerCase();
    if (!t) return users;
    return users.filter(
      (u) =>
        u.username.toLowerCase().includes(t) ||
        u.displayName.toLowerCase().includes(t),
    );
  }, [users, q]);

  const toggle = useCallback((u: DirectoryUser) => {
    setSelected((prev) => {
      const next = { ...prev };
      if (next[u.id]) delete next[u.id];
      else next[u.id] = u;
      return next;
    });
  }, []);

  const selectedList = useMemo(
    () => Object.values(selected),
    [selected],
  );

  const next = () => {
    const memberIds = selectedList.map((u) => u.id);
    if (memberIds.length < 2) return;
    navigate('/messages/group/name', { state: { memberIds } });
  };

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col bg-[#faf8f5]">
      <header className="border-b border-amber-100/80 bg-[#f5f0e8] px-3 py-3">
        <div className="flex items-center justify-between">
          <Link
            to="/messages/new"
            className="text-sm font-medium text-violet-800"
          >
            ←
          </Link>
          <h1 className="text-base font-semibold text-neutral-900">
            Tạo nhóm mới
          </h1>
          <span className="w-8" />
        </div>
      </header>

      <div className="flex flex-wrap gap-2 border-b border-neutral-200/80 bg-[#faf8f5] px-3 py-3">
        {selectedList.map((u) => (
          <span
            key={u.id}
            className="inline-flex items-center gap-1.5 rounded-full bg-violet-100 px-2 py-1 text-sm text-violet-900"
          >
            <Avatar
              size="sm"
              image={u.image || undefined}
              firstName={u.firstName}
              lastName={u.lastName}
              displayName={u.displayName}
              username={u.username}
            />
            <span className="max-w-[10rem] truncate">
              {u.displayName || u.username}
            </span>
            <button
              type="button"
              className="ml-1 text-violet-600"
              onClick={() => toggle(u)}
              aria-label="Bỏ chọn"
            >
              ×
            </button>
          </span>
        ))}
      </div>

      <div className="px-3 pt-3">
        <div className="flex items-center gap-2 rounded-full border border-neutral-200 bg-[#f5f0e8] px-3 py-2">
          <svg
            className="h-5 w-5 text-neutral-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <input
            className="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-neutral-400"
            placeholder="Thêm người…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-1 pb-28 pt-2">
        {loading && (
          <p className="px-3 py-4 text-sm text-neutral-500">Đang tải…</p>
        )}
        <ul className="flex flex-col">
          {filtered.map((u) => {
            const isOn = !!selected[u.id];
            return (
              <li key={u.id}>
                <button
                  type="button"
                  onClick={() => toggle(u)}
                  className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left ${
                    isOn
                      ? 'border border-violet-400 bg-violet-50'
                      : 'border border-transparent'
                  }`}
                >
                  <Avatar
                    image={u.image || undefined}
                    firstName={u.firstName}
                    lastName={u.lastName}
                    displayName={u.displayName}
                    username={u.username}
                  />
                  <span className="flex-1 font-medium text-neutral-900">
                    {u.displayName || u.username}
                  </span>
                  {isOn && (
                    <span className="flex h-8 w-8 items-center justify-center rounded-full bg-violet-600 text-white">
                      ✓
                    </span>
                  )}
                </button>
              </li>
            );
          })}
        </ul>
      </div>

      <div className="sticky bottom-0 border-t border-neutral-200 bg-[#faf8f5] px-4 pb-6 pt-3">
        <button
          type="button"
          disabled={selectedList.length < 2}
          onClick={next}
          className="w-full rounded-full bg-violet-600 py-3.5 text-center text-base font-semibold text-white disabled:opacity-40"
        >
          Tiếp theo ({selectedList.length})
        </button>
        <p className="mt-2 text-center text-xs text-neutral-400">
          Chọn nhiều người, thấy ngay số lượng
        </p>
      </div>
    </div>
  );
};

export default CreateGroup;
