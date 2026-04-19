<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useAccessStore } from '@vben/stores';

import { productApi, orderApi } from '#/api/modules';

const route = useRoute();
const router = useRouter();
const message = useMessage();
const accessStore = useAccessStore();

const product = ref<any>(null);
const loading = ref(true);
const buying = ref(false);
const quantity = ref(1);
const showBuyModal = ref(false);
const contactInfo = ref('');
const remark = ref('');

async function loadProduct() {
  try {
    product.value = await productApi.detail(Number(route.params.id));
  } catch {
    message.error('Failed to load product');
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
      message.success('Order created successfully');
      router.push('/orders');
    }
  } catch (e: any) {
    message.error(e.message || 'Purchase failed');
  } finally {
    buying.value = false;
    showBuyModal.value = false;
  }
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <template v-else-if="product">
      <n-grid :cols="24" :x-gap="48">
        <n-grid-item :span="12">
          <div
            style="
              border-radius: 10px;
              overflow: hidden;
              background: rgba(128, 128, 128, 0.06);
              aspect-ratio: 1;
            "
          >
            <img
              v-if="product.coverImage"
              :src="product.coverImage"
              style="width: 100%; height: 100%; object-fit: cover"
            />
            <div
              v-else
              style="
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
              "
            >
              <span style="font-size: 64px; opacity: 0.3">📦</span>
            </div>
          </div>
        </n-grid-item>

        <n-grid-item :span="12">
          <div style="display: flex; gap: 8px; margin-bottom: 12px">
            <n-tag :type="product.productType === 1 ? 'info' : 'success'">
              {{ product.productType === 1 ? 'Virtual' : 'Physical' }}
            </n-tag>
            <n-tag v-if="product.categoryName" type="info">
              {{ product.categoryName }}
            </n-tag>
          </div>

          <h1
            style="
              font-size: 28px;
              font-weight: 700;
              margin-bottom: 12px;
            "
          >
            {{ product.name }}
          </h1>
          <p
            style="
              font-size: 16px;
              opacity: 0.6;
              margin-bottom: 24px;
              line-height: 1.65;
            "
          >
            {{ product.description }}
          </p>

          <n-card
            embedded
            style="margin-bottom: 24px; border-radius: 10px"
          >
            <span style="font-size: 36px; font-weight: 700; color: #18a058">
              {{ product.price }}
            </span>
            <span style="font-size: 14px; opacity: 0.5; margin-left: 4px">
              Credits
            </span>
          </n-card>

          <div
            style="
              display: flex;
              gap: 24px;
              margin-bottom: 24px;
              font-size: 14px;
              opacity: 0.6;
            "
          >
            <span>
              Stock:
              <strong style="opacity: 1">{{ product.stock }}</strong>
            </span>
            <span>
              Sold:
              <strong style="opacity: 1">{{ product.soldCount }}</strong>
            </span>
          </div>

          <div v-if="product.status === 1" style="display: flex; align-items: center; gap: 16px">
            <n-input-number
              v-model:value="quantity"
              :min="1"
              :max="product.stock"
              style="width: 120px"
            />
            <n-button
              type="primary"
              size="large"
              style="flex: 1"
              @click="handleBuyClick"
            >
              Buy Now
            </n-button>
          </div>
          <div v-else>
            <n-tag type="error" size="large">Off Shelf</n-tag>
          </div>
        </n-grid-item>
      </n-grid>
    </template>

    <n-modal
      v-model:show="showBuyModal"
      preset="card"
      title="Confirm Purchase"
      style="max-width: 480px"
    >
      <n-form label-placement="top">
        <n-form-item label="Contact Info">
          <n-input v-model:value="contactInfo" placeholder="Your contact information" />
        </n-form-item>
        <n-form-item label="Remark">
          <n-input
            v-model:value="remark"
            type="textarea"
            :rows="3"
            placeholder="Optional remark"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showBuyModal = false">Cancel</n-button>
          <n-button
            type="primary"
            :loading="buying"
            @click="handleBuy"
          >
            Confirm ({{ product?.price * quantity }} Credits)
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
