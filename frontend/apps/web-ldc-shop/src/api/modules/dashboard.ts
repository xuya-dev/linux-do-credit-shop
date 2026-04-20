import type { DashboardResult } from '#/api/types';

import { requestClient } from '#/api/request';

export const dashboardApi = {
  getData: () => requestClient.get<DashboardResult>('/admin/dashboard'),
};
