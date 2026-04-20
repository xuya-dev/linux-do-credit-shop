import type {
  Order,
  OrderCreateParams,
  PaginatedResult,
  PaymentSubmitResult,
} from '#/api/types';

import { requestClient } from '#/api/request';

export const orderApi = {
  create: (data: OrderCreateParams) =>
    requestClient.post<Order>('/user/orders', data),

  userList: (params: {
    page?: number;
    size?: number;
    paymentStatus?: number;
    deliveryStatus?: number;
  }) =>
    requestClient.get<PaginatedResult<Order>>('/user/orders', { params }),

  detail: (id: number) => requestClient.get<Order>(`/user/orders/${id}`),

  pay: (id: number) =>
    requestClient.post<PaymentSubmitResult>(`/user/orders/${id}/pay`),

  adminList: (params: any) => requestClient.get('/admin/orders', { params }),
  adminDetail: (id: number) => requestClient.get(`/admin/orders/${id}`),
  adminDeliver: (id: number, data: any) =>
    requestClient.put(`/admin/orders/${id}/deliver`, data),
  adminRefund: (id: number) =>
    requestClient.put(`/admin/orders/${id}/refund`),
};
