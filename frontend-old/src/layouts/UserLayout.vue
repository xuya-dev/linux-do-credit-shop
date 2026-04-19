<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { NIcon } from 'naive-ui'

const { t, locale } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const mobileMenuOpen = ref(false)

/** 导航菜单 / Navigation Menu */
const navItems = computed(() => [
  { key: 'home', label: t('nav.home'), path: '/' },
  { key: 'products', label: t('nav.products'), path: '/products' },
  { key: 'announcements', label: t('nav.announcements'), path: '/announcements' },
  ...(authStore.isLoggedIn
    ? [
        { key: 'orders', label: t('nav.orders'), path: '/orders' },
        { key: 'disputes', label: t('nav.disputes'), path: '/disputes' },
      ]
    : []),
])

/** 切换主题 / Toggle theme */
function toggleTheme() {
  themeStore.toggleTheme()
}

/** 切换语言 / Toggle language */
function toggleLanguage() {
  const newLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = newLocale
  localStorage.setItem('ldc-shop-locale', newLocale)
}

/** 退出登录 / Logout */
async function handleLogout() {
  await authStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="user-layout">
    <!-- 顶部导航 / Top Navigation -->
    <header style="background: var(--color-bg); border-bottom: 1px solid var(--color-border); height: 60px; position: sticky; top: 0; z-index: 40;">
      <div class="content-wrapper" style="max-width: 1120px; margin: 0 auto; padding: 0 24px; height: 100%; display: flex; align-items: center; justify-content: space-between;">
        <!-- Logo -->
        <div style="display: flex; align-items: center; gap: 8px; cursor: pointer;" @click="router.push('/')">
          <span style="font-size: 20px; font-weight: 700; color: var(--color-text-dark); letter-spacing: -0.02em;">LDC Shop</span>
        </div>

        <!-- Desktop Nav -->
        <nav style="display: flex; align-items: center; gap: 32px;">
          <template v-for="item in navItems" :key="item.key">
            <a
              :href="item.path"
              @click.prevent="router.push(item.path)"
              :style="{
                fontSize: '14px',
                fontWeight: route.path === item.path ? 600 : 400,
                color: route.path === item.path ? 'var(--color-primary)' : 'var(--color-text-mid)',
                borderBottom: route.path === item.path ? '2px solid var(--color-primary)' : '2px solid transparent',
                paddingBottom: '2px',
                transition: 'all 0.2s',
              }"
            >{{ item.label }}</a>
          </template>
        </nav>

        <!-- Right Actions -->
        <div style="display: flex; align-items: center; gap: 12px;">
          <!-- Theme Toggle -->
          <button @click="toggleTheme" style="background: none; border: none; cursor: pointer; padding: 6px; border-radius: 6px; color: var(--color-text-mid);" :title="t('nav.theme')">
            <svg v-if="themeStore.mode === 'light'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
          </button>

          <!-- Language Toggle -->
          <button @click="toggleLanguage" style="background: none; border: none; cursor: pointer; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; color: var(--color-text-mid); border: 1px solid var(--color-border);">
            {{ locale === 'zh-CN' ? 'EN' : '中' }}
          </button>

          <!-- Auth -->
          <template v-if="authStore.isLoggedIn">
            <div style="display: flex; align-items: center; gap: 8px; cursor: pointer;" @click="router.push('/profile')">
              <img v-if="authStore.user?.avatar" :src="authStore.user.avatar" style="width: 28px; height: 28px; border-radius: 50%;" />
              <span style="font-size: 14px; font-weight: 500; color: var(--color-text-dark);">{{ authStore.user?.username }}</span>
            </div>
            <a v-if="authStore.isAdmin" @click.prevent="router.push('/admin')" style="font-size: 12px; font-weight: 600; color: var(--color-primary); cursor: pointer;">{{ t('nav.admin') }}</a>
            <a @click.prevent="handleLogout" style="font-size: 14px; color: var(--color-text-light); cursor: pointer;">{{ t('nav.logout') }}</a>
          </template>
          <template v-else>
            <button @click="router.push('/login')" style="background: var(--color-primary); color: white; border: none; padding: 6px 16px; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer;">{{ t('nav.login') }}</button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容 / Main Content -->
    <main>
      <router-view />
    </main>

    <!-- 页脚 / Footer -->
    <footer style="border-top: 1px solid var(--color-border); padding: 40px 24px; margin-top: 80px;">
      <div class="content-wrapper" style="max-width: 1120px; margin: 0 auto; text-align: center;">
        <p style="font-size: 14px; color: var(--color-text-light);">
          &copy; {{ new Date().getFullYear() }} LDC Shop · Powered by <a href="https://credit.linux.do" target="_blank" style="color: var(--color-primary);">LINUX DO Credit</a>
        </p>
      </div>
    </footer>
  </div>
</template>
