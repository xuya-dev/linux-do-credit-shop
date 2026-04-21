<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Dispute } from '#/api/types';
import { DISPUTE_STATUS_MAP } from '#/api/types';
import { disputeApi } from '#/api/modules';
import '#/styles/faka-common.css';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const { t } = useI18n();

const dispute = ref<Dispute | null>(null);
const loading = ref(true);

const STATUS_CLASS_MAP: Record<number, string> = {
  0: 'status-0',
  1: 'status-1',
  2: 'status-2',
  3: 'status-3',
};

onMounted(async () => {
  try {
    dispute.value = await disputeApi.detail(Number(route.params.id));
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.user.loadDisputeDetailFailed'));
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/disputes')">
      <span>{{ t('page.user.afterSaleDispute') }}</span> &gt; <span>{{ t('page.user.disputeDetail') }}</span>
    </div>

    <div class="faka-card">
      <div v-if="loading" class="loading-spin"><n-spin size="large" /></div>
      
      <template v-else-if="dispute">
        <div class="card-header highlight">
          {{ t('page.user.disputeNo') }} {{ dispute.sn }}
          <span :class="['faka-status', STATUS_CLASS_MAP[dispute.status] || '']">
            {{ DISPUTE_STATUS_MAP[dispute.status]?.i18nKey ? t(DISPUTE_STATUS_MAP[dispute.status].i18nKey) : t('page.user.unknown') }}
          </span>
        </div>
        
        <div class="card-body">
          <div class="info-grid">
            <div class="info-row">
              <label>{{ t('page.user.relatedOrder') }}</label>
              <span class="link-text" @click="router.push(`/order/${dispute.orderId}`)">
                {{ t('page.user.viewRelatedOrder') }}
              </span>
            </div>
            <div class="info-row">
              <label>{{ t('page.user.createTime') }}</label>
              <span>{{ dispute.createdAt }}</span>
            </div>
            <div class="info-row block-row">
              <label>{{ t('page.user.disputeReasonLabel') }}</label>
              <div class="text-box">{{ dispute.reason }}</div>
            </div>
            <div class="info-row block-row" v-if="dispute.result">
              <label>{{ t('page.user.processResult') }}</label>
              <div class="text-box result-box">{{ dispute.result }}</div>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="empty-state">{{ t('page.user.recordNotFound') }}</div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 800px; }
.faka-card { border: 1px solid var(--faka-border); }
.card-header { display: flex; justify-content: space-between; align-items: center;}
.card-header.highlight { background: #fafafa; }
.card-body { padding: 20px; }

.status-1 { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }

.info-grid { display: flex; flex-direction: column; gap: 16px; }
.info-row { display: flex; font-size: 14px; }
.info-row label { width: 80px; color: var(--faka-text-sub); flex-shrink: 0; }
.info-row.block-row { flex-direction: column; gap: 8px; }
.text-box { background: var(--faka-tag-bg); padding: 12px; border: 1px dashed var(--faka-border); border-radius: 2px; line-height: 1.6; }
.result-box { border-color: #91d5ff; background: #e6f7ff; color: #096dd9; }
.link-text { color: #1890ff; cursor: pointer; text-decoration: underline; }
.loading-spin { padding: 60px; text-align: center; }
.empty-state { padding: 60px; }
</style>
