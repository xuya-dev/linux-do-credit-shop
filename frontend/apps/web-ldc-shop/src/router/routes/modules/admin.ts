import type { RouteRecordRaw } from 'vue-router';

const layout = () => import('#/layouts/basic.vue');
const authority = ['admin'];

const routes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: layout,
    meta: {
      noBasicLayout: true,
      authority,
      title: 'page.admin.dashboard',
      icon: 'lucide:shield',
      order: 100,
    },
    redirect: '/admin/dashboard',
    children: [
      {
        name: 'AdminDashboard',
        path: 'dashboard',
        component: () => import('#/views/admin/dashboard.vue'),
        meta: {
          title: 'page.admin.dashboard',
          icon: 'lucide:layout-dashboard',
          authority,
          order: 100,
        },
      },
      {
        name: 'AdminProducts',
        path: 'products',
        component: () => import('#/views/admin/product-manage.vue'),
        meta: {
          title: 'page.admin.products',
          icon: 'lucide:package',
          authority,
          order: 101,
        },
      },
      {
        name: 'AdminCategories',
        path: 'categories',
        component: () => import('#/views/admin/category-manage.vue'),
        meta: {
          title: 'page.admin.categories',
          icon: 'lucide:folder',
          authority,
          order: 102,
        },
      },
      {
        name: 'AdminCards',
        path: 'cards',
        component: () => import('#/views/admin/card-manage.vue'),
        meta: {
          title: 'page.admin.cards',
          icon: 'lucide:key',
          authority,
          order: 103,
        },
      },
      {
        name: 'AdminOrders',
        path: 'orders',
        component: () => import('#/views/admin/order-manage.vue'),
        meta: {
          title: 'page.admin.orders',
          icon: 'lucide:file-text',
          authority,
          order: 104,
        },
      },
      {
        name: 'AdminDisputes',
        path: 'disputes',
        component: () => import('#/views/admin/dispute-manage.vue'),
        meta: {
          title: 'page.admin.disputes',
          icon: 'lucide:shield-alert',
          authority,
          order: 105,
        },
      },
      {
        name: 'AdminAnnouncements',
        path: 'announcements',
        component: () => import('#/views/admin/announcement-manage.vue'),
        meta: {
          title: 'page.admin.announcements',
          icon: 'lucide:megaphone',
          authority,
          order: 106,
        },
      },
      {
        name: 'AdminUsers',
        path: 'users',
        component: () => import('#/views/admin/user-manage.vue'),
        meta: {
          title: 'page.admin.users',
          icon: 'lucide:users',
          authority,
          order: 107,
        },
      },
      {
        name: 'AdminSettings',
        path: 'settings',
        component: () => import('#/views/admin/settings.vue'),
        meta: {
          title: 'page.admin.settings',
          icon: 'lucide:settings',
          authority,
          order: 108,
        },
      },
    ],
  },
];

export default routes;
