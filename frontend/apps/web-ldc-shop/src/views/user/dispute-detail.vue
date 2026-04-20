<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';

import type { Dispute } from '#/api/types';
import { DISPUTE_STATUS_MAP } from '#/api/types';

import { disputeApi } from '#/api/modules';

const route = useRoute();
const router = useRouter();
const message = useMessage();

const dispute = ref<Dispute | null>(null);
const loading = ref(true);

function openImage(url: string) {
  window.open(url, '_blank');
}

onMounted(async () => {
  try {
    dispute.value = await disputeApi.userDetail(Number(route.params.id));
  } catch {
    message.error('加载争议详情失败');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="dispute-detail-page">
    <n-button text class="back-btn" @click="router.back()">
      ← 返回
    </n-button>

    <n-spin v-if="loading" size="large" style="padding: 80px" />

    <template v-else-if="dispute">
      <!-- 状态卡片 -->
      <n-card class="status-card">
        <div class="status-header">
          <div class="status-info">
            <span class="status-label">争议状态</span>
            <n-tag
              :type="DISPUTE_STATUS_MAP[dispute.status]?.type || 'default'"
              size="large"
            >
              {{ dispute.statusName }}
            </n-tag>
          </div>
          <div class="status-meta">
            <span>订单号: {{ dispute.orderNo }}</span>
            <span>提交时间: {{ dispute.createdAt }}</span>
          </div>
        </div>
      </n-card>

      <div class="detail-grid">
        <!-- 左侧：争议信息 -->
        <div class="detail-left">
          <n-card title="争议详情">
            <n-descriptions label-placement="top" :column="1">
              <n-descriptions-item label="商品名称">
                {{ dispute.productName }}
              </n-descriptions-item>
              <n-descriptions-item label="争议原因">
                <div class="reason-text">{{ dispute.reason }}</div>
              </n-descriptions-item>
              <n-descriptions-item
                v-if="dispute.evidence?.length"
                label="证据图片"
              >
                <div class="evidence-list">
                  <img
                    v-for="(img, idx) in dispute.evidence"
                    :key="idx"
                    :src="img"
                    class="evidence-img"
                    @click="openImage(img)"
                  />
                </div>
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
        </div>

        <!-- 右侧：处理结果 -->
        <div class="detail-right">
          <n-card title="处理结果">
            <div v-if="dispute.adminNote" class="result-section">
              <div class="result-label">管理员备注</div>
              <div class="result-content">{{ dispute.adminNote }}</div>
            </div>
            <div v-else class="result-empty">
              <span>⏳</span>
              <p>正在处理中，请耐心等待...</p>
            </div>

            <n-divider />

            <n-descriptions :column="1" label-placement="top">
              <n-descriptions-item v-if="dispute.handlerName" label="处理人">
                {{ dispute.handlerName }}
              </n-descriptions-item>
              <n-descriptions-item v-if="dispute.handledAt" label="处理时间">
                {{ dispute.handledAt }}
              </n-descriptions-item>
            </n-descriptions>
          </n-card>
        </div>
      </div>
    </template>

    <n-empty v-else description="争议不存在" />
  </div>
</template>

<style scoped>
.dispute-detail-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
}

.back-btn {
  margin-bottom: 16px;
}

.status-card {
  margin-bottom: 24px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-label {
  font-size: 13px;
  opacity: 0.5;
}

.status-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  opacity: 0.6;
  text-align: right;
}

.detail-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.reason-text {
  line-height: 1.7;
  padding: 12px;
  border-radius: 8px;
  background: rgba(128, 128, 128, 0.06);
}

.evidence-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.evidence-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.evidence-img:hover {
  transform: scale(1.05);
}

.result-section {
  padding: 12px;
  border-radius: 8px;
  background: rgba(128, 128, 128, 0.06);
}

.result-label {
  font-size: 13px;
  opacity: 0.5;
  margin-bottom: 8px;
}

.result-content {
  line-height: 1.7;
}

.result-empty {
  text-align: center;
  padding: 40px 20px;
  opacity: 0.6;
}

.result-empty span {
  font-size: 48px;
  line-height: 1;
  display: block;
  margin-bottom: 12px;
}

.result-empty p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .status-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .status-meta {
    text-align: left;
  }
}
</style>
