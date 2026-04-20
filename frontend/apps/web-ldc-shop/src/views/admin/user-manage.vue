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
const keyword = ref('');

async function loadUsers() {
  loading.value = true;
  try {
    const res = await userApi.adminList({
      page: page.value,
      size: 10,
      keyword: keyword.value || undefined,
    });
    users.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function toggleStatus(id: number, currentStatus: number) {
  try {
    await userApi.adminUpdateStatus(id, currentStatus === 1 ? 0 : 1);
    message.success(t('page.admin.operationSuccess'));
    loadUsers();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

function confirmToggleRole(id: number, currentRole: string) {
  const newRole = currentRole === 'admin' ? 'user' : 'admin';
  dialog.warning({
    title: t('page.admin.confirm'),
    content: `${t('page.admin.role')}: ${newRole === 'admin' ? t('page.shop.admin') : t('page.shop.user')}`,
    positiveText: t('page.admin.confirm'),
    negativeText: t('page.admin.cancel'),
    onPositiveClick: async () => {
      try {
        await userApi.adminUpdateRole(id, newRole);
        message.success(t('page.admin.operationSuccess'));
        loadUsers();
      } catch (e: any) {
        message.error(e.message || t('page.admin.operationFailed'));
      }
    },
  });
}

const columns = computed(() => [
  {
    title: t('page.admin.username'),
    key: 'username',
    render: (row: any) =>
      h(NSpace, { align: 'center', size: 'small' }, {
        default: () => [
          h(NAvatar, { src: row.avatar, size: 'small', round: true }),
          h('span', { style: 'font-weight: 500' }, row.username),
        ],
      }),
  },
  { title: t('page.shop.nickname'), key: 'nickname', width: 120 },
  {
    title: t('page.admin.role'),
    key: 'role',
    width: 100,
    render: (row: any) => {
      const isAdmin = row.role === 'admin';
      return h(NTag, { type: isAdmin ? 'success' : 'info', size: 'small' }, {
        default: () => isAdmin ? t('page.shop.admin') : t('page.shop.user'),
      });
    },
  },
  {
    title: t('page.admin.status'),
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, {
        default: () => row.status === 1 ? t('page.admin.enabled') : t('page.admin.disabled'),
      }),
  },
  {
    title: t('page.admin.actions'),
    key: 'actions',
    width: 220,
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: row.status === 1 ? 'warning' : 'success',
            onClick: () => toggleStatus(row.id, row.status),
          }, { default: () => row.status === 1 ? t('page.admin.disable') : t('page.admin.enable') }),
          h(NButton, {
            size: 'small',
            type: row.role === 'admin' ? 'warning' : 'info',
            onClick: () => confirmToggleRole(row.id, row.role),
          }, { default: () => row.role === 'admin' ? t('page.shop.user') : t('page.shop.admin') }),
        ],
      }),
  },
]);

onMounted(loadUsers);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">{{ t('page.admin.users') }}</n-h3>
      <n-space align="center">
        <n-input
          v-model:value="keyword"
          :placeholder="t('page.admin.searchUsers')"
          style="width: 240px"
          @keyup.enter="page = 1; loadUsers()"
        />
        <n-button @click="page = 1; loadUsers()">{{ t('page.admin.search') }}</n-button>
      </n-space>
    </div>

    <n-data-table
      :columns="columns"
      :data="users"
      :loading="loading"
      :pagination="{ page: page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadUsers() } }"
      :bordered="false"
    />
  </div>
</template>
