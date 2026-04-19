<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { categoryApi, productApi } from '#/api/modules';

const router = useRouter();

const products = ref<any[]>([]);
const categories = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const keyword = ref('');
const selectedCategory = ref<number | null>(null);
const selectedType = ref<number | null>(null);

const typeOptions = [
  { label: 'All Types', value: null },
  { label: 'Virtual', value: 1 },
  { label: 'Physical', value: 2 },
];

const categoryOptions = ref<{ label: string; value: number | null }[]>([
  { label: 'All Categories', value: null },
]);

async function loadData() {
  loading.value = true;
  try {
    const res = await productApi.userList({
      page: page.value,
      size: 12,
      categoryId: selectedCategory.value || undefined,
      keyword: keyword.value || undefined,
      productType: selectedType.value || undefined,
    });
    products.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    const catRes = await categoryApi.userList();
    categories.value = catRes || [];
    categoryOptions.value = [
      { label: 'All Categories', value: null },
      ...categories.value.map((c: any) => ({
        label: c.name,
        value: c.id,
      })),
    ];
  } catch {
    // ignore
  }
  await loadData();
});

watch([selectedCategory, selectedType], () => {
  page.value = 1;
  loadData();
});

function searchProducts() {
  page.value = 1;
  loadData();
}

function handlePageChange(p: number) {
  page.value = p;
  loadData();
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <div style="margin-bottom: 32px">
      <h1
        style="
          font-size: 32px;
          font-weight: 700;
          letter-spacing: -0.02em;
        "
      >
        Products
      </h1>
    </div>

    <n-space style="margin-bottom: 24px" :wrap="true">
      <n-input
        v-model:value="keyword"
        placeholder="Search products..."
        clearable
        style="max-width: 320px"
        @keyup.enter="searchProducts"
      />
      <n-select
        v-model:value="selectedCategory"
        :options="categoryOptions"
        style="width: 180px"
      />
      <n-select
        v-model:value="selectedType"
        :options="typeOptions"
        style="width: 150px"
      />
      <n-button type="primary" @click="searchProducts">Search</n-button>
    </n-space>

    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <n-grid
      v-else-if="products.length"
      :cols="4"
      :x-gap="20"
      :y-gap="20"
      responsive="screen"
      item-responsive
    >
      <n-grid-item v-for="p in products" :key="p.id">
        <n-card
          hoverable
          style="cursor: pointer"
          @click="router.push('/product/' + p.id)"
        >
          <template #cover>
            <div v-if="p.coverImage" style="aspect-ratio: 1; overflow: hidden">
              <img
                :src="p.coverImage"
                style="width: 100%; height: 100%; object-fit: cover"
              />
            </div>
            <div
              v-else
              style="
                aspect-ratio: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                background: rgba(128, 128, 128, 0.06);
                font-size: 40px;
              "
            >
              📦
            </div>
          </template>
          <div style="display: flex; gap: 6px; margin-bottom: 6px">
            <n-tag :type="p.productType === 1 ? 'info' : 'success'" size="small">
              {{ p.productType === 1 ? 'Virtual' : 'Physical' }}
            </n-tag>
          </div>
          <h3
            style="
              font-size: 15px;
              font-weight: 600;
              margin: 0 0 4px;
            "
          >
            {{ p.name }}
          </h3>
          <p
            style="
              font-size: 13px;
              opacity: 0.5;
              margin: 0 0 8px;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
            "
          >
            {{ p.description }}
          </p>
          <div
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            <span style="font-size: 18px; font-weight: 700; color: #18a058">
              {{ p.price }}
            </span>
            <span style="font-size: 12px; opacity: 0.5">
              Stock: {{ p.stock }} | Sold: {{ p.soldCount }}
            </span>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-empty v-else description="No products found" style="padding: 60px" />

    <div
      v-if="total > 12"
      style="display: flex; justify-content: center; margin-top: 32px"
    >
      <n-pagination
        :page="page"
        :page-count="Math.ceil(total / 12)"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>
