<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { settingsApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const loading = ref(true);
const saving = ref(false);

const shopInfo = reactive<Record<string, string>>({});
const paymentConfig = reactive<Record<string, string>>({});
const oauthConfig = reactive<Record<string, string>>({});

const shopInfoFields = computed(() => [
  { key: 'shop_name', label: t('page.admin.shopName') },
  { key: 'shop_description', label: t('page.admin.shopDescription') },
  { key: 'shop_logo', label: t('page.admin.shopLogo') },
  { key: 'shop_notice', label: t('page.admin.shopNotice') },
]);

const paymentFields = computed(() => [
  { key: 'ldc_payment_client_id', label: t('page.admin.ldcClientId') },
  { key: 'ldc_payment_client_secret', label: t('page.admin.ldcClientSecret') },
  { key: 'ldc_payment_private_key', label: t('page.admin.ldcPrivateKey') },
  { key: 'ldc_payment_public_key', label: t('page.admin.ldcPublicKey') },
  { key: 'ldc_payment_gateway_url', label: t('page.admin.ldcGatewayUrl') },
  { key: 'ldc_payment_notify_url', label: t('page.admin.ldcNotifyUrl') },
  { key: 'ldc_payment_return_url', label: t('page.admin.ldcReturnUrl') },
]);

const oauthFields = computed(() => [
  { key: 'ldc_oauth_client_id', label: t('page.admin.clientId') },
  { key: 'ldc_oauth_client_secret', label: t('page.admin.clientSecret') },
  { key: 'ldc_oauth_redirect_uri', label: t('page.admin.redirectUri') },
  { key: 'ldc_oauth_authorize_url', label: t('page.admin.authorizeUrl') },
  { key: 'ldc_oauth_token_url', label: t('page.admin.tokenUrl') },
]);

async function loadSettings() {
  loading.value = true;
  try {
    const data = await settingsApi.getAll();
    const settings: Record<string, string> = data || {};

    shopInfoFields.value.forEach((f) => (shopInfo[f.key] = settings[f.key] || ''));
    paymentFields.value.forEach((f) => (paymentConfig[f.key] = settings[f.key] || ''));
    oauthFields.value.forEach((f) => (oauthConfig[f.key] = settings[f.key] || ''));
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.admin.operationFailed'));
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
    message.success(t('page.admin.saveSuccess'));
  } catch (e: any) {
    message.error(e.message || t('page.admin.saveFailed'));
  } finally {
    saving.value = false;
  }
}

onMounted(loadSettings);
</script>

<template>
  <div class="p-5">
    <n-spin :show="loading">
      <n-space vertical :size="24">
        <n-card :title="t('page.admin.shopInfo')">
          <n-form label-placement="left" label-width="120">
            <n-form-item v-for="field in shopInfoFields" :key="field.key" :label="field.label">
              <n-input v-model:value="shopInfo[field.key]" :placeholder="field.label" />
            </n-form-item>
          </n-form>
        </n-card>

        <n-card :title="t('page.admin.paymentConfig')">
          <n-form label-placement="left" label-width="120">
            <n-form-item v-for="field in paymentFields" :key="field.key" :label="field.label">
              <n-input
                v-model:value="paymentConfig[field.key]"
                :placeholder="field.label"
                :type="field.key.includes('secret') ? 'password' : 'text'"
                show-password-on="click"
              />
            </n-form-item>
          </n-form>
        </n-card>

        <n-card :title="t('page.admin.oauthConfig')">
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
          <n-button type="primary" :loading="saving" @click="handleSave">{{ t('page.admin.saveSettings') }}</n-button>
        </div>
      </n-space>
    </n-spin>
  </div>
</template>
