<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { announcementApi } from '#/api/modules';

const router = useRouter();

const announcements = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await announcementApi.userList({ page: 1, size: 20 });
    announcements.value = res?.records || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>首页</span> &gt; <span>站点公告</span>
    </div>

    <div class="faka-card">
      <div class="card-header">公告列表</div>
      <div class="card-body">
        <n-spin v-if="loading" size="large" class="loading-spin" />
        
        <div v-else-if="announcements.length" class="ann-list">
          <div 
            v-for="ann in announcements" 
            :key="ann.id"
            class="ann-row"
            @click="router.push(`/announcement/${ann.id}`)"
          >
            <div class="ann-title">
              <span class="ann-badge">公告</span>
              {{ ann.title }}
            </div>
            <div class="ann-date">{{ ann.createdAt }}</div>
          </div>
        </div>

        <div v-else class="empty-state">
          暂无公告信息
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}
.breadcrumb {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
  margin-bottom: 20px;
  cursor: pointer;
}
.breadcrumb span:hover { color: #1890ff; }

.faka-card {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  color: var(--faka-text-main, #333);
}
.card-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
}
.card-body { padding: 8px 0; }
.loading-spin { display: flex; justify-content: center; padding: 60px; }
.empty-state { text-align: center; padding: 60px; color: var(--faka-text-sub); }

.ann-list {
  display: flex;
  flex-direction: column;
}
.ann-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px dashed var(--faka-border, #f0f0f0);
  cursor: pointer;
  transition: background 0.2s;
}
.ann-row:hover { background: var(--faka-tag-bg, #fafafa); }
.ann-row:last-child { border-bottom: none; }

.ann-badge {
  background: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
  margin-right: 8px;
}
.ann-title {
  font-size: 14px;
  color: var(--faka-text-main, #333);
}
.ann-date {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
}
</style>
