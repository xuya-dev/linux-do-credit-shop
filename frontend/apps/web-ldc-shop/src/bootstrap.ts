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
import { setAdminApp } from '#/store/admin-init';
import { useShopSettingsStore } from '#/store';

import App from './app.vue';
import { router } from './router';

async function bootstrap(namespace: string) {
  const app = createApp(App);

  app.config.errorHandler = (err, instance, info) => {
    console.error('[Vue Error]', info, err);
  };

  app.component('NDropdown', NDropdown);
  app.component('NAvatar', NAvatar);
  app.component('NSpin', NSpin);
  app.component('NPagination', NPagination);
  app.component('NSkeleton', NSkeleton);
  app.component('NModal', NModal);
  app.component('NInputNumber', NInputNumber);
  app.component('NButton', NButton);

  setAdminApp(app);

  await setupI18n(app);

  await initStores(app, { namespace });

  registerAccessDirective(app);

  app.use(router);

  const { MotionPlugin } = await import('@vben/plugins/motion');
  app.use(MotionPlugin);

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

  watchEffect(() => {
    if (preferences.app.dynamicTitle) {
      const routeTitle = router.currentRoute.value.meta?.title;
      const pageTitle =
        (routeTitle ? `${$t(routeTitle)} - ` : '') + preferences.app.name;
      useTitle(pageTitle);
    }
  });

  app.mount('#app');
}

export { bootstrap };
