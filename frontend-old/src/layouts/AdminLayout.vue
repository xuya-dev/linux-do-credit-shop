<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const { t, locale } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

/** 侧边栏菜单 / Sidebar Menu */
const sidebarItems = computed(() => [
  { key: 'dashboard', label: t('nav.dashboard'), path: '/admin', icon: 'grid' },
  { key: 'products', label: t('nav.products'), path: '/admin/products', icon: 'package' },
  { key: 'categories', label: t('nav.categories'), path: '/admin/categories', icon: 'folder' },
  { key: 'cards', label: t('nav.cards'), path: '/admin/cards', icon: 'key' },
  { key: 'orders', label: t('nav.orders'), path: '/admin/orders', icon: 'file-text' },
  { key: 'disputes', label: t('nav.disputes'), path: '/admin/disputes', icon: 'alert-triangle' },
  { key: 'announcements', label: t('nav.announcements'), path: '/admin/announcements', icon: 'megaphone' },
  { key: 'users', label: t('nav.users'), path: '/admin/users', icon: 'users' },
  { key: 'settings', label: t('nav.settings'), path: '/admin/settings', icon: 'settings' },
])

/** 判断是否激活 / Check if active */
function isActive(path: string) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

function toggleTheme() {
  themeStore.toggleTheme()
}

function toggleLanguage() {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localStorage.setItem('ldc-shop-locale', newLocale)
}
</script>

<template>
  <div class="admin-layout">
    <!-- 侧边栏 / Sidebar -->
    <aside class="admin-sidebar">
      <!-- Logo -->
      <div style="padding: 16px 20px; border-bottom: 1px solid rgba(255,255,255,0.06);">
        <span style="font-size: 18px; font-weight: 700; color: #f1f5f9; letter-spacing: -0.02em;">LDC Shop</span>
        <span style="font-size: 11px; color: #64748b; display: block; margin-top: 2px;">Admin Panel</span>
      </div>

      <!-- 菜单 / Menu -->
      <nav style="padding: 12px 8px;">
        <a
          v-for="item in sidebarItems"
          :key="item.key"
          @click.prevent="router.push(item.path)"
          :style="{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            padding: '10px 12px',
            borderRadius: '6px',
            fontSize: '14px',
            fontWeight: isActive(item.path) ? 600 : 400,
            color: isActive(item.path) ? '#f1f5f9' : '#94a3b8',
            background: isActive(item.path) ? 'rgba(59, 130, 246, 0.15)' : 'transparent',
            cursor: 'pointer',
            marginBottom: '2px',
            transition: 'all 0.15s',
          }"
        >
          {{ item.label }}
        </a>
      </nav>

      <!-- 底部 / Bottom -->
      <div style="position: absolute; bottom: 0; left: 0; right: 0; padding: 16px; border-top: 1px solid rgba(255,255,255,0.06);">
        <a @click.prevent="router.push('/')" style="font-size: 13px; color: #64748b; cursor: pointer;">← {{ t('nav.home') }}</a>
      </div>
    </aside>

    <!-- 主区域 / Main Area -->
    <div class="admin-main">
      <!-- 顶栏 / Top Bar -->
      <header class="admin-topbar">
        <h1 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t(String(route.name || '')) }}</h1>
        <div style="display: flex; align-items: center; gap: 12px;">
          <button @click="toggleLanguage" style="background: none; border: 1px solid var(--admin-card-border); cursor: pointer; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; color: var(--color-text-mid);">
            {{ locale === 'zh-CN' ? 'EN' : '中' }}
          </button>
          <button @click="toggleTheme" style="background: none; border: none; cursor: pointer; padding: 6px; color: var(--color-text-mid);">
            <svg v-if="themeStore.mode === 'light'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
          </button>
          <div style="display: flex; align-items: center; gap: 8px;">
            <img v-if="authStore.user?.avatar" :src="authStore.user.avatar" style="width: 28px; height: 28px; border-radius: 50%;" />
            <span style="font-size: 14px; color: var(--color-text-dark);">{{ authStore.user?.username }}</span>
          </div>
        </div>
      </header>

      <!-- 内容区 / Content -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>
