<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { useAuthStore } from '#/store';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const message = useMessage();
const { t } = useI18n();

const error = ref('');
const loading = ref(true);

onMounted(async () => {
  const code = route.query.code as string;
  if (!code) {
    error.value = t('page.user.authCodeMissing');
    loading.value = false;
    return;
  }
  try {
    await authStore.oauthLogin(
      code,
      window.location.origin + '/auth/callback',
    );
    message.success(t('page.user.loginSuccess'));
    const redirectParam = route.query.redirect as string;
    const redirect = (redirectParam && /^\/[a-zA-Z0-9]/.test(redirectParam) && !redirectParam.startsWith('//')) ? redirectParam : '/home';
    router.push(redirect);
  } catch (e: any) {
    error.value = e.message || t('page.user.loginFailed');
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
        {{ t('page.user.backToLogin') }}
      </n-button>
    </div>
    <div v-else class="callback-loading">
      <n-spin size="large" />
      <p class="loading-text">{{ t('page.user.loggingIn') }}</p>
      <p class="loading-sub">{{ t('page.user.verifyingIdentity') }}</p>
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
