<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import type { Dispute } from '#/api/types';
import { DISPUTE_STATUS_MAP } from '#/api/types';

import { disputeApi } from '#/api/modules';

const router = useRouter();

const disputes = ref<Dispute[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const statusTabs = [
  { label: '全部', value: null },
  { label: '处理中', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 },
  { label: '平台介入', value: 3 },
];

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

function goDetail(id: number) {
  router.push(`/dispute/${id}`);
}
</script>

<template>
  <div class="dispute-list-page">
    <div class="page-header">
      <h1 class="page-title">我的争议</h1>
      <p class="page-subtitle">查看和管理您的争议记录</p>
    </div>

    <n-tabs
      :value="statusFilter"
      type="segment"
      class="status-tabs"
      @update:value="handleTabChange"
    >
      <n-tab-pane
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        :name="tab.value"
        :tab="tab.label"
      />
    </n-tabs>

    <div v-if="loading" class="dispute-skeleton-list">
      <n-card v-for="i in 3" :key="i" class="dispute-skeleton">
        <n-skeleton text :repeat="3" />
      </n-card>
    </div>

    <template v-else-if="disputes.length">
      <div class="dispute-list">
        <n-card
          v-for="d in disputes"
          :key="d.id"
          hoverable
          class="dispute-card"
          @click="goDetail(d.id)"
        >
          <div class="dispute-header">
            <div class="dispute-meta">
              <span class="dispute-order">{{ d.orderNo }}</span>
              <span class="dispute-date">{{ d.createdAt }}</span>
            </div>
            <n-tag
              :type="DISPUTE_STATUS_MAP[d.status]?.type || 'default'"
              size="small"
            >
              {{ d.statusName }}
            </n-tag>
          </div>

          <div class="dispute-body">
            <div class="dispute-product">{{ d.productName }}</div>
            <p class="dispute-reason">{{ d.reason }}</p>
          </div>

          <div v-if="d.adminNote" class="dispute-note">
            <span class="note-label">管理员回复:</span>
            <span class="note-content">{{ d.adminNote }}</span>
          </div>
        </n-card>
      </div>

      <div v-if="total > 10" class="pagination-wrapper">
        <n-pagination
          :page="page"
          :page-count="Math.ceil(total / 10)"
          @update:page="handlePageChange"
        />
      </div>
    </template>

    <n-empty v-else description="暂无争议记录" />
  </div>
</template>

<style scoped>
.dispute-list-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 15px;
  opacity: 0.5;
  margin: 0;
}

.status-tabs {
  margin-bottom: 24px;
}

.dispute-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dispute-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dispute-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.dispute-card:hover {
  transform: translateY(-2px);
}

.dispute-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(128, 128, 128, 0.08);
}

.dispute-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dispute-order {
  font-size: 13px;
  font-weight: 600;
  opacity: 0.8;
  font-family: monospace;
}

.dispute-date {
  font-size: 12px;
  opacity: 0.4;
}

.dispute-body {
  margin-bottom: 12px;
}

.dispute-product {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.dispute-reason {
  font-size: 14px;
  opacity: 0.7;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dispute-note {
  padding: 12px;
  border-radius: 8px;
  background: rgba(24, 160, 88, 0.06);
  font-size: 13px;
}

.note-label {
  font-weight: 600;
  color: #18a058;
  margin-right: 8px;
}

.note-content {
  opacity: 0.8;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .dispute-list-page {
    padding: 20px 16px;
  }
}
</style>
