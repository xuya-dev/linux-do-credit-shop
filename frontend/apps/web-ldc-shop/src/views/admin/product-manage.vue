<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';

import { productApi, categoryApi } from '#/api/modules';

const message = useMessage();
const dialog = useDialog();

const products = ref<any[]>([]);
const categories = ref<any[]>([]);
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
    message.success('操作成功');
    loadProducts();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该商品吗？此操作不可撤销。',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await productApi.adminDelete(id);
        message.success('删除成功');
        loadProducts();
      } catch (e: any) {
        message.error(e.message || '删除失败');
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
    message.success('操作成功');
    showModal.value = false;
    loadProducts();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  }
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '商品名称', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'productType',
    width: 100,
    render: (row: any) =>
      h(
        NTag,
        { type: row.productType === 1 ? 'info' : 'success', size: 'small' },
        { default: () => (row.productType === 1 ? '虚拟' : '实物') },
      ),
  },
  { title: '价格', key: 'price', width: 100 },
  { title: '库存', key: 'stock', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === 1 ? '上架' : '下架') },
      ),
  },
  {
    title: '操作',
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
            { default: () => (row.status === 1 ? '下架' : '上架') },
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
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-input
        v-model:value="keyword"
        placeholder="搜索商品..."
        style="width: 240px"
        @keyup.enter="page = 1; loadProducts()"
      />
      <n-button type="primary" @click="openCreate">创建商品</n-button>
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
      :title="editingId ? '编辑商品' : '创建商品'"
      preset="card"
      style="width: 600px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item label="商品名称">
          <n-input v-model:value="form.name" placeholder="请输入商品名称" />
        </n-form-item>
        <n-form-item label="商品类型">
          <n-select
            v-model:value="form.productType"
            :options="[
              { label: '虚拟', value: 1 },
              { label: '实物', value: 2 },
            ]"
          />
        </n-form-item>
        <n-form-item label="分类">
          <n-select
            v-model:value="form.categoryId"
            :options="categoryOptions"
            placeholder="选择分类"
            clearable
          />
        </n-form-item>
        <n-form-item label="价格">
          <n-input-number v-model:value="form.price" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="库存">
          <n-input-number v-model:value="form.stock" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sortOrder" style="width: 100%" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="4"
            placeholder="商品描述"
          />
        </n-form-item>
        <n-form-item label="封面图">
          <n-input v-model:value="form.coverImage" placeholder="封面图片URL" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :unchecked-value="0" :checked-value="1">
            <template #checked>上架</template>
            <template #unchecked>下架</template>
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
