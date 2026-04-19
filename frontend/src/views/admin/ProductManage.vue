<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { productApi, categoryApi } from '@/api'

const { t } = useI18n()
const message = useMessage()

const products = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const keyword = ref('')

async function loadProducts() {
  loading.value = true
  try {
    const res = await productApi.adminList({ page: page.value, size: 10, keyword: keyword.value || undefined })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function toggleStatus(id: number, currentStatus: number) {
  try {
    await productApi.adminUpdateStatus(id, currentStatus === 1 ? 0 : 1)
    message.success(t('common.success'))
    loadProducts()
  } catch (e: any) { message.error(e.message) }
}

async function deleteProduct(id: number) {
  if (!confirm('确认删除？ / Confirm delete?')) return
  try {
    await productApi.adminDelete(id)
    message.success(t('common.success'))
    loadProducts()
  } catch (e: any) { message.error(e.message) }
}

onMounted(async () => {
  const catRes = await categoryApi.userList()
  categories.value = catRes.data || []
  loadProducts()
})
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('nav.products') }}</h2>
      <div style="display: flex; gap: 8px;">
        <input v-model="keyword" @keyup.enter="page = 1; loadProducts()" :placeholder="t('common.search')" style="padding: 6px 12px; border: 1px solid var(--admin-card-border); border-radius: 6px; font-size: 14px; width: 240px; background: var(--admin-card-bg); color: var(--color-text-dark);" />
        <button @click="page = 1; loadProducts()" style="padding: 6px 16px; border: 1px solid var(--admin-card-border); border-radius: 6px; background: none; cursor: pointer; font-size: 14px; color: var(--color-text-mid);">{{ t('common.search') }}</button>
      </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafb; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.08em;">ID</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('product.name') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('product.type') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('product.price') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('product.stock') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.status') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id" style="border-bottom: 1px solid #e5e7eb; height: 52px;" onmouseover="this.style.background='#f9fafb'" onmouseout="this.style.background='transparent'">
            <td style="padding: 12px 16px; font-size: 14px; color: #6b7280;">{{ p.id }}</td>
            <td style="padding: 12px 16px; font-size: 14px; font-weight: 500; color: #111827;">{{ p.name }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="p.productType === 1 ? 'badge-info' : 'badge-success'">{{ p.productType === 1 ? t('product.virtual') : t('product.physical') }}</span></td>
            <td style="padding: 12px 16px; font-size: 14px; font-weight: 600; color: #2563eb;">{{ p.price }}</td>
            <td style="padding: 12px 16px; font-size: 14px; color: #374151;">{{ p.stock }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="p.status === 1 ? 'badge-success' : 'badge-error'">{{ p.status === 1 ? t('admin.product.onShelf') : t('admin.product.offShelf') }}</span></td>
            <td style="padding: 12px 16px; text-align: right;">
              <button @click="toggleStatus(p.id, p.status)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #3b82f6; padding: 4px 8px;">{{ p.status === 1 ? t('product.offFromShelf') : t('product.addToShelf') }}</button>
              <button @click="deleteProduct(p.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #dc2626; padding: 4px 8px;">{{ t('common.delete') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
