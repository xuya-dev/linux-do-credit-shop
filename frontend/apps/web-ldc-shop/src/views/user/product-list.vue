<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from '@vben/locales';
import type { Category, Product } from '#/api/types';
import { categoryApi, productApi } from '#/api/modules';
import '#/styles/faka-common.css';

const { t } = useI18n();
const router = useRouter();

const products = ref<Product[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const keyword = ref('');
const selectedCategory = ref<number | null>(null);

async function loadData() {
  loading.value = true;
  try {
    const res = await productApi.userList({
      page: page.value,
      size: 20,
      categoryId: selectedCategory.value || undefined,
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

onMounted(async () => {
  try {
    categories.value = await categoryApi.userList() || [];
  } catch {
    // ignore
  }
  await loadData();
});

watch([selectedCategory], () => {
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
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>{{ t('page.user.home') }}</span> &gt; <span>{{ t('page.user.allProducts') }}</span>
    </div>

    <!-- 搜索与筛选区 -->
    <div class="faka-card filter-card">
      <div class="search-box">
        <input 
          type="text" 
          v-model="keyword" 
          class="faka-input" 
          :placeholder="t('page.user.searchProductPlaceholder')"
          @keyup.enter="searchProducts"
        />
        <button class="faka-btn" @click="searchProducts">{{ t('page.user.search') }}</button>
      </div>

      <div class="category-filter">
        <span class="filter-label">{{ t('page.user.categoryDirectory') }}</span>
        <div class="filter-options">
          <span 
            :class="['filter-item', { active: selectedCategory === null }]"
            @click="selectedCategory = null"
          >
            {{ t('page.user.all') }}
          </span>
          <span 
            v-for="cat in categories" 
            :key="cat.id"
            :class="['filter-item', { active: selectedCategory === cat.id }]"
            @click="selectedCategory = cat.id"
          >
            {{ cat.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="faka-card mt-24">
      <div class="card-header">
        <span class="header-title">{{ t('page.user.productList') }} ({{ t('page.user.totalCount', { count: total }) }})</span>
      </div>

      <div class="card-body">
        <n-spin v-if="loading" size="large" class="loading-spin" />
        
        <div v-else-if="products.length" class="product-list">
          <div
            v-for="p in products"
            :key="p.id"
            class="product-row"
            @click="goDetail(p.id)"
          >
            <div class="product-name">
              {{ p.name }}
              <span v-if="p.isHot" class="hot-badge">{{ t('page.user.hot') }}</span>
            </div>
            <div class="product-info-right">
              <span class="product-price">{{ p.price }} {{ t('page.user.credits') }}</span>
              <span class="product-stock">{{ t('page.user.stock') }} {{ p.stock || 0 }}</span>
              <button class="buy-small-btn">{{ t('page.user.exchange') }}</button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          {{ t('page.user.noMatchingProducts') }}
        </div>

        <!-- 分页 -->
        <div v-if="total > 20" class="pagination-wrapper">
          <n-pagination
            v-model:page="page"
            :page-count="Math.ceil(total / 20)"
            @update:page="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 筛选区 */
.filter-card {
  padding: 20px;
}

.search-box {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 500px;
}

.faka-input {
  flex: 1;
  height: 36px;
  border: 1px solid var(--faka-border, #d9d9d9);
  border-radius: 2px;
  padding: 0 12px;
  background: transparent;
  color: var(--faka-text-main, #333);
  font-size: 13px;
  outline: none;
}
.faka-input:focus {
  border-color: #1890ff;
}

.faka-btn {
  background: #1890ff;
  color: #fff;
  border: none;
  padding: 0 24px;
  height: 36px;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  border-left: 3px solid #1890ff;
  padding-left: 10px;
}

.category-filter {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
}
.filter-label {
  color: var(--faka-text-sub, #8c8c8c);
  margin-right: 16px;
  white-space: nowrap;
  line-height: 24px;
}
.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.filter-item {
  color: var(--faka-text-main, #333);
  padding: 2px 12px;
  cursor: pointer;
  border-radius: 2px;
  line-height: 20px;
}
.filter-item:hover { color: #1890ff; }
.filter-item.active {
  background: #1890ff;
  color: #fff;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
}

.product-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px dashed var(--faka-border, #f0f0f0);
  cursor: pointer;
  transition: background 0.2s;
}
.product-row:last-child { border-bottom: none; }
.product-row:hover {
  background: var(--faka-tag-bg, #fafafa);
}

.product-name {
  font-size: 14px;
  color: var(--faka-text-main, #333);
  display: flex;
  align-items: center;
  gap: 8px;
}

.hot-badge {
  background: #fff1f0;
  color: #f5222d;
  font-size: 12px;
  padding: 0 4px;
  border-radius: 2px;
  border: 1px solid #ffa39e;
}

.product-info-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.product-price {
  color: #f5222d;
  font-weight: 700;
  font-size: 15px;
}

.product-stock {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  min-width: 70px;
  text-align: center;
}

.buy-small-btn {
  background: transparent;
  color: #1890ff;
  border: 1px solid #1890ff;
  padding: 2px 14px;
  border-radius: 2px;
  font-size: 12px;
  cursor: pointer;
}
.product-row:hover .buy-small-btn {
  background: #1890ff;
  color: #fff;
}

.pagination-wrapper { display: flex; justify-content: flex-end; padding: 20px; }
</style>
