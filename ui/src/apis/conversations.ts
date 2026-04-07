import axios from 'axios';
import { client } from './client';
import type { DirectoryUser } from './users';

export type ConversationDto = {
  id: string;
  type: string;
  name: string | null;
  avatarUrl: string | null;
  lastMessageAt: string | null;
};

export type GroupSearchHit = {
  id: string;
  name: string;
  avatarUrl: string;
  memberCount: number;
};

export type ChatSearchResponse = {
  users: DirectoryUser[];
  groups: GroupSearchHit[];
};

export type MemberDto = {
  userId: string;
  username: string;
  displayName: string;
  role: string;
  joinedAt: string;
};

export type MessageDto = {
  id: string;
  conversationId: string;
  senderId: string;
  type: string;
  content: string;
  replyToId: string | null;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CreateConversationBody =
  | { type: 'DIRECT'; otherUserId: string }
  | { type: 'GROUP'; name: string; memberIds: string[] };

export async function fetchConversations(): Promise<ConversationDto[]> {
  const { data } = await client.get<ConversationDto[]>('/api/conversations');
  return data;
}

export async function getDirectConversation(
  userId: string,
): Promise<ConversationDto | null> {
  try {
    const { data } = await client.get<ConversationDto>(
      '/api/conversations/direct',
      { params: { userId } },
    );
    return data;
  } catch (e) {
    if (axios.isAxiosError(e) && e.response?.status === 404) {
      return null;
    }
    throw e;
  }
}

export async function createConversation(
  body: CreateConversationBody,
): Promise<ConversationDto> {
  const { data } = await client.post<ConversationDto>(
    '/api/conversations',
    body,
  );
  return data;
}

export async function chatSearch(q: string): Promise<ChatSearchResponse> {
  const { data } = await client.get<ChatSearchResponse>('/api/chat/search', {
    params: { q },
  });
  return data;
}

export async function fetchConversationMembers(
  conversationId: string,
): Promise<MemberDto[]> {
  const { data } = await client.get<MemberDto[]>(
    `/api/conversations/${conversationId}/members`,
  );
  return data;
}

export async function fetchConversation(
  conversationId: string,
): Promise<ConversationDto> {
  const { data } = await client.get<ConversationDto>(
    `/api/conversations/${conversationId}`,
  );
  return data;
}

export async function fetchMessages(
  conversationId: string,
  page = 0,
  size = 50,
): Promise<{
  content: MessageDto[];
  totalElements: number;
  totalPages: number;
}> {
  const { data } = await client.get<{
    content: MessageDto[];
    totalElements: number;
    totalPages: number;
  }>(`/api/conversations/${conversationId}/messages`, {
    params: { page, size, sort: 'createdAt,desc' },
  });
  return data;
}

export async function sendMessage(
  conversationId: string,
  content: string,
): Promise<MessageDto> {
  const { data } = await client.post<MessageDto>(
    `/api/conversations/${conversationId}/messages`,
    { content, replyToId: null },
  );
  return data;
}
