<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, NTag, NButton } from 'naive-ui';

import { disputeApi } from '#/api/modules';

const message = useMessage();

const disputes = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const showHandle = ref(false);
const selectedDispute = ref<any>(null);
const handleForm = ref({ status: 1, adminNote: '' });

const statusTabs = [
  { label: '全部', value: null },
  { label: '待处理', value: 0 },
  { label: '已接受', value: 1 },
  { label: '已拒绝', value: 2 },
  { label: '平台处理', value: 3 },
];

async function loadDisputes() {
  loading.value = true;
  try {
    const res = await disputeApi.adminList({
      page: page.value,
      size: 10,
      status: statusFilter.value ?? undefined,
    });
    disputes.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function openHandle(row: any) {
  selectedDispute.value = row;
  handleForm.value = { status: 1, adminNote: '' };
  showHandle.value = true;
}

async function handleDispute() {
  try {
    await disputeApi.adminHandle(selectedDispute.value.id, handleForm.value);
    message.success('处理成功');
    showHandle.value = false;
    loadDisputes();
  } catch (e: any) {
    message.error(e.message || '处理失败');
  }
}

const disputeStatusMap: Record<number, { label: string; type: 'warning' | 'success' | 'error' | 'info' }> = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '已接受', type: 'success' },
  2: { label: '已拒绝', type: 'error' },
  3: { label: '平台处理', type: 'info' },
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
    title: '原因',
    key: 'reason',
    ellipsis: { tooltip: true },
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => {
      const info = disputeStatusMap[row.status] || {
        label: '未知',
        type: 'default' as const,
      };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row: any) =>
      row.status === 0
        ? h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openHandle(row) },
            { default: () => '处理' },
          )
        : null,
  },
];

onMounted(loadDisputes);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">纠纷管理</n-h3>
      <n-space>
        <n-button
          v-for="tab in statusTabs"
          :key="String(tab.value)"
          :type="statusFilter === tab.value ? 'primary' : 'default'"
          size="small"
          @click="statusFilter = tab.value; page = 1; loadDisputes()"
        >
          {{ tab.label }}
        </n-button>
      </n-space>
    </div>

    <n-data-table
      :columns="columns"
      :data="disputes"
      :loading="loading"
      :pagination="{ page: page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadDisputes() } }"
      :bordered="false"
    />

    <n-modal
      v-model:show="showHandle"
      title="处理纠纷"
      preset="card"
      style="width: 480px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item label="原因">
          <n-text>{{ selectedDispute?.reason }}</n-text>
        </n-form-item>
        <n-form-item label="处理结果">
          <n-select
            v-model:value="handleForm.status"
            :options="[
              { label: '接受', value: 1 },
              { label: '拒绝', value: 2 },
              { label: '平台处理', value: 3 },
            ]"
          />
        </n-form-item>
        <n-form-item label="管理员备注">
          <n-input
            v-model:value="handleForm.adminNote"
            type="textarea"
            :rows="3"
            placeholder="处理备注"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showHandle = false">取消</n-button>
          <n-button type="primary" @click="handleDispute">确认</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
