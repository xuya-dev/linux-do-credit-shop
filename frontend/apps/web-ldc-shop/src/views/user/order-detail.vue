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
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/orders')">
      <span>我的订单</span> &gt; <span>订单详情</span>
    </div>

    <div v-if="loading" class="loading-spin">
      <n-spin size="large" />
    </div>

    <template v-else-if="order">
      <!-- 提取结果（发卡核心模块） -->
      <div v-if="order.paymentStatus === 1" class="faka-card highlight-card mb-24">
        <div class="card-header highlight">
          📝 订单已支付 / 提取卡密
          <div class="header-tags">
            <span class="dlv-badge">状态：{{ DELIVERY_STATUS_MAP[order.deliveryStatus]?.label }}</span>
          </div>
        </div>
        <div class="card-body">
          <div v-if="order.cardContents?.length" class="card-codes">
            <div v-for="(card, i) in order.cardContents" :key="i" class="code-box">
              <span class="code-text">{{ card }}</span>
              <button class="faka-btn plain sm" @click="copyCard(card)">复制卡密</button>
            </div>
            <div class="alert-tip mt-12">
              💡 提示：请妥善保管您的卡密，切勿泄露给他人。
            </div>
          </div>
          <div v-else class="empty-code">
            <div v-if="order.deliveryStatus === 0">
              您的商品正在处理中，请稍后刷新查看...
            </div>
            <div v-else>
              没有检测到卡密信息。该商品可能属于人工发货或直充类型。
            </div>
          </div>
        </div>
      </div>

      <!-- 订单基础信息 -->
      <div class="faka-card">
        <div class="card-header">
          订单信息
          <span :class="['status-tag-sm', 'status-' + order.paymentStatus]" style="margin-left:8px;">
            {{ PAYMENT_STATUS_MAP[order.paymentStatus]?.label }}
          </span>
        </div>
        <div class="card-body order-info-grid">
          <div class="info-item">
            <label>订单编号：</label>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <label>商品名称：</label>
            <span>{{ order.productName }}</span>
          </div>
          <div class="info-item">
            <label>购买单价：</label>
            <span>{{ order.unitPrice }} 积分</span>
          </div>
          <div class="info-item">
            <label>购买数量：</label>
            <span>{{ order.quantity }}</span>
          </div>
          <div class="info-item">
            <label>实付金额：</label>
            <span class="highlight-price">{{ order.totalAmount }} 积分</span>
          </div>
          <div class="info-item">
            <label>下单时间：</label>
            <span>{{ order.createdAt }}</span>
          </div>
          <div class="info-item" v-if="order.contactInfo">
            <label>预留联系：</label>
            <span>{{ order.contactInfo }}</span>
          </div>
          <div class="info-item" v-if="order.remark">
            <label>订单备注：</label>
            <span>{{ order.remark }}</span>
          </div>
          <div class="info-item" v-if="order.deliveryInfo">
            <label>发货详情：</label>
            <span>{{ order.deliveryInfo }}</span>
          </div>
        </div>
        <div class="card-footer actions">
          <button v-if="order.paymentStatus === 0" class="faka-btn primary" @click="goPay">
            立即支付 ({{ order.totalAmount }} 积分)
          </button>
          
          <button v-if="canCreateDispute()" class="faka-btn warning outline" @click="showDisputeModal = true">
            发起售后争议
          </button>
        </div>
      </div>

    </template>

    <div v-else class="empty-state">
      找不到该订单
    </div>

    <!-- 发起争议弹窗 -->
    <n-modal v-model:show="showDisputeModal" preset="card" title="申请售后" style="max-width: 440px" class="faka-modal">
      <div class="modal-body">
        <label class="modal-label">争议原因</label>
        <textarea 
          v-model="disputeReason" 
          class="faka-textarea w-full" 
          placeholder="请详细描述卡密无效或缺失等情况，以便卖家核实。" 
          rows="4"
        ></textarea>
      </div>
      <template #action>
        <div class="modal-actions">
          <button class="faka-btn plain" @click="showDisputeModal = false">取消</button>
          <button class="faka-btn warning" @click="createDispute" :disabled="creatingDispute">
            {{ creatingDispute ? '提交中...' : '提交售后记录' }}
          </button>
        </div>
      </template>
    </n-modal>

  </div>
</template>

<style scoped>
.faka-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
  margin-bottom: 20px;
  cursor: pointer;
}
.breadcrumb span:hover { color: #1890ff; }

.mb-24 { margin-bottom: 24px; }
.mt-12 { margin-top: 12px; }

.faka-card {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  color: var(--faka-text-main, #333);
}
.highlight-card {
  border: 1px solid #91d5ff;
}

.card-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header.highlight {
  background: #e6f7ff;
  border-bottom-color: #91d5ff;
  color: #096dd9;
}

.dlv-badge {
  font-size: 12px;
  background: rgba(24, 144, 255, 0.1);
  padding: 2px 8px;
  border-radius: 2px;
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
  color: #fa8c16;
  background: #fff7e6;
  padding: 8px 12px;
  border-radius: 2px;
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
.status-0 { background: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.status-1 { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-2 { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }

/* 按钮通用 */
.faka-btn {
  padding: 6px 20px;
  font-size: 14px;
  border-radius: 2px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: opacity 0.2s;
}
.faka-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.faka-btn:hover:not(:disabled) { opacity: 0.85; }

.faka-btn.sm { font-size: 12px; padding: 4px 12px; }
.faka-btn.primary { background: #1890ff; color: #fff; }
.faka-btn.warning { background: #faad14; color: #fff; }
.faka-btn.warning.outline { background: transparent; color: #faad14; border-color: #faad14; }
.faka-btn.plain { background: transparent; border-color: var(--faka-border, #d9d9d9); color: var(--faka-text-main, #333); }

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
.empty-state { padding: 80px; text-align: center; color: var(--faka-text-sub); }

@media (max-width: 768px) {
  .order-info-grid { grid-template-columns: 1fr; }
  .card-footer { flex-direction: column; }
}
</style>
