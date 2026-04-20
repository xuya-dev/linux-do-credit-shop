import type { RouteRecordRaw } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';

const ShopLayout = () => import('#/layouts/shop-layout.vue');
const AuthPageLayout = () => import('#/layouts/auth.vue');

const fallbackNotFoundRoute: RouteRecordRaw = {
  component: () => import('#/views/_core/fallback/not-found.vue'),
  meta: {
    hideInBreadcrumb: true,
    hideInMenu: true,
    hideInTab: true,
    title: '404',
  },
  name: 'FallbackNotFound',
  path: '/:path(.*)*',
};

const coreRoutes: RouteRecordRaw[] = [
  {
    component: ShopLayout,
    meta: {
      hideInBreadcrumb: true,
      title: 'Shop',
    },
    name: 'Shop',
    path: '/',
    redirect: preferences.app.defaultHomePath,
    children: [
      {
        name: 'Home',
        path: '/home',
        component: () => import('#/views/user/home.vue'),
        meta: { title: '首页' },
      },
      {
        name: 'Products',
        path: '/products',
        component: () => import('#/views/user/product-list.vue'),
        meta: { title: '商品' },
      },
      {
        name: 'ProductDetail',
        path: '/product/:id',
        component: () => import('#/views/user/product-detail.vue'),
        meta: { title: '商品详情' },
      },
      {
        name: 'Announcements',
        path: '/announcements',
        component: () => import('#/views/user/announcement-list.vue'),
        meta: { title: '公告' },
      },
      {
        name: 'AnnouncementDetail',
        path: '/announcement/:id',
        component: () => import('#/views/user/announcement-detail.vue'),
        meta: { title: '公告详情' },
      },
      {
        name: 'Orders',
        path: '/orders',
        component: () => import('#/views/user/order-list.vue'),
        meta: { title: '我的订单', requiresAuth: true },
      },
      {
        name: 'OrderDetail',
        path: '/order/:id',
        component: () => import('#/views/user/order-detail.vue'),
        meta: { title: '订单详情', requiresAuth: true },
      },
      {
        name: 'Disputes',
        path: '/disputes',
        component: () => import('#/views/user/dispute-list.vue'),
        meta: { title: '争议', requiresAuth: true },
      },
      {
        name: 'DisputeDetail',
        path: '/dispute/:id',
        component: () => import('#/views/user/dispute-detail.vue'),
        meta: { title: '争议详情', requiresAuth: true },
      },
      {
        name: 'Profile',
        path: '/profile',
        component: () => import('#/views/user/profile.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
    ],
  },
  {
    component: AuthPageLayout,
    meta: {
      hideInTab: true,
      title: 'Authentication',
    },
    name: 'Authentication',
    path: '/auth',
    redirect: LOGIN_PATH,
    children: [
      {
        name: 'Login',
        path: 'login',
        component: () => import('#/views/user/login.vue'),
        meta: {
          title: '登录',
          hideInTab: true,
        },
      },
      {
        name: 'AuthCallback',
        path: 'callback',
        component: () => import('#/views/user/auth-callback.vue'),
        meta: {
          title: 'OAuth Callback',
          hideInTab: true,
        },
      },
    ],
  },
];

export { coreRoutes, fallbackNotFoundRoute };
