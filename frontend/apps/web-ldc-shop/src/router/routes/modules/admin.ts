import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    component: () => import('#/layouts/basic.vue'),
    meta: {
      icon: 'lucide:settings',
      title: '管理后台',
      order: 100,
      authority: ['admin'],
    },
    name: 'Admin',
    path: '/admin',
    redirect: '/admin/dashboard',
    children: [
      {
        name: 'AdminDashboard',
        path: 'dashboard',
        component: () => import('#/views/admin/dashboard.vue'),
        meta: {
          title: '数据看板',
          icon: 'lucide:layout-dashboard',
          order: 1,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminProducts',
        path: 'products',
        component: () => import('#/views/admin/product-manage.vue'),
        meta: {
          title: '商品管理',
          icon: 'lucide:package',
          order: 2,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminCategories',
        path: 'categories',
        component: () => import('#/views/admin/category-manage.vue'),
        meta: {
          title: '分类管理',
          icon: 'lucide:folder',
          order: 3,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminCards',
        path: 'cards',
        component: () => import('#/views/admin/card-manage.vue'),
        meta: {
          title: '卡密管理',
          icon: 'lucide:key',
          order: 4,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminOrders',
        path: 'orders',
        component: () => import('#/views/admin/order-manage.vue'),
        meta: {
          title: '订单管理',
          icon: 'lucide:file-text',
          order: 5,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminDisputes',
        path: 'disputes',
        component: () => import('#/views/admin/dispute-manage.vue'),
        meta: {
          title: '争议管理',
          icon: 'lucide:shield-alert',
          order: 6,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminAnnouncements',
        path: 'announcements',
        component: () => import('#/views/admin/announcement-manage.vue'),
        meta: {
          title: '公告管理',
          icon: 'lucide:megaphone',
          order: 7,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminUsers',
        path: 'users',
        component: () => import('#/views/admin/user-manage.vue'),
        meta: {
          title: '用户管理',
          icon: 'lucide:users',
          order: 8,
          authority: ['admin'],
        },
      },
      {
        name: 'AdminSettings',
        path: 'settings',
        component: () => import('#/views/admin/settings.vue'),
        meta: {
          title: '系统设置',
          icon: 'lucide:settings',
          order: 9,
          authority: ['admin'],
        },
      },
    ],
  },
];

export default routes;
