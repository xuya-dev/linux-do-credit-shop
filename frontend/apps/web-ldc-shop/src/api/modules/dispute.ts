import { requestClient } from '#/api/request';

export const disputeApi = {
  create: (data: any) => requestClient.post('/user/disputes', data),
  userList: (params: any) => requestClient.get('/user/disputes', { params }),
  userDetail: (id: number) => requestClient.get(`/user/disputes/${id}`),
  adminList: (params: any) =>
    requestClient.get('/admin/disputes', { params }),
  adminDetail: (id: number) =>
    requestClient.get(`/admin/disputes/${id}`),
  adminHandle: (id: number, data: any) =>
    requestClient.put(`/admin/disputes/${id}/handle`, data),
};
