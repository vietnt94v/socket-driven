import type { AuthUser } from '../stores/authStore';
import { client } from './client';

export type LoginResponse = AuthUser & {
  accessToken: string;
  refreshToken: string;
};

export async function login(username: string, password: string) {
  const { data } = await client.post<LoginResponse>('/api/auth/login', {
    username,
    password,
  });
  const { accessToken, refreshToken, ...user } = data;
  void refreshToken;
  return { accessToken, user };
}

export type AuthTokenPair = {
  accessToken: string;
  refreshToken: string;
};

export async function refreshTokens(refreshToken: string) {
  const { data } = await client.post<AuthTokenPair>('/api/auth/refresh', {
    refreshToken,
  });
  return data;
}

export async function logout(refreshToken?: string | null) {
  await client.post('/api/auth/logout', {
    refreshToken: refreshToken ?? undefined,
  });
}
