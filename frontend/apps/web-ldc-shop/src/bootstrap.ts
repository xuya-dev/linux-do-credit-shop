import { createApp, watchEffect } from 'vue';

import { registerAccessDirective } from '@vben/access';
import { preferences } from '@vben/preferences';
import { initStores } from '@vben/stores';
import '@vben/styles';
import '@vben/styles/naive';

import { useTitle } from '@vueuse/core';

import { $t, setupI18n } from '#/locales';

import App from './app.vue';
import { router } from './router';

async function bootstrap(namespace: string) {

  // // 设置弹窗的默认配置
  // setDefaultModalProps({
  //   fullscreenButton: false,
  // });
  // // 设置抽屉的默认配置
  // setDefaultDrawerProps({
  //   // zIndex: 2000,
  // });

  const app = createApp(App);

  // 移除全局同步的 v-loading 注册（按需加载）

  // 国际化 i18n 配置
  await setupI18n(app);

  // 配置 pinia-tore
  await initStores(app, { namespace });

  // 安装权限指令
  registerAccessDirective(app);

  // 移除全局同步的 tippy 初始化（按需加载）

  // 配置路由及路由守卫
  app.use(router);

  // 配置Motion插件
  const { MotionPlugin } = await import('@vben/plugins/motion');
  app.use(MotionPlugin);

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
      // 动态引入只在后台用到的组件和适配器
      const [
        { initComponentAdapter },
        { initSetupVbenForm },
        { registerLoadingDirective },
        { initTippy }
      ] = await Promise.all([
        import('./adapter/component'),
        import('./adapter/form'),
        import('@vben/common-ui'),
        import('@vben/common-ui/es/tippy')
      ]);

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
