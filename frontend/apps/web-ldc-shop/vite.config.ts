import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            target: 'http://localhost:5320/api',
            ws: true,
          },
        },
      },
      build: {
        rolldownOptions: {
          output: {
            manualChunks(id: string) {
              // 将 naive-ui 单独分包
              if (id.includes('naive-ui')) {
                return 'naive-ui';
              }
              // 将 vben 核心库分包
              if (id.includes('@vben/')) {
                return 'vben-core';
              }
              // 将 vue 生态分包
              if (id.includes('node_modules/vue') || id.includes('node_modules/@vue')) {
                return 'vue-vendor';
              }
            },
          },
        },
      },
    },
  };
});

