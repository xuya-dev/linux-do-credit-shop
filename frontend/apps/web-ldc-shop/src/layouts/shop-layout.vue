<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
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

const navItems = computed(() => {
  const items = [
    { label: '首页', labelEn: 'Home', path: '/home' },
    { label: '商品', labelEn: 'Products', path: '/products' },
    { label: '公告', labelEn: 'Announcements', path: '/announcements' },
  ];
  if (isLoggedIn.value) {
    items.push(
      { label: '我的订单', labelEn: 'Orders', path: '/orders' },
      { label: '争议', labelEn: 'Disputes', path: '/disputes' },
    );
  }
  return items;
});

async function handleLogout() {
  await authStore.logout(false);
  router.push('/home');
}
</script>

<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column">
    <header
      style="
        position: sticky;
        top: 0;
        z-index: 100;
        background: var(--n-color, #fff);
        border-bottom: 1px solid rgba(128, 128, 128, 0.1);
      "
    >
      <div
        style="
          max-width: 1200px;
          margin: 0 auto;
          padding: 0 24px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          height: 60px;
        "
      >
        <div
          style="
            display: flex;
            align-items: center;
            gap: 32px;
          "
        >
          <div
            style="
              font-size: 20px;
              font-weight: 700;
              cursor: pointer;
              letter-spacing: -0.02em;
            "
            @click="router.push('/home')"
          >
            LDC Shop
          </div>
          <nav style="display: flex; gap: 4px">
            <n-button
              v-for="item in navItems"
              :key="item.path"
              :type="route.path === item.path || route.path.startsWith(item.path + '/') ? 'primary' : 'default'"
              text
              style="font-size: 14px"
              @click="router.push(item.path)"
            >
              {{ item.label }}
            </n-button>
          </nav>
        </div>

        <div style="display: flex; align-items: center; gap: 12px">
          <n-button
            v-if="isAdmin"
            size="small"
            type="info"
            quaternary
            @click="router.push('/admin/dashboard')"
          >
            管理后台
          </n-button>

          <template v-if="isLoggedIn && userInfo">
            <n-dropdown
              :options="[
                { label: '个人中心', key: 'profile' },
                { label: '退出登录', key: 'logout' },
              ]"
              @select="(key: string) => key === 'profile' ? router.push('/profile') : handleLogout()"
            >
              <div
                style="
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  cursor: pointer;
                "
              >
                <n-avatar :size="28" round>
                  {{ (userInfo.username || userInfo.realName || 'U')[0].toUpperCase() }}
                </n-avatar>
                <span style="font-size: 14px">
                  {{ userInfo.username || userInfo.realName }}
                </span>
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
        </div>
      </div>
    </header>

    <main style="flex: 1">
      <slot />
    </main>

    <footer
      style="
        border-top: 1px solid rgba(128, 128, 128, 0.1);
        padding: 24px;
        text-align: center;
        font-size: 13px;
        opacity: 0.5;
      "
    >
      LDC Shop &copy; {{ new Date().getFullYear() }} &mdash; LINUX DO Credit Shop
    </footer>
  </div>
</template>
