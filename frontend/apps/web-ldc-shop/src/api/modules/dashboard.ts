import { requestClient } from '#/api/request';

export const dashboardApi = {
  getData: () => requestClient.get('/admin/dashboard'),
};
