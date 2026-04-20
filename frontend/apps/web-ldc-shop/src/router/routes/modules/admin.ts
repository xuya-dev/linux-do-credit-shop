import type { RouteRecordRaw } from 'vue-router';

const layout = () => import('#/layouts/basic.vue');
const authority = ['admin'];
const noBasicLayout = true;

const routes: RouteRecordRaw[] = [
  {
    path: '/admin',
    redirect: '/admin/dashboard',
    meta: { hideMenu: true },
  },
  {
    path: '/admin/dashboard',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('#/views/admin/dashboard.vue'),
        meta: { title: 'page.admin.dashboard', icon: 'lucide:layout-dashboard', order: 100, authority },
      },
    ],
  },
  {
    path: '/admin/products',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminProducts',
        component: () => import('#/views/admin/product-manage.vue'),
        meta: { title: 'page.admin.products', icon: 'lucide:package', order: 101, authority },
      },
    ],
  },
  {
    path: '/admin/categories',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminCategories',
        component: () => import('#/views/admin/category-manage.vue'),
        meta: { title: 'page.admin.categories', icon: 'lucide:folder', order: 102, authority },
      },
    ],
  },
  {
    path: '/admin/cards',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminCards',
        component: () => import('#/views/admin/card-manage.vue'),
        meta: { title: 'page.admin.cards', icon: 'lucide:key', order: 103, authority },
      },
    ],
  },
  {
    path: '/admin/orders',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminOrders',
        component: () => import('#/views/admin/order-manage.vue'),
        meta: { title: 'page.admin.orders', icon: 'lucide:file-text', order: 104, authority },
      },
    ],
  },
  {
    path: '/admin/disputes',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminDisputes',
        component: () => import('#/views/admin/dispute-manage.vue'),
        meta: { title: 'page.admin.disputes', icon: 'lucide:shield-alert', order: 105, authority },
      },
    ],
  },
  {
    path: '/admin/announcements',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminAnnouncements',
        component: () => import('#/views/admin/announcement-manage.vue'),
        meta: { title: 'page.admin.announcements', icon: 'lucide:megaphone', order: 106, authority },
      },
    ],
  },
  {
    path: '/admin/users',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminUsers',
        component: () => import('#/views/admin/user-manage.vue'),
        meta: { title: 'page.admin.users', icon: 'lucide:users', order: 107, authority },
      },
    ],
  },
  {
    path: '/admin/settings',
    component: layout,
    meta: { noBasicLayout, authority },
    children: [
      {
        path: '',
        name: 'AdminSettings',
        component: () => import('#/views/admin/settings.vue'),
        meta: { title: 'page.admin.settings', icon: 'lucide:settings', order: 108, authority },
      },
    ],
  },
];

export default routes;
