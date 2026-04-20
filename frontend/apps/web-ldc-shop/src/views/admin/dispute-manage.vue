<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { disputeApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();

const disputes = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const showHandle = ref(false);
const selectedDispute = ref<any>(null);
const handleForm = ref({ status: 1, adminNote: '' });

const statusTabs = computed(() => [
  { label: t('page.admin.all'), value: null },
  { label: t('page.admin.processing'), value: 0 },
  { label: t('page.admin.accepted'), value: 1 },
  { label: t('page.admin.rejected'), value: 2 },
  { label: t('page.admin.platformIntervened'), value: 3 },
]);

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
    message.success(t('page.admin.operationSuccess'));
    showHandle.value = false;
    loadDisputes();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

const disputeStatusMap = computed(() => ({
  0: { label: t('page.admin.processing'), type: 'warning' as const },
  1: { label: t('page.admin.accepted'), type: 'success' as const },
  2: { label: t('page.admin.rejected'), type: 'error' as const },
  3: { label: t('page.admin.platformIntervened'), type: 'info' as const },
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
  { title: t('page.admin.disputeReason'), key: 'reason', ellipsis: { tooltip: true } },
  {
    title: t('page.admin.status'),
    key: 'status',
    width: 100,
    render: (row: any) => {
      const info = disputeStatusMap.value[row.status as keyof typeof disputeStatusMap.value] || {
        label: String(row.status),
        type: 'default' as const,
      };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.actions'),
    key: 'actions',
    width: 80,
    render: (row: any) =>
      row.status === 0
        ? h(NButton, { size: 'small', type: 'primary', onClick: () => openHandle(row) }, {
            default: () => t('page.admin.confirm'),
          })
        : null,
  },
]);

onMounted(loadDisputes);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">{{ t('page.admin.disputes') }}</n-h3>
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
      :title="t('page.admin.disputes')"
      preset="card"
      style="width: 480px"
    >
      <n-form label-placement="left" label-width="100">
        <n-form-item :label="t('page.admin.disputeReason')">
          <n-text>{{ selectedDispute?.reason }}</n-text>
        </n-form-item>
        <n-form-item :label="t('page.admin.disputeStatus')">
          <n-select
            v-model:value="handleForm.status"
            :options="[
              { label: t('page.admin.accept'), value: 1 },
              { label: t('page.admin.reject'), value: 2 },
              { label: t('page.admin.platformIntervene'), value: 3 },
            ]"
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.disputeNote')">
          <n-input
            v-model:value="handleForm.adminNote"
            type="textarea"
            :rows="3"
            :placeholder="t('page.admin.disputeNote')"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showHandle = false">{{ t('page.admin.cancel') }}</n-button>
          <n-button type="primary" @click="handleDispute">{{ t('page.admin.confirm') }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
