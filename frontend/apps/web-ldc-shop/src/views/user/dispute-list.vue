<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Dispute } from '#/api/types';
import { DISPUTE_STATUS_MAP } from '#/api/types';
import { disputeApi } from '#/api/modules';
import '#/styles/faka-common.css';

const router = useRouter();
const message = useMessage();
const { t } = useI18n();
const disputes = ref<Dispute[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const STATUS_CLASS_MAP: Record<number, string> = {
  0: 'status-0',
  1: 'status-1',
  2: 'status-2',
  3: 'status-3',
};

async function loadDisputes() {
  loading.value = true;
  try {
    const res = await disputeApi.userList({ page: page.value, size: 10 });
    disputes.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.user.loadDisputeListFailed'));
  } finally {
    loading.value = false;
  }
}

onMounted(loadDisputes);

function goDetail(id: number) {
  router.push(`/dispute/${id}`);
}
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/profile')">
      <span>{{ t('page.user.profile') }}</span> &gt; <span>{{ t('page.user.afterSaleDispute') }}</span>
    </div>

    <div class="faka-card">
      <div class="card-header">{{ t('page.user.myAfterSaleFeedback') }}</div>
      <div class="card-body">
        <n-spin v-if="loading" size="large" class="loading-spin" />

        <template v-else-if="disputes.length">
          <div class="dispute-list">
            <div 
              v-for="item in disputes" 
              :key="item.id" 
              class="dispute-row"
              @click="goDetail(item.id)"
            >
              <div class="dispute-info">
                <span class="d-sn">{{ t('page.user.feedbackNo') }} {{ item.sn }}</span>
                <span class="d-date">{{ item.createdAt }}</span>
              </div>
              <div class="dispute-status">
                <span :class="['faka-status', STATUS_CLASS_MAP[item.status] || '']">
                  {{ DISPUTE_STATUS_MAP[item.status]?.i18nKey ? t(DISPUTE_STATUS_MAP[item.status].i18nKey) : t('page.user.unknown') }}
                </span>
                <span class="faka-arrow">&gt;</span>
              </div>
            </div>
          </div>
          <div v-if="total > 10" class="pagination-wrapper mt-24">
            <n-pagination :page="page" :page-count="Math.ceil(total / 10)" @update:page="(p) => { page = p; loadDisputes(); }" />
          </div>
        </template>
        
        <div v-else class="empty-state">{{ t('page.user.noDisputeRecords') }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 1000px; }
.faka-card { border: 1px solid var(--faka-border); }
.loading-spin { padding: 60px; text-align: center; }
.mt-24 { display: flex; justify-content: flex-end; padding: 0 20px 20px;}

.dispute-list { display: flex; flex-direction: column; }
.dispute-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px dashed var(--faka-border); cursor: pointer;
}
.dispute-row:hover { background: var(--faka-tag-bg); }
.dispute-row:last-child { border-bottom: none; }
.dispute-info { display: flex; gap: 16px; font-size: 14px; }
.d-sn { font-weight: 600; }
.d-date { color: var(--faka-text-sub); font-size: 13px; }
.dispute-status { display: flex; align-items: center; gap: 12px; }

.status-1 { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.faka-arrow { color: var(--faka-border); font-family: monospace; }
</style>
