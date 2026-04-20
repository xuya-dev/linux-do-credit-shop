<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import type { Announcement } from '#/api/types';
import { ANNOUNCEMENT_TYPE_MAP } from '#/api/types';

import { announcementApi } from '#/api/modules';

const router = useRouter();

const announcements = ref<Announcement[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const typeFilter = ref<number | null>(null);

const typeTabs = [
  { label: '全部', value: null },
  { label: '通知', value: 1 },
  { label: '活动', value: 2 },
  { label: '更新', value: 3 },
];

async function loadAnnouncements() {
  loading.value = true;
  try {
    const res = await announcementApi.userList({
      page: page.value,
      size: 10,
      type: typeFilter.value ?? undefined,
    });
    announcements.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

onMounted(loadAnnouncements);

function handleTabChange(value: number | null) {
  typeFilter.value = value;
  page.value = 1;
  loadAnnouncements();
}

function handlePageChange(p: number) {
  page.value = p;
  loadAnnouncements();
}

function goDetail(id: number) {
  router.push(`/announcement/${id}`);
}
</script>

<template>
  <div class="announcement-list-page">
    <div class="page-header">
      <h1 class="page-title">公告中心</h1>
      <p class="page-subtitle">获取最新的活动、通知和更新信息</p>
    </div>

    <n-tabs
      :value="typeFilter"
      type="segment"
      class="type-tabs"
      @update:value="handleTabChange"
    >
      <n-tab-pane
        v-for="tab in typeTabs"
        :key="String(tab.value)"
        :name="tab.value"
        :tab="tab.label"
      />
    </n-tabs>

    <div v-if="loading" class="announcement-skeleton-list">
      <n-card v-for="i in 3" :key="i" class="announcement-skeleton">
        <n-skeleton text :repeat="3" />
      </n-card>
    </div>

    <template v-else-if="announcements.length">
      <div class="announcement-list">
        <n-card
          v-for="ann in announcements"
          :key="ann.id"
          hoverable
          class="announcement-card"
          @click="goDetail(ann.id)"
        >
          <div class="announcement-content">
            <div class="announcement-tags">
              <n-tag
                :type="ANNOUNCEMENT_TYPE_MAP[ann.type]?.type || 'default'"
                size="small"
                :bordered="false"
              >
                {{ ann.typeName }}
              </n-tag>
              <n-tag v-if="ann.isTop" type="error" size="small" :bordered="false">
                置顶
              </n-tag>
            </div>
            <h3 class="announcement-title">{{ ann.title }}</h3>
            <div class="announcement-footer">
              <span class="announcement-date">{{ ann.publishedAt }}</span>
              <span class="announcement-arrow">→</span>
            </div>
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

    <n-empty v-else description="暂无公告" />
  </div>
</template>

<style scoped>
.announcement-list-page {
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

.type-tabs {
  margin-bottom: 24px;
}

.announcement-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.announcement-card:hover {
  transform: translateY(-2px);
}

.announcement-content {
  padding: 8px 4px;
}

.announcement-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.announcement-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px;
  line-height: 1.4;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-date {
  font-size: 13px;
  opacity: 0.4;
}

.announcement-arrow {
  font-size: 18px;
  opacity: 0.3;
  transition: opacity 0.2s, transform 0.2s;
}

.announcement-card:hover .announcement-arrow {
  opacity: 0.6;
  transform: translateX(4px);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .announcement-list-page {
    padding: 20px 16px;
  }
}
</style>
