<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { announcementApi } from '#/api/modules';

const router = useRouter();

const announcements = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const typeFilter = ref<number | null>(null);

const typeTabs = [
  { label: 'All', value: null },
  { label: 'Notice', value: 1 },
  { label: 'Activity', value: 2 },
  { label: 'Update', value: 3 },
];

function getTypeType(t: number): 'default' | 'success' | 'warning' | 'info' {
  if (t === 2) return 'warning';
  if (t === 3) return 'info';
  return 'success';
}

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
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <h1
      style="
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 24px;
      "
    >
      Announcements
    </h1>

    <n-space style="margin-bottom: 20px">
      <n-button
        v-for="tp in typeTabs"
        :key="String(tp.value)"
        :type="typeFilter === tp.value ? 'primary' : 'default'"
        size="small"
        @click="handleTabChange(tp.value)"
      >
        {{ tp.label }}
      </n-button>
    </n-space>

    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <template v-else>
      <n-card
        v-for="ann in announcements"
        :key="ann.id"
        hoverable
        style="margin-bottom: 12px; cursor: pointer"
        @click="router.push('/announcement/' + ann.id)"
      >
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
          "
        >
          <div style="flex: 1">
            <div
              style="
                display: flex;
                gap: 8px;
                align-items: center;
                margin-bottom: 8px;
              "
            >
              <n-tag :type="getTypeType(ann.type)" size="small">
                {{ ann.typeName }}
              </n-tag>
              <n-tag v-if="ann.isTop" type="error" size="small" :bordered="false">
                TOP
              </n-tag>
            </div>
            <h3
              style="
                font-size: 16px;
                font-weight: 600;
                margin: 0;
              "
            >
              {{ ann.title }}
            </h3>
          </div>
          <span
            style="
              font-size: 13px;
              opacity: 0.5;
              white-space: nowrap;
              margin-left: 16px;
            "
          >
            {{ ann.publishedAt }}
          </span>
        </div>
      </n-card>

      <n-empty
        v-if="!announcements.length"
        description="No announcements"
        style="padding: 60px"
      />
    </template>

    <div
      v-if="total > 10"
      style="display: flex; justify-content: center; margin-top: 32px"
    >
      <n-pagination
        :page="page"
        :page-count="Math.ceil(total / 10)"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>
