<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';

import type { Order } from '#/api/types';
import {
  DELIVERY_STATUS_MAP,
  PAYMENT_STATUS_MAP,
} from '#/api/types';

import { orderApi } from '#/api/modules';
import { copyToClipboard } from '#/utils/format';

const route = useRoute();
const router = useRouter();
const message = useMessage();

const order = ref<Order | null>(null);
const loading = ref(true);
const showDisputeModal = ref(false);
const disputeReason = ref('');
const creatingDispute = ref(false);

onMounted(async () => {
  try {
    order.value = await orderApi.detail(Number(route.params.id));
  } catch {
    message.error('加载订单失败');
  } finally {
    loading.value = false;
  }
});

async function copyCard(text: string) {
  const ok = await copyToClipboard(text);
  if (ok) {
    message.success('已复制到剪贴板');
  } else {
    message.error('复制失败');
  }
}

const statusSteps = computed(() => {
  if (!order.value) return [];
  const steps = [
    { label: '创建订单', done: true },
    { label: '支付完成', done: order.value.paymentStatus >= 1 },
    { label: '已发货', done: order.value.deliveryStatus >= 1 },
    { label: '已完成', done: order.value.deliveryStatus >= 2 },
  ];
  return steps;
});

function canCreateDispute() {
  return (
    order.value?.paymentStatus === 1 &&
    order.value?.deliveryStatus >= 1
  );
}

async function createDispute() {
  if (!disputeReason.value.trim()) {
    message.warning('请输入争议原因');
    return;
  }
  creatingDispute.value = true;
  try {
    const { disputeApi } = await import('#/api/modules');
    await disputeApi.create({
      orderId: order.value!.id,
      reason: disputeReason.value,
    });
    message.success('争议提交成功');
    showDisputeModal.value = false;
    router.push('/disputes');
  } catch (e: any) {
    message.error(e.message || '提交失败');
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
  });
}
</script>

