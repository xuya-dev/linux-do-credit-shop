import type { RouteRecordRaw } from 'vue-router';

const layout = () => import('#/layouts/basic.vue');
const authority = ['admin'];

function adminRoute(
  name: string,
  path: string,
  component: () => Promise<any>,
  icon: string,
  titleKey: string,
  order: number,
): RouteRecordRaw {
  return {
    path: `/admin/${path}`,
    component: layout,
    meta: {
      noBasicLayout: true,
      authority,
      title: titleKey,
      icon,
      order,
      hideChildrenInMenu: true,
    },
    children: [
      {
        path: '',
        name,
        component,
        meta: { title: titleKey, icon, authority, hideInMenu: true },
      },
    ],
  };
}

const routes: RouteRecordRaw[] = [
  {
    path: '/admin',
    redirect: '/admin/dashboard',
    meta: { hideInMenu: true },
  },
  adminRoute('AdminDashboard',     'dashboard',     () => import('#/views/admin/dashboard.vue'),          'lucide:layout-dashboard', 'page.admin.dashboard',     100),
  adminRoute('AdminProducts',      'products',      () => import('#/views/admin/product-manage.vue'),     'lucide:package',          'page.admin.products',      101),
  adminRoute('AdminCategories',    'categories',    () => import('#/views/admin/category-manage.vue'),    'lucide:folder',           'page.admin.categories',    102),
  adminRoute('AdminCards',         'cards',         () => import('#/views/admin/card-manage.vue'),        'lucide:key',              'page.admin.cards',         103),
  adminRoute('AdminOrders',        'orders',        () => import('#/views/admin/order-manage.vue'),       'lucide:file-text',        'page.admin.orders',        104),
  adminRoute('AdminDisputes',      'disputes',      () => import('#/views/admin/dispute-manage.vue'),     'lucide:shield-alert',     'page.admin.disputes',      105),
  adminRoute('AdminAnnouncements', 'announcements', () => import('#/views/admin/announcement-manage.vue'),'lucide:megaphone',        'page.admin.announcements', 106),
  adminRoute('AdminUsers',         'users',         () => import('#/views/admin/user-manage.vue'),        'lucide:users',            'page.admin.users',         107),
  adminRoute('AdminSettings',      'settings',      () => import('#/views/admin/settings.vue'),           'lucide:settings',         'page.admin.settings',      108),
];

export default routes;
