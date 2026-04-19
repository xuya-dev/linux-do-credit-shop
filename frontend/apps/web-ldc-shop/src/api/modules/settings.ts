import { requestClient } from '#/api/request';

export const settingsApi = {
  getAll: () => requestClient.get('/admin/settings'),
  batchUpdate: (data: any) => requestClient.put('/admin/settings', data),
};
