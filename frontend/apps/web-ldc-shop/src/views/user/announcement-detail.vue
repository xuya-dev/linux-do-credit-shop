<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';
import { announcementApi } from '#/api/modules';
import '#/styles/faka-common.css';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const { t } = useI18n();

const ann = ref<any>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    ann.value = await announcementApi.detail(Number(route.params.id));
  } catch {
    message.error(t('page.user.loadAnnouncementFailed'));
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/announcements')">
      <span>{{ t('page.user.siteAnnouncements') }}</span> &gt; <span>{{ t('page.user.announcementDetail') }}</span>
    </div>

    <div class="faka-card">
      <div v-if="loading" class="loading-spin">
        <n-spin size="large" />
      </div>

      <template v-else-if="ann">
        <div class="ann-header">
          <h1 class="ann-title">{{ ann.title }}</h1>
          <div class="ann-meta">
            {{ t('page.user.publishedAt') }} {{ ann.createdAt }}
          </div>
        </div>
        <div class="ann-content html-content">
          <p>{{ ann.content }}</p>
        </div>
      </template>

      <div v-else class="empty-state">
        {{ t('page.user.announcementNotFound') }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-container { max-width: 1000px; }
.breadcrumb { margin-bottom: 20px; }

.faka-card {
  padding: 40px;
}

.loading-spin { text-align: center; padding: 60px; }
.empty-state { padding: 60px; }

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
