const BasicLayout = () => import('./basic.vue');
const AuthPageLayout = () => import('./auth.vue');
const ShopLayout = () => import('./shop-layout.vue');

const IFrameView = () => import('@vben/layouts').then((m) => m.IFrameView);

export { AuthPageLayout, BasicLayout, IFrameView, ShopLayout };
