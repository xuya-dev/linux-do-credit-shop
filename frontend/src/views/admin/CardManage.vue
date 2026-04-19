<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { cardApi, productApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const cards = ref<any[]>([])
const products = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const filterProductId = ref<number | null>(null)
const filterStatus = ref<number | null>(null)
const showImport = ref(false)
const importForm = ref({ productId: null as number | null, cards: '' })

async function loadCards() {
  loading.value = true
  try {
    const res = await cardApi.adminList({ page: page.value, size: 20, productId: filterProductId.value ?? undefined, status: filterStatus.value ?? undefined })
    cards.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleImport() {
  if (!importForm.value.productId || !importForm.value.cards.trim()) {
    message.error('请填写完整 / Please fill all fields')
    return
  }
  try {
    const cardsList = importForm.value.cards.split('\n').filter(l => l.trim())
    const res = await cardApi.adminImport({ productId: importForm.value.productId, cards: cardsList })
    message.success(t('admin.card.importSuccess', { n: res.data }))
    showImport.value = false
    importForm.value = { productId: null, cards: '' }
    loadCards()
  } catch (e: any) { message.error(e.message) }
}

async function deleteCard(id: number) {
  try {
    await cardApi.adminDelete(id)
    message.success(t('common.success'))
    loadCards()
  } catch (e: any) { message.error(e.message) }
}

onMounted(async () => {
  const prodRes = await productApi.adminList({ page: 1, size: 100 })
  products.value = prodRes.data?.records || []
  loadCards()
})
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('admin.card.title') }}</h2>
      <button @click="showImport = true" style="background: #3b82f6; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px;">{{ t('admin.card.importTitle') }}</button>
    </div>

    <!-- 导入弹窗 / Import Modal -->
    <div v-if="showImport" style="position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100;" @click.self="showImport = false">
      <div class="card" style="width: 500px;">
        <h3 style="font-size: 18px; font-weight: 700; color: var(--color-text-dark); margin-bottom: 16px;">{{ t('admin.card.importTitle') }}</h3>
        <div style="margin-bottom: 12px;">
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.card.productId') }}</label>
          <select v-model="importForm.productId" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;">
            <option :value="null" disabled>选择商品</option>
            <option v-for="p in products.filter(p => p.productType === 1)" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div style="margin-bottom: 16px;">
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.card.content') }}</label>
          <textarea v-model="importForm.cards" rows="8" placeholder="每行一个卡密 / One card per line" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 13px; font-family: monospace; resize: vertical;"></textarea>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <button @click="showImport = false" style="padding: 8px 16px; border: 1px solid #d1d5db; background: none; border-radius: 6px; cursor: pointer;">{{ t('common.cancel') }}</button>
          <button @click="handleImport" style="padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">{{ t('common.submit') }}</button>
        </div>
      </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafc; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">ID</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('admin.card.productId') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('admin.card.content') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.status') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in cards" :key="c.id" style="border-bottom: 1px solid #e5e7eb;">
            <td style="padding: 12px 16px; font-size: 14px; color: #6b7280;">{{ c.id }}</td>
            <td style="padding: 12px 16px; font-size: 14px; color: #374151;">{{ c.productId }}</td>
            <td style="padding: 12px 16px; font-size: 13px; font-family: monospace; color: #111827; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ c.cardContent }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="c.status === 0 ? 'badge-success' : c.status === 1 ? 'badge-warning' : 'badge-error'">{{ c.status === 0 ? t('admin.card.available') : c.status === 1 ? t('admin.card.sold') : t('admin.card.disabled') }}</span></td>
            <td style="padding: 12px 16px; text-align: right;">
              <button v-if="c.status === 0" @click="deleteCard(c.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #dc2626;">{{ t('common.delete') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
