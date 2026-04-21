<script setup lang="ts">
import { ref, onMounted, h, computed, reactive } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Announcement, AnnouncementFormData } from '#/api/types';
import { ANNOUNCEMENT_TYPE_MAP } from '#/api/types';
import { announcementApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const announcements = ref<Announcement[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onUpdatePage: (p: number) => {
    page.value = p;
    pagination.page = p;
    loadAnnouncements();
  },
});

const searchForm = ref({ title: '', type: null as number | null, status: null as number | null });

const showModal = ref(false);
const editingId = ref<number | null>(null);
const form = ref<AnnouncementFormData>({ title: '', type: 1, content: '', status: 0, isTop: 0 });

const typeOptions = computed(() => [
  { label: t('page.admin.all'), value: null },
  { label: t('page.admin.notice'), value: 1 },
  { label: t('page.admin.activity'), value: 3 },
  { label: t('page.admin.update'), value: 2 },
]);

const statusOptions = computed(() => [
  { label: t('page.admin.all'), value: null },
  { label: t('page.admin.published'), value: 1 },
  { label: t('page.admin.draft'), value: 0 },
]);

async function loadAnnouncements() {
  loading.value = true;
  try {
    const res = await announcementApi.adminList({
      page: page.value,
      size: 10,
      title: searchForm.value.title || undefined,
      type: searchForm.value.type ?? undefined,
      status: searchForm.value.status ?? undefined,
    });
    announcements.value = res?.records || [];
    total.value = res?.total || 0;
    pagination.itemCount = res?.total || 0;
    pagination.page = page.value;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function handleSearch() { page.value = 1; pagination.page = 1; loadAnnouncements(); }
function resetSearch() { searchForm.value = { title: '', type: null, status: null }; page.value = 1; pagination.page = 1; loadAnnouncements(); }

function openCreate() {
  editingId.value = null;
  form.value = { title: '', type: 1, content: '', status: 0, isTop: 0 };
  showModal.value = true;
}

function openEdit(row: any) {
  editingId.value = row.id;
  form.value = { title: row.title, type: row.type, content: row.content, status: row.status, isTop: row.isTop || 0 };
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
    title: t('page.admin.announcementType'), key: 'type', width: 90,
    render: (row: any) => {
      const info = ANNOUNCEMENT_TYPE_MAP[row.type] || { label: String(row.type), type: 'default' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.status'), key: 'status', width: 80,
    render: (row: any) => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? t('page.admin.published') : t('page.admin.draft') }),
  },
  {
    title: t('page.admin.isTop'), key: 'isTop', width: 70,
    render: (row: any) => h(NTag, { type: row.isTop === 1 ? 'warning' : 'default', size: 'small' }, { default: () => row.isTop === 1 ? t('page.admin.yes') : t('page.admin.no') }),
  },
  { title: t('page.admin.createdAt'), key: 'createdAt', width: 160 },
  {
    title: t('page.admin.actions'), key: 'actions', width: 120,
    render: (row: any) => h(NSpace, { size: 'small' }, { default: () => [
      h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => t('page.admin.edit') }),
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => t('page.admin.delete') }),
    ]}),
  },
]);

onMounted(loadAnnouncements);
</script>

<template>
  <div class="p-5">
    <n-card :bordered="false" style="margin-bottom:16px">
      <n-grid :cols="3" :x-gap="16" :y-gap="12">
        <n-gi>
          <n-form-item :label="t('page.admin.announcementTitle')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-input v-model:value="searchForm.title" :placeholder="t('page.admin.searchAnnouncements')" clearable @keyup.enter="handleSearch" />
          </n-form-item>
        </n-gi>
        <n-gi>
          <n-form-item :label="t('page.admin.announcementType')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-select v-model:value="searchForm.type" :options="typeOptions" clearable />
          </n-form-item>
        </n-gi>
        <n-gi>
          <n-form-item :label="t('page.admin.status')" label-placement="left" :label-width="80" style="margin-bottom:0">
            <n-select v-model:value="searchForm.status" :options="statusOptions" clearable />
          </n-form-item>
        </n-gi>
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
        <span style="font-size:15px;font-weight:600">{{ t('page.admin.announcements') }}</span>
        <n-button type="primary" @click="openCreate">+ {{ t('page.admin.create') }}</n-button>
      </div>
      <n-data-table :columns="columns" :data="announcements" :loading="loading"
        :remote="true"
        :pagination="pagination"
        :bordered="false" />
    </n-card>

    <n-modal v-model:show="showModal" :title="editingId ? t('page.admin.edit') : t('page.admin.create')" preset="card" style="width:600px">
      <n-form label-placement="left" label-width="80">
        <n-form-item :label="t('page.admin.announcementTitle')"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item :label="t('page.admin.announcementType')">
          <n-select v-model:value="form.type" :options="[{ label: t('page.admin.notice'), value: 1 }, { label: t('page.admin.update'), value: 2 }, { label: t('page.admin.activity'), value: 3 }]" />
        </n-form-item>
        <n-form-item :label="t('page.admin.announcementContent')">
          <n-input v-model:value="form.content" type="textarea" :rows="8" />
        </n-form-item>
        <n-form-item :label="t('page.admin.isTop')">
          <n-switch v-model:value="form.isTop" :unchecked-value="0" :checked-value="1">
            <template #checked>{{ t('page.admin.yes') }}</template>
            <template #unchecked>{{ t('page.admin.no') }}</template>
          </n-switch>
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
