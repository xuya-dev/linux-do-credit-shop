<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { disputeApi } from '#/api/modules';

const router = useRouter();
const disputes = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const STATUS_MAP: Record<number, { label: string; class: string }> = {
  0: { label: '待处理', class: 'status-0' },
  1: { label: '处理中', class: 'status-1' },
  2: { label: '已解决', class: 'status-2' },
  3: { label: '已驳回', class: 'status-3' },
};

async function loadDisputes() {
  loading.value = true;
  try {
    const res = await disputeApi.userList({ page: page.value, size: 10 });
    disputes.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
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
      <span>个人中心</span> &gt; <span>售后争议</span>
    </div>

    <div class="faka-card">
      <div class="card-header">我的售后反馈</div>
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
                <span class="d-sn">反馈编号: {{ item.sn }}</span>
                <span class="d-date">{{ item.createdAt }}</span>
              </div>
              <div class="dispute-status">
                <span :class="['faka-status', STATUS_MAP[item.status]?.class]">
                  {{ STATUS_MAP[item.status]?.label }}
                </span>
                <span class="faka-arrow">&gt;</span>
              </div>
            </div>
          </div>
          <div v-if="total > 10" class="pagination-wrapper mt-24">
            <n-pagination :page="page" :page-count="Math.ceil(total / 10)" @update:page="(p) => { page = p; loadDisputes(); }" />
          </div>
        </template>
        
        <div v-else class="empty-state">暂无售后记录</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 1000px; margin: 0 auto; padding: 24px; }
.breadcrumb { font-size: 13px; color: var(--faka-text-sub); margin-bottom: 20px; cursor: pointer; }
.breadcrumb span:hover { color: #1890ff; }
.faka-card { background: var(--faka-bg-header); border-radius: 4px; border: 1px solid var(--faka-border); color: var(--faka-text-main); }
.card-header { padding: 16px 20px; font-weight: 600; border-bottom: 1px solid var(--faka-border); font-size: 15px;}
.card-body { padding: 8px 0; }
.loading-spin { padding: 60px; text-align: center; }
.mt-24 { margin-top: 24px; display: flex; justify-content: flex-end; padding: 0 20px 20px;}

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

.faka-status { font-size: 12px; padding: 2px 8px; border-radius: 2px; }
.status-0 { background: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.status-1 { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.status-2 { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-3 { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }
.faka-arrow { color: var(--faka-border); font-family: monospace; }
.empty-state { text-align: center; padding: 60px; color: var(--faka-text-sub); }
</style>
