<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { disputeApi } from '#/api/modules';

const route = useRoute();
const router = useRouter();

const dispute = ref<any>(null);
const loading = ref(true);

const STATUS_MAP: Record<number, { label: string; class: string }> = {
  0: { label: '待处理', class: 'status-0' },
  1: { label: '处理中', class: 'status-1' },
  2: { label: '已解决', class: 'status-2' },
  3: { label: '已驳回', class: 'status-3' },
};

onMounted(async () => {
  try {
    dispute.value = await disputeApi.detail(Number(route.params.id));
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/disputes')">
      <span>售后争议</span> &gt; <span>争议详情</span>
    </div>

    <div class="faka-card">
      <div v-if="loading" class="loading-spin"><n-spin size="large" /></div>
      
      <template v-else-if="dispute">
        <div class="card-header highlight">
          争议编号: {{ dispute.sn }}
          <span :class="['faka-status', STATUS_MAP[dispute.status]?.class]">
            {{ STATUS_MAP[dispute.status]?.label }}
          </span>
        </div>
        
        <div class="card-body">
          <div class="info-grid">
            <div class="info-row">
              <label>相关订单</label>
              <span class="link-text" @click="router.push(`/order/${dispute.orderId}`)">
                查看关联订单
              </span>
            </div>
            <div class="info-row">
              <label>创建时间</label>
              <span>{{ dispute.createdAt }}</span>
            </div>
            <div class="info-row block-row">
              <label>争议原因</label>
              <div class="text-box">{{ dispute.reason }}</div>
            </div>
            <div class="info-row block-row" v-if="dispute.result">
              <label>处理结果</label>
              <div class="text-box result-box">{{ dispute.result }}</div>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="empty-state">记录不存在</div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 800px; margin: 0 auto; padding: 24px; }
.breadcrumb { font-size: 13px; color: var(--faka-text-sub); margin-bottom: 20px; cursor: pointer; }
.breadcrumb span:hover { color: #1890ff; }
.faka-card { background: var(--faka-bg-header); border-radius: 4px; border: 1px solid var(--faka-border); color: var(--faka-text-main); }
.card-header { padding: 16px 20px; font-weight: 600; border-bottom: 1px solid var(--faka-border); font-size: 15px; display: flex; justify-content: space-between; align-items: center;}
.card-header.highlight { background: #fafafa; }
.card-body { padding: 20px; }

.faka-status { font-size: 12px; padding: 2px 8px; border-radius: 2px; }
.status-0 { background: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.status-1 { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.status-2 { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-3 { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }

.info-grid { display: flex; flex-direction: column; gap: 16px; }
.info-row { display: flex; font-size: 14px; }
.info-row label { width: 80px; color: var(--faka-text-sub); flex-shrink: 0; }
.info-row.block-row { flex-direction: column; gap: 8px; }
.text-box { background: var(--faka-tag-bg); padding: 12px; border: 1px dashed var(--faka-border); border-radius: 2px; line-height: 1.6; }
.result-box { border-color: #91d5ff; background: #e6f7ff; color: #096dd9; }
.link-text { color: #1890ff; cursor: pointer; text-decoration: underline; }
.loading-spin { padding: 60px; text-align: center; }
.empty-state { text-align: center; padding: 60px; color: var(--faka-text-sub); }
</style>
