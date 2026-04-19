<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <h1 style="font-size: 28px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 32px;">{{ t('user.title') }}</h1>

    <div v-if="authStore.user" class="feature-card">
      <div style="display: flex; gap: 24px; align-items: center; margin-bottom: 24px;">
        <img v-if="authStore.user.avatar" :src="authStore.user.avatar" style="width: 80px; height: 80px; border-radius: 50%;" />
        <div>
          <h2 style="font-size: 24px; font-weight: 700; color: var(--color-text-dark);">{{ authStore.user.username }}</h2>
          <span class="badge" :class="authStore.user.role === 'admin' ? 'badge-info' : 'badge-success'">{{ authStore.user.role === 'admin' ? t('user.admin') : t('user.normalUser') }}</span>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
        <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('user.nickname') }}</span><p style="font-size: 15px; color: var(--color-text-dark); margin-top: 4px;">{{ authStore.user.nickname || '-' }}</p></div>
        <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('user.email') }}</span><p style="font-size: 15px; color: var(--color-text-dark); margin-top: 4px;">{{ authStore.user.email || '-' }}</p></div>
        <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('user.trustLevel') }}</span><p style="font-size: 15px; color: var(--color-text-dark); margin-top: 4px;">{{ authStore.user.trustLevel ?? '-' }}</p></div>
        <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('user.joinTime') }}</span><p style="font-size: 15px; color: var(--color-text-dark); margin-top: 4px;">{{ authStore.user.createdAt }}</p></div>
      </div>

      <div v-if="authStore.isAdmin" style="margin-top: 24px; padding-top: 24px; border-top: 1px solid var(--color-border);">
        <button @click="router.push('/admin')" style="background: var(--color-primary); color: white; border: none; padding: 10px 24px; border-radius: 8px; font-size: 14px; cursor: pointer;">{{ t('nav.admin') }} →</button>
      </div>
    </div>
  </div>
</template>
