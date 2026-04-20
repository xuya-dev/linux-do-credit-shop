<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import type { Category, Product } from '#/api/types';
import { PRODUCT_TYPE_MAP } from '#/api/types';

import { categoryApi, productApi } from '#/api/modules';

const router = useRouter();

const products = ref<Product[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const keyword = ref('');
const selectedCategory = ref<number | null>(null);
const selectedType = ref<number | null>(null);

const typeOptions = [
  { label: '全部类型', value: null },
  { label: '虚拟商品', value: 1 },
  { label: '实物商品', value: 2 },
];

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
    categories.value = await categoryApi.userList() || [];
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

function goDetail(id: number) {
  router.push(`/product/${id}`);
}
</script>

<template>
  <div class="product-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">商品列表</h1>
      <p class="page-subtitle">浏览所有可用商品，使用积分兑换</p>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <n-input
        v-model:value="keyword"
        placeholder="搜索商品..."
        clearable
        class="filter-search"
        @keyup.enter="searchProducts"
      >
        <template #suffix>
          <n-button text @click="searchProducts">🔍</n-button>
        </template>
      </n-input>

      <n-select
        v-model:value="selectedCategory"
        :options="[
          { label: '全部分类', value: null },
          ...categories.map((c) => ({ label: c.name, value: c.id })),
        ]"
        class="filter-select"
        placeholder="选择分类"
      />

      <n-select
        v-model:value="selectedType"
        :options="typeOptions"
        class="filter-select"
        placeholder="商品类型"
      />
    </div>

    <!-- 商品列表 -->
    <div v-if="loading" class="product-grid">
      <n-card v-for="i in 8" :key="i" class="product-skeleton">
        <n-skeleton height="180px" width="100%" />
        <n-skeleton text :repeat="2" style="margin-top: 12px" />
      </n-card>
    </div>

    <div v-else-if="products.length" class="product-grid">
      <n-card
        v-for="p in products"
        :key="p.id"
        hoverable
        class="product-card"
        @click="goDetail(p.id)"
      >
        <div class="product-cover">
          <img
            v-if="p.coverImage"
            :src="p.coverImage"
            :alt="p.name"
            loading="lazy"
          />
          <div v-else class="product-placeholder">📦</div>
          <n-tag
            v-if="p.productType"
            :type="PRODUCT_TYPE_MAP[p.productType]?.type || 'default'"
            size="small"
            class="product-type-tag"
          >
            {{ PRODUCT_TYPE_MAP[p.productType]?.label }}
          </n-tag>
        </div>

        <div class="product-info">
          <h3 class="product-name">{{ p.name }}</h3>
          <p v-if="p.description" class="product-desc">{{ p.description }}</p>
          <div class="product-meta">
            <span class="product-price">
              {{ p.price }}
              <span class="price-unit">积分</span>
            </span>
            <span class="product-stock">库存 {{ p.stock }}</span>
          </div>
        </div>
      </n-card>
    </div>

    <n-empty v-else description="没有找到符合条件的商品" />

    <!-- 分页 -->
    <div v-if="total > 12" class="pagination-wrapper">
      <n-pagination
        :page="page"
        :page-count="Math.ceil(total / 12)"
        :page-sizes="[12, 24, 36]"
        show-size-picker
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.product-list-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 15px;
  opacity: 0.5;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-search {
  max-width: 320px;
  flex: 1;
  min-width: 200px;
}

.filter-select {
  width: 160px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-cover {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  background: rgba(128, 128, 128, 0.06);
}

.product-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-cover img {
  transform: scale(1.05);
}

.product-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.product-type-tag {
  position: absolute;
  top: 8px;
  left: 8px;
}

.product-info {
  padding: 12px 4px 4px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  opacity: 0.5;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #18a058;
}

.price-unit {
  font-size: 12px;
  font-weight: 400;
  margin-left: 2px;
  opacity: 0.7;
}

.product-stock {
  font-size: 12px;
  opacity: 0.5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .filter-search {
    max-width: none;
    width: 100%;
  }

  .filter-select {
    width: calc(50% - 6px);
  }
}

@media (max-width: 480px) {
  .product-list-page {
    padding: 20px 16px;
  }
}
</style>
