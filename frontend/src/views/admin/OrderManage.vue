<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { orderApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const orders = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref<number | null>(null)
const showDeliver = ref(false)
const selectedOrder = ref<any>(null)
const deliverForm = ref({ deliveryInfo: '', adminRemark: '' })

async function loadOrders() {
  loading.value = true
  try {
    const res = await orderApi.adminList({ page: page.value, size: 10, keyword: keyword.value || undefined, paymentStatus: statusFilter.value ?? undefined })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleDeliver() {
  try {
    await orderApi.adminDeliver(selectedOrder.value.id, deliverForm.value)
    message.success(t('common.success'))
    showDeliver.value = false
    loadOrders()
  } catch (e: any) { message.error(e.message) }
}

async function handleRefund(id: number) {
  if (!confirm(t('admin.order.refundWarning'))) return
  try {
    await orderApi.adminRefund(id)
    message.success(t('common.success'))
    loadOrders()
  } catch (e: any) { message.error(e.message) }
}

onMounted(loadOrders)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('nav.orders') }}</h2>
      <div style="display: flex; gap: 8px;">
        <input v-model="keyword" @keyup.enter="page = 1; loadOrders()" :placeholder="t('common.search')" style="padding: 6px 12px; border: 1px solid #d1d5db; border-radius: 6px; width: 200px; font-size: 14px;" />
        <button v-for="s in [null, 0, 1, 2]" :key="String(s)" @click="statusFilter = s; page = 1; loadOrders()" :style="{ padding: '6px 12px', borderRadius: '6px', border: statusFilter === s ? '1px solid #3b82f6' : '1px solid #d1d5db', background: statusFilter === s ? '#3b82f6' : 'white', color: statusFilter === s ? 'white' : '#6b7280', cursor: 'pointer', fontSize: '13px' }">
          {{ s === null ? t('common.all') : s === 0 ? t('order.pending') : s === 1 ? t('order.paid') : t('order.refunded') }}
        </button>
      </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafc; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('order.orderNo') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('order.productName') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('order.amount') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('order.paymentStatus') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('order.deliveryStatus') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id" style="border-bottom: 1px solid #e5e7eb;">
            <td style="padding: 12px 16px; font-size: 13px; font-family: monospace; color: #374151;">{{ o.orderNo }}</td>
            <td style="padding: 12px 16px; font-size: 14px; color: #111827;">{{ o.productName }}</td>
            <td style="padding: 12px 16px; font-size: 14px; font-weight: 600; color: #2563eb;">{{ o.totalAmount }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="o.paymentStatus === 1 ? 'badge-success' : o.paymentStatus === 2 ? 'badge-warning' : 'badge-error'">{{ o.paymentStatus === 0 ? t('order.pending') : o.paymentStatus === 1 ? t('order.paid') : t('order.refunded') }}</span></td>
            <td style="padding: 12px 16px;"><span class="badge" :class="o.deliveryStatus === 2 ? 'badge-success' : 'badge-info'">{{ o.deliveryStatus === 0 ? t('order.pendingDelivery') : o.deliveryStatus === 1 ? t('order.delivered') : t('order.completed') }}</span></td>
            <td style="padding: 12px 16px; text-align: right; display: flex; gap: 4px; justify-content: flex-end;">
              <button v-if="o.paymentStatus === 1 && o.productType === 2 && o.deliveryStatus === 0" @click="selectedOrder = o; showDeliver = true" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #3b82f6;">{{ t('admin.order.deliver') }}</button>
              <button v-if="o.paymentStatus === 1" @click="handleRefund(o.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #dc2626;">{{ t('admin.order.refund') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 发货弹窗 / Deliver Modal -->
    <div v-if="showDeliver" style="position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100;" @click.self="showDeliver = false">
      <div class="card" style="width: 480px;">
        <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">{{ t('admin.order.confirmDeliver') }}</h3>
        <div style="margin-bottom: 12px;">
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.order.deliveryInfo') }}</label>
          <input v-model="deliverForm.deliveryInfo" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
        </div>
        <div style="margin-bottom: 16px;">
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.order.adminRemark') }}</label>
          <textarea v-model="deliverForm.adminRemark" rows="3" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;"></textarea>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <button @click="showDeliver = false" style="padding: 8px 16px; border: 1px solid #d1d5db; background: none; border-radius: 6px; cursor: pointer;">{{ t('common.cancel') }}</button>
          <button @click="handleDeliver" style="padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
