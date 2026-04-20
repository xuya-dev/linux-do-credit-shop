<script lang="ts" setup>
import { RouterView } from 'vue-router';
</script>

<template>
  <div class="auth-layout">
    <div class="auth-background">
      <div class="auth-grid"></div>
      <div class="auth-glow"></div>
    </div>
    <div class="auth-content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.auth-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(128, 128, 128, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(128, 128, 128, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
}

.auth-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(24, 160, 88, 0.08) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.auth-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 0 20px;
}
</style>
