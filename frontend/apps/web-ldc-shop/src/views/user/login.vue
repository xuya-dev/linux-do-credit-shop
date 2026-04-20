<script setup lang="ts">
import { useMessage } from 'naive-ui';

import { getAuthorizeUrlApi } from '#/api';

const message = useMessage();

async function handleLogin() {
  try {
    const url = await getAuthorizeUrlApi(
      window.location.origin + '/auth/callback',
    );
    if (url) {
      window.location.href = url;
    }
  } catch {
    message.error('获取授权链接失败，请稍后重试');
  }
}
</script>

<template>
  <div class="login-page">
    <n-card class="login-card" :bordered="false">
      <div class="login-brand">
        <div class="brand-logo">🚀</div>
        <h1 class="brand-title">LDC Shop</h1>
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

      <n-button
        type="primary"
        size="large"
        block
        class="login-btn"
        @click="handleLogin"
      >
        <span class="btn-content">
          <span>使用 LINUX DO 账号登录</span>
        </span>
      </n-button>

      <p class="login-hint">
        点击登录即表示您同意我们的服务条款
      </p>
    </n-card>
  </div>
</template>

<style scoped>
.login-page {
  width: 100%;
}

.login-card {
  background: transparent;
  text-align: center;
}

.login-brand {
  margin-bottom: 40px;
}

.brand-logo {
  font-size: 64px;
  line-height: 1;
  margin-bottom: 16px;
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
  border-radius: 12px;
  background: rgba(128, 128, 128, 0.06);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.8;
}

.feature-icon {
  font-size: 20px;
  line-height: 1;
}

.login-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
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
