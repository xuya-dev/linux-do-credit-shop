<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import type { Category, Product } from '#/api/types';
import { categoryApi, productApi } from '#/api/modules';

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
      <span>首页</span> &gt; <span>全部商品</span>
    </div>

    <!-- 搜索与筛选区 -->
    <div class="faka-card filter-card">
      <div class="search-box">
        <input 
          type="text" 
          v-model="keyword" 
          class="faka-input" 
          placeholder="请输入商品名称搜索..." 
          @keyup.enter="searchProducts"
        />
        <button class="faka-btn" @click="searchProducts">搜索</button>
      </div>

      <div class="category-filter">
        <span class="filter-label">分类目录：</span>
        <div class="filter-options">
          <span 
            :class="['filter-item', { active: selectedCategory === null }]"
            @click="selectedCategory = null"
          >
            全部
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
        <span class="header-title">产品列表 (共 {{ total }} 件)</span>
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
              <span v-if="p.isHot" class="hot-badge">热</span>
            </div>
            <div class="product-info-right">
              <span class="product-price">{{ p.price }} 积分</span>
              <span class="product-stock">库存 {{ p.stock || 0 }}</span>
              <button class="buy-small-btn">兑换</button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          未能找到匹配的商品
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
.faka-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
  margin-bottom: 16px;
  cursor: pointer;
}
.breadcrumb span:hover {
  color: #1890ff;
}

.faka-card {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  color: var(--faka-text-main, #333);
}

.mt-24 {
  margin-top: 24px;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
}
.header-title {
  font-size: 15px;
  font-weight: 600;
  border-left: 3px solid #1890ff;
  padding-left: 10px;
}

.card-body {
  padding: 8px 0;
}

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
  font-size: 14px;
  border-radius: 2px;
  cursor: pointer;
}
.faka-btn:hover { opacity: 0.85; }

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
.loading-spin {
  display: flex;
  justify-content: center;
  padding: 60px;
}

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

.empty-state { text-align: center; padding: 60px; color: var(--faka-text-sub); }
.pagination-wrapper { display: flex; justify-content: flex-end; padding: 20px; }
</style>
