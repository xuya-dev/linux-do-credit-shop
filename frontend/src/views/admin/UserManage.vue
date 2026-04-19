<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { userApi } from '@/api'

const { t } = useI18n()
const message = useMessage()
const users = ref<any[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const keyword = ref('')

async function loadUsers() {
  loading.value = true
  try {
    const res = await userApi.adminList({ page: page.value, size: 10, keyword: keyword.value || undefined })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function toggleStatus(id: number, currentStatus: number) {
  try { await userApi.adminUpdateStatus(id, currentStatus === 1 ? 0 : 1); message.success(t('common.success')); loadUsers() }
  catch (e: any) { message.error(e.message) }
}

async function toggleRole(id: number, currentRole: string) {
  try { await userApi.adminUpdateRole(id, currentRole === 'admin' ? 'user' : 'admin'); message.success(t('common.success')); loadUsers() }
  catch (e: any) { message.error(e.message) }
}

onMounted(loadUsers)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="font-size: 20px; font-weight: 700; color: var(--color-text-dark);">{{ t('admin.user.manageTitle') }}</h2>
      <input v-model="keyword" @keyup.enter="page = 1; loadUsers()" :placeholder="t('common.search')" style="padding: 6px 12px; border: 1px solid #d1d5db; border-radius: 6px; width: 240px; font-size: 14px;" />
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="background: #f9fafc; border-bottom: 2px solid #e5e7eb;">
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('user.username') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('user.nickname') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('user.role') }}</th>
            <th style="text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.status') }}</th>
            <th style="text-align: right; padding: 12px 16px; font-size: 12px; font-weight: 600; color: #6b7280;">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id" style="border-bottom: 1px solid #e5e7eb;">
            <td style="padding: 12px 16px; display: flex; align-items: center; gap: 8px;">
              <img v-if="u.avatar" :src="u.avatar" style="width: 28px; height: 28px; border-radius: 50%;" />
              <span style="font-size: 14px; font-weight: 500; color: #111827;">{{ u.username }}</span>
            </td>
            <td style="padding: 12px 16px; font-size: 14px; color: #374151;">{{ u.nickname || '-' }}</td>
            <td style="padding: 12px 16px;"><span class="badge" :class="u.role === 'admin' ? 'badge-info' : 'badge-success'">{{ u.role === 'admin' ? t('user.admin') : t('user.normalUser') }}</span></td>
            <td style="padding: 12px 16px;"><span class="badge" :class="u.status === 1 ? 'badge-success' : 'badge-error'">{{ u.status === 1 ? '正常' : '禁用' }}</span></td>
            <td style="padding: 12px 16px; text-align: right; display: flex; gap: 4px; justify-content: flex-end;">
              <button @click="toggleRole(u.id, u.role)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #3b82f6;">{{ u.role === 'admin' ? t('admin.user.setUser') : t('admin.user.setAdmin') }}</button>
              <button @click="toggleStatus(u.id, u.status)" style="background: none; border: none; cursor: pointer; font-size: 13px; color: #dc2626;">{{ u.status === 1 ? t('admin.user.disable') : t('admin.user.enable') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
