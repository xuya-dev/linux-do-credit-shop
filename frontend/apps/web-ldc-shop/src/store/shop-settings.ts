import { ref } from 'vue';

import { defineStore } from 'pinia';

import { settingsApi } from '#/api/modules';

export const useShopSettingsStore = defineStore('shopSettings', () => {
  const shopName = ref('');
  const shopLogo = ref('');
  const shopDescription = ref('');

  async function fetchPublicSettings() {
    try {
      const data = await settingsApi.getPublic();
      if (data) {
        shopName.value = data['shop_name'] || '';
        shopLogo.value = data['shop_logo'] || '';
        shopDescription.value = data['shop_description'] || '';
      }
    } catch {
      // 静默失败，使用默认值
    }
  }

  return { fetchPublicSettings, shopDescription, shopLogo, shopName };
});
