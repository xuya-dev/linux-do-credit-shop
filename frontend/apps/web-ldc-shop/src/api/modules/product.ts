import type { PaginatedResult, Product, ProductAdminListParams, ProductFormData } from '#/api/types';

import { requestClient } from '#/api/request';

export const productApi = {
  userList: (params: {
    page?: number;
    size?: number;
    categoryId?: number;
    keyword?: string;
    productType?: number;
  }) => requestClient.get<PaginatedResult<Product>>('/user/products', { params }),

  detail: (id: number) => requestClient.get<Product>(`/user/products/${id}`),

  adminList: (params: ProductAdminListParams) => requestClient.get<PaginatedResult<Product>>('/admin/products', { params }),
  adminCreate: (data: ProductFormData) => requestClient.post<Product>('/admin/products', data),
  adminUpdate: (id: number, data: ProductFormData) =>
    requestClient.put<Product>(`/admin/products/${id}`, data),
  adminDelete: (id: number) => requestClient.delete(`/admin/products/${id}`),
  adminUpdateStatus: (id: number, status: number) =>
    requestClient.put(`/admin/products/${id}/status`, null, {
      params: { status },
    }),
};
