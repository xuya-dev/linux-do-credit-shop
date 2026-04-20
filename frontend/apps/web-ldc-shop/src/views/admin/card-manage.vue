<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue';
import { useMessage, useDialog, NTag, NButton } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { cardApi, productApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();
const dialog = useDialog();

const cards = ref<any[]>([]);
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
    message.error(t('page.admin.pleaseComplete'));
    return;
  }
  try {
    const cardsList = importForm.value.cards.split('\n').filter((l) => l.trim());
    const count = await cardApi.adminImport({
      productId: importForm.value.productId,
      cards: cardsList,
    });
    message.success(t('page.admin.importSuccess', { count }));
    showImport.value = false;
    importForm.value = { productId: null, cards: '' };
    loadCards();
  } catch (e: any) {
    message.error(e.message || t('page.admin.importFailed'));
  }
}

function confirmDelete(id: number) {
  dialog.warning({
    title: t('page.admin.confirm'),
    content: t('page.admin.confirmDeleteCard'),
    positiveText: t('page.admin.confirm'),
    negativeText: t('page.admin.cancel'),
    onPositiveClick: async () => {
      try {
        await cardApi.adminDelete(id);
        message.success(t('page.admin.deleteSuccess'));
        loadCards();
      } catch (e: any) {
        message.error(e.message || t('page.admin.deleteFailed'));
      }
    },
  });
}

const productOptions = ref<{ label: string; value: number }[]>([]);

const columns = computed(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('page.admin.selectProduct'), key: 'productId', width: 80 },
  {
    title: t('page.admin.cardContent'),
    key: 'cardContent',
    ellipsis: { tooltip: true },
    render: (row: any) =>
      h('span', { style: 'font-family: monospace; font-size: 13px;' }, row.cardContent),
  },
  {
    title: t('page.admin.cardStatus'),
    key: 'status',
    width: 100,
    render: (row: any) => {
      const map: Record<number, { label: string; type: 'success' | 'warning' | 'error' }> = {
        0: { label: t('page.admin.available'), type: 'success' },
        1: { label: t('page.admin.used'), type: 'warning' },
        2: { label: t('page.admin.disabled'), type: 'error' },
      };
      const info = map[row.status] || { label: String(row.status), type: 'error' as const };
      return h(NTag, { type: info.type, size: 'small' }, { default: () => info.label });
    },
  },
  {
    title: t('page.admin.actions'),
    key: 'actions',
    width: 80,
    render: (row: any) =>
      row.status === 0
        ? h(
            NButton,
            { size: 'small', type: 'error', onClick: () => confirmDelete(row.id) },
            { default: () => t('page.admin.delete') },
          )
        : null,
  },
]);

onMounted(async () => {
  const prodRes = await productApi.adminList({ page: 1, size: 100 });
  const prodList = prodRes?.records || [];
  productOptions.value = prodList
    .filter((p: any) => p.productType === 1)
    .map((p: any) => ({ label: p.name, value: p.id }));
  loadCards();
});
</script>

<template>
  <div class="p-5">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <n-space align="center">
        <n-h3 style="margin: 0">{{ t('page.admin.cards') }}</n-h3>
        <n-select
          v-model:value="filterProductId"
          :options="[{ label: t('page.admin.allProducts'), value: null }, ...productOptions]"
          :placeholder="t('page.admin.selectProduct')"
          style="width: 200px"
          clearable
          @update:value="page = 1; loadCards()"
        />
      </n-space>
      <n-button type="primary" @click="showImport = true">{{ t('page.admin.batchImport') }}</n-button>
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
      :title="t('page.admin.batchImport')"
      preset="card"
      style="width: 500px"
    >
      <n-form label-placement="left" label-width="80">
        <n-form-item :label="t('page.admin.selectProduct')">
          <n-select
            v-model:value="importForm.productId"
            :options="productOptions"
            :placeholder="t('page.admin.selectProduct')"
          />
        </n-form-item>
        <n-form-item :label="t('page.admin.cardContent')">
          <n-input
            v-model:value="importForm.cards"
            type="textarea"
            :rows="8"
            :placeholder="t('page.admin.cardContentPlaceholder')"
            style="font-family: monospace"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showImport = false">{{ t('page.admin.cancel') }}</n-button>
          <n-button type="primary" @click="handleImport">{{ t('page.admin.batchImport') }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
