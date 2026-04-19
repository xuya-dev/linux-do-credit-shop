import { requestClient } from '#/api/request';

export const orderApi = {
  create: (data: any) => requestClient.post('/user/orders', data),
  userList: (params: any) => requestClient.get('/user/orders', { params }),
  detail: (id: number) => requestClient.get(`/user/orders/${id}`),
  pay: (id: number) => requestClient.post(`/user/orders/${id}/pay`),
  adminList: (params: any) => requestClient.get('/admin/orders', { params }),
  adminDetail: (id: number) => requestClient.get(`/admin/orders/${id}`),
  adminDeliver: (id: number, data: any) =>
    requestClient.put(`/admin/orders/${id}/deliver`, data),
  adminRefund: (id: number) =>
    requestClient.put(`/admin/orders/${id}/refund`),
};
