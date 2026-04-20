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
const loading = ref(true);

onMounted(async () => {
  const code = route.query.code as string;
  if (!code) {
    error.value = '授权码缺失，请重新登录';
    loading.value = false;
    return;
  }
  try {
    await authStore.oauthLogin(
      code,
      window.location.origin + '/auth/callback',
    );
    message.success('登录成功');
    const redirect = (route.query.redirect as string) || '/home';
    router.push(redirect);
  } catch (e: any) {
    error.value = e.message || '登录失败，请重试';
    loading.value = false;
  }
});
</script>

<template>
  <div class="callback-page">
    <div v-if="error" class="callback-error">
      <div class="error-icon">❌</div>
      <p class="error-text">{{ error }}</p>
      <n-button type="primary" @click="router.push('/auth/login')">
        返回登录页
      </n-button>
    </div>
    <div v-else class="callback-loading">
      <n-spin size="large" />
      <p class="loading-text">正在登录中...</p>
      <p class="loading-sub">请稍候，正在验证您的身份</p>
    </div>
  </div>
</template>

<style scoped>
.callback-page {
  text-align: center;
  padding: 40px 20px;
}

.callback-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-text {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.loading-sub {
  font-size: 14px;
  opacity: 0.5;
  margin: 0;
}

.callback-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.error-icon {
  font-size: 48px;
  line-height: 1;
}

.error-text {
  font-size: 16px;
  color: #d03050;
  margin: 0;
}
</style>
