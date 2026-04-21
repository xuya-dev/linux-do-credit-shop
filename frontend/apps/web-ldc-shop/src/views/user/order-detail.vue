<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Order } from '#/api/types';
import {
  DELIVERY_STATUS_MAP,
  PAYMENT_STATUS_MAP,
} from '#/api/types';

import { orderApi } from '#/api/modules';
import { copyToClipboard } from '#/utils/format';
import '#/styles/faka-common.css';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const { t } = useI18n();

const order = ref<Order | null>(null);
const loading = ref(true);
const showDisputeModal = ref(false);
const disputeReason = ref('');
const creatingDispute = ref(false);

onMounted(async () => {
  try {
    order.value = await orderApi.detail(Number(route.params.id));
  } catch {
    message.error(t('page.user.loadOrderFailed'));
  } finally {
    loading.value = false;
  }
});

async function copyCard(text: string) {
  const ok = await copyToClipboard(text);
  if (ok) {
    message.success(t('page.user.copiedToClipboard'));
  } else {
    message.error(t('page.user.copyFailed'));
  }
}

function canCreateDispute() {
  return (
    order.value?.paymentStatus === 1 &&
    order.value?.deliveryStatus >= 1
  );
}

async function createDispute() {
  if (!disputeReason.value.trim()) {
    message.warning(t('page.user.enterDisputeReason'));
    return;
  }
  creatingDispute.value = true;
  try {
    const { disputeApi } = await import('#/api/modules');
    await disputeApi.create({
      orderId: order.value!.id,
      reason: disputeReason.value,
    });
    message.success(t('page.user.disputeSubmitted'));
    showDisputeModal.value = false;
    router.push('/disputes');
  } catch (e: any) {
    message.error(e.message || t('page.user.submitFailed'));
  } finally {
    creatingDispute.value = false;
  }
}

function goPay() {
  if (!order.value || order.value.paymentStatus !== 0) return;
  orderApi.pay(order.value.id).then((res) => {
    if (res?.payUrl) {
      window.location.href = res.payUrl;
    }
  }).catch((err) => {
    message.error(err.message || t('page.user.payInitFailed'));
  });
}
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/orders')">
      <span>{{ t('page.user.myOrders') }}</span> &gt; <span>{{ t('page.user.orderDetail') }}</span>
    </div>

    <div v-if="loading" class="loading-spin">
      <n-spin size="large" />
    </div>

    <template v-else-if="order">
      <!-- 提取结果（发卡核心模块） -->
      <div v-if="order.paymentStatus === 1" class="faka-card highlight-card mb-24">
        <div class="card-header highlight">
          📝 {{ t('page.user.orderPaidExtractCard') }}
          <div class="header-tags">
            <span class="dlv-badge">{{ t('page.user.status') }}{{ t(DELIVERY_STATUS_MAP[order.deliveryStatus]?.i18nKey || '') }}</span>
          </div>
        </div>
        <div class="card-body">
          <div v-if="order.cardContents?.length" class="card-codes">
            <div v-for="(card, i) in order.cardContents" :key="i" class="code-box">
              <span class="code-text">{{ card }}</span>
              <button class="faka-btn plain sm" @click="copyCard(card)">{{ t('page.user.copyCard') }}</button>
            </div>
            <div class="alert-tip mt-12">
              💡 {{ t('page.user.cardTip') }}
            </div>
          </div>
          <div v-else class="empty-code">
            <div v-if="order.deliveryStatus === 0">
              {{ t('page.user.orderProcessing') }}
            </div>
            <div v-else>
              {{ t('page.user.noCardInfo') }}
            </div>
          </div>
        </div>
      </div>

      <!-- 订单基础信息 -->
      <div class="faka-card">
        <div class="card-header">
          {{ t('page.user.orderInfo') }}
          <span :class="['status-tag-sm', 'status-' + order.paymentStatus]" style="margin-left:8px;">
            {{ t(PAYMENT_STATUS_MAP[order.paymentStatus]?.i18nKey || '') }}
          </span>
        </div>
        <div class="card-body order-info-grid">
          <div class="info-item">
            <label>{{ t('page.user.orderNumber') }}</label>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <label>{{ t('page.user.productName') }}</label>
            <span>{{ order.productName }}</span>
          </div>
          <div class="info-item">
            <label>{{ t('page.user.unitPrice') }}</label>
            <span>{{ order.unitPrice }} {{ t('page.user.credits') }}</span>
          </div>
          <div class="info-item">
            <label>{{ t('page.user.purchaseQuantity') }}</label>
            <span>{{ order.quantity }}</span>
          </div>
          <div class="info-item">
            <label>{{ t('page.user.paidAmount') }}</label>
            <span class="highlight-price">{{ order.totalAmount }} {{ t('page.user.credits') }}</span>
          </div>
          <div class="info-item">
            <label>{{ t('page.user.orderTime') }}</label>
            <span>{{ order.createdAt }}</span>
          </div>
          <div class="info-item" v-if="order.contactInfo">
            <label>{{ t('page.user.reservedContact') }}</label>
            <span>{{ order.contactInfo }}</span>
          </div>
          <div class="info-item" v-if="order.remark">
            <label>{{ t('page.user.orderRemark') }}</label>
            <span>{{ order.remark }}</span>
          </div>
          <div class="info-item" v-if="order.deliveryInfo">
            <label>{{ t('page.user.deliveryDetail') }}</label>
            <span>{{ order.deliveryInfo }}</span>
          </div>
        </div>
        <div class="card-footer actions">
          <button v-if="order.paymentStatus === 0" class="faka-btn primary" @click="goPay">
            {{ t('page.user.payNow', { amount: order.totalAmount }) }}
          </button>

          <button v-if="canCreateDispute()" class="faka-btn warning outline" @click="showDisputeModal = true">
            {{ t('page.user.fileDispute') }}
          </button>
        </div>
      </div>

    </template>

    <div v-else class="empty-state">
      {{ t('page.user.orderNotFound') }}
    </div>

    <!-- 发起争议弹窗 -->
    <n-modal v-model:show="showDisputeModal" preset="card" :title="t('page.user.applyAfterSale')" style="max-width: 440px" class="faka-modal">
      <div class="modal-body">
        <label class="modal-label">{{ t('page.user.disputeReason') }}</label>
        <textarea
          v-model="disputeReason"
          class="faka-textarea w-full"
          :placeholder="t('page.user.disputeReasonPlaceholder')"
          rows="4"
        ></textarea>
      </div>
      <template #action>
        <div class="modal-actions">
          <button class="faka-btn plain" @click="showDisputeModal = false">{{ t('page.user.cancel') }}</button>
          <button class="faka-btn warning" @click="createDispute" :disabled="creatingDispute">
            {{ creatingDispute ? t('page.user.submitting') : t('page.user.submitAfterSale') }}
          </button>
        </div>
      </template>
    </n-modal>

  </div>
