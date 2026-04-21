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
            <h1 class="product-title">{{ product.name }}</h1>
            <div class="tag-row">
              <span class="faka-tag" v-if="product.categoryName">{{ product.categoryName }}</span>
              <span class="faka-tag outline" v-if="product.productType">
                {{ PRODUCT_TYPE_MAP[product.productType]?.i18nKey ? t(PRODUCT_TYPE_MAP[product.productType].i18nKey) : t('page.user.normalProduct') }}
              </span>
            </div>
            
            <div class="price-box">
              <div class="price-label">{{ t('page.user.price') }}</div>
              <div class="price-val">
                <span class="currency">💰</span>
                {{ product.price }}
                <span class="currency-unit">{{ t('page.user.credits') }}</span>
              </div>
              <div class="stock-info">{{ t('page.user.stock') }}: <span>{{ product.stock }}</span></div>
            </div>

            <!-- 内嵌购买表单 (Faka 标志性特点) -->
            <div class="buy-form" v-if="product.status === 1">
              <div class="form-row">
                <span class="form-label">{{ t('page.user.buyQuantity') }}</span>
                <n-input-number
                  v-model:value="quantity"
                  :min="1"
                  :max="product.stock"
                  button-placement="both"
                  class="faka-input-number"
                />
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
  gap: 32px;
}

/* 左侧图库 */
.gallery-col {
  width: 100%;
}
.main-img-box {
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid var(--faka-border, #f0f0f0);
  border-radius: 4px;
  overflow: hidden;
  background: var(--faka-tag-bg, #fcfcfc);
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
}
.product-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px;
}
.tag-row {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}
.faka-tag {
  background: #e6f7ff;
  color: #1890ff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 2px;
}
.faka-tag.outline {
  background: transparent;
  border: 1px solid #1890ff;
}

/* 发卡价格大区块 */
.price-box {
  background: var(--faka-tag-bg, #f8f8f8);
  padding: 16px 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}
.price-label {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
}
.price-val {
  font-size: 28px;
  font-weight: 700;
  color: #f5222d;
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.currency { font-size: 18px; }
.currency-unit { font-size: 14px; font-weight: 400; color: #f5222d; }
.stock-info {
  margin-left: auto;
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
}
.stock-info span { color: var(--faka-text-main, #333); font-weight: 600; }

/* 购买表单结构 */
.buy-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--faka-border, #f0f0f0);
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
  color: var(--faka-text-sub, #595959);
  text-align: right;
  flex-shrink: 0;
}

.faka-input-number {
  width: 180px;
}

/* 修复 NInputNumber 在 flex 容器中的对齐 */
.faka-input-number :deep(.n-input__prefix),
.faka-input-number :deep(.n-input__suffix) {
  display: flex;
  align-items: center;
}

.faka-input-number :deep(.n-input__input-el) {
  text-align: center;
}

.faka-input {
  flex: 1;
  max-width: 320px;
  height: 36px;
  border: 1px solid var(--faka-border, #d9d9d9);
  border-radius: 2px;
  padding: 0 12px;
  background: var(--faka-bg-header, #fff);
  color: var(--faka-text-main, #333);
  font-size: 13px;
  outline: none;
}
.faka-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.faka-textarea {
  flex: 1;
  max-width: 320px;
  height: 60px;
  border: 1px solid var(--faka-border, #d9d9d9);
  border-radius: 2px;
  padding: 6px 12px;
  background: var(--faka-bg-header, #fff);
  color: var(--faka-text-main, #333);
  font-size: 13px;
  outline: none;
  resize: vertical;
}
.faka-textarea:focus { border-color: #1890ff; }

.action-row {
  margin-top: 12px;
  padding-left: 86px;
}
.faka-buy-btn {
  background: #1890ff;
  color: #fff;
  border: none;
  padding: 0 32px;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 2px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.faka-buy-btn:hover {
  opacity: 0.85;
}
.faka-buy-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.offline-box {
  padding: 24px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  color: #faad14;
  margin-top: 24px;
  border-radius: 2px;
  text-align: center;
}

.empty-state { padding: 100px; }

@media (max-width: 768px) {
  .detail-grid { grid-template-columns: 1fr; }
  .gallery-col { max-width: 360px; margin: 0 auto; }
  .action-row { padding-left: 0; }
  .faka-input, .faka-textarea { max-width: 100%; }
}
</style>
