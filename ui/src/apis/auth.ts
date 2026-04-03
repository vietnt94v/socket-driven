import type { AuthUser } from '../stores/authStore';
import { client } from './client';

export type LoginResponse = AuthUser & {
  accessToken: string;
  refreshToken: string;
};

export async function login(username: string, password: string) {
  const { data } = await client.post<LoginResponse>('/auth/login', {
    username,
    password,
  });
  const { accessToken, refreshToken, ...user } = data;
  void refreshToken;
  return { accessToken, user };
}