<template>
  <div class="order-detail-page">
    <!-- 返回按钮 -->
    <n-button text class="back-btn" @click="router.back()">
      ← 返回
    </n-button>

    <n-spin v-if="loading" size="large" style="padding: 80px" />

    <template v-else-if="order">
      <!-- 状态进度 -->
      <n-card class="status-card">
        <div class="status-header">
          <div class="status-info">
            <span class="status-label">订单状态</span>
            <n-tag
              :type="PAYMENT_STATUS_MAP[order.paymentStatus]?.type || 'default'"
              size="large"
            >
              {{ PAYMENT_STATUS_MAP[order.paymentStatus]?.label }}
            </n-tag>
          </div>
          <div v-if="order.paymentStatus === 1" class="status-info">
            <span class="status-label">发货状态</span>
            <n-tag
              :type="DELIVERY_STATUS_MAP[order.deliveryStatus]?.type || 'default'"
              size="large"
            >
              {{ DELIVERY_STATUS_MAP[order.deliveryStatus]?.label }}
            </n-tag>
          </div>
        </div>

        <div class="status-steps">
          <div
            v-for="(step, idx) in statusSteps"
            :key="idx"
            :class="['step-item', { done: step.done }]"
          >
            <div class="step-dot"></div>
            <span class="step-label">{{ step.label }}</span>
            <div v-if="idx < statusSteps.length - 1" class="step-line"></div>
          </div>
        </div>
      </n-card>

      <div class="detail-grid">
        <!-- 左侧：商品信息 -->
        <div class="detail-left">
          <n-card title="商品信息">
            <div class="product-detail">
              <div class="product-cover-large">
                <img
                  v-if="order.productCoverImage"
                  :src="order.productCoverImage"
                  :alt="order.productName"
                />
                <div v-else>📦</div>
              </div>
              <div class="product-detail-info">
                <h3>{{ order.productName }}</h3>
                <p class="order-no">订单号: {{ order.orderNo }}</p>
              </div>
            </div>

            <n-divider />

            <n-descriptions :column="2" label-placement="top">
              <n-descriptions-item label="单价">
                {{ order.unitPrice }} 积分
              </n-descriptions-item>
              <n-descriptions-item label="数量">
                {{ order.quantity }}
              </n-descriptions-item>
              <n-descriptions-item label="总价">
                <span class="highlight">{{ order.totalAmount }} 积分</span>
              </n-descriptions-item>
              <n-descriptions-item label="创建时间">
                {{ order.createdAt }}
              </n-descriptions-item>
            </n-descriptions>
          </n-card>

          <!-- 卡密信息（虚拟商品） -->
          <n-card
            v-if="order.cardContents?.length"
            title="卡密信息"
            class="card-section"
          >
            <div class="card-list">
              <div
                v-for="(card, i) in order.cardContents"
                :key="i"
                class="card-item"
              >
                <code class="card-content">{{ card }}</code>
                <n-button
                  size="tiny"
                  type="primary"
                  ghost
                  @click="copyCard(card)"
                >
                  复制
                </n-button>
              </div>
            </div>
          </n-card>
        </div>

        <!-- 右侧：操作区 -->
        <div class="detail-right">
          <n-card title="订单详情">
            <n-descriptions label-placement="top" :column="1">
              <n-descriptions-item v-if="order.contactInfo" label="联系信息">
                {{ order.contactInfo }}
              </n-descriptions-item>
              <n-descriptions-item v-if="order.remark" label="备注">
                {{ order.remark }}
              </n-descriptions-item>
              <n-descriptions-item v-if="order.deliveryInfo" label="发货信息">
                {{ order.deliveryInfo }}</n-descriptions-item>
              <n-descriptions-item v-if="order.adminRemark" label="管理员备注">
                {{ order.adminRemark }}</n-descriptions-item>
              <n-descriptions-item v-if="order.paidAt" label="支付时间">
                {{ order.paidAt }}
              </n-descriptions-item>
              <n-descriptions-item v-if="order.deliveredAt" label="发货时间">
                {{ order.deliveredAt }}
              </n-descriptions-item>
            </n-descriptions>
          </n-card>

          <!-- 操作按钮 -->
          <n-card class="action-card">
            <n-button
              v-if="order.paymentStatus === 0"
              type="primary"
              block
              size="large"
              @click="goPay"
            >
              立即支付 ({{ order.totalAmount }} 积分)
            </n-button>

            <n-button
              v-if="canCreateDispute()"
              block
              type="warning"
              ghost
              @click="showDisputeModal = true"
            >
              发起争议
            </n-button>
          </n-card>
        </div>
      </div>
    </template>

    <!-- 发起争议弹窗 -->
    <n-modal
      v-model:show="showDisputeModal"
      preset="card"
      title="发起争议"
      style="max-width: 480px"
    >
      <p class="dispute-hint">
        请详细描述您遇到的问题，管理员将在核实后进行处理。
      </p>
      <n-form label-placement="top">
        <n-form-item label="争议原因">
          <n-input
            v-model:value="disputeReason"
            type="textarea"
            :rows="4"
            placeholder="请描述您遇到的问题..."
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showDisputeModal = false">取消</n-button>
          <n-button
            type="warning"
            :loading="creatingDispute"
            @click="createDispute"
          >
            提交争议
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.order-detail-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
}

.back-btn {
  margin-bottom: 16px;
}

.status-card {
  margin-bottom: 24px;
}

.status-header {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-label {
  font-size: 13px;
  opacity: 0.5;
}

.status-steps {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
  flex: 1;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(128, 128, 128, 0.2);
  transition: background-color 0.3s;
}

.step-item.done .step-dot {
  background: #18a058;
}

.step-label {
  font-size: 12px;
  opacity: 0.5;
  transition: opacity 0.3s;
}

.step-item.done .step-label {
  opacity: 1;
  font-weight: 600;
  color: #18a058;
}

.step-line {
  position: absolute;
  top: 6px;
  left: calc(50% + 10px);
  width: calc(100% - 8px);
  height: 2px;
  background: rgba(128, 128, 128, 0.1);
}

.step-item.done + .step-item .step-line {
  background: #18a058;
  opacity: 0.3;
}

.detail-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.detail-left {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-detail {
  display: flex;
  gap: 16px;
}

.product-cover-large {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(128, 128, 128, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.product-cover-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-detail-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px;
}

.order-no {
  font-size: 12px;
  opacity: 0.5;
  margin: 0;
  font-family: monospace;
}

.highlight {
  color: #18a058;
  font-weight: 700;
  font-size: 16px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 8px;
  background: rgba(128, 128, 128, 0.06);
  gap: 12px;
}

.card-content {
  font-size: 14px;
  word-break: break-all;
  font-family: monospace;
}

.detail-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.action-card :deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dispute-hint {
  font-size: 14px;
  opacity: 0.7;
  margin: 0 0 16px;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .status-header {
    flex-direction: column;
    gap: 16px;
  }

  .status-steps {
    overflow-x: auto;
    padding-bottom: 8px;
  }
}
</style>
