import type {
  Dispute,
  DisputeCreateParams,
  PaginatedResult,
} from '#/api/types';

import { requestClient } from '#/api/request';

export const disputeApi = {
  create: (data: DisputeCreateParams) =>
    requestClient.post<number>('/user/disputes', data),

  userList: (params: {
    page?: number;
    size?: number;
    status?: number;
  }) =>
    requestClient.get<PaginatedResult<Dispute>>('/user/disputes', { params }),

  userDetail: (id: number) =>
    requestClient.get<Dispute>(`/user/disputes/${id}`),

  adminList: (params: any) =>
    requestClient.get('/admin/disputes', { params }),
  adminDetail: (id: number) =>
    requestClient.get(`/admin/disputes/${id}`),
  adminHandle: (id: number, data: any) =>
    requestClient.put(`/admin/disputes/${id}/handle`, data),
};
