<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, useDialog, NTag, NButton, NSpace } from 'naive-ui';

import { categoryApi } from '#/api/modules';

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
    message.success('创建成功');
    showCreate.value = false;
    form.value = { name: '', sortOrder: 0, status: 1 };
    loadCategories();
  } catch (e: any) {
    message.error(e.message || '创建失败');
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
    message.success('更新成功');
    showEdit.value = false;
    loadCategories();
  } catch (e: any) {
    message.error(e.message || '更新失败');
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该分类吗？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await categoryApi.adminDelete(id);
        message.success('删除成功');
        loadCategories();
      } catch (e: any) {
        message.error(e.message || '删除失败');
      }
    },
  });
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '分类名称', key: 'name' },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: any) =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === 1 ? '启用' : '禁用') },
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: any) =>
      h(NSpace, { size: 'small' }, {
        default: () => [
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

onMounted(loadCategories);
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-h3 style="margin: 0">分类管理</n-h3>
      <n-button type="primary" @click="showCreate = true; form = { name: '', sortOrder: 0, status: 1 }">
        创建分类
      </n-button>
    </div>

    <n-card v-if="showCreate" size="small" style="margin-bottom: 16px">
      <n-space align="center">
        <n-input v-model:value="form.name" placeholder="分类名称" style="width: 200px" />
        <n-input-number v-model:value="form.sortOrder" placeholder="排序" style="width: 120px" />
        <n-button type="primary" @click="createCategory">保存</n-button>
        <n-button @click="showCreate = false">取消</n-button>
      </n-space>
    </n-card>

    <n-data-table
      :columns="columns"
      :data="categories"
      :loading="loading"
      :bordered="false"
    />

    <n-modal
      v-model:show="showEdit"
      title="编辑分类"
      preset="card"
      style="width: 400px"
    >
      <n-form label-placement="left" label-width="60">
        <n-form-item label="名称">
          <n-input v-model:value="form.name" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sortOrder" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :unchecked-value="0" :checked-value="1">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEdit = false">取消</n-button>
          <n-button type="primary" @click="updateCategory">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
