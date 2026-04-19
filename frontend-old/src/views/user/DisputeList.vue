<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { disputeApi } from '@/api'

const { t } = useI18n()
const disputes = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref<number | null>(null)

async function loadDisputes() {
  loading.value = true
  try {
    const res = await disputeApi.userList({ page: page.value, size: 10, status: statusFilter.value ?? undefined })
    disputes.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadDisputes)
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px;">
    <h1 style="font-size: 28px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 24px;">{{ t('dispute.title') }}</h1>

    <div style="display: flex; gap: 8px; margin-bottom: 20px;">
      <button v-for="s in [null, 0, 1, 2, 3]" :key="String(s)" @click="statusFilter = s; page = 1; loadDisputes()" :style="{ padding: '6px 14px', borderRadius: '6px', border: statusFilter === s ? '1px solid var(--color-primary)' : '1px solid var(--color-border)', background: statusFilter === s ? 'var(--color-primary)' : 'transparent', color: statusFilter === s ? 'white' : 'var(--color-text-mid)', cursor: 'pointer', fontSize: '13px' }">
        {{ s === null ? t('common.all') : s === 0 ? t('dispute.pending') : s === 1 ? t('dispute.accepted') : s === 2 ? t('dispute.rejected') : t('dispute.platform') }}
      </button>
    </div>

    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>
    <div v-else>
      <div v-for="d in disputes" :key="d.id" class="feature-card" style="margin-bottom: 12px;">
        <div style="display: flex; justify-content: space-between;">
          <div>
            <h3 style="font-size: 15px; font-weight: 600; color: var(--color-text-dark);">{{ d.orderNo }}</h3>
            <p style="font-size: 13px; color: var(--color-text-light); margin-top: 2px;">{{ d.productName }}</p>
          </div>
          <span class="badge" :class="d.status === 0 ? 'badge-warning' : d.status === 1 ? 'badge-success' : d.status === 2 ? 'badge-error' : 'badge-info'">{{ d.statusName }}</span>
        </div>
        <p style="font-size: 14px; color: var(--color-text-mid); margin-top: 8px;">{{ d.reason }}</p>
        <p style="font-size: 12px; color: var(--color-text-light); margin-top: 8px;">{{ d.createdAt }}</p>
      </div>
      <div v-if="!disputes.length" style="text-align: center; padding: 60px; color: var(--color-text-light);">{{ t('app.noData') }}</div>
    </div>
  </div>
</template>
