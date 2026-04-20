<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { marked } from 'marked';

import type { Announcement } from '#/api/types';
import { ANNOUNCEMENT_TYPE_MAP } from '#/api/types';

import { announcementApi } from '#/api/modules';

const route = useRoute();
const router = useRouter();

const announcement = ref<Announcement | null>(null);
const loading = ref(true);

const renderedContent = computed(() => {
  if (!announcement.value?.content) return '';
  return marked(announcement.value.content) as string;
});

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
  <div class="announcement-detail-page">
    <n-button text class="back-btn" @click="router.back()">
      ← 返回公告列表
    </n-button>

    <n-spin v-if="loading" size="large" style="padding: 80px" />

    <article v-else-if="announcement" class="article">
      <div class="article-header">
        <div class="article-tags">
          <n-tag
            :type="ANNOUNCEMENT_TYPE_MAP[announcement.type]?.type || 'default'"
            size="small"
          >
            {{ announcement.typeName }}
          </n-tag>
          <span class="article-date">{{ announcement.publishedAt }}</span>
        </div>
        <h1 class="article-title">{{ announcement.title }}</h1>
      </div>

      <div
        class="article-body"
        v-html="renderedContent"
      />
    </article>

    <n-empty v-else description="公告不存在或已删除" />
  </div>
</template>

<style scoped>
.announcement-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.back-btn {
  margin-bottom: 20px;
}

.article {
  padding: 32px;
  border-radius: 16px;
  background: rgba(128, 128, 128, 0.03);
  border: 1px solid rgba(128, 128, 128, 0.08);
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(128, 128, 128, 0.1);
}

.article-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.article-date {
  font-size: 13px;
  opacity: 0.4;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.article-body {
  font-size: 16px;
  line-height: 1.8;
  opacity: 0.85;
}

.article-body :deep(h1),
.article-body :deep(h2),
.article-body :deep(h3) {
  margin-top: 32px;
  margin-bottom: 16px;
  opacity: 1;
  font-weight: 600;
}

.article-body :deep(h1) {
  font-size: 24px;
}

.article-body :deep(h2) {
  font-size: 20px;
}

.article-body :deep(h3) {
  font-size: 18px;
}

.article-body :deep(p) {
  margin-bottom: 16px;
}

.article-body :deep(ul),
.article-body :deep(ol) {
  margin-bottom: 16px;
  padding-left: 24px;
}

.article-body :deep(li) {
  margin-bottom: 8px;
}

.article-body :deep(code) {
  background: rgba(128, 128, 128, 0.12);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-family: monospace;
}

.article-body :deep(pre) {
  background: rgba(128, 128, 128, 0.08);
  padding: 20px;
  border-radius: 10px;
  overflow-x: auto;
  margin-bottom: 20px;
}

.article-body :deep(pre code) {
  background: none;
  padding: 0;
}

.article-body :deep(blockquote) {
  border-left: 4px solid #18a058;
  padding-left: 16px;
  margin: 20px 0;
  opacity: 0.7;
}

.article-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

.article-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

.article-body :deep(th),
.article-body :deep(td) {
  padding: 10px 12px;
  border: 1px solid rgba(128, 128, 128, 0.15);
  text-align: left;
}

.article-body :deep(th) {
  background: rgba(128, 128, 128, 0.06);
  font-weight: 600;
}

@media (max-width: 768px) {
  .article {
    padding: 20px;
  }

  .article-title {
    font-size: 24px;
  }
}
</style>
