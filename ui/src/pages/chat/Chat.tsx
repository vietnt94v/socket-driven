import { useCallback, useEffect, useId, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  fetchConversation,
  fetchConversationMembers,
  fetchMessages,
  sendMessage,
  type MessageDto,
} from '../../apis/conversations';
import { useAuthStore } from '../../stores/authStore';

const Chat = () => {
  const { conversationId } = useParams<{ conversationId: string }>();
  const listId = useId();
  const listRef = useRef<HTMLDivElement>(null);
  const myId = useAuthStore((s) => s.user?.id);
  const [title, setTitle] = useState('Chat');
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<MessageDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!conversationId) return;
    setLoading(true);
    setError(null);
    try {
      const conv = await fetchConversation(conversationId);
      if (conv.type === 'GROUP') {
        setTitle(conv.name || 'Nhóm');
      } else if (myId) {
        const members = await fetchConversationMembers(conversationId);
        const peer = members.find((m) => m.userId !== myId);
        setTitle(peer?.displayName || peer?.username || 'Chat');
      } else {
        setTitle('Chat');
      }
      const page = await fetchMessages(conversationId, 0, 100);
      setMessages([...page.content].reverse());
    } catch {
      setError('Không tải được cuộc trò chuyện.');
    } finally {
      setLoading(false);
    }
  }, [conversationId, myId]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    const el = listRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages]);

  const submit = async () => {
    const text = input.trim();
    if (!text || !conversationId) return;
    setInput('');
    try {
      const msg = await sendMessage(conversationId, text);
      setMessages((prev) => [...prev, msg]);
    } catch {
      setInput(text);
    }
  };

  if (!conversationId) {
    return (
      <p className="p-6 text-center text-neutral-500">Thiếu mã hội thoại.</p>
    );
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col bg-neutral-50">
      <header className="sticky top-0 z-10 flex items-center gap-2 border-b border-neutral-200 bg-white px-3 py-3">
        <Link
          to="/messages"
          className="rounded-lg px-2 py-1 text-sm font-medium text-violet-700"
        >
          ←
        </Link>
        <h1 className="min-w-0 flex-1 truncate text-center text-base font-semibold text-neutral-900">
          {title}
        </h1>
        <span className="w-8" />
      </header>

      {loading && (
        <p className="px-4 py-6 text-center text-sm text-neutral-500">
          Đang tải…
        </p>
      )}
      {error && (
        <p className="px-4 py-6 text-center text-sm text-red-600">{error}</p>
      )}

      <div
        ref={listRef}
        id={listId}
        className="flex min-h-0 flex-1 flex-col gap-2 overflow-y-auto px-3 py-3"
        role="log"
        aria-live="polite"
      >
        {!loading &&
          messages.map((m) => {
            const self = myId && m.senderId === myId;
            return (
              <div
                key={m.id}
                className={
                  self
                    ? 'self-end max-w-[85%] rounded-lg bg-violet-600 px-3 py-2 text-sm text-white'
                    : 'self-start max-w-[85%] rounded-lg bg-neutral-200 px-3 py-2 text-sm text-neutral-900'
                }
              >
                {m.content}
              </div>
            );
          })}
      </div>

      <div className="border-t border-neutral-200 bg-white p-3">
        <div className="flex gap-2">
          <input
            className="min-w-0 flex-1 rounded-full border border-neutral-300 px-4 py-2.5 text-sm outline-none focus:border-violet-500"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                void submit();
              }
            }}
            placeholder="Nhắn tin…"
            aria-label="Nhắn tin"
          />
          <button
            type="button"
            className="shrink-0 rounded-full bg-violet-600 px-5 py-2.5 text-sm font-medium text-white"
            onClick={() => void submit()}
          >
            Gửi
          </button>
        </div>
      </div>
    </div>
  );
};

export default Chat;
