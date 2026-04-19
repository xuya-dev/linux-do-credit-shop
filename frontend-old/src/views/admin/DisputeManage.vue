<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { disputeApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const disputes = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref<number | null>(null)
const showHandle = ref(false)
const selectedDispute = ref<any>(null)
const handleForm = ref({ status: 1, adminNote: '' })

async function loadDisputes() {
  loading.value = true
  try {
    const res = await disputeApi.adminList({ page: page.value, size: 10, status: statusFilter.value ?? undefined })
    disputes.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleDispute() {
  try {
    await disputeApi.adminHandle(selectedDispute.value.id, handleForm.value)
    message.success(t('common.success'))
    showHandle.value = false
    loadDisputes()
  } catch (e: any) { message.error(e.message) }
}

onMounted(loadDisputes)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('nav.disputes') }}</h2>
      <div style="display: flex; gap: 8px;">
        <button v-for="s in [null, 0, 1, 2, 3]" :key="String(s)" @click="statusFilter = s; page = 1; loadDisputes()" :style="{ padding: '6px 12px', borderRadius: '6px', border: statusFilter === s ? '1px solid #3b82f6' : '1px solid #d1d5db', background: statusFilter === s ? '#3b82f6' : 'white', color: statusFilter === s ? 'white' : '#6b7280', cursor: 'pointer', fontSize: '13px' }">
          {{ s === null ? t('common.all') : s === 0 ? t('dispute.pending') : s === 1 ? t('dispute.accepted') : s === 2 ? t('dispute.rejected') : t('dispute.platform') }}
        </button>
      </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafc; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('dispute.orderNo') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('dispute.productName') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('dispute.reason') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('dispute.status') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in disputes" :key="d.id" style="border-bottom: 1px solid #e5e7eb;">
            <td style="padding: 12px 16px; font-size: 13px; color: #374151;">{{ d.orderNo }}</td>
            <td style="padding: 12px 16px; font-size: 14px; color: #111827;">{{ d.productName }}</td>
            <td style="padding: 12px 16px; font-size: 14px; color: #374151; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ d.reason }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="d.status === 0 ? 'badge-warning' : d.status === 1 ? 'badge-success' : d.status === 2 ? 'badge-error' : 'badge-info'">{{ d.statusName }}</span></td>
            <td style="padding: 12px 16px; text-align: right;">
              <button v-if="d.status === 0" @click="selectedDispute = d; showHandle = true; handleForm = { status: 1, adminNote: '' }" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #3b82f6;">{{ t('admin.dispute.handle') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 处理弹窗 / Handle Modal -->
    <div v-if="showHandle" style="position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100;" @click.self="showHandle = false">
      <div class="card" style="width: 480px;">
        <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">{{ t('admin.dispute.handle') }}</h3>
        <p style="font-size: 14px; color: #374151; margin-bottom: 12px;"><strong>{{ t('dispute.reason') }}:</strong> {{ selectedDispute?.reason }}</p>
        <div style="margin-bottom: 12px;">
          <label style="font-size: 12px; color: #6b7280;">{{ t('common.status') }}</label>
          <select v-model="handleForm.status" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px;">
            <option :value="1">{{ t('admin.dispute.accept') }}</option>
            <option :value="2">{{ t('admin.dispute.reject') }}</option>
            <option :value="3">{{ t('admin.dispute.platform') }}</option>
          </select>
        </div>
        <div style="margin-bottom: 16px;">
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.dispute.adminNote') }}</label>
          <textarea v-model="handleForm.adminNote" rows="3" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px;"></textarea>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <button @click="showHandle = false" style="padding: 8px 16px; border: 1px solid #d1d5db; background: none; border-radius: 6px; cursor: pointer;">{{ t('common.cancel') }}</button>
          <button @click="handleDispute" style="padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
