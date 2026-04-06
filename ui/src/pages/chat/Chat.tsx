import { useEffect, useId, useRef, useState } from 'react';
import { closeSocket, createChatSocket } from '../../apis';
import { useAuthStore } from '../../stores/authStore';

type Role = 'self' | 'peer';

type ChatMessage = {
  id: string;
  role: Role;
  text: string;
};

const wsBase =
  import.meta.env.VITE_WS_URL ?? 'ws://localhost:8081/ws/chat';

const Chat = () => {
  const listId = useId();
  const listRef = useRef<HTMLDivElement>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const token = useAuthStore((s) => s.token);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([
    { id: 'seed-1', role: 'peer', text: 'Hi.' },
  ]);

  useEffect(() => {
    const url = token
      ? `${wsBase}?token=${encodeURIComponent(token)}`
      : undefined;
    const ws = createChatSocket(url, {
      onMessage: (data) => {
        setMessages((prev) => [
          ...prev,
          { id: crypto.randomUUID(), role: 'peer', text: data },
        ]);
      },
    });
    wsRef.current = ws;
    return () => {
      closeSocket(wsRef.current);
      wsRef.current = null;
    };
  }, [token]);

  useEffect(() => {
    const el = listRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages]);

  const send = () => {
    const text = input.trim();
    if (!text) return;
    setMessages((prev) => [
      ...prev,
      { id: crypto.randomUUID(), role: 'self', text },
    ]);
    setInput('');
    const ws = wsRef.current;
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(text);
    }
  };

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col gap-3 p-4">
      <div
        ref={listRef}
        id={listId}
        className="flex min-h-0 flex-1 flex-col gap-2 overflow-y-auto rounded-lg border border-neutral-200 p-3 dark:border-neutral-700"
        role="log"
        aria-live="polite"
      >
        {messages.map((m) => (
          <div
            key={m.id}
            className={
              m.role === 'self'
                ? 'self-end max-w-[85%] rounded-lg bg-violet-600 px-3 py-2 text-sm text-white'
                : 'self-start max-w-[85%] rounded-lg bg-neutral-200 px-3 py-2 text-sm text-neutral-900 dark:bg-neutral-700 dark:text-neutral-100'
            }
          >
            {m.text}
          </div>
        ))}
      </div>
      <div className="flex gap-2">
        <input
          className="min-w-0 flex-1 rounded border border-neutral-300 px-3 py-2 dark:border-neutral-600 dark:bg-neutral-900"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              send();
            }
          }}
          placeholder="Message"
          aria-label="Message"
        />
        <button
          type="button"
          className="shrink-0 rounded bg-violet-600 px-4 py-2 font-medium text-white"
          onClick={send}
        >
          Send
        </button>
      </div>
    </div>
  );
};

export default Chat;
