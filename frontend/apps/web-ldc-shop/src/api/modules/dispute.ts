import type {
  Dispute,
  DisputeCreateParams,
  DisputeHandleData,
  DisputeListParams,
  PaginatedResult,
} from '#/api/types';

import { requestClient } from '#/api/request';

export const disputeApi = {
  create: (data: DisputeCreateParams) =>
    requestClient.post<number>('/user/disputes', data),

  userList: (params: DisputeListParams) =>
    requestClient.get<PaginatedResult<Dispute>>('/user/disputes', { params }),

  userDetail: (id: number) =>
    requestClient.get<Dispute>(`/user/disputes/${id}`),

  /** alias for userDetail */
  detail: (id: number) =>
    requestClient.get<Dispute>(`/user/disputes/${id}`),

  adminList: (params: DisputeListParams) =>
    requestClient.get<PaginatedResult<Dispute>>('/admin/disputes', { params }),
  adminDetail: (id: number) =>
    requestClient.get<Dispute>(`/admin/disputes/${id}`),
  adminHandle: (id: number, data: DisputeHandleData) =>
    requestClient.put(`/admin/disputes/${id}/handle`, data),
};
