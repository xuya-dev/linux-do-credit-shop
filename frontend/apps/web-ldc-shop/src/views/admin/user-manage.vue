<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace, NAvatar } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { userApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const users = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const searchForm = ref({ keyword: '', role: null as string | null });

const roleOptions = computed(() => [
  { label: t('page.admin.all'), value: null },
  { label: t('page.shop.user'), value: 'user' },
  { label: t('page.shop.admin'), value: 'admin' },
]);

async function loadUsers() {
  loading.value = true;
  try {
    const res = await userApi.adminList({
      page: page.value,
      size: 10,
      keyword: searchForm.value.keyword || undefined,
      role: searchForm.value.role || undefined,
    });
    users.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function handleSearch() { page.value = 1; loadUsers(); }
function resetSearch() { searchForm.value = { keyword: '', role: null }; page.value = 1; loadUsers(); }

async function toggleUserStatus(id: number, currentStatus: number) {
  try {
    await userApi.adminUpdateStatus(id, currentStatus === 1 ? 0 : 1);
    message.success(t('page.admin.operationSuccess'));
    loadUsers();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 60 },
  {
    title: t('page.shop.nickname'), key: 'username', width: 160,
    render: (row: any) => h(NSpace, { align: 'center', size: 'small' }, { default: () => [
      h(NAvatar, { src: row.avatarUrl, size: 28, round: true, fallbackSrc: '/logo.png' }),
      h('span', {}, row.username),
    ]}),
  },
  { title: t('page.shop.email'), key: 'email', ellipsis: { tooltip: true } },
  { title: t('page.shop.trustLevel'), key: 'trustLevel', width: 90 },
  { title: t('page.admin.creditBalance'), key: 'creditBalance', width: 100 },
  {
    title: t('page.admin.role'), key: 'role', width: 80,
    render: (row: any) => h(NTag, { type: row.role === 'admin' ? 'error' : 'info', size: 'small' }, { default: () => row.role === 'admin' ? t('page.shop.admin') : t('page.shop.user') }),
  },
  {
    title: t('page.admin.userStatus'), key: 'status', width: 80,
    render: (row: any) => h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => row.status === 1 ? t('page.admin.enabled') : t('page.admin.disabled') }),
  },
  { title: t('page.shop.joined'), key: 'createdAt', width: 160 },
  {
    title: t('page.admin.actions'), key: 'actions', width: 100,
    render: (row: any) => h(NButton, {
      size: 'small',
      type: row.status === 1 ? 'warning' : 'success',
      onClick: () => toggleUserStatus(row.id, row.status),
    }, { default: () => row.status === 1 ? t('page.admin.disable') : t('page.admin.enable') }),
  },
]);

onMounted(loadUsers);
</script>

<template>
  <div class="p-5">
    <n-card :bordered="false" style="margin-bottom:16px">
      <n-grid :cols="3" :x-gap="16" :y-gap="12">
        <n-gi>
          <n-form-item :label="t('page.admin.username')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-input v-model:value="searchForm.keyword" :placeholder="t('page.admin.searchUsers')" clearable @keyup.enter="handleSearch" />
          </n-form-item>
        </n-gi>
        <n-gi>
          <n-form-item :label="t('page.admin.role')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-select v-model:value="searchForm.role" :options="roleOptions" clearable />
          </n-form-item>
        </n-gi>
        <n-gi></n-gi>
        <n-gi :span="3" style="text-align:right">
          <n-space justify="end">
            <n-button @click="resetSearch">{{ t('page.admin.cancel') }}</n-button>
            <n-button type="primary" @click="handleSearch">{{ t('page.admin.search') }}</n-button>
          </n-space>
        </n-gi>
      </n-grid>
    </n-card>

    <n-card :bordered="false">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <span style="font-size:15px;font-weight:600">{{ t('page.admin.users') }}</span>
      </div>
      <n-data-table :columns="columns" :data="users" :loading="loading"
        :pagination="{ page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadUsers() } }"
        :bordered="false" />
    </n-card>
  </div>
</template>
