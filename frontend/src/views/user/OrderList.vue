<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { orderApi } from '@/api'

const { t } = useI18n()

const orders = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref<number | null>(null)

async function loadOrders() {
  loading.value = true
  try {
    const res = await orderApi.userList({ page: page.value, size: 10, paymentStatus: statusFilter.value ?? undefined })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadOrders)

function getPaymentBadge(status: number) {
  if (status === 1) return 'badge-success'
  if (status === 2) return 'badge-warning'
  return 'badge-error'
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <h1 style="font-size: 28px; font-weight: 700; color: var(--color-text-dark);">{{ t('order.title') }}</h1>
      <div style="display: flex; gap: 8px;">
        <button v-for="s in [null, 0, 1, 2]" :key="String(s)" @click="statusFilter = s; page = 1; loadOrders()" :style="{ padding: '6px 14px', borderRadius: '6px', border: statusFilter === s ? '1px solid var(--color-primary)' : '1px solid var(--color-border)', background: statusFilter === s ? 'var(--color-primary)' : 'transparent', color: statusFilter === s ? 'white' : 'var(--color-text-mid)', cursor: 'pointer', fontSize: '13px' }">
          {{ s === null ? t('common.all') : s === 0 ? t('order.pending') : s === 1 ? t('order.paid') : t('order.refunded') }}
        </button>
      </div>
    </div>

    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>

    <div v-else>
      <div v-for="order in orders" :key="order.id" class="feature-card" style="margin-bottom: 12px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <span style="font-size: 13px; color: var(--color-text-light);">{{ order.orderNo }}</span>
            <h3 style="font-size: 16px; font-weight: 600; color: var(--color-text-dark); margin-top: 4px;">{{ order.productName }}</h3>
          </div>
          <div style="text-align: right;">
            <span style="font-size: 18px; font-weight: 700; color: var(--color-primary);">{{ order.totalAmount }} <small style="font-size: 12px;">积分</small></span>
          </div>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--color-border);">
          <div style="display: flex; gap: 8px; align-items: center;">
            <span class="badge" :class="getPaymentBadge(order.paymentStatus)">{{ order.paymentStatus === 0 ? t('order.pending') : order.paymentStatus === 1 ? t('order.paid') : t('order.refunded') }}</span>
            <span v-if="order.paymentStatus === 1" class="badge" :class="order.deliveryStatus === 2 ? 'badge-success' : 'badge-info'">{{ order.deliveryStatus === 0 ? t('order.pendingDelivery') : order.deliveryStatus === 1 ? t('order.delivered') : t('order.completed') }}</span>
          </div>
          <div style="display: flex; gap: 8px; align-items: center;">
            <span style="font-size: 13px; color: var(--color-text-light);">{{ order.createdAt }}</span>
            <a @click.prevent="$router.push('/order/' + order.id)" style="font-size: 13px; font-weight: 500;">{{ t('common.more') }} →</a>
          </div>
        </div>
      </div>
      <div v-if="!orders.length" style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.noData') }}</div>
    </div>
  </div>
</template>
