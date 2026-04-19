<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { marked } from 'marked';

import { announcementApi } from '#/api/modules';

const route = useRoute();
const announcement = ref<any>(null);
const loading = ref(true);

const renderedContent = computed(() => {
  if (!announcement.value?.content) return '';
  return marked(announcement.value.content) as string;
});

function getTypeType(t: number): 'default' | 'success' | 'warning' | 'info' {
  if (t === 2) return 'warning';
  if (t === 3) return 'info';
  return 'success';
}

onMounted(async () => {
  try {
    announcement.value = await announcementApi.userDetail(
      Number(route.params.id),
    );
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div style="max-width: 760px; margin: 0 auto; padding: 40px 24px">
    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <article v-else-if="announcement">
      <div
        style="
          display: flex;
          gap: 8px;
          align-items: center;
          margin-bottom: 16px;
        "
      >
        <n-tag
          :type="getTypeType(announcement.type)"
          size="small"
        >
          {{ announcement.typeName }}
        </n-tag>
        <span style="font-size: 13px; opacity: 0.5">
          {{ announcement.publishedAt }}
        </span>
      </div>
      <h1
        style="
          font-size: 32px;
          font-weight: 700;
          margin-bottom: 24px;
          letter-spacing: -0.02em;
        "
      >
        {{ announcement.title }}
      </h1>
      <div
        class="prose-content"
        v-html="renderedContent"
        style="
          font-size: 16px;
          line-height: 1.75;
          opacity: 0.8;
        "
      />
    </article>
  </div>
</template>

<style scoped>
.prose-content :deep(h1),
.prose-content :deep(h2),
.prose-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
  opacity: 1;
}
.prose-content :deep(p) {
  margin-bottom: 12px;
}
.prose-content :deep(code) {
  background: rgba(128, 128, 128, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
}
.prose-content :deep(pre) {
  background: rgba(128, 128, 128, 0.08);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 16px;
}
.prose-content :deep(pre code) {
  background: none;
  padding: 0;
}
</style>
