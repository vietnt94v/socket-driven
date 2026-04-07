import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Avatar } from '../../components/Avatar';
import {
  fetchConversationMembers,
  fetchConversations,
  type ConversationDto,
  type MemberDto,
} from '../../apis/conversations';
import { useAuthStore } from '../../stores/authStore';

type Row = {
  conversation: ConversationDto;
  title: string;
  peer?: MemberDto;
};

const MessagesInbox = () => {
  const myId = useAuthStore((s) => s.user?.id);
  const [rows, setRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (uid: string) => {
    setLoading(true);
    setError(null);
    try {
      const list = await fetchConversations();
      const enriched = await Promise.all(
        list.map(async (c): Promise<Row> => {
          if (c.type === 'GROUP') {
            return {
              conversation: c,
              title: c.name || 'Nhóm',
            };
          }
          const members = await fetchConversationMembers(c.id);
          const peer = members.find((m) => m.userId !== uid);
          return {
            conversation: c,
            title: peer?.displayName || peer?.username || 'Chat',
            peer,
          };
        }),
      );
      setRows(enriched);
    } catch {
      setError('Không tải được danh sách.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!myId) {
      setLoading(false);
      setError('Thiếu thông tin người dùng.');
      return;
    }
    void load(myId);
  }, [load, myId]);

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col bg-neutral-50">
      <header className="sticky top-0 z-10 flex items-center justify-between border-b border-neutral-200 bg-white px-4 py-3">
        <h1 className="text-lg font-semibold text-neutral-900">Tin nhắn</h1>
        <Link
          to="/messages/new"
          className="flex h-10 w-10 items-center justify-center rounded-full bg-violet-600 text-white shadow-sm"
          aria-label="Tạo đoạn chat mới"
        >
          <svg
            className="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
            aria-hidden
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
            />
          </svg>
        </Link>
      </header>

      <div className="flex-1 px-2 pb-8 pt-2">
        {loading && (
          <p className="px-3 py-6 text-center text-sm text-neutral-500">
            Đang tải…
          </p>
        )}
        {error && (
          <p className="px-3 py-6 text-center text-sm text-red-600">{error}</p>
        )}
        {!loading && !error && rows.length === 0 && (
          <p className="px-3 py-8 text-center text-sm text-neutral-500">
            Chưa có cuộc trò chuyện. Nhấn nút bút để bắt đầu.
          </p>
        )}
        <ul className="flex flex-col gap-1">
          {rows.map(({ conversation: c, title, peer }) => (
            <li key={c.id}>
              <Link
                to={`/chat/${c.id}`}
                className="flex items-center gap-3 rounded-xl px-3 py-3 transition hover:bg-violet-50"
              >
                {c.type === 'GROUP' ? (
                  <Avatar
                    displayName={c.name || 'N'}
                    username={c.id}
                    image={c.avatarUrl ?? undefined}
                  />
                ) : (
                  <Avatar
                    displayName={peer?.displayName}
                    username={peer?.username ?? ''}
                  />
                )}
                <div className="min-w-0 flex-1">
                  <p className="truncate font-semibold text-neutral-900">
                    {title}
                  </p>
                  {c.lastMessageAt && (
                    <p className="truncate text-xs text-neutral-500">
                      {new Date(c.lastMessageAt).toLocaleString()}
                    </p>
                  )}
                </div>
              </Link>
            </li>
          ))}
        </ul>
      </div>

      <p className="px-4 pb-4 text-center text-xs text-neutral-400">
        Nhấn biểu tượng bút để tạo chat mới
      </p>
    </div>
  );
};

export default MessagesInbox;
