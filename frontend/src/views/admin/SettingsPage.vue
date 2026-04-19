<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { settingsApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const settings = ref<Record<string, string>>({})
const loading = ref(true)

async function loadSettings() {
  try {
    const res = await settingsApi.getAll()
    settings.value = res.data || {}
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function saveSettings() {
  try {
    await settingsApi.batchUpdate(settings.value)
    message.success(t('admin.setting.saveSuccess'))
  } catch (e: any) { message.error(e.message) }
}

onMounted(loadSettings)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('admin.setting.title') }}</h2>
      <button @click="saveSettings" style="background: #3b82f6; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px;">{{ t('common.save') }}</button>
    </div>

    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>

    <div v-else style="display: grid; gap: 24px;">
      <!-- 商店信息 / Shop Info -->
      <div class="card">
        <h3 style="font-size: 14px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 16px;">{{ t('admin.setting.shopName') }}</h3>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
          <div>
            <label style="font-size: 12px; color: #6b7280;">{{ t('admin.setting.shopName') }}</label>
            <input v-model="settings['shop_name']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
          </div>
          <div>
            <label style="font-size: 12px; color: #6b7280;">{{ t('admin.setting.shopDescription') }}</label>
            <input v-model="settings['shop_description']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
          </div>
        </div>
      </div>

      <!-- LDC 配置 / LDC Config -->
      <div class="card">
        <h3 style="font-size: 14px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 16px;">{{ t('admin.setting.ldcConfig') }}</h3>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
          <div>
            <label style="font-size: 12px; color: #6b7280;">Client ID</label>
            <input v-model="settings['ldc_client_id']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px; font-family: monospace;" />
          </div>
          <div>
            <label style="font-size: 12px; color: #6b7280;">Client Secret</label>
            <input v-model="settings['ldc_client_secret']" type="password" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px; font-family: monospace;" />
          </div>
          <div>
            <label style="font-size: 12px; color: #6b7280;">Gateway URL</label>
            <input v-model="settings['ldc_gateway_url']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
          </div>
          <div>
            <label style="font-size: 12px; color: #6b7280;">Notify URL</label>
            <input v-model="settings['ldc_notify_url']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
          </div>
          <div>
            <label style="font-size: 12px; color: #6b7280;">Return URL</label>
            <input v-model="settings['ldc_return_url']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
          </div>
        </div>
      </div>

      <!-- OAuth 配置 / OAuth Config -->
      <div class="card">
        <h3 style="font-size: 14px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 16px;">{{ t('admin.setting.oauthConfig') }}</h3>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
          <div>
            <label style="font-size: 12px; color: #6b7280;">OAuth Client ID</label>
            <input v-model="settings['oauth_client_id']" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px; font-family: monospace;" />
          </div>
          <div>
            <label style="font-size: 12px; color: #6b7280;">OAuth Client Secret</label>
            <input v-model="settings['oauth_client_secret']" type="password" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px; font-family: monospace;" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
