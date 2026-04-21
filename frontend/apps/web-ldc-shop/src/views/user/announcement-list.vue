<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';
import { announcementApi } from '#/api/modules';
import '#/styles/faka-common.css';

const router = useRouter();
const message = useMessage();
const { t } = useI18n();

const announcements = ref<any[]>([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await announcementApi.userList({ page: 1, size: 20 });
    announcements.value = res?.records || [];
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.user.loadAnnouncementsFailed'));
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>{{ t('page.user.home') }}</span> &gt; <span>{{ t('page.user.siteAnnouncements') }}</span>
    </div>

    <div class="faka-card">
      <div class="card-header">{{ t('page.user.announcementList') }}</div>
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
              <span class="ann-badge">{{ t('page.user.announcement') }}</span>
              {{ ann.title }}
            </div>
            <div class="ann-date">{{ ann.createdAt }}</div>
          </div>
        </div>

        <div v-else class="empty-state">
          {{ t('page.user.noAnnouncements') }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 1000px; }
.breadcrumb { margin-bottom: 20px; }

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
