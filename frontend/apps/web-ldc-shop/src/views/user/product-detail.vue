<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useAccessStore } from '@vben/stores';
import { useI18n } from '@vben/locales';

import type { Product } from '#/api/types';
import { PRODUCT_TYPE_MAP } from '#/api/types';
import { orderApi, productApi } from '#/api/modules';
import '#/styles/faka-common.css';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const accessStore = useAccessStore();
const { t } = useI18n();

const product = ref<Product | null>(null);
const loading = ref(true);
const buying = ref(false);
const quantity = ref(1);
const contactInfo = ref('');
const remark = ref('');
const currentImage = ref(0);

async function loadProduct() {
  try {
    product.value = await productApi.detail(Number(route.params.id));
    currentImage.value = 0;
  } catch {
    message.error(t('page.user.loadProductFailed'));
  } finally {
    loading.value = false;
  }
}

onMounted(loadProduct);

async function handleBuy() {
  if (!accessStore.accessToken) {
    router.push('/auth/login');
    return;
  }
  
  if (!product.value) return;
  if (!contactInfo.value) {
    message.warning(t('page.user.enterContact'));
    return;
  }
  
  buying.value = true;
  try {
    const orderRes = await orderApi.create({
      productId: product.value.id,
      quantity: quantity.value,
      contactInfo: contactInfo.value,
      remark: remark.value,
    });
    const payRes = await orderApi.pay(orderRes.id);
    if (payRes?.payUrl) {
      window.location.href = payRes.payUrl;
    } else {
      message.success(t('page.user.orderCreated'));
      router.push('/orders');
    }
  } catch (e: any) {
    message.error(e.message || t('page.user.purchaseFailed'));
  } finally {
    buying.value = false;
  }
}

const allImages = computed(() => {
  if (!product.value) return [];
  const images: string[] = [];
  if (product.value.coverImage) images.push(product.value.coverImage);
  if (product.value.images?.length) {
    images.push(...product.value.images.filter((img) => img !== product.value?.coverImage));
  }
  return images;
});
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>{{ t('page.user.home') }}</span> &gt; <span>{{ t('page.user.productDetail') }}</span>
    </div>

    <n-spin v-if="loading" size="large" class="loading-spin" />

    <template v-else-if="product">
      <!-- 详情主卡片 -->
      <div class="faka-card detail-main-card">
        <div class="detail-grid">
          <!-- 左侧预览图 -->
          <div class="gallery-col">
            <div class="main-img-box">
              <img v-if="allImages.length" :src="allImages[currentImage]" :alt="product.name" loading="lazy" />
              <div v-else class="img-placeholder">{{ t('page.user.noImage') }}</div>
            </div>
            <div class="thumb-list" v-if="allImages.length > 1">
              <div
                v-for="(img, idx) in allImages"
                :key="idx"
                :class="['thumb-item', { active: currentImage === idx }]"
                @click="currentImage = idx"
              >
                <img :src="img" loading="lazy" />
              </div>
            </div>
          </div>

          <!-- 右侧信息与购买区 -->
          <div class="info-col">
            <div class="info-header">
              <h1 class="product-title">{{ product.name }}</h1>
              <div class="tag-row">
                <span class="faka-tag category-tag" v-if="product.categoryName">
                  <span class="tag-icon">🏷️</span>{{ product.categoryName }}
                </span>
                <span class="faka-tag type-tag" v-if="product.productType">
                  <span class="tag-icon">📦</span>{{ PRODUCT_TYPE_MAP[product.productType]?.i18nKey ? t(PRODUCT_TYPE_MAP[product.productType].i18nKey) : t('page.user.normalProduct') }}
                </span>
              </div>
            </div>
            
            <div class="price-box">
              <div class="price-main">
                <span class="price-label">{{ t('page.user.price') }}</span>
                <div class="price-val">
                  <span class="currency">💰</span>
                  <span class="price-number">{{ product.price }}</span>
                  <span class="currency-unit">{{ t('page.user.credits') }}</span>
                </div>
              </div>
              <div class="stock-info" :class="{ 'low-stock': product.stock <= 10 }">
                <span class="stock-label">{{ t('page.user.stock') }}</span>
                <span class="stock-value">{{ product.stock }}</span>
              </div>
            </div>

            <!-- 内嵌购买表单 (Faka 标志性特点) -->
            <div class="buy-form" v-if="product.status === 1">
              <div class="form-row">
                <span class="form-label">{{ t('page.user.buyQuantity') }}</span>
                <div class="quantity-selector">
                  <button class="qty-btn" @click="quantity > 1 && quantity--" :disabled="quantity <= 1">−</button>
                  <input type="number" v-model.number="quantity" class="qty-input" :min="1" :max="product.stock" />
                  <button class="qty-btn" @click="quantity < product.stock && quantity++" :disabled="quantity >= product.stock">+</button>
                </div>
              </div>
              <div class="form-row">
                <span class="form-label">{{ t('page.user.contactInfo') }}</span>
                <input 
                  type="text" 
                  v-model="contactInfo" 
                  class="faka-input" 
                  :placeholder="t('page.user.contactPlaceholder')"
                />
              </div>
              <div class="form-row align-start">
                <span class="form-label">{{ t('page.user.remarkOptional') }}</span>
                <textarea 
                  v-model="remark" 
                  class="faka-textarea" 
                  :placeholder="t('page.user.remarkPlaceholder')"
                ></textarea>
              </div>
              
              <div class="action-row">
                <button 
                  class="faka-buy-btn" 
                  :disabled="buying"
                  @click="handleBuy"
                >
                  <span v-if="buying">{{ t('page.user.processing') }}</span>
                  <span v-else>{{ t('page.user.buyNowCredits', { amount: product.price * quantity }) }}</span>
                </button>
              </div>
            </div>

            <!-- 下架状态 -->
            <div v-else class="offline-box">
              {{ t('page.user.productOffline') }}
            </div>
          </div>
        </div>
      </div>

      <!-- 详情内容区 -->
      <div class="faka-card mt-24">
        <div class="card-header">{{ t('page.user.productIntro') }}</div>
        <div class="card-body html-content">
          <p class="desc-text">{{ product.description || t('page.user.noDescription') }}</p>
        </div>
      </div>

    </template>
    <div v-else class="empty-state">{{ t('page.user.productNotFound') }}</div>
  </div>
