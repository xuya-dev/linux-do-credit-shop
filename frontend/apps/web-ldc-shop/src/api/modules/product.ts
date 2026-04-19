import { requestClient } from '#/api/request';

export const productApi = {
  userList: (params: any) => requestClient.get('/user/products', { params }),
  detail: (id: number) => requestClient.get(`/user/products/${id}`),
  adminList: (params: any) => requestClient.get('/admin/products', { params }),
  adminCreate: (data: any) => requestClient.post('/admin/products', data),
  adminUpdate: (id: number, data: any) =>
    requestClient.put(`/admin/products/${id}`, data),
  adminDelete: (id: number) => requestClient.delete(`/admin/products/${id}`),
  adminUpdateStatus: (id: number, status: number) =>
    requestClient.put(`/admin/products/${id}/status`, null, {
      params: { status },
    }),
};
