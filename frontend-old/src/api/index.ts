import { request } from '@/utils/http'
import type { PageResult } from '@/utils/http'

/**
 * 认证 API / Auth API
 */
export const authApi = {
  getAuthorizeUrl: (redirectUri?: string) =>
    request.get<string>('/auth/authorize-url', { redirectUri }),

  callback: (code: string, redirectUri?: string) =>
    request.post('/auth/callback', { code, redirectUri }),

  getUserInfo: () => request.get('/auth/userinfo'),

  logout: () => request.post('/auth/logout'),
}

/**
 * 商品 API / Product API
 */
export const productApi = {
  userList: (params: { page: number; size: number; categoryId?: number; keyword?: string; productType?: number }) =>
    request.get<PageResult<any>>('/user/products', params),

  detail: (id: number) => request.get('/user/products/' + id),

  adminList: (params: { page: number; size: number; categoryId?: number; keyword?: string; status?: number }) =>
    request.get<PageResult<any>>('/admin/products', params),

  adminCreate: (data: any) => request.post('/admin/products', data),

  adminUpdate: (id: number, data: any) => request.put('/admin/products/' + id, data),

  adminDelete: (id: number) => request.delete('/admin/products/' + id),

  adminUpdateStatus: (id: number, status: number) =>
    request.put('/admin/products/' + id + '/status', null, { params: { status } } as any),
}

/**
 * 分类 API / Category API
 */
export const categoryApi = {
  userList: () => request.get('/user/categories'),

  adminList: (params: { page: number; size: number }) =>
    request.get<PageResult<any>>('/admin/categories', params),

  adminCreate: (data: any) => request.post('/admin/categories', data),

  adminUpdate: (id: number, data: any) => request.put('/admin/categories/' + id, data),

  adminDelete: (id: number) => request.delete('/admin/categories/' + id),
}

/**
 * 订单 API / Order API
 */
export const orderApi = {
  create: (data: any) => request.post('/user/orders', data),

  userList: (params: { page: number; size: number; paymentStatus?: number; deliveryStatus?: number }) =>
    request.get<PageResult<any>>('/user/orders', params),

  detail: (id: number) => request.get('/user/orders/' + id),

  pay: (id: number) => request.post('/user/orders/' + id + '/pay'),

  adminList: (params: { page: number; size: number; paymentStatus?: number; deliveryStatus?: number; keyword?: string }) =>
    request.get<PageResult<any>>('/admin/orders', params),

  adminDetail: (id: number) => request.get('/admin/orders/' + id),

  adminDeliver: (id: number, data: any) => request.put('/admin/orders/' + id + '/deliver', data),

  adminRefund: (id: number) => request.put('/admin/orders/' + id + '/refund'),
}

/**
 * 卡密 API / Card API
 */
export const cardApi = {
  adminList: (params: { page: number; size: number; productId?: number; status?: number }) =>
    request.get<PageResult<any>>('/admin/cards', params),

  adminImport: (data: any) => request.post('/admin/cards/import', data),

  adminDelete: (id: number) => request.delete('/admin/cards/' + id),

  adminAvailableCount: (productId: number) =>
    request.get('/admin/cards/available-count', { productId }),
}

/**
 * 公告 API / Announcement API
 */
export const announcementApi = {
  userList: (params: { page: number; size: number; type?: number }) =>
    request.get<PageResult<any>>('/user/announcements', params),

  userDetail: (id: number) => request.get('/user/announcements/' + id),

  latest: (limit?: number) => request.get('/user/announcements/latest', { limit }),

  adminList: (params: { page: number; size: number; type?: number; status?: number }) =>
    request.get<PageResult<any>>('/admin/announcements', params),

  adminDetail: (id: number) => request.get('/admin/announcements/' + id),

  adminCreate: (data: any) => request.post('/admin/announcements', data),

  adminUpdate: (id: number, data: any) => request.put('/admin/announcements/' + id, data),

  adminDelete: (id: number) => request.delete('/admin/announcements/' + id),

  adminPublish: (id: number) => request.put('/admin/announcements/' + id + '/publish'),

  adminUnpublish: (id: number) => request.put('/admin/announcements/' + id + '/unpublish'),

  adminToggleTop: (id: number) => request.put('/admin/announcements/' + id + '/top'),
}

/**
 * 争议 API / Dispute API
 */
export const disputeApi = {
  create: (data: any) => request.post('/user/disputes', data),

  userList: (params: { page: number; size: number; status?: number }) =>
    request.get<PageResult<any>>('/user/disputes', params),

  userDetail: (id: number) => request.get('/user/disputes/' + id),

  adminList: (params: { page: number; size: number; status?: number }) =>
    request.get<PageResult<any>>('/admin/disputes', params),

  adminDetail: (id: number) => request.get('/admin/disputes/' + id),

  adminHandle: (id: number, data: any) => request.put('/admin/disputes/' + id + '/handle', data),
}

/**
 * 用户管理 API / User Management API
 */
export const userApi = {
  adminList: (params: { page: number; size: number; keyword?: string; role?: string }) =>
    request.get<PageResult<any>>('/admin/users', params),

  adminUpdateStatus: (id: number, status: number) =>
    request.put('/admin/users/' + id + '/status', null, { params: { status } } as any),

  adminUpdateRole: (id: number, role: string) =>
    request.put('/admin/users/' + id + '/role', null, { params: { role } } as any),
}

/**
 * 看板 API / Dashboard API
 */
export const dashboardApi = {
  getData: () => request.get('/admin/dashboard'),
}

/**
 * 设置 API / Settings API
 */
export const settingsApi = {
  getAll: () => request.get('/admin/settings'),

  batchUpdate: (data: Record<string, string>) => request.put('/admin/settings', data),
}
