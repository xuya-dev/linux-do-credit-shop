import { requestClient } from '#/api/request';

export const announcementApi = {
  userList: (params: any) =>
    requestClient.get('/user/announcements', { params }),
  userDetail: (id: number) =>
    requestClient.get(`/user/announcements/${id}`),
  latest: (limit?: number) =>
    requestClient.get('/user/announcements/latest', { params: { limit } }),
  adminList: (params: any) =>
    requestClient.get('/admin/announcements', { params }),
  adminDetail: (id: number) =>
    requestClient.get(`/admin/announcements/${id}`),
  adminCreate: (data: any) =>
    requestClient.post('/admin/announcements', data),
  adminUpdate: (id: number, data: any) =>
    requestClient.put(`/admin/announcements/${id}`, data),
  adminDelete: (id: number) =>
    requestClient.delete(`/admin/announcements/${id}`),
  adminPublish: (id: number) =>
    requestClient.put(`/admin/announcements/${id}/publish`),
  adminUnpublish: (id: number) =>
    requestClient.put(`/admin/announcements/${id}/unpublish`),
  adminToggleTop: (id: number) =>
    requestClient.put(`/admin/announcements/${id}/top`),
};
