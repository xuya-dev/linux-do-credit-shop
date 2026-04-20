import type { ShopSettings } from '#/api/types';

import { requestClient } from '#/api/request';

export const settingsApi = {
  getAll: () => requestClient.get<ShopSettings>('/admin/settings'),
  batchUpdate: (data: ShopSettings) =>
    requestClient.put('/admin/settings', data),
};
