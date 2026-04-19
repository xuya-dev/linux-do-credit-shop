import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * 路由配置 / Router Configuration
 */
const routes: RouteRecordRaw[] = [
  // ============================================================
  // 公开页面 / Public Pages
  // ============================================================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/LoginPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/auth/callback',
    name: 'AuthCallback',
    component: () => import('@/views/user/AuthCallback.vue'),
    meta: { requiresAuth: false },
  },

  // ============================================================
  // 用户端页面 / User Pages
  // ============================================================
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/user/HomePage.vue'),
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/user/ProductList.vue'),
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/user/ProductDetail.vue'),
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/user/OrderList.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'disputes',
        name: 'Disputes',
        component: () => import('@/views/user/DisputeList.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/user/AnnouncementList.vue'),
      },
      {
        path: 'announcement/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/user/AnnouncementDetail.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfilePage.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },

  // ============================================================
  // 管理端页面 / Admin Pages
  // ============================================================
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardPage.vue'),
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductManage.vue'),
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryManage.vue'),
      },
      {
        path: 'cards',
        name: 'AdminCards',
        component: () => import('@/views/admin/CardManage.vue'),
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderManage.vue'),
      },
      {
        path: 'disputes',
        name: 'AdminDisputes',
        component: () => import('@/views/admin/DisputeManage.vue'),
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/AnnouncementManage.vue'),
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/SettingsPage.vue'),
      },
    ],
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

/**
 * 路由守卫 / Navigation Guards
 */
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  if (authStore.isLoggedIn && !authStore.userFetched) {
    await authStore.fetchUser()
  }

  if (to.meta.requiresAuth !== false && !authStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ name: 'Home' })
    return
  }

  if (to.name === 'Login' && authStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
