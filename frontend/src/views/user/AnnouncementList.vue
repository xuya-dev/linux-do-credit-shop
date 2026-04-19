<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { announcementApi } from '@/api'

const { t } = useI18n()
const announcements = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const typeFilter = ref<number | null>(null)

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await announcementApi.userList({ page: page.value, size: 10, type: typeFilter.value ?? undefined })
    announcements.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadAnnouncements)
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <h1 style="font-size: 28px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 24px;">{{ t('announcement.title') }}</h1>

    <div style="display: flex; gap: 8px; margin-bottom: 20px;">
      <button v-for="tp in [null, 1, 2, 3]" :key="String(tp)" @click="typeFilter = tp; page = 1; loadAnnouncements()" :style="{ padding: '6px 14px', borderRadius: '6px', border: typeFilter === tp ? '1px solid var(--color-primary)' : '1px solid var(--color-border)', background: typeFilter === tp ? 'var(--color-primary)' : 'transparent', color: typeFilter === tp ? 'white' : 'var(--color-text-mid)', cursor: 'pointer', fontSize: '13px' }">
        {{ tp === null ? t('announcement.allTypes') : tp === 1 ? t('announcement.notice') : tp === 2 ? t('announcement.activity') : t('announcement.update') }}
      </button>
    </div>

    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>
    <div v-else>
      <div v-for="ann in announcements" :key="ann.id" class="feature-card" style="margin-bottom: 12px; cursor: pointer;" @click="$router.push('/announcement/' + ann.id)">
        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
          <div style="flex: 1;">
            <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px;">
              <span class="badge" :class="ann.type === 2 ? 'badge-warning' : ann.type === 3 ? 'badge-info' : 'badge-success'">{{ ann.typeName }}</span>
              <span v-if="ann.isTop" style="font-size: 11px; font-weight: 600; color: var(--color-error);">TOP</span>
            </div>
            <h3 style="font-size: 16px; font-weight: 600; color: var(--color-text-dark);">{{ ann.title }}</h3>
          </div>
          <span style="font-size: 13px; color: var(--color-text-light); white-space: nowrap; margin-left: 16px;">{{ ann.publishedAt }}</span>
        </div>
      </div>
      <div v-if="!announcements.length" style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.noData') }}</div>
    </div>
  </div>
</template>
