<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace, NAvatar } from 'naive-ui';

import { userApi } from '#/api/modules';

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
    message.success('操作成功');
    loadUsers();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  }
}

function confirmToggleRole(id: number, currentRole: string) {
  const newRole = currentRole === 'admin' ? 'user' : 'admin';
  dialog.warning({
    title: '确认修改角色',
    content: `确定要将该用户角色改为 ${newRole === 'admin' ? '管理员' : '普通用户'} 吗？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await userApi.adminUpdateRole(id, newRole);
        message.success('角色修改成功');
        loadUsers();
      } catch (e: any) {
        message.error(e.message || '修改失败');
      }
    },
  });
}

const roleMap: Record<string, { label: string; type: 'success' | 'info' }> = {
  admin: { label: '管理员', type: 'success' },
  user: { label: '用户', type: 'info' },
};

const columns = [
  {
    title: '用户',
    key: 'username',
    render: (row: any) =>
      h(NSpace, { align: 'center', size: 'small' }, {
        default: () => [
          h(NAvatar, {
            src: row.avatar,
            size: 'small',
            round: true,
          }),
          h('span', { style: 'font-weight: 500' }, row.username),
        ],
      }),
  },
  { title: '昵称', key: 'nickname', width: 120 },
  {
    title: '角色',
    key: 'role',
    width: 100,
    render: (row: any) => {
      const info = roleMap[row.role] || { label: row.role, type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === 1 ? '正常' : '禁用') },
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: row.status === 1 ? 'warning' : 'success',
              onClick: () => toggleStatus(row.id, row.status),
            },
            { default: () => (row.status === 1 ? '禁用' : '启用') },
          ),
          h(
            NButton,
            {
              size: 'small',
              type: row.role === 'admin' ? 'warning' : 'info',
              onClick: () => confirmToggleRole(row.id, row.role),
            },
            { default: () => (row.role === 'admin' ? '设为用户' : '设为管理员') },
          ),
        ],
      }),
  },
];

onMounted(loadUsers);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">用户管理</n-h3>
      <n-space align="center">
        <n-input
          v-model:value="keyword"
          placeholder="搜索用户名/昵称..."
          style="width: 240px"
          @keyup.enter="page = 1; loadUsers()"
        />
        <n-button @click="page = 1; loadUsers()">搜索</n-button>
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
