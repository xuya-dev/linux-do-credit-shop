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
              if (id.includes('node_modules/echarts') || id.includes('node_modules/zrender')) {
                return 'echarts';
              }
              if (id.includes('node_modules/naive-ui')) {
                return 'naive-ui';
              }
              if (id.includes('node_modules/@vben/')) {
                return 'vben-core';
              }
              if (id.includes('node_modules/vue/') || id.includes('node_modules/@vue/')) {
                return 'vue-vendor';
              }
              if (id.includes('node_modules/@vueuse/')) {
                return 'vueuse';
              }
              if (id.includes('node_modules/dayjs/')) {
                return 'dayjs';
              }
            },
          },
        },
      },
    },
  };
});
