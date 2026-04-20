/**
 * LDC Shop - 全局类型定义
 * 与后端 Entity/Result/Param 一一对应
 */

// ============================================================
// 基础分页类型
// ============================================================
export interface PaginatedResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// ============================================================
// 枚举常量映射
// ============================================================

/** 支付状态 */
export enum PaymentStatus {
  PENDING = 0,
  PAID = 1,
  REFUNDED = 2,
}

/** 发货状态 */
export enum DeliveryStatus {
  PENDING = 0,
  DELIVERED = 1,
  COMPLETED = 2,
}

/** 争议状态 */
export enum DisputeStatus {
  PENDING = 0,
  ACCEPTED = 1,
  REJECTED = 2,
  PLATFORM = 3,
}

/** 商品类型 */
export enum ProductType {
  VIRTUAL = 1,
  PHYSICAL = 2,
}

/** 公告类型 */
export enum AnnouncementType {
  NOTICE = 1,
  ACTIVITY = 2,
  UPDATE = 3,
}

/** 用户状态 */
export enum UserStatus {
  DISABLED = 0,
  ENABLED = 1,
}

/** 商品状态 */
export enum ProductStatus {
  OFF_SHELF = 0,
  ON_SHELF = 1,
}

// ============================================================
// 标签映射工具
// ============================================================

export const PAYMENT_STATUS_MAP: Record<number, { label: string; type: 'default' | 'success' | 'warning' | 'error' }> = {
  [PaymentStatus.PENDING]: { label: '待支付', type: 'warning' },
  [PaymentStatus.PAID]: { label: '已支付', type: 'success' },
  [PaymentStatus.REFUNDED]: { label: '已退款', type: 'default' },
};

export const DELIVERY_STATUS_MAP: Record<number, { label: string; type: 'default' | 'success' | 'info' }> = {
  [DeliveryStatus.PENDING]: { label: '待发货', type: 'default' },
  [DeliveryStatus.DELIVERED]: { label: '已发货', type: 'info' },
  [DeliveryStatus.COMPLETED]: { label: '已完成', type: 'success' },
};

export const DISPUTE_STATUS_MAP: Record<number, { label: string; type: 'default' | 'success' | 'warning' | 'error' | 'info' }> = {
  [DisputeStatus.PENDING]: { label: '处理中', type: 'warning' },
  [DisputeStatus.ACCEPTED]: { label: '已通过', type: 'success' },
  [DisputeStatus.REJECTED]: { label: '已驳回', type: 'error' },
  [DisputeStatus.PLATFORM]: { label: '平台介入', type: 'info' },
};

export const PRODUCT_TYPE_MAP: Record<number, { label: string; type: 'info' | 'success' }> = {
  [ProductType.VIRTUAL]: { label: '虚拟商品', type: 'info' },
  [ProductType.PHYSICAL]: { label: '实物商品', type: 'success' },
};

export const ANNOUNCEMENT_TYPE_MAP: Record<number, { label: string; type: 'default' | 'success' | 'warning' | 'info' }> = {
  [AnnouncementType.NOTICE]: { label: '通知', type: 'success' },
  [AnnouncementType.ACTIVITY]: { label: '活动', type: 'warning' },
  [AnnouncementType.UPDATE]: { label: '更新', type: 'info' },
};

// ============================================================
// 用户相关
// ============================================================

export interface UserInfo {
  id: number;
  ldcUid: string;
  username: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  trustLevel: number;
  role: string;
  roles?: string[];
  status: UserStatus;
  lastLoginAt?: string;
  createdAt?: string;
  /** 前端额外补充 */
  homePath?: string;
  realName?: string;
}

export interface LoginResult {
  token: string;
  user: UserInfo;
  isNewUser: boolean;
}

// ============================================================
// 商品相关
// ============================================================

export interface Category {
  id: number;
  name: string;
  icon?: string;
  sortOrder: number;
  status: number;
  productCount?: number;
  createdAt?: string;
}

export interface Product {
  id: number;
  categoryId?: number;
  categoryName?: string;
  name: string;
  description?: string;
  coverImage?: string;
  /** 商品图片列表（JSON 数组） */
  images?: string[];
  price: number;
  productType: ProductType;
  stock: number;
  soldCount: number;
  status: ProductStatus;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

// ============================================================
// 订单相关
// ============================================================

export interface Order {
  id: number;
  orderNo: string;
  userId: number;
  buyerName?: string;
  productId: number;
  productName: string;
  productType: ProductType;
  productCoverImage?: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  ldcTradeNo?: string;
  paymentStatus: PaymentStatus;
  deliveryStatus: DeliveryStatus;
  deliveryInfo?: string;
  contactInfo?: string;
  remark?: string;
  adminRemark?: string;
  /** 虚拟商品卡密列表 */
  cardContents?: string[];
  paidAt?: string;
  deliveredAt?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface PaymentSubmitResult {
  orderNo: string;
  payUrl: string;
}

export interface OrderCreateParams {
  productId: number;
  quantity: number;
  contactInfo?: string;
  remark?: string;
  deliveryInfo?: string;
}

// ============================================================
// 争议相关
// ============================================================

export interface Dispute {
  id: number;
  orderId: number;
  orderNo?: string;
  productName?: string;
  userId: number;
  username?: string;
  reason: string;
  evidence?: string[];
  status: DisputeStatus;
  statusName?: string;
  adminNote?: string;
  handlerName?: string;
  handledAt?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface DisputeCreateParams {
  orderId: number;
  reason: string;
  evidence?: string[];
}

// ============================================================
// 公告相关
// ============================================================

export interface Announcement {
  id: number;
  title: string;
  content?: string;
  type: AnnouncementType;
  typeName?: string;
  isTop: number;
  status: number;
  coverImage?: string;
  publishedAt?: string;
  createdByName?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ============================================================
// 看板相关（管理端，但类型定义放在此处统一管理）
// ============================================================

export interface DashboardResult {
  todayOrderCount: number;
  todaySalesAmount: number;
  totalUserCount: number;
  totalOrderCount: number;
  pendingDisputeCount: number;
  pendingDeliveryCount: number;
  salesTrend: Array<{ date: string; amount: number }>;
  paymentStatusDistribution: Array<{ status: number; statusName: string; count: number }>;
  productSalesRank: Array<{ productName: string; totalQuantity: number; totalAmount: number }>;
  categorySalesDistribution: Array<{ categoryName: string; totalAmount: number }>;
}

// ============================================================
// 卡密相关（管理端）
// ============================================================

export interface ProductCard {
  id: number;
  productId: number;
  cardContent: string;
  status: number;
  orderId?: number;
  soldAt?: string;
  createdAt: string;
}

export interface CardImportParams {
  productId: number;
  cards: string[];
}

// ============================================================
// 系统设置（管理端）
// ============================================================

export type ShopSettings = Record<string, string>;
