<script lang="ts" setup>
import { computed, onMounted } from 'vue';
import { RouterView, useRoute, useRouter } from 'vue-router';
import { useAccessStore, useUserStore } from '@vben/stores';
import { LanguageToggle, ThemeToggle } from '@vben/layouts';
import { useI18n } from '@vben/locales';
import { useAuthStore } from '#/store';
import { settingsApi } from '#/api/modules/settings';
import { ref } from 'vue';

const { t } = useI18n();

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const accessStore = useAccessStore();
const authStore = useAuthStore();

const siteName = ref('LDC Shop');
const siteLogo = ref('/logo.png');

const isLoggedIn = computed(() => !!accessStore.accessToken);
const userInfo = computed(() => userStore.userInfo);

const navItems = computed(() => {
  const items = [
    { label: t('page.shop.home'), path: '/home' },
    { label: t('page.shop.announcements'), path: '/announcements' },
  ];
  if (isLoggedIn.value) {
    items.push(
      { label: t('page.shop.disputes'), path: '/disputes' },
    );
  }
  return items;
});

const activePath = computed(() => {
  return navItems.value.find(
    (item) => route.path === item.path || route.path.startsWith(item.path + '/'),
  )?.path;
});

async function handleLogout() {
  await authStore.logout(false);
  router.push('/home');
}

function navigate(path: string) {
  router.push(path);
}

const userOptions = computed(() => {
  const options = [
    { label: t('page.shop.profile'), key: 'profile' },
  ];
  if (userStore.userInfo?.role === 'admin' || userStore.userInfo?.roles?.includes('admin')) {
    options.push({ label: t('page.shop.adminPanel'), key: 'admin' });
  }
  options.push(
    { type: 'divider', key: 'd1' },
    { label: t('page.shop.logout'), key: 'logout' }
  );
  return options;
});

function handleUserSelect(key: string) {
  if (key === 'profile') router.push('/profile');
  else if (key === 'admin') router.push('/admin/dashboard');
  else if (key === 'logout') handleLogout();
}

onMounted(() => {
  if (accessStore.accessToken && !userStore.userInfo) {
    authStore.fetchUserInfo().catch(() => {
      accessStore.setAccessToken('');
    });
  }
  settingsApi.getPublic().then((res) => {
    if (res?.site_name) siteName.value = res.site_name;
    if (res?.site_logo) siteLogo.value = res.site_logo;
  }).catch(() => {});
});
</script>

<template>
  <div class="faka-layout faka-theme">
    <!-- 顶部导航栏 -->
    <header class="faka-header">
      <div class="faka-header-container">
        <!-- Logo区及标签 -->
        <div class="header-left">
          <div class="faka-logo" @click="router.push('/home')">
            <img v-if="siteLogo" :src="siteLogo" class="logo-img" alt="logo" />
            <span class="logo-text">{{ siteName }}</span>
          </div>
        </div>

        <!-- 右侧导航及操作 -->
        <div class="header-right">
          <nav class="faka-nav">
            <a
              v-for="item in navItems"
              :key="item.path"
              href="javascript:void(0)"
              class="nav-link"
              :class="{ active: activePath === item.path }"
              @click="navigate(item.path)"
            >
              {{ item.label }}
            </a>
          </nav>

          <div style="display: flex; align-items: center; gap: 8px; margin-left: 16px;">
            <ThemeToggle />
            <LanguageToggle />
          </div>

          <template v-if="isLoggedIn && userInfo">
            <n-dropdown :options="userOptions" trigger="click" @select="handleUserSelect">
              <div class="user-avatar-trigger" style="margin-left: 16px; cursor: pointer;">
                <n-avatar 
                  round 
                  size="small" 
                  :src="userInfo?.avatar"
                  style="background-color: #4f46e5; font-size: 14px;"
                >
                  <template v-if="!userInfo?.avatar">
                    {{ (userInfo?.username?.[0] || userInfo?.nickname?.[0] || 'U').toUpperCase() }}
                  </template>
                </n-avatar>
              </div>
            </n-dropdown>
          </template>
          <template v-else>
            <span class="nav-link" style="margin-left: 16px; cursor: pointer;" @click="handleLoginClick">{{ t('page.auth.login') }}</span>
          </template>

          <div class="primary-search-btn" @click="router.push('/orders')">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            {{ t('page.shop.orders') }}
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="faka-main">
      <RouterView />
    </main>

    <!-- 底部页脚 -->
    <footer class="faka-footer">
      <div class="footer-inner">
        <span>Powered by LINUX DO Credit Shop</span>
        <span>|</span>
        <span>© 2026 虚拟商品兑换平台</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 骨架主题变量 - 深浅切换核心支持 */
.faka-theme {
  --faka-bg-body: #f4f5f7;
  --faka-bg-header: #ffffff;
  --faka-text-main: #333333;
  --faka-text-sub: #595959;
  --faka-border: #f0f0f0;
  --faka-tag-bg: #f5f5f5;
  --faka-tag-active-bg: #e6f7ff;
  --faka-primary-bg: #f0f5ff;
  --faka-primary-color: #1890ff;
  --faka-hover-bg: #fafafa;
  --faka-hint-color: #bfbfbf;
}
:global(.dark) .faka-theme, :root.dark .faka-theme {
  --faka-bg-body: #000000;
  --faka-bg-header: #141414;
  --faka-text-main: #e0e0e0;
  --faka-text-sub: #a6a6a6;
  --faka-border: #303030;
  --faka-tag-bg: #1f1f1f;
  --faka-tag-active-bg: #112840;
  --faka-primary-bg: #111d2c;
  --faka-primary-color: #177ddc;
  --faka-hover-bg: #262626;
  --faka-hint-color: #666666;
}

/* 发卡网经典底色 */
.faka-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--faka-bg-body);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: var(--faka-text-main);
  transition: background-color 0.3s;
}

/* 头部导航 */
.faka-header {
  background: var(--faka-bg-header);
  height: 64px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.faka-header-container {
  max-width: 1300px;
  margin: 0 auto;
  height: 100%;
  padding: 0 24px 0 60px; /* 留出左侧悬浮的视觉空间或居中偏右 */
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.faka-logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.logo-img {
  height: 28px;
  width: auto;
  border-radius: 4px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--faka-text-main);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.faka-nav {
  display: flex;
  gap: 24px;
}

.nav-link {
  text-decoration: none;
  font-size: 14px;
  color: var(--faka-text-sub);
  transition: color 0.3s;
}

.nav-link:hover, .nav-link.active {
  color: #1890ff;
}

.primary-search-btn {
  background: #4f46e5;
  color: #fff;
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  margin-left: 16px;
  box-shadow: 0 2px 6px rgba(79, 70, 229, 0.3);
  transition: background 0.2s;
}
.primary-search-btn:hover {
  background: #4338ca;
}

/* 主体及页脚 */
.faka-main {
  flex: 1;
  padding: 24px 0;
}

.faka-footer {
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
  color: var(--faka-text-sub);
}

.footer-inner {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

@media (max-width: 900px) {
  .faka-nav { display: none; }
  .faka-header-container { padding: 0 16px; }
}
</style>
