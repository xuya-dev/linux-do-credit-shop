<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { announcementApi } from '#/api/modules';

const route = useRoute();
const router = useRouter();
const message = useMessage();

const ann = ref<any>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    ann.value = await announcementApi.detail(Number(route.params.id));
  } catch {
    message.error('加载公告失败');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/announcements')">
      <span>站点公告</span> &gt; <span>公告详情</span>
    </div>

    <div class="faka-card">
      <div v-if="loading" class="loading-spin">
        <n-spin size="large" />
      </div>

      <template v-else-if="ann">
        <div class="ann-header">
          <h1 class="ann-title">{{ ann.title }}</h1>
          <div class="ann-meta">
            发布于: {{ ann.createdAt }}
          </div>
        </div>
        <div class="ann-content html-content">
          <p>{{ ann.content }}</p>
        </div>
      </template>

      <div v-else class="empty-state">
        公告不存在
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
  padding: 40px;
}

.loading-spin { text-align: center; padding: 60px; }
.empty-state { text-align: center; padding: 60px; color: var(--faka-text-sub); }

.ann-header {
  text-align: center;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
  padding-bottom: 24px;
  margin-bottom: 32px;
}
.ann-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 16px;
  color: var(--faka-text-main, #333);
}
.ann-meta {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
}

.ann-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--faka-text-sub, #595959);
}
</style>
