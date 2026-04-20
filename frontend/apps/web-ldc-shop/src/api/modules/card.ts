import type { PaginatedResult, ProductCard } from '#/api/types';

import { requestClient } from '#/api/request';

export const cardApi = {
  adminList: (params: any) =>
    requestClient.get<PaginatedResult<ProductCard>>('/admin/cards', { params }),
  adminImport: (data: any) =>
    requestClient.post<number>('/admin/cards/import', data),
  adminDelete: (id: number) =>
    requestClient.delete(`/admin/cards/${id}`),
  adminAvailableCount: (productId: number) =>
    requestClient.get<number>('/admin/cards/available-count', {
      params: { productId },
    }),
};
