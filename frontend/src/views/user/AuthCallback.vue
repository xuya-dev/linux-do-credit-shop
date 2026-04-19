<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()
const error = ref('')

onMounted(async () => {
  const code = route.query.code as string
  if (!code) {
    error.value = '授权码缺失 / Authorization code missing'
    return
  }
  try {
    await authStore.login(code, window.location.origin + '/auth/callback')
    message.success('登录成功 / Login successful')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    error.value = e.message || '登录失败 / Login failed'
  }
})
</script>

<template>
  <div style="min-height: 80vh; display: flex; align-items: center; justify-content: center;">
    <div v-if="error" style="text-align: center;">
      <p style="color: var(--color-error); margin-bottom: 16px;">{{ error }}</p>
      <button @click="router.push('/login')" style="background: var(--color-primary); color: white; border: none; padding: 10px 24px; border-radius: 8px; cursor: pointer;">{{ t('common.back') }}</button>
    </div>
    <div v-else style="text-align: center; color: var(--color-text-mid);">
      <p>{{ t('auth.redirecting') }}</p>
    </div>
  </div>
</template>
