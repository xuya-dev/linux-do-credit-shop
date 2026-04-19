import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:store',
      title: '商城',
      order: 0,
    },
    name: 'Shop',
    path: '/',
    children: [
      {
        name: 'Home',
        path: '/home',
        component: () => import('#/views/user/home.vue'),
        meta: {
          title: '首页',
          icon: 'lucide:home',
          order: 1,
          ignoreAccess: true,
        },
      },
      {
        name: 'Products',
        path: '/products',
        component: () => import('#/views/user/product-list.vue'),
        meta: {
          title: '商品',
          icon: 'lucide:package',
          order: 2,
          ignoreAccess: true,
        },
      },
      {
        name: 'ProductDetail',
        path: '/product/:id',
        component: () => import('#/views/user/product-detail.vue'),
        meta: {
          title: '商品详情',
          hideInMenu: true,
          hideInTab: true,
          ignoreAccess: true,
        },
      },
      {
        name: 'Announcements',
        path: '/announcements',
        component: () => import('#/views/user/announcement-list.vue'),
        meta: {
          title: '公告',
          icon: 'lucide:megaphone',
          order: 5,
          ignoreAccess: true,
        },
      },
      {
        name: 'AnnouncementDetail',
        path: '/announcement/:id',
        component: () => import('#/views/user/announcement-detail.vue'),
        meta: {
          title: '公告详情',
          hideInMenu: true,
          hideInTab: true,
          ignoreAccess: true,
        },
      },
      {
        name: 'Orders',
        path: '/orders',
        component: () => import('#/views/user/order-list.vue'),
        meta: {
          title: '我的订单',
          icon: 'lucide:file-text',
          order: 3,
          authority: ['user', 'admin'],
        },
      },
      {
        name: 'OrderDetail',
        path: '/order/:id',
        component: () => import('#/views/user/order-detail.vue'),
        meta: {
          title: '订单详情',
          hideInMenu: true,
          hideInTab: true,
          authority: ['user', 'admin'],
        },
      },
      {
        name: 'Disputes',
        path: '/disputes',
        component: () => import('#/views/user/dispute-list.vue'),
        meta: {
          title: '争议',
          icon: 'lucide:shield-alert',
          order: 4,
          authority: ['user', 'admin'],
        },
      },
      {
        name: 'Profile',
        path: '/profile',
        component: () => import('#/views/user/profile.vue'),
        meta: {
          title: '个人中心',
          icon: 'lucide:user',
          order: 6,
          authority: ['user', 'admin'],
        },
      },
    ],
  },
];

export default routes;
