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
    message.error('Failed to get authorize URL');
  }
}
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
    <div style="text-align: center; max-width: 400px">
      <h1
        style="
          font-size: 42px;
          font-weight: 700;
          margin-bottom: 12px;
          letter-spacing: -0.03em;
        "
      >
        LDC Shop
      </h1>
      <p style="font-size: 16px; opacity: 0.6; margin-bottom: 40px">
        LINUX DO Credit Shop
      </p>
      <n-button
        type="primary"
        size="large"
        style="padding: 14px 48px; font-size: 16px"
        @click="handleLogin"
      >
        Login with LINUX DO
      </n-button>
    </div>
  </div>
</template>
