<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';

import { announcementApi } from '#/api/modules';

const message = useMessage();
const dialog = useDialog();

const announcements = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const showModal = ref(false);
const editingId = ref<number | null>(null);
const form = ref({
  title: '',
  type: 1,
  content: '',
  status: 0,
});

const typeMap: Record<number, { label: string; type: 'info' | 'success' | 'warning' }> = {
  1: { label: '通知', type: 'info' },
  2: { label: '公告', type: 'success' },
  3: { label: '活动', type: 'warning' },
};

async function loadAnnouncements() {
  loading.value = true;
  try {
    const res = await announcementApi.adminList({
      page: page.value,
      size: 10,
    });
    announcements.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  form.value = { title: '', type: 1, content: '', status: 0 };
  showModal.value = true;
}

function openEdit(row: any) {
  editingId.value = row.id;
  form.value = {
    title: row.title,
    type: row.type,
    content: row.content,
    status: row.status,
  };
  showModal.value = true;
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await announcementApi.adminUpdate(editingId.value, form.value);
    } else {
      await announcementApi.adminCreate(form.value);
    }
    message.success('操作成功');
    showModal.value = false;
    loadAnnouncements();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  }
}

async function togglePublish(row: any) {
  try {
    if (row.status === 1) {
      await announcementApi.adminUnpublish(row.id);
      message.success('已取消发布');
    } else {
      await announcementApi.adminPublish(row.id);
      message.success('已发布');
    }
    loadAnnouncements();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  }
}

async function toggleTop(row: any) {
  try {
    await announcementApi.adminToggleTop(row.id);
    message.success(row.isTop ? '已取消置顶' : '已置顶');
    loadAnnouncements();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该公告吗？此操作不可撤销。',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await announcementApi.adminDelete(id);
        message.success('删除成功');
        loadAnnouncements();
      } catch (e: any) {
        message.error(e.message || '删除失败');
      }
    },
  });
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: (row: any) => {
      const info = typeMap[row.type] || { label: '未知', type: 'default' as const };
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
        { type: row.status === 1 ? 'success' : 'default', size: 'small' },
        { default: () => (row.status === 1 ? '已发布' : '草稿') },
      ),
  },
  {
    title: '置顶',
    key: 'isTop',
    width: 60,
    render: (row: any) =>
      row.isTop
        ? h(NTag, { type: 'warning', size: 'small' }, { default: () => '置顶' })
        : null,
  },
  {
    title: '发布时间',
    key: 'publishedAt',
    width: 160,
    render: (row: any) => (row.publishedAt ? row.publishedAt : '-'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 300,
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
          h(
            NButton,
            { size: 'small', type: row.isTop ? 'warning' : 'default', onClick: () => toggleTop(row) },
            { default: () => (row.isTop ? '取消置顶' : '置顶') },
          ),
          h(
            NButton,
            { size: 'small', type: row.status === 1 ? 'warning' : 'success', onClick: () => togglePublish(row) },
            { default: () => (row.status === 1 ? '取消发布' : '发布') },
          ),
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openEdit(row) },
            { default: () => '编辑' },
          ),
          h(
            NButton,
            { size: 'small', type: 'error', onClick: () => handleDelete(row.id) },
            { default: () => '删除' },
          ),
        ],
      }),
  },
];

onMounted(loadAnnouncements);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">公告管理</n-h3>
      <n-button type="primary" @click="openCreate">创建公告</n-button>
    </div>

    <n-data-table
      :columns="columns"
      :data="announcements"
      :loading="loading"
      :pagination="{ page: page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadAnnouncements() } }"
      :bordered="false"
    />

    <n-modal
      v-model:show="showModal"
      :title="editingId ? '编辑公告' : '创建公告'"
      preset="card"
      style="width: 600px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item label="标题">
          <n-input v-model:value="form.title" placeholder="请输入公告标题" />
        </n-form-item>
        <n-form-item label="类型">
          <n-select
            v-model:value="form.type"
            :options="[
              { label: '通知', value: 1 },
              { label: '公告', value: 2 },
              { label: '活动', value: 3 },
            ]"
          />
        </n-form-item>
        <n-form-item label="内容">
          <n-input
            v-model:value="form.content"
            type="textarea"
            :rows="8"
            placeholder="公告内容"
          />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :unchecked-value="0" :checked-value="1">
            <template #checked>发布</template>
            <template #unchecked>草稿</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
