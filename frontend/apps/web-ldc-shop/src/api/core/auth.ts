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
  return requestClient.post<AuthApi.CallbackResult>('/auth/callback', {
    code,
    redirectUri,
  });
}

export async function getUserInfoApi() {
  return requestClient.get('/auth/userinfo');
}

export async function logoutApi() {
  return baseRequestClient.post('/auth/logout');
}
