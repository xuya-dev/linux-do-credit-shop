import type { LoginResult, UserInfo } from '#/api/types';

import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  export interface CallbackResult {
    isNewUser: boolean;
    token: string;
    user: any;
  }
}

export async function getAuthorizeUrlApi(redirectUri: string) {
  return requestClient.get<string>('/auth/authorize-url', {
    params: { redirectUri },
  });
}

export async function callbackApi(code: string, redirectUri: string) {
  return requestClient.post<LoginResult>('/auth/callback', {
    code,
    redirectUri,
  });
}

export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/auth/userinfo');
}

export async function logoutApi() {
  return baseRequestClient.post('/auth/logout');
}
