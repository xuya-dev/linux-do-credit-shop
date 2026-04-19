<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { categoryApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const categories = ref<any[]>([])
const loading = ref(true)
const showCreate = ref(false)
const form = ref({ name: '', icon: '', sortOrder: 0, status: 1 })

async function loadCategories() {
  loading.value = true
  try {
    const res = await categoryApi.adminList({ page: 1, size: 100 })
    categories.value = res.data?.records || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function createCategory() {
  try {
    await categoryApi.adminCreate(form.value)
    message.success(t('common.success'))
    showCreate.value = false
    form.value = { name: '', icon: '', sortOrder: 0, status: 1 }
    loadCategories()
  } catch (e: any) { message.error(e.message) }
}

async function deleteCategory(id: number) {
  if (!confirm('确认删除？')) return
  try {
    await categoryApi.adminDelete(id)
    message.success(t('common.success'))
    loadCategories()
  } catch (e: any) { message.error(e.message) }
}

onMounted(loadCategories)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('nav.categories') }}</h2>
      <button @click="showCreate = true" style="background: #3b82f6; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px;">{{ t('common.create') }}</button>
    </div>

    <!-- 创建表单 / Create Form -->
    <div v-if="showCreate" class="card" style="margin-bottom: 20px;">
      <div style="display: flex; gap: 12px; align-items: flex-end;">
        <div style="flex: 1;">
          <label style="font-size: 12px; color: #6b7280; display: block; margin-bottom: 4px;">{{ t('admin.product.name') }}</label>
          <input v-model="form.name" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px;" />
        </div>
        <div style="width: 120px;">
          <label style="font-size: 12px; color: #6b7280; display: block; margin-bottom: 4px;">{{ t('admin.product.sortOrder') }}</label>
          <input v-model.number="form.sortOrder" type="number" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px;" />
        </div>
        <button @click="createCategory" style="padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">{{ t('common.save') }}</button>
        <button @click="showCreate = false" style="padding: 8px 16px; border: 1px solid #d1d5db; background: none; border-radius: 6px; cursor: pointer;">{{ t('common.cancel') }}</button>
      </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafb; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">ID</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('admin.product.name') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('admin.product.sortOrder') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.status') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in categories" :key="c.id" style="border-bottom: 1px solid #e5e7eb;">
            <td style="padding: 12px 16px; font-size: 14px; color: #6b7280;">{{ c.id }}</td>
            <td style="padding: 12px 16px; font-size: 14px; font-weight: 500; color: #111827;">{{ c.name }}</td>
            <td style="padding: 12px 16px; font-size: 14px; color: #374151;">{{ c.sortOrder }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="c.status === 1 ? 'badge-success' : 'badge-error'">{{ c.status === 1 ? '启用' : '禁用' }}</span></td>
            <td style="padding: 12px 16px; text-align: right;">
              <button @click="deleteCategory(c.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #dc2626;">{{ t('common.delete') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
