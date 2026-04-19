<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { announcementApi } from '@/api'
import { marked } from 'marked'

const { t } = useI18n()
const route = useRoute()
const announcement = ref<any>(null)
const loading = ref(true)

const renderedContent = computed(() => {
  if (!announcement.value?.content) return ''
  return marked(announcement.value.content)
})

onMounted(async () => {
  try {
    const res = await announcementApi.userDetail(Number(route.params.id))
    announcement.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<template>
  <div style="max-width: 760px; margin: 0 auto; padding: 40px 24px;">
    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>
    <article v-else-if="announcement">
      <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 16px;">
        <span class="badge" :class="announcement.type === 2 ? 'badge-warning' : announcement.type === 3 ? 'badge-info' : 'badge-success'">{{ announcement.typeName }}</span>
        <span style="font-size: 13px; color: var(--color-text-light);">{{ announcement.publishedAt }}</span>
      </div>
      <h1 style="font-size: 32px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 24px; letter-spacing: -0.02em;">{{ announcement.title }}</h1>
      <div v-html="renderedContent" style="font-size: 16px; line-height: 1.75; color: var(--color-text-mid);" class="prose-content"></div>
    </article>
  </div>
</template>

<style scoped>
.prose-content :deep(h1), .prose-content :deep(h2), .prose-content :deep(h3) {
  color: var(--color-text-dark);
  margin-top: 24px;
  margin-bottom: 12px;
}
.prose-content :deep(p) { margin-bottom: 12px; }
.prose-content :deep(code) { background: var(--color-bg-secondary); padding: 2px 6px; border-radius: 4px; font-size: 14px; }
.prose-content :deep(pre) { background: var(--color-bg-secondary); padding: 16px; border-radius: 8px; overflow-x: auto; margin-bottom: 16px; }
</style>
