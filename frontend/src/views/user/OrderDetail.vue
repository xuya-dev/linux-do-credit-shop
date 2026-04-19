<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { orderApi } from '@/api'

const { t } = useI18n()
const route = useRoute()
const message = useMessage()
const order = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await orderApi.detail(Number(route.params.id))
    order.value = res.data
  } catch (e) { message.error('加载失败') }
  finally { loading.value = false }
})

function copyCard(text: string) {
  navigator.clipboard.writeText(text)
  message.success(t('order.cardCopied'))
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>
    <div v-else-if="order">
      <div class="feature-card" style="margin-bottom: 24px;">
        <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
          <div>
            <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ order.productName }}</h2>
            <p style="font-size: 13px; color: var(--color-text-light); margin-top: 4px;">{{ order.orderNo }}</p>
          </div>
          <div style="text-align: right;">
            <span style="font-size: 28px; font-weight: 700; color: var(--color-primary);">{{ order.totalAmount }}</span>
            <span style="font-size: 14px; color: var(--color-text-light);"> 积分</span>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;">
          <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.paymentStatus') }}</span><br/><span class="badge" :class="order.paymentStatus === 1 ? 'badge-success' : order.paymentStatus === 2 ? 'badge-warning' : 'badge-error'">{{ order.paymentStatus === 0 ? t('order.pending') : order.paymentStatus === 1 ? t('order.paid') : t('order.refunded') }}</span></div>
          <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.deliveryStatus') }}</span><br/><span class="badge" :class="order.deliveryStatus === 2 ? 'badge-success' : 'badge-info'">{{ order.deliveryStatus === 0 ? t('order.pendingDelivery') : order.deliveryStatus === 1 ? t('order.delivered') : t('order.completed') }}</span></div>
          <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.quantity') }}</span><br/><span style="font-weight: 600; color: var(--color-text-dark);">{{ order.quantity }}</span></div>
        </div>
      </div>

      <!-- 卡密内容 / Card Content -->
      <div v-if="order.cardContents?.length" class="feature-card" style="margin-bottom: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 16px;">{{ t('order.cardContent') }}</h3>
        <div v-for="(card, i) in order.cardContents" :key="i" style="display: flex; align-items: center; justify-content: space-between; padding: 12px; background: var(--color-bg-secondary); border-radius: 8px; margin-bottom: 8px;">
          <code style="font-size: 14px; color: var(--color-text-dark); word-break: break-all;">{{ card }}</code>
          <button @click="copyCard(card)" style="background: var(--color-primary); color: white; border: none; padding: 4px 12px; border-radius: 6px; font-size: 12px; cursor: pointer; white-space: nowrap; margin-left: 12px;">{{ t('order.copyCard') }}</button>
        </div>
      </div>

      <!-- 其他信息 / Other Info -->
      <div class="feature-card">
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
          <div v-if="order.contactInfo"><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.contactInfo') }}</span><p style="font-size: 14px; color: var(--color-text-dark); margin-top: 4px;">{{ order.contactInfo }}</p></div>
          <div v-if="order.remark"><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.remark') }}</span><p style="font-size: 14px; color: var(--color-text-dark); margin-top: 4px;">{{ order.remark }}</p></div>
          <div v-if="order.deliveryInfo"><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.deliveryInfo') }}</span><p style="font-size: 14px; color: var(--color-text-dark); margin-top: 4px;">{{ order.deliveryInfo }}</p></div>
          <div><span style="font-size: 12px; color: var(--color-text-light);">{{ t('order.createTime') }}</span><p style="font-size: 14px; color: var(--color-text-dark); margin-top: 4px;">{{ order.createdAt }}</p></div>
        </div>
      </div>
    </div>
  </div>
</template>
