<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { useMessage, useDialog, NTag, NButton } from 'naive-ui';

import { cardApi, productApi } from '#/api/modules';

const message = useMessage();
const dialog = useDialog();

const cards = ref<any[]>([]);
const products = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const filterProductId = ref<number | null>(null);

const showImport = ref(false);
const importForm = ref({ productId: null as number | null, cards: '' });

async function loadCards() {
  loading.value = true;
  try {
    const res = await cardApi.adminList({
      page: page.value,
      size: 20,
      productId: filterProductId.value ?? undefined,
    });
    cards.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function handleImport() {
  if (!importForm.value.productId || !importForm.value.cards.trim()) {
    message.error('请填写完整');
    return;
  }
  try {
    const cardsList = importForm.value.cards.split('\n').filter((l) => l.trim());
    const count = await cardApi.adminImport({
      productId: importForm.value.productId,
      cards: cardsList,
    });
    message.success(`成功导入 ${count} 张卡密`);
    showImport.value = false;
    importForm.value = { productId: null, cards: '' };
    loadCards();
  } catch (e: any) {
    message.error(e.message || '导入失败');
  }
}

function confirmDelete(id: number) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该卡密吗？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await cardApi.adminDelete(id);
        message.success('删除成功');
        loadCards();
      } catch (e: any) {
        message.error(e.message || '删除失败');
      }
    },
  });
}

const productOptions = ref<{ label: string; value: number }[]>([]);

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '商品ID', key: 'productId', width: 80 },
  {
    title: '卡密内容',
    key: 'cardContent',
    ellipsis: { tooltip: true },
    render: (row: any) =>
      h(
        'span',
        {
          style:
            'font-family: monospace; font-size: 13px;',
        },
        row.cardContent,
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => {
      const map: Record<number, { label: string; type: 'success' | 'warning' | 'error' }> = {
        0: { label: '可用', type: 'success' },
        1: { label: '已售', type: 'warning' },
        2: { label: '禁用', type: 'error' },
      };
      const info = map[row.status] || { label: '未知', type: 'error' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row: any) =>
      row.status === 0
        ? h(
            NButton,
            { size: 'small', type: 'error', onClick: () => confirmDelete(row.id) },
            { default: () => '删除' },
          )
        : null,
  },
];

onMounted(async () => {
  const prodRes = await productApi.adminList({ page: 1, size: 100 });
  const prodList = prodRes?.records || [];
  products.value = prodList;
  productOptions.value = prodList
    .filter((p: any) => p.productType === 1)
    .map((p: any) => ({ label: p.name, value: p.id }));
  loadCards();
});
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-space align="center">
        <n-h3 style="margin: 0">卡密管理</n-h3>
        <n-select
          v-model:value="filterProductId"
          :options="[{ label: '全部商品', value: null }, ...productOptions]"
          placeholder="筛选商品"
          style="width: 200px"
          clearable
          @update:value="page = 1; loadCards()"
        />
      </n-space>
      <n-button type="primary" @click="showImport = true">批量导入</n-button>
    </div>

    <n-data-table
      :columns="columns"
      :data="cards"
      :loading="loading"
      :pagination="{ page: page, itemCount: total, pageSize: 20, onChange: (p: number) => { page = p; loadCards() } }"
      :bordered="false"
    />

    <n-modal
      v-model:show="showImport"
      title="批量导入卡密"
      preset="card"
      style="width: 500px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item label="选择商品">
          <n-select
            v-model:value="importForm.productId"
            :options="productOptions"
            placeholder="选择虚拟商品"
          />
        </n-form-item>
        <n-form-item label="卡密内容">
          <n-input
            v-model:value="importForm.cards"
            type="textarea"
            :rows="8"
            placeholder="每行一个卡密"
            style="font-family: monospace"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showImport = false">取消</n-button>
          <n-button type="primary" @click="handleImport">导入</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
