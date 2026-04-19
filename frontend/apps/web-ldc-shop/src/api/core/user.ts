import { requestClient } from '#/api/request';

export async function getUserInfoApi() {
  return requestClient.get('/auth/userinfo');
}
