<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from '@vben/locales';
import { useMessage } from 'naive-ui';
import type { Announcement, Product } from '#/api/types';
import { announcementApi, productApi, categoryApi } from '#/api/modules';
import { useShopSettingsStore } from '#/store';
import '#/styles/faka-common.css';

const { t } = useI18n();
const router = useRouter();
const message = useMessage();
const shopSettingsStore = useShopSettingsStore();

const products = ref<Product[]>([]);
const announcements = ref<Announcement[]>([]);
const categories = ref<any[]>([]);
const loadingProducts = ref(true);
const loadingAnnouncements = ref(true);
const searchQuery = ref('');
const activeCategoryId = ref<number | null>(null);
const viewMode = ref<'list' | 'grid'>('grid');

const displayNotice = computed(() => {
  return shopSettingsStore.shopNotice || '';
});

const filteredProducts = computed(() => {
  if (!searchQuery.value) return products.value;
  const q = searchQuery.value.toLowerCase();
  return products.value.filter(p => p.name.toLowerCase().includes(q));
});

onMounted(async () => {
  try {
    const [catRes, annRes] = await Promise.all([
      categoryApi.userList(),
      announcementApi.latest(5),
    ]);
    
    categories.value = catRes || [];
    announcements.value = annRes || [];

    if (categories.value.length > 0) {
      activeCategoryId.value = categories.value[0].id;
      await loadProducts(activeCategoryId.value);
    } else {
      await loadProducts();
    }
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.user.loadFailed'));
  } finally {
    loadingAnnouncements.value = false;
  }
});

async function loadProducts(categoryId?: number | null) {
  loadingProducts.value = true;
  try {
    const params: any = { page: 1, size: 20 };
    if (categoryId) params.categoryId = categoryId;
    
    const res = await productApi.userList(params);
    products.value = res?.records || [];
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.user.loadProductsFailed'));
  } finally {
    loadingProducts.value = false;
  }
}

function handleCategoryClick(catId: number) {
  activeCategoryId.value = catId;
  loadProducts(catId);
}

function goProductDetail(id: number) {
  router.push(`/product/${id}`);
}

function getProductImage(p: Product) {
  return p.coverImage || '/placeholder-product.png';
}
</script>

