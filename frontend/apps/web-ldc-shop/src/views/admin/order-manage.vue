<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';

import { orderApi } from '#/api/modules';

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

const statusTabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已退款', value: 2 },
];

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
    message.success('发货成功');
    showDeliver.value = false;
    loadOrders();
  } catch (e: any) {
    message.error(e.message || '发货失败');
  }
}

function handleRefund(id: number) {
  dialog.warning({
    title: '确认退款',
    content: '确定要对该订单进行退款吗？此操作不可撤销。',
    positiveText: '确认退款',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await orderApi.adminRefund(id);
        message.success('退款成功');
        loadOrders();
      } catch (e: any) {
        message.error(e.message || '退款失败');
      }
    },
  });
}

const paymentStatusMap: Record<number, { label: string; type: 'default' | 'success' | 'warning' | 'error' }> = {
  0: { label: '待支付', type: 'default' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已退款', type: 'warning' },
};

const deliveryStatusMap: Record<number, { label: string; type: 'default' | 'info' | 'success' }> = {
  0: { label: '待发货', type: 'default' },
  1: { label: '已发货', type: 'info' },
  2: { label: '已完成', type: 'success' },
};

const columns = [
  {
    title: '订单号',
    key: 'orderNo',
    width: 180,
    render: (row: any) =>
      h('span', { style: 'font-family: monospace; font-size: 13px' }, row.orderNo),
  },
  { title: '商品名称', key: 'productName', ellipsis: { tooltip: true } },
  {
    title: '金额',
    key: 'totalAmount',
    width: 100,
    render: (row: any) =>
      h('span', { style: 'font-weight: 600; color: #2563eb' }, row.totalAmount),
  },
  {
    title: '支付状态',
    key: 'paymentStatus',
    width: 100,
    render: (row: any) => {
      const info = paymentStatusMap[row.paymentStatus] || {
        label: '未知',
        type: 'default' as const,
      };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: '发货状态',
    key: 'deliveryStatus',
    width: 100,
    render: (row: any) => {
      const info = deliveryStatusMap[row.deliveryStatus] || {
        label: '未知',
        type: 'default' as const,
      };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: any) => {
      const btns = [];
      if (row.paymentStatus === 1 && row.productType === 2 && row.deliveryStatus === 0) {
        btns.push(
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openDeliver(row) },
            { default: () => '发货' },
          ),
        );
      }
      if (row.paymentStatus === 1) {
        btns.push(
          h(
            NButton,
            { size: 'small', type: 'error', onClick: () => handleRefund(row.id) },
            { default: () => '退款' },
          ),
        );
      }
      return h(NSpace, { size: 'small' }, { default: () => btns });
    },
  },
];

onMounted(loadOrders);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-space align="center">
        <n-input
          v-model:value="keyword"
          placeholder="搜索订单..."
          style="width: 200px"
          @keyup.enter="page = 1; loadOrders()"
        />
        <n-button @click="page = 1; loadOrders()">搜索</n-button>
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
      title="确认发货"
      preset="card"
      style="width: 480px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item label="物流信息">
          <n-input v-model:value="deliverForm.deliveryInfo" placeholder="请输入物流信息" />
        </n-form-item>
        <n-form-item label="管理员备注">
          <n-input
            v-model:value="deliverForm.adminRemark"
            type="textarea"
            :rows="3"
            placeholder="备注信息"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDeliver = false">取消</n-button>
          <n-button type="primary" @click="handleDeliver">确认发货</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
