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
const showBuyModal = ref(false);
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

function handleBuyClick() {
  if (!accessStore.accessToken) {
    router.push('/auth/login');
    return;
  }
  showBuyModal.value = true;
}

async function handleBuy() {
  if (!product.value) return;
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
    showBuyModal.value = false;
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
  <div class="product-detail-page">
    <n-spin v-if="loading" size="large" style="padding: 80px" />

    <template v-else-if="product">
      <div class="detail-layout">
        <!-- 左侧图片区 -->
        <div class="detail-gallery">
          <div class="main-image">
            <img
              v-if="allImages.length"
              :src="allImages[currentImage]"
              :alt="product.name"
            />
            <div v-else class="image-placeholder">📦</div>
          </div>
          <div v-if="allImages.length > 1" class="thumb-list">
            <div
              v-for="(img, idx) in allImages"
              :key="idx"
              :class="['thumb-item', { active: currentImage === idx }]"
              @click="currentImage = idx"
            >
              <img :src="img" :alt="product.name" />
            </div>
          </div>
        </div>

        <!-- 右侧信息区 -->
        <div class="detail-info">
          <div class="info-tags">
            <n-tag
              v-if="product.productType"
              :type="PRODUCT_TYPE_MAP[product.productType]?.type || 'default'"
            >
              {{ PRODUCT_TYPE_MAP[product.productType]?.label }}
            </n-tag>
            <n-tag v-if="product.categoryName" type="info">
              {{ product.categoryName }}
            </n-tag>
          </div>

          <h1 class="info-title">{{ product.name }}</h1>

          <p class="info-desc">{{ product.description }}</p>

          <div class="info-price">
            <span class="price-value">{{ product.price }}</span>
            <span class="price-unit">积分</span>
          </div>

          <div class="info-stats">
            <div class="stat-item">
              <span class="stat-label">库存</span>
              <span class="stat-value">{{ product.stock }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已售</span>
              <span class="stat-value">{{ product.soldCount }}</span>
            </div>
          </div>

          <div v-if="product.status === 1" class="info-actions">
            <div class="quantity-selector">
              <span class="qty-label">数量</span>
              <n-input-number
                v-model:value="quantity"
                :min="1"
                :max="product.stock"
                style="width: 120px"
              />
            </div>
            <n-button
              type="primary"
              size="large"
              class="buy-btn"
              @click="handleBuyClick"
            >
              立即购买 ({{ product.price * quantity }} 积分)
            </n-button>
          </div>
          <n-tag v-else type="error" size="large">已下架</n-tag>
        </div>
      </div>
    </template>

    <!-- 购买弹窗 -->
    <n-modal
      v-model:show="showBuyModal"
      preset="card"
      title="确认购买"
      style="max-width: 480px"
    >
      <n-form label-placement="top">
        <n-form-item label="联系信息">
          <n-input
            v-model:value="contactInfo"
            placeholder="请输入您的联系信息（邮箱/QQ/微信等）"
          />
        </n-form-item>
        <n-form-item label="备注">
          <n-input
            v-model:value="remark"
            type="textarea"
            :rows="3"
            placeholder="可选备注信息"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showBuyModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="buying"
            @click="handleBuy"
          >
            确认支付 ({{ (product?.price || 0) * quantity }} 积分)
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.product-detail-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: start;
}

.detail-gallery {
  position: sticky;
  top: 84px;
}

.main-image {
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(128, 128, 128, 0.06);
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  opacity: 0.3;
}

.thumb-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.thumb-item {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
  flex-shrink: 0;
}

.thumb-item.active {
  border-color: #18a058;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  padding-top: 8px;
}

.info-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.info-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px;
  line-height: 1.3;
}

.info-desc {
  font-size: 15px;
  opacity: 0.7;
  line-height: 1.7;
  margin: 0 0 24px;
}

.info-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 24px;
  padding: 20px;
  border-radius: 12px;
  background: rgba(128, 128, 128, 0.06);
}

.price-value {
  font-size: 36px;
  font-weight: 800;
  color: #18a058;
  letter-spacing: -0.02em;
}

.price-unit {
  font-size: 14px;
  opacity: 0.6;
}

.info-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(128, 128, 128, 0.1);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.5;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
}

.info-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.qty-label {
  font-size: 14px;
  font-weight: 500;
}

.buy-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .detail-gallery {
    position: static;
  }

  .info-title {
    font-size: 22px;
  }

  .price-value {
    font-size: 28px;
  }
}
</style>
