<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { announcementApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const announcements = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const showModal = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ title: '', type: 1, content: '', status: 0 });

const typeMap = computed(() => ({
  1: { label: t('page.admin.notice'), type: 'info' as const },
  2: { label: t('page.admin.announcementTitle'), type: 'success' as const },
  3: { label: t('page.admin.activity'), type: 'warning' as const },
}));

async function loadAnnouncements() {
  loading.value = true;
  try {
    const res = await announcementApi.adminList({ page: page.value, size: 10 });
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
  form.value = { title: row.title, type: row.type, content: row.content, status: row.status };
  showModal.value = true;
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await announcementApi.adminUpdate(editingId.value, form.value);
    } else {
      await announcementApi.adminCreate(form.value);
    }
    message.success(t('page.admin.operationSuccess'));
    showModal.value = false;
    loadAnnouncements();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

async function togglePublish(row: any) {
  try {
    if (row.status === 1) {
      await announcementApi.adminUnpublish(row.id);
    } else {
      await announcementApi.adminPublish(row.id);
    }
    message.success(t('page.admin.operationSuccess'));
    loadAnnouncements();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

async function toggleTop(row: any) {
  try {
    await announcementApi.adminToggleTop(row.id);
    message.success(t('page.admin.operationSuccess'));
    loadAnnouncements();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: t('page.admin.confirm'),
    content: t('page.admin.confirmDeleteContent'),
    positiveText: t('page.admin.confirm'),
    negativeText: t('page.admin.cancel'),
    onPositiveClick: async () => {
      try {
        await announcementApi.adminDelete(id);
        message.success(t('page.admin.deleteSuccess'));
        loadAnnouncements();
      } catch (e: any) {
        message.error(e.message || t('page.admin.deleteFailed'));
      }
    },
  });
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('page.admin.announcementTitle'), key: 'title', ellipsis: { tooltip: true } },
  {
    title: t('page.admin.announcementType'),
    key: 'type',
    width: 80,
    render: (row: any) => {
      const info = typeMap.value[row.type as keyof typeof typeMap.value] || { label: String(row.type), type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.status'),
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, {
        default: () => row.status === 1 ? t('page.admin.published') : t('page.admin.draft'),
      }),
  },
  {
    title: t('page.admin.isTop'),
    key: 'isTop',
    width: 60,
    render: (row: any) =>
      row.isTop ? h(NTag, { type: 'warning', size: 'small' }, { default: () => t('page.admin.yes') }) : null,
  },
  {
    title: t('page.admin.actions'),
    key: 'actions',
    width: 300,
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', type: row.isTop ? 'warning' : 'default', onClick: () => toggleTop(row) }, {
            default: () => row.isTop ? t('page.admin.no') : t('page.admin.isTop'),
          }),
          h(NButton, { size: 'small', type: row.status === 1 ? 'warning' : 'success', onClick: () => togglePublish(row) }, {
            default: () => row.status === 1 ? t('page.admin.draft') : t('page.admin.published'),
          }),
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => t('page.admin.edit') }),
          h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => t('page.admin.delete') }),
        ],
      }),
  },
]);

onMounted(loadAnnouncements);
</script>

<template>
  <div class="p-5">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">{{ t('page.admin.announcements') }}</n-h3>
      <n-button type="primary" @click="openCreate">{{ t('page.admin.create') }}</n-button>
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
      :title="editingId ? t('page.admin.edit') : t('page.admin.create')"
      preset="card"
      style="width: 600px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item :label="t('page.admin.announcementTitle')">
          <n-input v-model:value="form.title" :placeholder="t('page.admin.announcementTitle')" />
        </n-form-item>
        <n-form-item :label="t('page.admin.announcementType')">
          <n-select
            v-model:value="form.type"
            :options="[
              { label: t('page.admin.notice'), value: 1 },
              { label: t('page.admin.announcementTitle'), value: 2 },
              { label: t('page.admin.activity'), value: 3 },
            ]"
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.announcementContent')">
          <n-input
            v-model:value="form.content"
            type="textarea"
            :rows="8"
            :placeholder="t('page.admin.announcementContent')"
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.status')">
          <n-switch v-model:value="form.status" :unchecked-value="0" :checked-value="1">
            <template #checked>{{ t('page.admin.published') }}</template>
            <template #unchecked>{{ t('page.admin.draft') }}</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">{{ t('page.admin.cancel') }}</n-button>
          <n-button type="primary" @click="handleSubmit">{{ t('page.admin.save') }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
