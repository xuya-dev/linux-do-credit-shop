import type { Category, CategoryFormData, CategoryListParams, PaginatedResult } from '#/api/types';

import { requestClient } from '#/api/request';

export const categoryApi = {
  userList: () => requestClient.get<Category[]>('/user/categories'),

  adminList: (params: CategoryListParams) =>
    requestClient.get<PaginatedResult<Category>>('/admin/categories', { params }),
  adminCreate: (data: CategoryFormData) => requestClient.post<Category>('/admin/categories', data),
  adminUpdate: (id: number, data: CategoryFormData) =>
    requestClient.put<Category>(`/admin/categories/${id}`, data),
  adminDelete: (id: number) =>
    requestClient.delete(`/admin/categories/${id}`),
};
