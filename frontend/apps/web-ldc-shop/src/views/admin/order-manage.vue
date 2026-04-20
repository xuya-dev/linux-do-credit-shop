<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { orderApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const orders = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const keyword = ref('');
const statusFilter = ref<number | null>(null);

const showDeliver = ref(false);
const selectedOrder = ref<any>(null);
const deliverForm = ref({ deliveryInfo: '', adminRemark: '' });

const statusTabs = computed(() => [
  { label: t('page.admin.all'), value: null },
  { label: t('page.admin.pending'), value: 0 },
  { label: t('page.admin.paid'), value: 1 },
  { label: t('page.admin.refunded'), value: 2 },
]);

async function loadOrders() {
  loading.value = true;
  try {
    const res = await orderApi.adminList({
      page: page.value,
      size: 10,
      keyword: keyword.value || undefined,
      paymentStatus: statusFilter.value ?? undefined,
    });
    orders.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

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

const paymentStatusMap = computed(() => ({
  0: { label: t('page.admin.pending'), type: 'default' as const },
  1: { label: t('page.admin.paid'), type: 'success' as const },
  2: { label: t('page.admin.refunded'), type: 'warning' as const },
}));

const deliveryStatusMap = computed(() => ({
  0: { label: t('page.admin.pendingDelivery'), type: 'default' as const },
  1: { label: t('page.admin.delivered'), type: 'info' as const },
  2: { label: t('page.shop.completed'), type: 'success' as const },
}));

const columns = computed(() => [
  {
    title: t('page.admin.orderNo'),
    key: 'orderNo',
    width: 180,
    render: (row: any) =>
      h('span', { style: 'font-family: monospace; font-size: 13px' }, row.orderNo),
  },
  { title: t('page.admin.productNameCol'), key: 'productName', ellipsis: { tooltip: true } },
  {
    title: t('page.admin.amount'),
    key: 'totalAmount',
    width: 100,
    render: (row: any) =>
      h('span', { style: 'font-weight: 600; color: #2563eb' }, row.totalAmount),
  },
  {
    title: t('page.admin.paymentStatus'),
    key: 'paymentStatus',
    width: 100,
    render: (row: any) => {
      const info = paymentStatusMap.value[row.paymentStatus as keyof typeof paymentStatusMap.value] || {
        label: row.paymentStatus,
        type: 'default' as const,
      };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.deliveryStatus'),
    key: 'deliveryStatus',
    width: 100,
    render: (row: any) => {
      const info = deliveryStatusMap.value[row.deliveryStatus as keyof typeof deliveryStatusMap.value] || {
        label: row.deliveryStatus,
        type: 'default' as const,
      };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.actions'),
    key: 'actions',
    width: 160,
    render: (row: any) => {
      const btns: any[] = [];
      if (row.paymentStatus === 1 && row.productType === 2 && row.deliveryStatus === 0) {
        btns.push(
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openDeliver(row) },
            { default: () => t('page.admin.confirmDelivery') },
          ),
        );
      }
      if (row.paymentStatus === 1) {
        btns.push(
          h(
            NButton,
            { size: 'small', type: 'error', onClick: () => handleRefund(row.id) },
            { default: () => t('page.admin.confirmRefund') },
          ),
        );
      }
      return h(NSpace, { size: 'small' }, { default: () => btns });
    },
  },
]);

onMounted(loadOrders);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-space align="center">
        <n-input
          v-model:value="keyword"
          :placeholder="t('page.admin.searchOrders')"
          style="width: 200px"
          @keyup.enter="page = 1; loadOrders()"
        />
        <n-button @click="page = 1; loadOrders()">{{ t('page.admin.search') }}</n-button>
      </n-space>
      <n-space>
        <n-button
          v-for="tab in statusTabs"
          :key="String(tab.value)"
          :type="statusFilter === tab.value ? 'primary' : 'default'"
          size="small"
          @click="statusFilter = tab.value; page = 1; loadOrders()"
        >
          {{ tab.label }}
        </n-button>
      </n-space>
    </div>

    <n-data-table
      :columns="columns"
      :data="orders"
      :loading="loading"
      :pagination="{ page: page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadOrders() } }"
      :bordered="false"
    />

    <n-modal
      v-model:show="showDeliver"
      :title="t('page.admin.confirmDelivery')"
      preset="card"
      style="width: 480px"
    >
      <n-form label-placement="left" label-width="100">
        <n-form-item :label="t('page.admin.deliveryInfo')">
          <n-input v-model:value="deliverForm.deliveryInfo" :placeholder="t('page.admin.deliveryInfo')" />
        </n-form-item>
        <n-form-item :label="t('page.admin.adminRemark')">
          <n-input
            v-model:value="deliverForm.adminRemark"
            type="textarea"
            :rows="3"
            :placeholder="t('page.admin.adminRemark')"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDeliver = false">{{ t('page.admin.cancel') }}</n-button>
          <n-button type="primary" @click="handleDeliver">{{ t('page.admin.confirmDelivery') }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
