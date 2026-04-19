<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';

import { useAuthStore } from '#/store';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const message = useMessage();
const error = ref('');

onMounted(async () => {
  const code = route.query.code as string;
  if (!code) {
    error.value = 'Authorization code missing';
    return;
  }
  try {
    await authStore.oauthLogin(
      code,
      window.location.origin + '/auth/callback',
    );
    message.success('Login successful');
    const redirect =
      (route.query.redirect as string) || '/home';
    router.push(redirect);
  } catch (e: any) {
    error.value = e.message || 'Login failed';
  }
});
</script>

<template>
  <div
    style="
      min-height: 80vh;
      display: flex;
      align-items: center;
      justify-content: center;
    "
  >
    <div v-if="error" style="text-align: center">
      <p style="color: #d03050; margin-bottom: 16px">{{ error }}</p>
      <n-button type="primary" @click="router.push('/auth/login')">
        Back to Login
      </n-button>
    </div>
    <div v-else style="text-align: center; opacity: 0.6">
      <n-spin size="large" />
      <p style="margin-top: 16px">Logging in, please wait...</p>
    </div>
  </div>
</template>
