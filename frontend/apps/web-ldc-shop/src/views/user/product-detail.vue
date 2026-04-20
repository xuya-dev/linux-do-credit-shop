<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useAccessStore } from '@vben/stores';

import type { Product } from '#/api/types';
import { PRODUCT_TYPE_MAP } from '#/api/types';
import { orderApi, productApi } from '#/api/modules';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const accessStore = useAccessStore();

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
    message.error('加载商品失败');
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
    message.warning('请输入联系方式（如邮箱）确保商品送达');
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
      message.success('订单创建成功');
      router.push('/orders');
    }
  } catch (e: any) {
    message.error(e.message || '购买失败');
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
      <span>首页</span> &gt; <span>商品详情</span>
    </div>

    <n-spin v-if="loading" size="large" class="loading-spin" />

    <template v-else-if="product">
      <!-- 详情主卡片 -->
      <div class="faka-card detail-main-card">
        <div class="detail-grid">
          <!-- 左侧预览图 -->
          <div class="gallery-col">
            <div class="main-img-box">
              <img v-if="allImages.length" :src="allImages[currentImage]" :alt="product.name" />
              <div v-else class="img-placeholder">暂无图片</div>
            </div>
            <div class="thumb-list" v-if="allImages.length > 1">
              <div
                v-for="(img, idx) in allImages"
                :key="idx"
                :class="['thumb-item', { active: currentImage === idx }]"
                @click="currentImage = idx"
              >
                <img :src="img" />
              </div>
            </div>
          </div>

          <!-- 右侧信息与购买区 -->
          <div class="info-col">
            <h1 class="product-title">{{ product.name }}</h1>
            <div class="tag-row">
              <span class="faka-tag" v-if="product.categoryName">{{ product.categoryName }}</span>
              <span class="faka-tag outline" v-if="product.productType">
                {{ PRODUCT_TYPE_MAP[product.productType]?.label || '普通产品' }}
              </span>
            </div>
            
            <div class="price-box">
              <div class="price-label">售价</div>
              <div class="price-val">
                <span class="currency">💰</span>
                {{ product.price }}
                <span class="currency-unit">积分</span>
              </div>
              <div class="stock-info">库存: <span>{{ product.stock }}</span></div>
            </div>

            <!-- 内嵌购买表单 (Faka 标志性特点) -->
            <div class="buy-form" v-if="product.status === 1">
              <div class="form-row">
                <span class="form-label">购买数量</span>
                <n-input-number
                  v-model:value="quantity"
                  :min="1"
                  :max="product.stock"
                  button-placement="both"
                  class="faka-input-number"
                />
              </div>
              <div class="form-row">
                <span class="form-label">联系方式</span>
                <input 
                  type="text" 
                  v-model="contactInfo" 
                  class="faka-input" 
                  placeholder="请输入您的邮箱/QQ/手机号" 
                />
              </div>
              <div class="form-row align-start">
                <span class="form-label">备注(选填)</span>
                <textarea 
                  v-model="remark" 
                  class="faka-textarea" 
                  placeholder="如有特殊要求请填写" 
                ></textarea>
              </div>
              
              <div class="action-row">
                <button 
                  class="faka-buy-btn" 
                  :disabled="buying"
                  @click="handleBuy"
                >
                  <span v-if="buying">处理中...</span>
                  <span v-else>立即兑换 ({{ (product.price * quantity) }} 积分)</span>
                </button>
              </div>
            </div>

            <!-- 下架状态 -->
            <div v-else class="offline-box">
              对不起，该商品已下架或售罄，无法购买！
            </div>
          </div>
        </div>
      </div>

      <!-- 详情内容区 -->
      <div class="faka-card mt-24">
        <div class="card-header">商品介绍</div>
        <div class="card-body html-content">
          <p class="desc-text">{{ product.description || '店主很懒，没有任何介绍。' }}</p>
        </div>
      </div>

    </template>
    <div v-else class="empty-state">未找到商品信息</div>
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

.loading-spin {
  display: flex;
  justify-content: center;
  padding: 100px;
}

/* 卡片 */
.faka-card {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  color: var(--faka-text-main, #333);
  padding: 24px;
}

.mt-24 {
  margin-top: 24px;
}

.card-header {
  font-size: 15px;
  font-weight: 600;
  color: var(--faka-text-main, #333);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
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
  color: #f5222d; /* 经典发卡红或蓝 */
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
  width: 140px;
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
  padding-left: 86px; /* label 70 + gap 16 */
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

.empty-state { text-align: center; padding: 100px; color: var(--faka-text-sub); }

@media (max-width: 768px) {
  .detail-grid { grid-template-columns: 1fr; }
  .gallery-col { max-width: 360px; margin: 0 auto; }
  .action-row { padding-left: 0; }
  .faka-input, .faka-textarea { max-width: 100%; }
}
</style>
