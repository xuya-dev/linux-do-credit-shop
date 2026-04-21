<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Order, OrderDeliveryParams } from '#/api/types';
import { PAYMENT_STATUS_MAP, DELIVERY_STATUS_MAP } from '#/api/types';
import { orderApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const orders = ref<Order[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const searchForm = ref({ keyword: '', paymentStatus: null as number | null });

const showDeliver = ref(false);
const selectedOrder = ref<Order | null>(null);
const deliverForm = ref<OrderDeliveryParams>({ deliveryInfo: '', adminRemark: '' });

const showDetail = ref(false);
const detailOrder = ref<Order | null>(null);
const detailLoading = ref(false);

const paymentStatusOptions = computed(() => [
  { label: t('page.admin.all'), value: null },
  { label: t('page.admin.pending'), value: 0 },
  { label: t('page.admin.paid'), value: 1 },
  { label: t('page.admin.refunded'), value: 2 },
  { label: t('page.admin.cancelled'), value: 3 },
]);

async function loadOrders() {
  loading.value = true;
  try {
    const res = await orderApi.adminList({
      page: page.value,
      size: 10,
      keyword: searchForm.value.keyword || undefined,
      paymentStatus: searchForm.value.paymentStatus ?? undefined,
    });
    orders.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.admin.operationFailed'));
  } finally {
    loading.value = false;
  }
}

function handleSearch() { page.value = 1; loadOrders(); }
function resetSearch() { searchForm.value = { keyword: '', paymentStatus: null }; page.value = 1; loadOrders(); }

function openDeliver(row: any) {
  selectedOrder.value = row;
  deliverForm.value = { deliveryInfo: '', adminRemark: '' };
  showDeliver.value = true;
}

async function handleDeliver() {
  try {
    await orderApi.adminDeliver(selectedOrder.value.id, deliverForm.value);
    message.success(t('page.admin.deliverSuccess'));
    showDeliver.value = false;
    loadOrders();
  } catch (e: any) {
    message.error(e.message || t('page.admin.deliverFailed'));
  }
}

function handleRefund(id: number) {
  dialog.warning({
    title: t('page.admin.confirmRefund'),
    content: t('page.admin.confirmRefundContent'),
    positiveText: t('page.admin.confirmRefund'),
    negativeText: t('page.admin.cancel'),
    onPositiveClick: async () => {
      try {
        await orderApi.adminRefund(id);
        message.success(t('page.admin.refundSuccess'));
        loadOrders();
      } catch (e: any) {
        message.error(e.message || t('page.admin.refundFailed'));
      }
    },
  });
}

async function openDetail(row: any) {
  detailLoading.value = true;
  showDetail.value = true;
  try {
    detailOrder.value = await orderApi.adminDetail(row.id);
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
    showDetail.value = false;
  } finally {
    detailLoading.value = false;
  }
}

function copyCard(text: string) {
  navigator.clipboard.writeText(text).then(() => {
    message.success(t('page.admin.copied'));
  }).catch(() => {
    message.error(t('page.admin.copyFailed'));
  });
}

const columns = computed(() => [
  {
    title: t('page.admin.orderNo'), key: 'orderNo', width: 180,
    render: (row: any) => h('span', { style: 'font-family:monospace;font-size:13px' }, row.orderNo),
  },
  { title: t('page.admin.productNameCol'), key: 'productName', ellipsis: { tooltip: true } },
  {
    title: t('page.admin.amount'), key: 'totalAmount', width: 100,
    render: (row: any) => h('span', { style: 'font-weight:600;color:#2563eb' }, row.totalAmount),
  },
  {
    title: t('page.admin.paymentStatus'), key: 'paymentStatus', width: 100,
    render: (row: any) => {
      const info = PAYMENT_STATUS_MAP[row.paymentStatus] || { label: String(row.paymentStatus), type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.deliveryStatus'), key: 'deliveryStatus', width: 100,
    render: (row: any) => {
      const info = DELIVERY_STATUS_MAP[row.deliveryStatus] || { label: String(row.deliveryStatus), type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  { title: t('page.admin.buyer'), key: 'buyerName', width: 100, render: (row: any) => h('span', {}, row.buyerName || '-') },
  {
    title: t('page.admin.contactInfoCol'), key: 'contactInfo', width: 140, ellipsis: { tooltip: true },
    render: (row: any) => h('span', { style: 'color:#666' }, row.contactInfo || '-'),
  },
  { title: t('page.admin.createdAt'), key: 'createdAt', width: 160 },
  {
    title: t('page.admin.actions'), key: 'actions', width: 220,
    render: (row: any) => {
      const btns: any[] = [];
      btns.push(h(NButton, { size: 'small', onClick: () => openDetail(row) }, { default: () => t('page.admin.viewDetail') }));
      if (row.paymentStatus === 1 && row.productType === 2 && row.deliveryStatus === 0) {
        btns.push(h(NButton, { size: 'small', type: 'primary', onClick: () => openDeliver(row) }, { default: () => t('page.admin.confirmDelivery') }));
      }
      if (row.paymentStatus === 1) {
        btns.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleRefund(row.id) }, { default: () => t('page.admin.confirmRefund') }));
      }
      return h(NSpace, { size: 'small' }, { default: () => btns });
    },
  },
]);

onMounted(loadOrders);
</script>

<template>
  <div class="p-5">
    <n-card :bordered="false" style="margin-bottom:16px">
      <n-grid :cols="3" :x-gap="16" :y-gap="12">
        <n-gi>
          <n-form-item :label="t('page.admin.orderNo')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-input v-model:value="searchForm.keyword" :placeholder="t('page.admin.searchOrders')" clearable @keyup.enter="handleSearch" />
          </n-form-item>
        </n-gi>
        <n-gi>
          <n-form-item :label="t('page.admin.paymentStatus')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-select v-model:value="searchForm.paymentStatus" :options="paymentStatusOptions" clearable />
          </n-form-item>
        </n-gi>
        <n-gi></n-gi>
        <n-gi :span="3" style="text-align:right">
          <n-space justify="end">
            <n-button @click="resetSearch">{{ t('page.admin.cancel') }}</n-button>
            <n-button type="primary" @click="handleSearch">{{ t('page.admin.search') }}</n-button>
          </n-space>
        </n-gi>
      </n-grid>
    </n-card>

    <n-card :bordered="false">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <span style="font-size:15px;font-weight:600">{{ t('page.admin.orders') }}</span>
      </div>
      <n-data-table :columns="columns" :data="orders" :loading="loading"
        :pagination="{ page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadOrders() } }"
        :bordered="false" />
    </n-card>

    <n-modal v-model:show="showDeliver" :title="t('page.admin.confirmDelivery')" preset="card" style="width:480px">
      <n-form label-placement="left" label-width="100">
        <n-form-item :label="t('page.admin.deliveryInfo')"><n-input v-model:value="deliverForm.deliveryInfo" /></n-form-item>
        <n-form-item :label="t('page.admin.adminRemark')"><n-input v-model:value="deliverForm.adminRemark" type="textarea" :rows="3" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDeliver = false">{{ t('page.admin.cancel') }}</n-button>
          <n-button type="primary" @click="handleDeliver">{{ t('page.admin.confirmDelivery') }}</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showDetail" :title="t('page.admin.orderDetail')" preset="card" style="width:640px">
      <n-spin :show="detailLoading">
        <template v-if="detailOrder">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.orderNo') }}</span>
              <span class="detail-value" style="font-family:monospace">{{ detailOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.buyer') }}</span>
              <span class="detail-value">{{ detailOrder.buyerName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.productNameCol') }}</span>
              <span class="detail-value">{{ detailOrder.productName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.quantityCol') }}</span>
              <span class="detail-value">{{ detailOrder.quantity }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.amount') }}</span>
              <span class="detail-value" style="font-weight:600;color:#2563eb">{{ detailOrder.totalAmount }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.paymentStatus') }}</span>
              <span class="detail-value">
                <n-tag :type="(PAYMENT_STATUS_MAP[detailOrder.paymentStatus]?.type || 'default') as any" size="small">
                  {{ PAYMENT_STATUS_MAP[detailOrder.paymentStatus]?.label || detailOrder.paymentStatus }}
                </n-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.deliveryStatus') }}</span>
              <span class="detail-value">
                <n-tag :type="(DELIVERY_STATUS_MAP[detailOrder.deliveryStatus]?.type || 'default') as any" size="small">
                  {{ DELIVERY_STATUS_MAP[detailOrder.deliveryStatus]?.label || detailOrder.deliveryStatus }}
                </n-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">{{ t('page.admin.createdAt') }}</span>
              <span class="detail-value">{{ detailOrder.createdAt }}</span>
            </div>
            <div v-if="detailOrder.paidAt" class="detail-item">
              <span class="detail-label">{{ t('page.admin.paidAt') }}</span>
              <span class="detail-value">{{ detailOrder.paidAt }}</span>
            </div>
            <div v-if="detailOrder.deliveredAt" class="detail-item">
              <span class="detail-label">{{ t('page.admin.deliveredAt') }}</span>
              <span class="detail-value">{{ detailOrder.deliveredAt }}</span>
            </div>
          </div>

          <div v-if="detailOrder.contactInfo || detailOrder.remark || detailOrder.deliveryInfo || detailOrder.adminRemark" class="detail-section">
            <div v-if="detailOrder.contactInfo" class="detail-row">
              <span class="detail-label">{{ t('page.admin.contactInfoCol') }}</span>
              <span class="detail-value">{{ detailOrder.contactInfo }}</span>
            </div>
            <div v-if="detailOrder.remark" class="detail-row">
              <span class="detail-label">{{ t('page.admin.remarkCol') }}</span>
              <span class="detail-value">{{ detailOrder.remark }}</span>
            </div>
            <div v-if="detailOrder.deliveryInfo" class="detail-row">
              <span class="detail-label">{{ t('page.admin.deliveryInfo') }}</span>
              <span class="detail-value">{{ detailOrder.deliveryInfo }}</span>
            </div>
            <div v-if="detailOrder.adminRemark" class="detail-row">
              <span class="detail-label">{{ t('page.admin.adminRemark') }}</span>
              <span class="detail-value">{{ detailOrder.adminRemark }}</span>
            </div>
          </div>

          <div v-if="detailOrder.cardContents?.length" class="detail-section">
            <div class="detail-label" style="margin-bottom:8px;font-weight:600">{{ t('page.admin.cardContents') }}</div>
            <div v-for="(card, i) in detailOrder.cardContents" :key="i" class="card-row">
              <span class="card-text">{{ card }}</span>
              <n-button size="tiny" @click="copyCard(card)">{{ t('page.admin.copy') }}</n-button>
            </div>
          </div>
        </template>
      </n-spin>
    </n-modal>
  </div>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.detail-label {
  font-size: 12px;
  color: #8c8c8c;
}
.detail-value {
  font-size: 14px;
  color: #333;
  word-break: break-all;
}
.detail-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  gap: 12px;
}
.detail-row .detail-label {
  width: 80px;
  flex-shrink: 0;
  line-height: 22px;
}
.detail-row .detail-value {
  flex: 1;
  line-height: 22px;
}
.card-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  margin-bottom: 6px;
}
.card-text {
  font-family: monospace;
  font-size: 13px;
  word-break: break-all;
  flex: 1;
  margin-right: 12px;
}
</style>