</template>

<style scoped>
.faka-container {
  max-width: 1000px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.mb-24 { margin-bottom: 24px; }
.mt-12 { margin-top: 12px; }

.highlight-card {
  border: 1px solid var(--faka-highlight-border, #91d5ff);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header.highlight {
  background: var(--faka-highlight-bg, #e6f7ff);
  border-bottom-color: var(--faka-highlight-border, #91d5ff);
  color: var(--faka-highlight-text, #096dd9);
}

.dlv-badge {
  font-size: 12px;
  background: var(--faka-highlight-badge-bg, rgba(24, 144, 255, 0.1));
  padding: 2px 8px;
  border-radius: 2px;
  color: var(--faka-highlight-text, #096dd9);
}

.card-body {
  padding: 20px;
}
.card-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--faka-border, #f0f0f0);
  background: var(--faka-tag-bg, #fafafa);
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

/* 提取卡密区 */
.card-codes {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.code-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--faka-tag-bg, #fafafa);
  border: 1px dashed var(--faka-border, #d9d9d9);
  padding: 12px 16px;
  border-radius: 2px;
}
.code-text {
  font-family: monospace;
  font-size: 15px;
  color: var(--faka-text-main, #333);
  word-break: break-all;
}
.empty-code {
  color: var(--faka-text-sub, #8c8c8c);
  font-size: 14px;
}
.alert-tip {
  font-size: 12px;
  color: var(--faka-warning-text, #fa8c16);
  background: var(--faka-warning-bg, #fff7e6);
  padding: 8px 12px;
  border-radius: 2px;
  border: 1px solid var(--faka-warning-border, #ffd591);
}

/* 订单明细 */
.order-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.info-item {
  display: flex;
  font-size: 14px;
}
.info-item label {
  color: var(--faka-text-sub, #8c8c8c);
  width: 80px;
  flex-shrink: 0;
}
.info-item span {
  color: var(--faka-text-main, #333);
  word-break: break-all;
}
.highlight-price {
  color: #f5222d !important;
  font-weight: 700;
}

.status-tag-sm {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
}

/* Modal Elements */
.modal-label {
  display: block;
  font-size: 13px;
  color: var(--faka-text-main, #333);
  margin-bottom: 8px;
}
.w-full { width: 100%; }
.faka-textarea {
  border: 1px solid var(--faka-border, #d9d9d9);
  border-radius: 2px;
  padding: 8px 12px;
  background: var(--faka-bg-header, #fff);
  color: var(--faka-text-main, #333);
  outline: none;
  font-size: 13px;
  resize: vertical;
}
.faka-textarea:focus { border-color: #faad14; }

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.loading-spin { padding: 80px; text-align: center; }
.empty-state { padding: 80px; text-align: center; }

@media (max-width: 768px) {
  .order-info-grid { grid-template-columns: 1fr; }
  .card-footer { flex-direction: column; }
}
</style>
