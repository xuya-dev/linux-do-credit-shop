import { requestClient } from '#/api/request';

export const categoryApi = {
  userList: () => requestClient.get('/user/categories'),
  adminList: (params: any) =>
    requestClient.get('/admin/categories', { params }),
  adminCreate: (data: any) => requestClient.post('/admin/categories', data),
  adminUpdate: (id: number, data: any) =>
    requestClient.put(`/admin/categories/${id}`, data),
  adminDelete: (id: number) =>
    requestClient.delete(`/admin/categories/${id}`),
};
