import { requestClient } from '#/api/request';

export const userApi = {
  adminList: (params: any) => requestClient.get('/admin/users', { params }),
  adminUpdateStatus: (id: number, status: number) =>
    requestClient.put(`/admin/users/${id}/status`, null, { params: { status } }),
  adminUpdateRole: (id: number, role: string) =>
    requestClient.put(`/admin/users/${id}/role`, null, { params: { role } }),
};
