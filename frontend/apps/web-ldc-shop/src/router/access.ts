import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
  MenuRecordRaw,
} from '@vben/types';

import { generateAccessible } from '@vben/access';
import { preferences } from '@vben/preferences';

import { message } from '#/adapter/naive';
import { getAllMenusApi } from '#/api';
import { BasicLayout, IFrameView } from '#/layouts';
import { $t } from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');

/**
 * Promote admin children to top-level menu items so they appear flat in the sidebar.
 */
function flattenAdminMenus(menus: MenuRecordRaw[]): MenuRecordRaw[] {
  const result: MenuRecordRaw[] = [];
  for (const menu of menus) {
    if (menu.path === '/admin' && menu.children && menu.children.length > 0) {
      result.push(...menu.children);
    } else {
      result.push(menu);
    }
  }
  return result;
}

async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = import.meta.glob('../views/**/*.vue');

  const layoutMap: ComponentRecordType = {
    BasicLayout,
    IFrameView,
  };

  const result = await generateAccessible(preferences.app.accessMode, {
    ...options,
    fetchMenuListAsync: async () => {
      message.loading(`${$t('common.loadingMenu')}...`, {
        duration: 1.5,
      });
      return await getAllMenusApi();
    },
    // 可以指定没有权限跳转403页面
    forbiddenComponent,
    // 如果 route.meta.menuVisibleWithForbidden = true
    layoutMap,
    pageMap,
  });

  return {
    ...result,
    accessibleMenus: flattenAdminMenus(result.accessibleMenus),
  };
}

export { generateAccess };
