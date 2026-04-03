export function createChatSocket(
  url: string | undefined,
  handlers: {
    onMessage?: (data: string) => void;
    onOpen?: () => void;
    onClose?: () => void;
    onError?: (ev: Event) => void;
  } = {},
): WebSocket | null {
  if (!url) return null;
  const ws = new WebSocket(url);
  ws.onmessage = (ev) => handlers.onMessage?.(String(ev.data));
  ws.onopen = () => handlers.onOpen?.();
  ws.onclose = () => handlers.onClose?.();
  ws.onerror = (ev) => handlers.onError?.(ev);
  return ws;
}

export function closeSocket(ws: WebSocket | null) {
  if (ws && ws.readyState === WebSocket.OPEN) ws.close();
}
