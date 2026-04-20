<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import type { Announcement, Product } from '#/api/types';

import { announcementApi, productApi } from '#/api/modules';

const router = useRouter();

const products = ref<Product[]>([]);
const announcements = ref<Announcement[]>([]);
const loadingProducts = ref(true);
const loadingAnnouncements = ref(true);

onMounted(async () => {
  try {
    const [prodRes, annRes] = await Promise.all([
      productApi.userList({ page: 1, size: 8 }),
      announcementApi.latest(5),
    ]);
    products.value = prodRes?.records || [];
    announcements.value = annRes || [];
  } catch (e) {
    console.error(e);
  } finally {
    loadingProducts.value = false;
    loadingAnnouncements.value = false;
  }
});

function goProductDetail(id: number) {
  router.push(`/product/${id}`);
}

function goAnnouncementDetail(id: number) {
  router.push(`/announcement/${id}`);
}
</script>

<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">LINUX DO Credit</div>
        <h1 class="hero-title">LDC Shop</h1>
        <p class="hero-desc">
          使用 LINUX DO 积分兑换精选商品，安全、快捷、透明
        </p>
        <n-space justify="center">
          <n-button
            type="primary"
            size="large"
            class="hero-btn"
            @click="router.push('/products')"
          >
            浏览商品
          </n-button>
          <n-button
            quaternary
            size="large"
            @click="router.push('/announcements')"
          >
            查看公告
          </n-button>
        </n-space>
      </div>
      <div class="hero-decoration"></div>
    </section>

    <!-- 公告区 -->
    <section v-if="announcements.length || loadingAnnouncements" class="section">
      <div class="section-header">
        <h2 class="section-title">最新公告</h2>
        <n-button text @click="router.push('/announcements')">
          查看更多 →
        </n-button>
      </div>

      <n-skeleton v-if="loadingAnnouncements" :repeat="3" text style="padding: 12px 0" />

      <div v-else class="announcement-list">
        <div
          v-for="ann in announcements"
          :key="ann.id"
          class="announcement-item"
          @click="goAnnouncementDetail(ann.id)"
        >
          <n-tag
            :type="ann.type === 2 ? 'warning' : ann.type === 3 ? 'info' : 'success'"
            size="small"
            :bordered="false"
          >
            {{ ann.typeName }}
          </n-tag>
          <span v-if="ann.isTop" class="top-badge">置顶</span>
          <span class="ann-title">{{ ann.title }}</span>
          <span class="ann-date">{{ ann.publishedAt }}</span>
        </div>
      </div>
    </section>

    <!-- 商品区 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">热门商品</h2>
        <n-button text @click="router.push('/products')">
          全部商品 →
        </n-button>
      </div>

      <!-- 骨架屏 -->
      <div v-if="loadingProducts" class="product-grid">
        <n-card v-for="i in 4" :key="i" class="product-skeleton">
          <n-skeleton height="200px" width="100%" />
          <n-skeleton text :repeat="2" style="margin-top: 12px" />
        </n-card>
      </div>

      <!-- 商品网格 -->
      <div v-else-if="products.length" class="product-grid">
        <n-card
          v-for="p in products"
          :key="p.id"
          hoverable
          class="product-card"
          @click="goProductDetail(p.id)"
        >
          <div class="product-cover">
            <img
              v-if="p.coverImage"
              :src="p.coverImage"
              :alt="p.name"
              loading="lazy"
            />
            <div v-else class="product-placeholder">
              📦
            </div>
          </div>

          <div class="product-info">
            <h3 class="product-name">{{ p.name }}</h3>
            <div class="product-meta">
              <span class="product-price">
                {{ p.price }}
                <span class="price-unit">积分</span>
              </span>
              <span class="product-sold">已售 {{ p.soldCount }}</span>
            </div>
          </div>
        </n-card>
      </div>

      <n-empty v-else description="暂无商品" />
    </section>
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 60px;
}

.hero-section {
  position: relative;
  padding: 80px 24px 100px;
  text-align: center;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 680px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: rgba(24, 160, 88, 0.12);
  color: #18a058;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  margin: 0 0 16px;
  letter-spacing: -0.04em;
  line-height: 1.1;
}

.hero-desc {
  font-size: 18px;
  opacity: 0.6;
  margin: 0 0 32px;
  line-height: 1.6;
}

.hero-btn {
  padding: 0 40px;
  height: 48px;
  font-size: 16px;
}

.hero-decoration {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(24, 160, 88, 0.06) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.02em;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s;
  border: 1px solid transparent;
}

.announcement-item:hover {
  background-color: rgba(128, 128, 128, 0.06);
  border-color: rgba(128, 128, 128, 0.1);
}

.top-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(208, 48, 80, 0.1);
  color: #d03050;
  font-weight: 600;
  flex-shrink: 0;
}

.ann-title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ann-date {
  font-size: 13px;
  opacity: 0.4;
  flex-shrink: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-cover {
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

.product-info {
  padding: 12px 4px 4px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.product-sold {
  font-size: 12px;
  opacity: 0.5;
}

.product-skeleton :deep(.n-skeleton) {
  border-radius: 8px;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 40px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .section {
    padding: 0 16px 40px;
  }

  .announcement-item {
    flex-wrap: wrap;
    gap: 8px;
  }

  .ann-title {
    width: 100%;
    order: 3;
  }

  .ann-date {
    order: 2;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-section {
    padding: 60px 16px 80px;
  }
}
</style>
