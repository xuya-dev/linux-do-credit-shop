<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { disputeApi } from '#/api/modules';

const disputes = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const statusTabs = [
  { label: 'All', value: null },
  { label: 'Pending', value: 0 },
  { label: 'Accepted', value: 1 },
  { label: 'Rejected', value: 2 },
  { label: 'Platform', value: 3 },
];

function getStatusType(status: number): 'default' | 'success' | 'warning' | 'error' | 'info' {
  if (status === 0) return 'warning';
  if (status === 1) return 'success';
  if (status === 2) return 'error';
  return 'info';
}

async function loadDisputes() {
  loading.value = true;
  try {
    const res = await disputeApi.userList({
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

onMounted(loadDisputes);

function handleTabChange(value: number | null) {
  statusFilter.value = value;
  page.value = 1;
  loadDisputes();
}

function handlePageChange(p: number) {
  page.value = p;
  loadDisputes();
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <h1
      style="
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 24px;
      "
    >
      My Disputes
    </h1>

    <n-space style="margin-bottom: 20px">
      <n-button
        v-for="s in statusTabs"
        :key="String(s.value)"
        :type="statusFilter === s.value ? 'primary' : 'default'"
        size="small"
        @click="handleTabChange(s.value)"
      >
        {{ s.label }}
      </n-button>
    </n-space>

    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <template v-else>
      <n-card
        v-for="d in disputes"
        :key="d.id"
        style="margin-bottom: 12px"
      >
        <div style="display: flex; justify-content: space-between">
          <div>
            <h3
              style="
                font-size: 15px;
                font-weight: 600;
                margin: 0;
              "
            >
              {{ d.orderNo }}
            </h3>
            <p
              style="
                font-size: 13px;
                opacity: 0.5;
                margin: 2px 0 0;
              "
            >
              {{ d.productName }}
            </p>
          </div>
          <n-tag :type="getStatusType(d.status)" size="small">
            {{ d.statusName }}
          </n-tag>
        </div>
        <p
          style="
            font-size: 14px;
            opacity: 0.6;
            margin: 8px 0 0;
          "
        >
          {{ d.reason }}
        </p>
        <p
          style="
            font-size: 12px;
            opacity: 0.4;
            margin: 8px 0 0;
          "
        >
          {{ d.createdAt }}
        </p>
      </n-card>

      <n-empty
        v-if="!disputes.length"
        description="No disputes found"
        style="padding: 60px"
      />
    </template>

    <div
      v-if="total > 10"
      style="display: flex; justify-content: center; margin-top: 32px"
    >
      <n-pagination
        :page="page"
        :page-count="Math.ceil(total / 10)"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>
