<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { productApi, categoryApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const products = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const keyword = ref('');

const showModal = ref(false);
const editingId = ref<number | null>(null);
const form = ref({
  name: '',
  productType: 1,
  categoryId: null as number | null,
  price: 0,
  stock: 0,
  sortOrder: 0,
  description: '',
  coverImage: '',
  status: 1,
});

const categoryOptions = ref<{ label: string; value: number }[]>([]);

async function loadProducts() {
  loading.value = true;
  try {
    const res = await productApi.adminList({
      page: page.value,
      size: 10,
      keyword: keyword.value || undefined,
    });
    products.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function toggleStatus(id: number, currentStatus: number) {
  try {
    await productApi.adminUpdateStatus(id, currentStatus === 1 ? 0 : 1);
    message.success(t('page.admin.operationSuccess'));
    loadProducts();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: t('page.admin.confirm'),
    content: t('page.admin.confirmDeleteProduct'),
    positiveText: t('page.admin.confirm'),
    negativeText: t('page.admin.cancel'),
    onPositiveClick: async () => {
      try {
        await productApi.adminDelete(id);
        message.success(t('page.admin.deleteSuccess'));
        loadProducts();
      } catch (e: any) {
        message.error(e.message || t('page.admin.deleteFailed'));
      }
    },
  });
}

function openCreate() {
  editingId.value = null;
  form.value = {
    name: '',
    productType: 1,
    categoryId: null,
    price: 0,
    stock: 0,
    sortOrder: 0,
    description: '',
    coverImage: '',
    status: 1,
  };
  showModal.value = true;
}

function openEdit(row: any) {
  editingId.value = row.id;
  form.value = {
    name: row.name,
    productType: row.productType,
    categoryId: row.categoryId,
    price: row.price,
    stock: row.stock,
    sortOrder: row.sortOrder || 0,
    description: row.description || '',
    coverImage: row.coverImage || '',
    status: row.status,
  };
  showModal.value = true;
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await productApi.adminUpdate(editingId.value, form.value);
    } else {
      await productApi.adminCreate(form.value);
    }
    message.success(t('page.admin.operationSuccess'));
    showModal.value = false;
    loadProducts();
  } catch (e: any) {
    message.error(e.message || t('page.admin.operationFailed'));
  }
}

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('page.admin.productName'), key: 'name', ellipsis: { tooltip: true } },
  {
    title: t('page.admin.productType'),
    key: 'productType',
    width: 100,
    render: (row: any) =>
      h(
        NTag,
        { type: row.productType === 1 ? 'info' : 'success', size: 'small' },
        { default: () => (row.productType === 1 ? t('page.admin.virtual') : t('page.admin.physical')) },
      ),
  },
  { title: t('page.admin.productPrice'), key: 'price', width: 100 },
  { title: t('page.admin.productStock'), key: 'stock', width: 80 },
  {
    title: t('page.admin.status'),
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === 1 ? t('page.admin.onShelf') : t('page.admin.offShelf')) },
      ),
  },
  {
    title: t('page.admin.actions'),
    key: 'actions',
    width: 260,
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
            { default: () => (row.status === 1 ? t('page.admin.offShelf') : t('page.admin.onShelf')) },
          ),
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openEdit(row) },
            { default: () => t('page.admin.edit') },
          ),
          h(
            NButton,
            { size: 'small', type: 'error', onClick: () => handleDelete(row.id) },
            { default: () => t('page.admin.delete') },
          ),
        ],
      }),
  },
]);

onMounted(async () => {
  const catRes = await categoryApi.userList();
  const catList = catRes || [];
  categoryOptions.value = catList.map((c: any) => ({
    label: c.name,
    value: c.id,
  }));
  loadProducts();
});
</script>

<template>
  <div class="p-5">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-input
        v-model:value="keyword"
        :placeholder="t('page.admin.searchProducts')"
        style="width: 240px"
        @keyup.enter="page = 1; loadProducts()"
      />
      <n-button type="primary" @click="openCreate">{{ t('page.admin.createProduct') }}</n-button>
    </div>

    <n-data-table
      :columns="columns"
      :data="products"
      :loading="loading"
      :pagination="{ page: page, itemCount: total, pageSize: 10, onChange: (p: number) => { page = p; loadProducts() } }"
      :bordered="false"
    />

    <n-modal
      v-model:show="showModal"
      :title="editingId ? t('page.admin.editProduct') : t('page.admin.createProduct')"
      preset="card"
      style="width: 600px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item :label="t('page.admin.productName')">
          <n-input v-model:value="form.name" :placeholder="t('page.admin.productName')" />
        </n-form-item>
        <n-form-item :label="t('page.admin.productType')">
          <n-select
            v-model:value="form.productType"
            :options="[
              { label: t('page.admin.virtual'), value: 1 },
              { label: t('page.admin.physical'), value: 2 },
            ]"
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.productCategory')">
          <n-select
            v-model:value="form.categoryId"
            :options="categoryOptions"
            :placeholder="t('page.admin.selectCategory')"
            clearable
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.productPrice')">
          <n-input-number v-model:value="form.price" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item :label="t('page.admin.productStock')">
          <n-input-number v-model:value="form.stock" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item :label="t('page.admin.sortOrder')">
          <n-input-number v-model:value="form.sortOrder" style="width: 100%" />
        </n-form-item>
        <n-form-item :label="t('page.admin.productDescription')">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="4"
            :placeholder="t('page.admin.productDescription')"
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.coverImage')">
          <n-input v-model:value="form.coverImage" :placeholder="t('page.admin.coverImage')" />
        </n-form-item>
        <n-form-item :label="t('page.admin.status')">
          <n-switch v-model:value="form.status" :unchecked-value="0" :checked-value="1">
            <template #checked>{{ t('page.admin.onShelf') }}</template>
            <template #unchecked>{{ t('page.admin.offShelf') }}</template>
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