</template>

<style scoped>
/* 卡片覆盖：detail 页的卡片有额外 padding */
.faka-card {
  padding: 24px;
}

.loading-spin {
  display: flex;
  justify-content: center;
  padding: 100px;
}

.card-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
}

.card-body.html-content {
  line-height: 1.6;
  font-size: 14px;
  color: var(--faka-text-sub, #595959);
  padding-bottom: 16px;
}

/* 顶部网格 */
.detail-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 40px;
}

/* 详情主卡片增强 */
.detail-main-card {
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  overflow: hidden;
}

/* 左侧图库 */
.gallery-col {
  width: 100%;
  padding: 24px 0 24px 24px;
}
.main-img-box {
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.main-img-box img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.img-placeholder {
  color: var(--faka-text-sub, #bfbfbf);
  font-size: 16px;
}

.thumb-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  overflow-x: auto;
}
.thumb-item {
  width: 60px;
  height: 60px;
  border: 1px solid var(--faka-border, #f0f0f0);
  border-radius: 4px;
  cursor: pointer;
  opacity: 0.6;
}
.thumb-item.active, .thumb-item:hover {
  opacity: 1;
  border-color: #1890ff;
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 右侧信息 */
.info-col {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  padding: 0;
}

.info-header {
  padding: 24px 24px 0;
}

.product-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 14px;
  line-height: 1.4;
  color: var(--faka-text-main, #1a1a1a);
}

.tag-row {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.faka-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.faka-tag .tag-icon {
  font-size: 11px;
}

.faka-tag.category-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #bae7ff;
}

.faka-tag.type-tag {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

/* 价格大区块 - 美化 */
.price-box {
  background: linear-gradient(135deg, #fff5f0 0%, #fff8f5 100%);
  padding: 20px 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 24px 24px;
  border: 1px solid #ffd8bf;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-label {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
}

.price-val {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-number {
  font-size: 32px;
  font-weight: 800;
  color: #f5222d;
  letter-spacing: -1px;
}

.currency { font-size: 20px; }
.currency-unit { font-size: 14px; font-weight: 500; color: #f5222d; }

.stock-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.stock-label {
  font-size: 12px;
  color: #8c8c8c;
}

.stock-value {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.stock-info.low-stock .stock-value {
  color: #f5222d;
}

.stock-info.low-stock .stock-label {
  color: #f5222d;
}

/* 购买表单结构 */
.buy-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
  border-radius: 0 0 8px 8px;
}
.form-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.form-row.align-start {
  align-items: flex-start;
}
.form-label {
  width: 70px;
  font-size: 13px;
  color: #595959;
  text-align: right;
  flex-shrink: 0;
}

/* 自定义数量选择器 */
.quantity-selector {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  width: 140px;
  background: #fff;
}

.qty-btn {
  width: 36px;
  height: 32px;
  border: none;
  background: #fafafa;
  color: #595959;
  font-size: 18px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.qty-btn:hover {
  background: #e6e6e6;
  color: #1890ff;
}

.qty-btn:disabled {
  color: #d9d9d9;
  cursor: not-allowed;
  background: #fafafa;
}

.qty-input {
  flex: 1;
  height: 32px;
  border: none;
  border-left: 1px solid #d9d9d9;
  border-right: 1px solid #d9d9d9;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  outline: none;
  background: #fff;
  -moz-appearance: textfield;
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.faka-input {
  flex: 1;
  max-width: 320px;
  height: 38px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 0 12px;
  background: #fff;
  color: #333;
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}
.faka-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.faka-textarea {
  flex: 1;
  max-width: 320px;
  height: 60px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 6px 12px;
  background: #fff;
  color: #333;
  font-size: 13px;
  outline: none;
  resize: vertical;
  transition: all 0.2s;
}
.faka-textarea:focus { 
  border-color: #1890ff; 
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.action-row {
  margin-top: 8px;
  padding-left: 86px;
}
.faka-buy-btn {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: #fff;
  border: none;
  padding: 0 40px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}
.faka-buy-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}
.faka-buy-btn:active {
  transform: translateY(0);
}
.faka-buy-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.offline-box {
  padding: 24px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  color: #faad14;
  margin: 24px;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}

.empty-state { padding: 100px; }

@media (max-width: 768px) {
  .detail-grid { grid-template-columns: 1fr; gap: 24px; }
  .gallery-col { max-width: 360px; margin: 0 auto; padding: 16px; }
  .info-header { padding: 20px 20px 0; }
  .price-box { margin: 0 20px 20px; padding: 16px 20px; }
  .buy-form { padding: 20px; }
  .action-row { padding-left: 0; }
  .faka-input, .faka-textarea { max-width: 100%; }
}
</style>
