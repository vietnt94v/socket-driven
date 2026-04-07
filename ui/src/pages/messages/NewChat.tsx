import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Avatar } from '../../components/Avatar';
import {
  chatSearch,
  createConversation,
  fetchConversationMembers,
  fetchConversations,
  getDirectConversation,
  type ConversationDto,
} from '../../apis/conversations';
import type { DirectoryUser } from '../../apis/users';
import { useDebouncedValue } from '../../hooks/useDebouncedValue';
import { useAuthStore } from '../../stores/authStore';

type RecentPeer = DirectoryUser;

const NewChat = () => {
  const navigate = useNavigate();
  const myId = useAuthStore((s) => s.user?.id);
  const [query, setQuery] = useState('');
  const debouncedQ = useDebouncedValue(query, 280);
  const [searchUsers, setSearchUsers] = useState<DirectoryUser[]>([]);
  const [searchGroups, setSearchGroups] = useState<
    { id: string; name: string; memberCount: number }[]
  >([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [recent, setRecent] = useState<RecentPeer[]>([]);
  const [recentLoading, setRecentLoading] = useState(true);
  const [busyUserId, setBusyUserId] = useState<string | null>(null);

  const searching = debouncedQ.trim().length > 0;

  const loadRecent = useCallback(async () => {
    if (!myId) return;
    setRecentLoading(true);
    try {
      const list = await fetchConversations();
      const directs = list.filter((c) => c.type === 'DIRECT');
      const peers: RecentPeer[] = [];
      for (const c of directs.slice(0, 8)) {
        const members = await fetchConversationMembers(c.id);
        const peer = members.find((m) => m.userId !== myId);
        if (!peer) continue;
        const dn = peer.displayName?.trim() || '';
        const parts = dn.split(' ', 2);
        peers.push({
          id: peer.userId,
          username: peer.username,
          displayName: dn,
          firstName: parts[0] ?? peer.username,
          lastName: parts.length > 1 ? parts[1] : '',
          image: '',
        });
      }
      const seen = new Set<string>();
      const uniq = peers.filter((p) => {
        if (seen.has(p.id)) return false;
        seen.add(p.id);
        return true;
      });
      setRecent(uniq.slice(0, 5));
    } finally {
      setRecentLoading(false);
    }
  }, [myId]);

  useEffect(() => {
    void loadRecent();
  }, [loadRecent]);

  useEffect(() => {
    if (!debouncedQ.trim()) {
      setSearchUsers([]);
      setSearchGroups([]);
      return;
    }
    let cancelled = false;
    setSearchLoading(true);
    void chatSearch(debouncedQ.trim())
      .then((res) => {
        if (!cancelled) {
          setSearchUsers(res.users);
          setSearchGroups(res.groups);
        }
      })
      .finally(() => {
        if (!cancelled) setSearchLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [debouncedQ]);

  const openOrCreateDirect = async (userId: string) => {
    setBusyUserId(userId);
    try {
      const existing = await getDirectConversation(userId);
      if (existing) {
        navigate(`/chat/${existing.id}`, { replace: true });
        return;
      }
      const created = await createConversation({
        type: 'DIRECT',
        otherUserId: userId,
      });
      navigate(`/chat/${created.id}`, { replace: true });
    } finally {
      setBusyUserId(null);
    }
  };

  const openGroup = (c: ConversationDto | { id: string }) => {
    navigate(`/chat/${c.id}`, { replace: true });
  };

  const showResults = searching;

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col bg-neutral-50">
      <header className="sticky top-0 z-10 border-b border-neutral-200 bg-white px-3 py-3">
        <div className="mb-3 flex items-center gap-2">
          <Link
            to="/messages"
            className="rounded-lg px-2 py-1 text-sm font-medium text-violet-700"
          >
            ←
          </Link>
          <h1 className="flex-1 text-center text-base font-semibold text-neutral-900">
            Tạo đoạn chat mới
          </h1>
          <span className="w-8" />
        </div>
        <div
          className={`flex items-center gap-2 rounded-full border bg-white px-3 py-2 ${
            query.trim()
              ? 'border-violet-500 ring-1 ring-violet-200'
              : 'border-neutral-200'
          }`}
        >
          <svg
            className="h-5 w-5 shrink-0 text-neutral-400"
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
            className="min-w-0 flex-1 bg-transparent text-sm text-neutral-900 outline-none placeholder:text-neutral-400"
            placeholder="Tìm tên hoặc số điện thoại…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            aria-label="Tìm kiếm"
          />
        </div>
      </header>

      <div className="flex-1 px-3 pb-10 pt-4">
        {!showResults && (
          <>
            <Link
              to="/messages/group"
              className="mb-6 flex items-center gap-3 rounded-2xl border border-violet-100 bg-violet-50 px-4 py-3 transition hover:bg-violet-100"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-lg font-semibold text-emerald-700">
                +
              </span>
              <div>
                <p className="font-semibold text-neutral-900">Tạo nhóm mới</p>
                <p className="text-xs text-neutral-500">
                  Thêm nhiều người cùng lúc
                </p>
              </div>
            </Link>

            <p className="mb-2 text-xs font-semibold tracking-wide text-neutral-400">
              GẦN ĐÂY
            </p>
            {recentLoading && (
              <p className="text-sm text-neutral-500">Đang tải…</p>
            )}
            {!recentLoading && recent.length === 0 && (
              <p className="text-sm text-neutral-500">
                Chưa có liên hệ gần đây.
              </p>
            )}
            <ul className="flex flex-col gap-1">
              {recent.map((u) => (
                <li key={u.id}>
                  <button
                    type="button"
                    disabled={busyUserId === u.id}
                    onClick={() => void openOrCreateDirect(u.id)}
                    className="flex w-full items-center gap-3 rounded-xl px-2 py-2 text-left transition hover:bg-white"
                  >
                    <Avatar
                      image={u.image || undefined}
                      firstName={u.firstName}
                      lastName={u.lastName}
                      displayName={u.displayName}
                      username={u.username}
                    />
                    <span className="font-medium text-neutral-900">
                      {u.displayName || u.username}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
            <p className="mt-6 text-center text-xs text-neutral-400">
              Gợi ý: tùy chọn &quot;Tạo nhóm&quot; luôn nằm đầu
            </p>
          </>
        )}

        {showResults && (
          <div
            className="flex flex-col gap-6"
            role="status"
            aria-live="polite"
          >
            {searchLoading && (
              <p className="text-sm text-neutral-500">Đang tìm…</p>
            )}
            {!searchLoading && searchUsers.length === 0 && searchGroups.length === 0 && (
              <p className="text-sm text-neutral-500">Không có kết quả.</p>
            )}

            {searchUsers.length > 0 && (
              <section>
                <p className="mb-2 text-xs font-semibold tracking-wide text-neutral-400">
                  NGƯỜI DÙNG
                </p>
                <ul className="flex flex-col gap-1">
                  {searchUsers.map((u) => (
                    <li key={u.id}>
                      <button
                        type="button"
                        disabled={busyUserId === u.id}
                        onClick={() => void openOrCreateDirect(u.id)}
                        className="flex w-full items-center gap-3 rounded-xl px-2 py-2 text-left transition hover:bg-white"
                      >
                        <Avatar
                          image={u.image || undefined}
                          firstName={u.firstName}
                          lastName={u.lastName}
                          displayName={u.displayName}
                          username={u.username}
                        />
                        <div>
                          <p className="font-semibold text-neutral-900">
                            {u.displayName || u.username}
                          </p>
                          <p className="text-xs text-neutral-500">
                            Chat 1-1 ngay
                          </p>
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              </section>
            )}

            {searchGroups.length > 0 && (
              <section>
                <p className="mb-2 text-xs font-semibold tracking-wide text-neutral-400">
                  NHÓM
                </p>
                <ul className="flex flex-col gap-1">
                  {searchGroups.map((g) => (
                    <li key={g.id}>
                      <button
                        type="button"
                        onClick={() => openGroup({ id: g.id })}
                        className="flex w-full items-center gap-3 rounded-xl px-2 py-2 text-left transition hover:bg-white"
                      >
                        <Avatar
                          displayName={g.name}
                          username={g.id}
                        />
                        <div>
                          <p className="font-semibold text-neutral-900">
                            {g.name}
                          </p>
                          <p className="text-xs text-neutral-500">
                            {g.memberCount} thành viên
                          </p>
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              </section>
            )}

            <p className="text-center text-xs text-neutral-400">
              Kết quả tách rõ người dùng và nhóm
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default NewChat;
