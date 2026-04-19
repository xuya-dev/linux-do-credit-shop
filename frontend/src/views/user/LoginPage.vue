<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()

async function handleLogin() {
  try {
    const res = await authApi.getAuthorizeUrl(window.location.origin + '/auth/callback')
    if (res.data?.url) {
      window.location.href = res.data.url
    } else {
      // 直接拼接 / Build URL directly
      window.location.href = `/api/auth/authorize-url?redirectUri=${encodeURIComponent(window.location.origin + '/auth/callback')}`
    }
  } catch (e) {
    message.error(t('common.failed'))
  }
}
</script>

<template>
  <div style="min-height: 80vh; display: flex; align-items: center; justify-content: center;">
    <div style="text-align: center; max-width: 400px;">
      <h1 style="font-size: 32px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 12px;">{{ t('app.title') }}</h1>
      <p style="font-size: 16px; color: var(--color-text-mid); margin-bottom: 32px;">{{ t('app.subtitle') }}</p>
      <button @click="handleLogin" style="background: var(--color-primary); color: white; border: none; padding: 14px 40px; border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer; display: flex; align-items: center; gap: 8px; margin: 0 auto;">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
        {{ t('auth.loginWithLDC') }}
      </button>
    </div>
  </div>
</template>
