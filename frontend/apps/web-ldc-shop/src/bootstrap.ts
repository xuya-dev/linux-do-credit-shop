import { createApp, watchEffect } from 'vue';

import { registerAccessDirective } from '@vben/access';
import { preferences, updatePreferences } from '@vben/preferences';
import { initStores } from '@vben/stores';
import '@vben/styles';
import '@vben/styles/naive';
import {
  NDropdown,
  NAvatar,
  NSpin,
  NPagination,
  NSkeleton,
  NModal,
  NInputNumber,
  NButton
} from 'naive-ui';

import { useTitle } from '@vueuse/core';

import { $t, setupI18n } from '#/locales';
import { useShopSettingsStore } from '#/store';

import App from './app.vue';
import { router } from './router';

async function bootstrap(namespace: string) {
  const app = createApp(App);

  app.config.errorHandler = (err, instance, info) => {
    console.error('[Vue Error]', info, err);
  };

  // 注册前台用户端必须的轻量级 Naive UI 组件
  app.component('NDropdown', NDropdown);
  app.component('NAvatar', NAvatar);
  app.component('NSpin', NSpin);
  app.component('NPagination', NPagination);
  app.component('NSkeleton', NSkeleton);
  app.component('NModal', NModal);
  app.component('NInputNumber', NInputNumber);
  app.component('NButton', NButton);

  // 国际化 i18n 配置
  await setupI18n(app);

  // 配置 pinia-store
  await initStores(app, { namespace });

  // 安装权限指令
  registerAccessDirective(app);

  // 配置路由及路由守卫
  app.use(router);

  // 配置Motion插件
  const { MotionPlugin } = await import('@vben/plugins/motion');
  app.use(MotionPlugin);

  // 获取公共设置并更新应用标题和 logo
  const shopSettingsStore = useShopSettingsStore();
  await shopSettingsStore.fetchPublicSettings();
  const updates: Record<string, any> = {};
  if (shopSettingsStore.shopName) {
    updates.app = { name: shopSettingsStore.shopName };
  }
  if (shopSettingsStore.shopLogo) {
    updates.logo = { source: shopSettingsStore.shopLogo };
  }
  if (Object.keys(updates).length > 0) {
    updatePreferences(updates);
  }

  // 动态更新标题
  watchEffect(() => {
    if (preferences.app.dynamicTitle) {
      const routeTitle = router.currentRoute.value.meta?.title;
      const pageTitle =
        (routeTitle ? `${$t(routeTitle)} - ` : '') + preferences.app.name;
      useTitle(pageTitle);
    }
  });

  // ========== 按需懒加载重量级后台组件 ==========
  let adminInitialized = false;
  router.beforeEach(async (to) => {
    if (to.path.startsWith('/admin') && !adminInitialized) {
      const [
        { initComponentAdapter },
        { initSetupVbenForm },
        { registerLoadingDirective },
        { initTippy },
        naiveUI,
      ] = await Promise.all([
        import('./adapter/component'),
        import('./adapter/form'),
        import('@vben/common-ui'),
        import('@vben/common-ui/es/tippy'),
        import('naive-ui'),
      ]);

      // 注册管理端所需的 Naive UI 组件
      // 必须从 naive-ui 统一导入，确保注入上下文与 NConfigProvider 一致
      app.component('NGrid', naiveUI.NGrid);
      app.component('NGi', naiveUI.NGi);
      app.component('NCard', naiveUI.NCard);
      app.component('NDataTable', naiveUI.NDataTable);
      app.component('NForm', naiveUI.NForm);
      app.component('NFormItem', naiveUI.NFormItem);
      app.component('NTag', naiveUI.NTag);
      app.component('NText', naiveUI.NText);
      app.component('NSwitch', naiveUI.NSwitch);
      app.component('NSelect', naiveUI.NSelect);
      app.component('NSpace', naiveUI.NSpace);
      app.component('NInput', naiveUI.NInput);

      await initComponentAdapter();
      await initSetupVbenForm();

      registerLoadingDirective(app, {
        loading: 'loading',
        spinning: 'spinning',
      });
      initTippy(app);

      adminInitialized = true;
    }
    return true;
  });
  // ===========================================

  app.mount('#app');
}

export { bootstrap };
