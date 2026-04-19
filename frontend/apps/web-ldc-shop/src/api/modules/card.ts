import { requestClient } from '#/api/request';

export const cardApi = {
  adminList: (params: any) => requestClient.get('/admin/cards', { params }),
  adminImport: (data: any) => requestClient.post('/admin/cards/import', data),
  adminDelete: (id: number) => requestClient.delete(`/admin/cards/${id}`),
  adminAvailableCount: (productId: number) =>
    requestClient.get('/admin/cards/available-count', {
      params: { productId },
    }),
};
