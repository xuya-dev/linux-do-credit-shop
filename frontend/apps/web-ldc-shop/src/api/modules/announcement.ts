import type { Announcement, AnnouncementFormData, AnnouncementListParams, PaginatedResult } from '#/api/types';

import { requestClient } from '#/api/request';

export const announcementApi = {
  userList: (params: AnnouncementListParams) =>
    requestClient.get<PaginatedResult<Announcement>>('/user/announcements', {
      params,
    }),

  userDetail: (id: number) =>
    requestClient.get<Announcement>(`/user/announcements/${id}`),

  latest: (limit?: number) =>
    requestClient.get<Announcement[]>('/user/announcements/latest', {
      params: { limit },
    }),

  adminList: (params: AnnouncementListParams) =>
    requestClient.get<PaginatedResult<Announcement>>('/admin/announcements', { params }),
  adminDetail: (id: number) =>
    requestClient.get<Announcement>(`/admin/announcements/${id}`),
  adminCreate: (data: AnnouncementFormData) =>
    requestClient.post<Announcement>('/admin/announcements', data),
  adminUpdate: (id: number, data: AnnouncementFormData) =>
    requestClient.put<Announcement>(`/admin/announcements/${id}`, data),
  adminDelete: (id: number) =>
    requestClient.delete(`/admin/announcements/${id}`),
  adminPublish: (id: number) =>
    requestClient.put(`/admin/announcements/${id}/publish`),
  adminUnpublish: (id: number) =>
    requestClient.put(`/admin/announcements/${id}/unpublish`),
  adminToggleTop: (id: number) =>
    requestClient.put(`/admin/announcements/${id}/top`),
};
