import { client } from './client';

export const USER_LIST_MAX = 10;

export type DirectoryUser = {
  id: string;
  username: string;
  displayName: string;
  firstName: string;
  lastName: string;
  image: string;
};

export async function fetchUsers(
  limit: number = USER_LIST_MAX,
): Promise<DirectoryUser[]> {
  const capped = Math.min(Math.max(1, limit), USER_LIST_MAX);
  const { data } = await client.get<DirectoryUser[]>('/api/users');
  return data.slice(0, capped);
}

export async function fetchAllUsers(): Promise<DirectoryUser[]> {
  const { data } = await client.get<DirectoryUser[]>('/api/users');
  return data;
}
