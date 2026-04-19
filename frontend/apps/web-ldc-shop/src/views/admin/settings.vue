<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { useMessage } from 'naive-ui';

import { settingsApi } from '#/api/modules';

const message = useMessage();
const loading = ref(true);
const saving = ref(false);

const shopInfo = reactive<Record<string, string>>({});
const paymentConfig = reactive<Record<string, string>>({});
const oauthConfig = reactive<Record<string, string>>({});

const shopInfoFields = [
  { key: 'shop_name', label: '店铺名称' },
  { key: 'shop_description', label: '店铺描述' },
  { key: 'shop_logo', label: '店铺Logo URL' },
  { key: 'shop_notice', label: '店铺公告' },
];

const paymentFields = [
  { key: 'payment_method', label: '支付方式' },
  { key: 'payment_api_key', label: '支付API Key' },
  { key: 'payment_api_url', label: '支付API地址' },
  { key: 'payment_notify_url', label: '回调地址' },
];

const oauthFields = [
  { key: 'oauth_client_id', label: 'Client ID' },
  { key: 'oauth_client_secret', label: 'Client Secret' },
  { key: 'oauth_redirect_uri', label: '回调地址' },
  { key: 'oauth_authorize_url', label: '授权地址' },
  { key: 'oauth_token_url', label: 'Token地址' },
];

function applySettings(data: Record<string, string>) {
  for (const [key, value] of Object.entries(data)) {
    if (key in shopInfo) shopInfo[key] = value;
    else if (key in paymentConfig) paymentConfig[key] = value;
    else if (key in oauthConfig) oauthConfig[key] = value;
  }
}

async function loadSettings() {
  loading.value = true;
  try {
    const data = await settingsApi.getAll();
    const settings: Record<string, string> = data || {};

    shopInfoFields.forEach((f) => (shopInfo[f.key] = settings[f.key] || ''));
    paymentFields.forEach((f) => (paymentConfig[f.key] = settings[f.key] || ''));
    oauthFields.forEach((f) => (oauthConfig[f.key] = settings[f.key] || ''));

    const knownKeys = new Set([
      ...shopInfoFields.map((f) => f.key),
      ...paymentFields.map((f) => f.key),
      ...oauthFields.map((f) => f.key),
    ]);

    for (const [key, value] of Object.entries(settings)) {
      if (!knownKeys.has(key)) {
        shopInfo[key] = value;
      }
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  saving.value = true;
  try {
    const data = {
      ...shopInfo,
      ...paymentConfig,
      ...oauthConfig,
    };
    await settingsApi.batchUpdate(data);
    message.success('保存成功');
  } catch (e: any) {
    message.error(e.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(loadSettings);
</script>

<template>
  <div>
    <n-spin :show="loading">
      <n-space vertical :size="24">
        <n-card title="店铺信息">
          <n-form label-placement="left" label-width="120">
            <n-form-item v-for="field in shopInfoFields" :key="field.key" :label="field.label">
              <n-input v-model:value="shopInfo[field.key]" :placeholder="field.label" />
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="支付配置">
          <n-form label-placement="left" label-width="120">
            <n-form-item v-for="field in paymentFields" :key="field.key" :label="field.label">
              <n-input
                v-model:value="paymentConfig[field.key]"
                :placeholder="field.label"
                :type="field.key.includes('secret') || field.key.includes('key') ? 'password' : 'text'"
                show-password-on="click"
              />
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="OAuth 配置">
          <n-form label-placement="left" label-width="120">
            <n-form-item v-for="field in oauthFields" :key="field.key" :label="field.label">
              <n-input
                v-model:value="oauthConfig[field.key]"
                :placeholder="field.label"
                :type="field.key.includes('secret') ? 'password' : 'text'"
                show-password-on="click"
              />
            </n-form-item>
          </n-form>
        </n-card>

        <div style="text-align: center">
          <n-button type="primary" :loading="saving" @click="handleSave">保存设置</n-button>
        </div>
      </n-space>
    </n-spin>
  </div>
</template>
