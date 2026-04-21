<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Dispute, DisputeHandleData } from '#/api/types';
import { DISPUTE_STATUS_MAP } from '#/api/types';
import { disputeApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();

const disputes = ref<Dispute[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const searchForm = ref({ orderNo: '', status: null as number | null });

const showHandle = ref(false);
const selectedDispute = ref<Dispute | null>(null);
const handleForm = ref<DisputeHandleData>({ status: 1, adminNote: '' });

const statusOptions = computed(() => [
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
      orderNo: searchForm.value.orderNo || undefined,
      status: searchForm.value.status ?? undefined,
    });
    disputes.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.admin.operationFailed'));
  } finally {
    loading.value = false;
  }
}

function handleSearch() { page.value = 1; loadDisputes(); }
function resetSearch() { searchForm.value = { orderNo: '', status: null }; page.value = 1; loadDisputes(); }

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

const columns = computed(() => [
  {
    title: t('page.admin.orderNo'), key: 'orderNo', width: 180,
    render: (row: any) => h('span', { style: 'font-family:monospace;font-size:13px' }, row.orderNo),
  },
  { title: t('page.admin.productNameCol'), key: 'productName', ellipsis: { tooltip: true } },
  { title: t('page.admin.buyer'), key: 'username', width: 100 },
  { title: t('page.admin.disputeReason'), key: 'reason', ellipsis: { tooltip: true } },
  {
    title: t('page.admin.status'), key: 'status', width: 110,
    render: (row: any) => {
      const info = DISPUTE_STATUS_MAP[row.status] || { label: String(row.status), type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  { title: t('page.admin.createdAt'), key: 'createdAt', width: 160 },
  {
    title: t('page.admin.actions'), key: 'actions', width: 80,
    render: (row: any) => row.status === 0
      ? h(NButton, { size: 'small', type: 'primary', onClick: () => openHandle(row) }, { default: () => t('page.admin.confirm') })
      : null,
  },
]);

onMounted(loadDisputes);
</script>

<template>
  <div class="p-5">
    <n-card :bordered="false" style="margin-bottom:16px">
      <n-grid :cols="3" :x-gap="16" :y-gap="12">
        <n-gi>
          <n-form-item :label="t('page.admin.orderNo')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-input v-model:value="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
          </n-form-item>
        </n-gi>
        <n-gi>
          <n-form-item :label="t('page.admin.disputeStatus')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-select v-model:value="searchForm.status" :options="statusOptions" clearable />
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
        <span style="font-size:15px;font-weight:600">{{ t('page.admin.disputes') }}</span>
      </div>
      <n-data-table :columns="columns" :data="disputes" :loading="loading"
        :pagination="{ page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadDisputes() } }"
        :bordered="false" />
    </n-card>

    <n-modal v-model:show="showHandle" :title="t('page.admin.disputes')" preset="card" style="width:480px">
      <n-form label-placement="left" label-width="100">
        <n-form-item :label="t('page.admin.disputeReason')"><n-text>{{ selectedDispute?.reason }}</n-text></n-form-item>
        <n-form-item :label="t('page.admin.disputeStatus')">
          <n-select v-model:value="handleForm.status" :options="[
            { label: t('page.admin.accept'), value: 1 },
            { label: t('page.admin.reject'), value: 2 },
            { label: t('page.admin.platformIntervene'), value: 3 },
          ]" />
        </n-form-item>
        <n-form-item :label="t('page.admin.disputeNote')">
          <n-input v-model:value="handleForm.adminNote" type="textarea" :rows="3" :placeholder="t('page.admin.disputeNote')" />
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
