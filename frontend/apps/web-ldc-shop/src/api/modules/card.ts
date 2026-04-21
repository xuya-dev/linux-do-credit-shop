import type { CardImportParams, CardListParams, PaginatedResult, ProductCard } from '#/api/types';

import { requestClient } from '#/api/request';

export const cardApi = {
  adminList: (params: CardListParams) =>
    requestClient.get<PaginatedResult<ProductCard>>('/admin/cards', { params }),
  adminImport: (data: CardImportParams) =>
    requestClient.post<number>('/admin/cards/import', data),
  adminDelete: (id: number) =>
    requestClient.delete(`/admin/cards/${id}`),
  adminAvailableCount: (productId: number) =>
    requestClient.get<number>('/admin/cards/available-count', {
      params: { productId },
    }),
};
