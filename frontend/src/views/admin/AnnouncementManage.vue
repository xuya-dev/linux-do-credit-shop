<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { announcementApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const announcements = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const showCreate = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ title: '', content: '', type: 1, status: 0, isTop: 0, coverImage: '' })

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await announcementApi.adminList({ page: page.value, size: 10 })
    announcements.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function resetForm() {
  form.value = { title: '', content: '', type: 1, status: 0, isTop: 0, coverImage: '' }
  editingId.value = null
}

async function saveAnnouncement() {
  try {
    if (editingId.value) {
      await announcementApi.adminUpdate(editingId.value, form.value)
    } else {
      await announcementApi.adminCreate(form.value)
    }
    message.success(t('common.success'))
    showCreate.value = false
    resetForm()
    loadAnnouncements()
  } catch (e: any) { message.error(e.message) }
}

async function publish(id: number) {
  try { await announcementApi.adminPublish(id); message.success(t('common.success')); loadAnnouncements() }
  catch (e: any) { message.error(e.message) }
}

async function unpublish(id: number) {
  try { await announcementApi.adminUnpublish(id); message.success(t('common.success')); loadAnnouncements() }
  catch (e: any) { message.error(e.message) }
}

async function toggleTop(id: number) {
  try { await announcementApi.adminToggleTop(id); message.success(t('common.success')); loadAnnouncements() }
  catch (e: any) { message.error(e.message) }
}

async function deleteAnn(id: number) {
  if (!confirm('确认删除？')) return
  try { await announcementApi.adminDelete(id); message.success(t('common.success')); loadAnnouncements() }
  catch (e: any) { message.error(e.message) }
}

onMounted(loadAnnouncements)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('nav.announcements') }}</h2>
      <button @click="resetForm(); showCreate = true" style="background: #3b82f6; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px;">{{ t('common.create') }}</button>
    </div>

    <!-- 创建/编辑 / Create/Edit -->
    <div v-if="showCreate" class="card" style="margin-bottom: 20px;">
      <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">{{ editingId ? t('admin.announcement.editTitle') : t('admin.announcement.createTitle') }}</h3>
      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 12px; margin-bottom: 12px;">
        <div>
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.announcement.title') }}</label>
          <input v-model="form.title" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px;" />
        </div>
        <div>
          <label style="font-size: 12px; color: #6b7280;">{{ t('admin.announcement.type') }}</label>
          <select v-model="form.type" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px;">
            <option :value="1">{{ t('admin.announcement.notice') }}</option>
            <option :value="2">{{ t('admin.announcement.activity') }}</option>
            <option :value="3">{{ t('admin.announcement.update') }}</option>
          </select>
        </div>
      </div>
      <div style="margin-bottom: 12px;">
        <label style="font-size: 12px; color: #6b7280;">{{ t('admin.announcement.content') }} (Markdown)</label>
        <textarea v-model="form.content" rows="6" style="width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; margin-top: 4px; font-size: 14px; font-family: monospace; resize: vertical;"></textarea>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 8px;">
        <button @click="showCreate = false" style="padding: 8px 16px; border: 1px solid #d1d5db; background: none; border-radius: 6px; cursor: pointer;">{{ t('common.cancel') }}</button>
        <button @click="saveAnnouncement" style="padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">{{ t('common.save') }}</button>
      </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafc; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('admin.announcement.title') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('admin.announcement.type') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.status') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('announcement.publishedAt') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in announcements" :key="a.id" style="border-bottom: 1px solid #e5e7eb;">
            <td style="padding: 12px 16px; font-size: 14px; color: #111827; font-weight: 500;">
              <span v-if="a.isTop" style="color: #dc2626; margin-right: 4px;">📌</span>
              {{ a.title }}
            </td>
            <td style="padding: 12px 16px;"><span class="badge" :class="a.type === 2 ? 'badge-warning' : a.type === 3 ? 'badge-info' : 'badge-success'">{{ a.typeName }}</span></td>
            <td style="padding: 12px 16px;"><span class="badge" :class="a.status === 1 ? 'badge-success' : 'badge-error'">{{ a.status === 1 ? t('admin.announcement.published') : t('admin.announcement.draft') }}</span></td>
            <td style="padding: 12px 16px; font-size: 13px; color: #6b7280;">{{ a.publishedAt || '-' }}</td>
            <td style="padding: 12px 16px; text-align: right; display: flex; gap: 4px; justify-content: flex-end;">
              <button @click="toggleTop(a.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #d97706;">{{ a.isTop ? t('admin.announcement.unpin') : t('admin.announcement.pin') }}</button>
              <button v-if="a.status === 0" @click="publish(a.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #059669;">{{ t('admin.announcement.publish') }}</button>
              <button v-else @click="unpublish(a.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #6b7280;">{{ t('admin.announcement.unpublish') }}</button>
              <button @click="deleteAnn(a.id)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #dc2626;">{{ t('common.delete') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
