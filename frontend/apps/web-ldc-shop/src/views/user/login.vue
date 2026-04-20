<script setup lang="ts">
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';
import { onMounted, ref } from 'vue';

import { getAuthorizeUrlApi } from '#/api';
import { settingsApi } from '#/api/modules/settings';

const { t } = useI18n();
const message = useMessage();

const siteName = ref('LDC Shop');
const siteLogo = ref('/logo.png');

onMounted(async () => {
  try {
    const res = await settingsApi.getPublic();
    if (res && res.site_name) siteName.value = res.site_name;
    if (res && res.site_logo) siteLogo.value = res.site_logo;
  } catch {
    // Fallback to defaults
  }
});

async function handleLogin() {
  try {
    const url = await getAuthorizeUrlApi(
      window.location.origin + '/auth/callback',
    );
    if (url) {
      window.location.href = url;
    }
  } catch {
    message.error(t('page.shop.failedToGetAuthorizeUrl'));
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-logo-img">
          <img :src="siteLogo" alt="Logo" />
        </div>
        <h1 class="brand-title">{{ siteName }}</h1>
        <p class="brand-subtitle">LINUX DO Credit Shop</p>
      </div>

      <div class="login-features">
        <div class="feature-item">
          <span class="feature-icon">🛡️</span>
          <span>安全可靠的积分交易</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">⚡</span>
          <span>自动发货，即时到账</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">🔒</span>
          <span>LINUX DO 账号一键登录</span>
        </div>
      </div>

      <button
        class="login-btn"
        @click="handleLogin"
      >
        <span class="btn-content">
          <span>{{ t('page.shop.loginWithLinuxDo') }}</span>
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  width: 100%;
}

.login-card {
  text-align: center;
  color: var(--faka-text-main, #333333);
  padding: 16px;
}

.login-brand {
  margin-bottom: 32px;
}

.brand-logo-img {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.brand-logo-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: -0.03em;
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.6;
  margin: 0;
}

.login-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
  padding: 20px;
  border-radius: 8px;
  background: var(--faka-tag-bg, rgba(0,0,0,0.02));
  border: 1px solid var(--faka-border, rgba(0,0,0,0.06));
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--faka-text-sub, #595959);
}

.feature-icon {
  font-size: 20px;
  line-height: 1;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  border: 1px solid #1890ff;
  background-color: #1890ff;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-hint {
  margin-top: 16px;
  font-size: 12px;
  opacity: 0.4;
}
</style>
