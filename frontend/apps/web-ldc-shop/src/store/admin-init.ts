import type { App } from 'vue';

let app: App | null = null;
let adminInitialized = false;

export function setAdminApp(appInstance: App) {
  app = appInstance;
}

export async function ensureAdminComponents() {
  if (adminInitialized || !app) return;

  const [
    { initComponentAdapter },
    { initSetupVbenForm },
    { registerLoadingDirective },
    { initTippy },
    naiveUI,
  ] = await Promise.all([
    import('#/adapter/component'),
    import('#/adapter/form'),
    import('@vben/common-ui'),
    import('@vben/common-ui/es/tippy'),
    import('naive-ui'),
  ]);

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