<template>
  <div class="faka-home">
    <div class="faka-container">
      <!-- 搜索栏区 -->
      <div class="search-bar-wrapper">
        <div class="search-tags">
          <span class="search-tag active">{{ t('page.shop.allProducts') }}({{ filteredProducts.length }})</span>
        </div>
        <div class="search-input">
          <input type="text" v-model="searchQuery" :placeholder="t('page.shop.searchPlaceholder')" />
          <div class="search-icon">🔍</div>
        </div>
      </div>

      <div class="faka-main-layout">
        <!-- 左侧分类菜单 -->
        <aside class="faka-sidebar">
          <div class="faka-card">
            <div class="card-header">{{ t('page.shop.allCategories') }}</div>
            <ul class="category-list">
              <li
                v-for="cat in categories"
                :key="cat.id"
                :class="['category-item', { active: activeCategoryId === cat.id }]"
                @click="handleCategoryClick(cat.id)"
              >
                <span class="cat-name">{{ cat.name }}</span>
              </li>
            </ul>
          </div>
        </aside>

        <!-- 右侧主体区 -->
        <div class="faka-content">
          <!-- 公告区 -->
          <div class="faka-card">
            <div class="card-header">{{ t('page.shop.announcements') }}</div>
            <div class="card-body ann-body">
              <n-skeleton v-if="loadingAnnouncements" text :repeat="2" />
              <template v-else-if="announcements.length">
                <div v-if="displayNotice" class="shop-notice">{{ displayNotice }}</div>
                <div class="ann-list">
                  <div v-for="ann in announcements" :key="ann.id" class="ann-item">
                    <span class="ann-type">[{{ ann.typeName }}]</span>
                    <span class="ann-title">{{ ann.title }}</span>
                    <span class="ann-date">{{ ann.publishedAt }}</span>
                  </div>
                </div>
              </template>
              <div v-else-if="displayNotice" class="shop-notice">{{ displayNotice }}</div>
              <p v-else class="empty-text">{{ t('page.shop.welcomeMessage') }}</p>
            </div>
          </div>

          <!-- 商品列表区 -->
          <div class="faka-card mt-24">
            <div class="card-header-with-actions">
              <span class="card-header">{{ t('page.shop.featuredProducts') }}</span>
              <div class="view-toggle">
                <button
                  :class="['toggle-btn', { active: viewMode === 'list' }]"
                  @click="viewMode = 'list'"
                  :title="t('page.shop.listView')"
                >☰</button>
                <button
                  :class="['toggle-btn', { active: viewMode === 'grid' }]"
                  @click="viewMode = 'grid'"
                  :title="t('page.shop.gridView')"
                >⊞</button>
              </div>
            </div>
            <div class="card-body">
              <n-skeleton v-if="loadingProducts" text :repeat="4" />
              
              <template v-else-if="filteredProducts.length">
                <!-- 列表模式 -->
                <div v-if="viewMode === 'list'" class="product-list">
                  <div
                    v-for="p in filteredProducts"
                    :key="p.id"
                    class="product-row"
                    @click="goProductDetail(p.id)"
                  >
                    <div class="product-row-left">
                      <img class="product-thumb" :src="getProductImage(p)" :alt="p.name" />
                      <div class="product-name">
                        {{ p.name }}
                        <span v-if="p.isHot" class="hot-badge">HOT</span>
                      </div>
                    </div>
                    <div class="product-info-right">
                      <span class="product-price">{{ p.price }} {{ t('page.shop.credits') }}</span>
                      <span class="product-stock">{{ t('page.shop.stock') }}: {{ p.stock || 0 }}</span>
                    </div>
                  </div>
                </div>

                <!-- 卡片模式 -->
                <div v-else class="product-grid">
                  <div
                    v-for="p in filteredProducts"
                    :key="p.id"
                    class="product-card"
                    @click="goProductDetail(p.id)"
                  >
                    <div class="product-card-img">
                      <img :src="getProductImage(p)" :alt="p.name" />
                    </div>
                    <div class="product-card-body">
                      <div class="product-card-name">
                        {{ p.name }}
                        <span v-if="p.isHot" class="hot-badge">HOT</span>
                      </div>
                      <div class="product-card-footer">
                        <span class="product-price">{{ p.price }} {{ t('page.shop.credits') }}</span>
                        <span class="product-stock">{{ t('page.shop.stock') }}: {{ p.stock || 0 }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="load-more">
                  <span>{{ t('page.shop.loadMore') }}</span>
                </div>
              </template>

              <div v-else class="empty-state">
                <p class="empty-text">{{ t('page.shop.noProducts') }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faka-home {
  width: 100%;
}

.faka-container {
  max-width: 1300px;
  padding: 0 24px;
}

/* 搜索栏区 */
.search-bar-wrapper {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  color: var(--faka-text-main, #333);
}

.search-tags {
  display: flex;
  gap: 12px;
}

.search-tag {
  font-size: 14px;
  color: var(--faka-text-sub, #595959);
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 16px;
}

.search-tag.active {
  background: var(--faka-tag-active-bg);
  color: var(--faka-primary-color);
}

.search-input {
  display: flex;
  align-items: center;
  background: var(--faka-tag-bg, #f5f5f5);
  border-radius: 4px;
  padding: 4px 12px;
  width: 280px;
}

.search-input input {
  border: none;
  background: transparent;
  outline: none;
  flex: 1;
  font-size: 13px;
  color: var(--faka-text-main, #333);
}

.search-icon {
  color: var(--faka-text-sub, #8c8c8c);
  font-size: 14px;
  cursor: pointer;
}

/* 左右分栏布局 */
.faka-main-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.faka-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.faka-content {
  flex: 1;
  min-width: 0;
}

/* 卡片覆盖：home 使用不同圆角和 overflow */
.faka-card {
  border-radius: 6px;
  overflow: hidden;
}

.card-header {
  font-size: 14px;
  color: var(--faka-text-sub, #8c8c8c);
}

.card-header-with-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 视图切换按钮 */
.view-toggle {
  display: flex;
  gap: 4px;
}

.toggle-btn {
  width: 28px;
  height: 28px;
  border: 1px solid var(--faka-border, #d9d9d9);
  border-radius: 4px;
  background: var(--faka-bg-header, #fff);
  color: var(--faka-text-sub, #8c8c8c);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.toggle-btn:hover {
  border-color: var(--faka-primary-color);
  color: var(--faka-primary-color);
}

.toggle-btn.active {
  background: var(--faka-primary-color);
  border-color: var(--faka-primary-color);
  color: #fff;
}

.ann-body {
  padding: 16px 20px;
  color: var(--faka-text-sub, #595959);
  font-size: 14px;
}

/* 左侧分类列表 */
.category-list {
  list-style: none;
  padding: 8px 0;
  margin: 0;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: var(--faka-text-sub, #595959);
  transition: all 0.2s;
}

.category-item:hover {
  background: var(--faka-hover-bg);
}

.category-item.active {
  background: var(--faka-primary-bg);
  color: var(--faka-primary-color);
  border-left: 3px solid var(--faka-primary-color);
}

.cat-count {
  color: var(--faka-hint-color);
  font-size: 12px;
}

/* 公告列表 */
.ann-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ann-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ann-type { color: var(--faka-warning-text, #faad14); }
.ann-title { flex: 1; color: var(--faka-text-main, #595959); }
.ann-date { color: var(--faka-hint-color); font-size: 12px; }

.shop-notice {
  padding: 10px 14px;
  margin-bottom: 10px;
  background: var(--faka-warning-bg, #fffbe6);
  border: 1px solid var(--faka-warning-border, #ffe58f);
  border-radius: 4px;
  color: var(--faka-warning-text, #595959);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* ========== 列表模式 ========== */
.product-list {
  display: flex;
  flex-direction: column;
}

.product-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px dashed var(--faka-border, #f0f0f0);
  cursor: pointer;
  transition: background 0.2s;
}

.product-row:last-child {
  border-bottom: none;
}

.product-row:hover {
  background: var(--faka-hover-bg);
}

.product-row-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--faka-tag-bg, #f5f5f5);
}

.product-name {
  font-size: 14px;
  color: var(--faka-text-main, #333);
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-badge {
  background: var(--faka-error-bg, #fff1f0);
  color: var(--faka-error-text, #f5222d);
  font-size: 12px;
  padding: 0 4px;
  border-radius: 2px;
  border: 1px solid var(--faka-error-border, #ffa39e);
}

.product-info-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.product-price {
  color: var(--faka-primary-color);
  font-weight: 700;
  font-size: 15px;
}

.product-stock {
  background: var(--faka-tag-active-bg);
  color: var(--faka-primary-color);
  border: 1px solid var(--faka-border);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  min-width: 80px;
  text-align: center;
}

/* ========== 卡片模式 ========== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 4px 0;
}

.product-card {
  border-radius: 8px;
  overflow: hidden;
  background: var(--faka-bg-header, #fff);
  border: 1px solid var(--faka-border, #f0f0f0);
  cursor: pointer;
  transition: all 0.2s;
}

.product-card:hover {
  border-color: var(--faka-primary-color);
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.product-card-img {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--faka-tag-bg, #f5f5f5);
}

.product-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-card-img img {
  transform: scale(1.05);
}

.product-card-body {
  padding: 12px;
}

.product-card-name {
  font-size: 14px;
  color: var(--faka-text-main, #333);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-card-footer .product-price {
  font-size: 14px;
}

.product-card-footer .product-stock {
  font-size: 11px;
  min-width: auto;
  padding: 1px 6px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
  color: var(--faka-hint-color);
  font-size: 13px;
  cursor: pointer;
}

.load-more:hover {
  color: var(--faka-text-sub);
}

.empty-state {
  padding: 48px;
}
.empty-text {
  color: var(--faka-hint-color);
}

@media (max-width: 900px) {
  .faka-main-layout {
    flex-direction: column;
  }
  .faka-sidebar {
    width: 100%;
  }
  .search-bar-wrapper {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  .search-input {
    width: 100%;
  }
  .product-info-right {
    gap: 12px;
  }
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
}
</style>
