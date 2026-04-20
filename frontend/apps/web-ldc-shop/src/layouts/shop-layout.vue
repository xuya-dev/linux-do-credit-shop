<script lang="ts" setup>
import { computed, ref } from 'vue';
import { RouterView, useRoute, useRouter } from 'vue-router';
import { useAccessStore, useUserStore } from '@vben/stores';

import { useAuthStore } from '#/store';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const accessStore = useAccessStore();
const authStore = useAuthStore();

const isLoggedIn = computed(() => !!accessStore.accessToken);
const userInfo = computed(() => userStore.userInfo);
const isAdmin = computed(() => userInfo.value?.roles?.includes('admin'));

const showMobileMenu = ref(false);

const navItems = computed(() => {
  const items = [
    { label: '首页', path: '/home', icon: '🏠' },
    { label: '商品', path: '/products', icon: '🛍️' },
    { label: '公告', path: '/announcements', icon: '📢' },
  ];
  if (isLoggedIn.value) {
    items.push(
      { label: '我的订单', path: '/orders', icon: '📦' },
      { label: '争议', path: '/disputes', icon: '⚖️' },
    );
  }
  return items;
});

const activePath = computed(() => {
  return navItems.value.find(
    (item) =>
      route.path === item.path || route.path.startsWith(item.path + '/'),
  )?.path;
});

async function handleLogout() {
  await authStore.logout(false);
  router.push('/home');
}

function navigate(path: string) {
  router.push(path);
  showMobileMenu.value = false;
}

const userOptions = [
  { label: '个人中心', key: 'profile' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' },
];

function handleUserSelect(key: string) {
  if (key === 'profile') router.push('/profile');
  else if (key === 'logout') handleLogout();
}
</script>

<template>
  <n-layout style="min-height: 100vh" has-sider>
    <n-layout>
      <!-- 导航栏 -->
      <n-layout-header
        bordered
        style="
          position: sticky;
          top: 0;
          z-index: 1000;
          backdrop-filter: blur(12px);
        "
      >
        <div class="nav-container">
          <!-- Logo -->
          <div class="nav-logo" @click="router.push('/home')">
            <span class="logo-icon">🚀</span>
            <span class="logo-text">LDC Shop</span>
          </div>

          <!-- 桌面端导航 -->
          <n-menu
            :value="activePath"
            mode="horizontal"
            class="desktop-nav"
            @update:value="navigate"
          >
            <n-menu-item
              v-for="item in navItems"
              :key="item.path"
              :value="item.path"
            >
              <span>{{ item.label }}</span>
            </n-menu-item>
          </n-menu>

          <!-- 右侧操作区 -->
          <div class="nav-actions">
            <n-button
              v-if="isAdmin"
              quaternary
              size="small"
              type="primary"
              @click="router.push('/admin/dashboard')"
            >
              管理后台
            </n-button>

            <template v-if="isLoggedIn && userInfo">
              <n-dropdown
                :options="userOptions"
                trigger="hover"
                @select="handleUserSelect"
              >
                <div class="user-avatar">
                  <n-avatar
                    :size="32"
                    round
                    :src="userInfo.avatar"
                  >
                    {{
                      (userInfo?.username?.[0] || userInfo?.realName?.[0] || 'U').toUpperCase()
                    }}
                  </n-avatar>
                  <span class="username">{{ userInfo?.username || userInfo?.realName }}</span>
                </div>
              </n-dropdown>
            </template>
            <n-button
              v-else
              type="primary"
              size="small"
              @click="router.push('/auth/login')"
            >
              登录
            </n-button>

            <!-- 移动端菜单按钮 -->
            <n-button
              quaternary
              class="mobile-menu-btn"
              @click="showMobileMenu = true"
            >
              <span style="font-size: 20px">☰</span>
            </n-button>
          </div>
        </div>
      </n-layout-header>

      <!-- 主内容区 -->
      <n-layout-content>
        <RouterView />
      </n-layout-content>

      <!-- 页脚 -->
      <n-layout-footer bordered class="site-footer">
        <div class="footer-content">
          <div class="footer-brand">
            <span class="logo-icon">🚀</span>
            <span>LDC Shop</span>
          </div>
          <p class="footer-desc">
            LINUX DO Credit Shop — 使用 LINUX DO 积分兑换商品
          </p>
          <p class="footer-copyright">
            &copy; {{ new Date().getFullYear() }} LDC Shop. All rights reserved.
          </p>
        </div>
      </n-layout-footer>
    </n-layout>

    <!-- 移动端抽屉菜单 -->
    <n-drawer
      v-model:show="showMobileMenu"
      placement="left"
      width="280"
      :auto-focus="false"
    >
      <div class="mobile-drawer">
        <div class="mobile-drawer-header">
          <span class="logo-icon">🚀</span>
          <span class="logo-text">LDC Shop</span>
        </div>
        <n-menu
          :value="activePath"
          @update:value="navigate"
        >
          <n-menu-item
            v-for="item in navItems"
            :key="item.path"
            :value="item.path"
          >
            <span style="margin-right: 8px">{{ item.icon }}</span>
            {{ item.label }}
          </n-menu-item>
        </n-menu>
      </div>
    </n-drawer>
  </n-layout>
</template>

<style scoped>
.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 24px;
  line-height: 1;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.desktop-nav {
  flex: 1;
  margin: 0 32px;
  max-width: 600px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-avatar:hover {
  background-color: rgba(128, 128, 128, 0.1);
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.mobile-menu-btn {
  display: none;
}

.site-footer {
  padding: 40px 24px;
}

.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  text-align: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}

.footer-desc {
  font-size: 14px;
  opacity: 0.6;
  margin: 0 0 8px;
}

.footer-copyright {
  font-size: 12px;
  opacity: 0.4;
  margin: 0;
}

.mobile-drawer {
  padding: 20px;
}

.mobile-drawer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(128, 128, 128, 0.15);
}

@media (max-width: 768px) {
  .desktop-nav {
    display: none !important;
  }

  .mobile-menu-btn {
    display: flex !important;
  }

  .username {
    display: none;
  }

  .nav-container {
    padding: 0 16px;
  }
}
</style>
