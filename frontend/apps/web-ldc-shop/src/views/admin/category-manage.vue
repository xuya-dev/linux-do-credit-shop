<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { categoryApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const categories = ref<any[]>([]);
const loading = ref(true);
const showCreate = ref(false);
const showEdit = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ name: '', sortOrder: 0, status: 1 });

async function loadCategories() {
  loading.value = true;
  try {
    const res = await categoryApi.adminList({ page: 1, size: 100 });
    categories.value = res?.records || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function createCategory() {
  try {
    await categoryApi.adminCreate(form.value);
    message.success(t('page.admin.saveSuccess'));
    showCreate.value = false;
    form.value = { name: '', sortOrder: 0, status: 1 };
    loadCategories();
  } catch (e: any) {
    message.error(e.message || t('page.admin.saveFailed'));
  }
}

function openEdit(row: any) {
  editingId.value = row.id;
  form.value = { name: row.name, sortOrder: row.sortOrder, status: row.status };
  showEdit.value = true;
}

async function updateCategory() {
  try {
    await categoryApi.adminUpdate(editingId.value!, form.value);
    message.success(t('page.admin.saveSuccess'));
    showEdit.value = false;
    loadCategories();
  } catch (e: any) {
    message.error(e.message || t('page.admin.saveFailed'));
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
        await categoryApi.adminDelete(id);
        message.success(t('page.admin.deleteSuccess'));
        loadCategories();
      } catch (e: any) {
        message.error(e.message || t('page.admin.deleteFailed'));
      }
    },
  });
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('page.admin.categoryName'), key: 'name' },
  { title: t('page.admin.categorySort'), key: 'sortOrder', width: 80 },
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
    width: 160,
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEdit(row) }, { default: () => t('page.admin.edit') }),
          h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, { default: () => t('page.admin.delete') }),
        ],
      }),
  },
]);

onMounted(loadCategories);
</script>

<template>
  <div class="p-5">
    <n-card :bordered="false">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <span style="font-size:15px;font-weight:600">{{ t('page.admin.categories') }}</span>
        <n-button type="primary" @click="showCreate = true; form = { name: '', sortOrder: 0, status: 1 }">
          + {{ t('page.admin.create') }}
        </n-button>
      </div>

      <n-card v-if="showCreate" size="small" style="margin-bottom:16px;background:var(--n-color-modal,#fafafa)">
        <n-space align="center">
          <n-input v-model:value="form.name" :placeholder="t('page.admin.categoryName')" style="width:200px" />
          <n-input-number v-model:value="form.sortOrder" :placeholder="t('page.admin.categorySort')" style="width:120px" />
          <n-button type="primary" @click="createCategory">{{ t('page.admin.save') }}</n-button>
          <n-button @click="showCreate = false">{{ t('page.admin.cancel') }}</n-button>
        </n-space>
      </n-card>

      <n-data-table :columns="columns" :data="categories" :loading="loading" :bordered="false" />
    </n-card>

    <n-modal
      v-model:show="showEdit"
      :title="t('page.admin.edit')"
      preset="card"
      style="width: 400px"
    >
      <n-form label-placement="left" label-width="60">
        <n-form-item :label="t('page.admin.categoryName')">
          <n-input v-model:value="form.name" />
        </n-form-item>
        <n-form-item :label="t('page.admin.categorySort')">
          <n-input-number v-model:value="form.sortOrder" style="width: 100%" />
        </n-form-item>
        <n-form-item :label="t('page.admin.status')">
          <n-switch v-model:value="form.status" :unchecked-value="0" :checked-value="1">
            <template #checked>{{ t('page.admin.enable') }}</template>
            <template #unchecked>{{ t('page.admin.disable') }}</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEdit = false">{{ t('page.admin.cancel') }}</n-button>
          <n-button type="primary" @click="updateCategory">{{ t('page.admin.save') }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
