import { client } from './client';

export const USER_LIST_MAX = 10;

export type DummyJsonUser = {
  id: number;
  firstName: string;
  lastName: string;
  image: string;
};

type UsersListResponse = {
  users: DummyJsonUser[];
  total: number;
  skip: number;
  limit: number;
};

export async function fetchUsers(
  limit: number = USER_LIST_MAX,
): Promise<DummyJsonUser[]> {
  const capped = Math.min(Math.max(1, limit), USER_LIST_MAX);
  const { data } = await client.get<UsersListResponse>('/users', {
    params: { limit: capped },
  });
  return data.users;
}
