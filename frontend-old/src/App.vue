<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { NMessageProvider, NDialogProvider, NConfigProvider, NNotificationProvider } from 'naive-ui'

const authStore = useAuthStore()
const themeStore = useThemeStore()

onMounted(() => {
  if (authStore.token) {
    authStore.fetchUser()
  }
})
</script>

<template>
  <NConfigProvider :theme="themeStore.naiveTheme">
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <router-view />
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
