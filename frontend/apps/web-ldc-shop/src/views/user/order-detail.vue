<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useMessage } from 'naive-ui';

import { orderApi } from '#/api/modules';

const route = useRoute();
const message = useMessage();
const order = ref<any>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    order.value = await orderApi.detail(Number(route.params.id));
  } catch {
    message.error('Failed to load order');
  } finally {
    loading.value = false;
  }
});

function copyCard(text: string) {
  navigator.clipboard.writeText(text);
  message.success('Copied to clipboard');
}

function getPaymentType(status: number): 'default' | 'success' | 'warning' | 'error' {
  if (status === 1) return 'success';
  if (status === 2) return 'warning';
  return 'error';
}

function getPaymentLabel(status: number) {
  if (status === 0) return 'Pending';
  if (status === 1) return 'Paid';
  return 'Refunded';
}

function getDeliveryType(status: number): 'default' | 'success' | 'info' {
  return status === 2 ? 'success' : 'info';
}

function getDeliveryLabel(status: number) {
  if (status === 0) return 'Pending Delivery';
  if (status === 1) return 'Delivered';
  return 'Completed';
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <template v-else-if="order">
      <n-card style="margin-bottom: 24px">
        <div
          style="
            display: flex;
            justify-content: space-between;
            margin-bottom: 20px;
          "
        >
          <div>
            <h2 style="font-size: 20px; font-weight: 700; margin: 0 0 4px">
              {{ order.productName }}
            </h2>
            <p style="font-size: 13px; opacity: 0.5; margin: 0">
              {{ order.orderNo }}
            </p>
          </div>
          <div style="text-align: right">
            <span style="font-size: 28px; font-weight: 700; color: #18a058">
              {{ order.totalAmount }}
            </span>
            <span style="font-size: 14px; opacity: 0.5"> Credits</span>
          </div>
        </div>

        <n-grid :cols="3" :x-gap="16">
          <n-grid-item>
            <span style="font-size: 12px; opacity: 0.5">Payment Status</span>
            <div style="margin-top: 4px">
              <n-tag
                :type="getPaymentType(order.paymentStatus)"
                size="small"
              >
                {{ getPaymentLabel(order.paymentStatus) }}
              </n-tag>
            </div>
          </n-grid-item>
          <n-grid-item>
            <span style="font-size: 12px; opacity: 0.5">Delivery Status</span>
            <div style="margin-top: 4px">
              <n-tag
                :type="getDeliveryType(order.deliveryStatus)"
                size="small"
              >
                {{ getDeliveryLabel(order.deliveryStatus) }}
              </n-tag>
            </div>
          </n-grid-item>
          <n-grid-item>
            <span style="font-size: 12px; opacity: 0.5">Quantity</span>
            <div style="margin-top: 4px">
              <span style="font-weight: 600">{{ order.quantity }}</span>
            </div>
          </n-grid-item>
        </n-grid>
      </n-card>

      <n-card
        v-if="order.cardContents?.length"
        style="margin-bottom: 24px"
      >
        <h3
          style="
            font-size: 16px;
            font-weight: 600;
            margin: 0 0 16px;
          "
        >
          Card Contents
        </h3>
        <div
          v-for="(card, i) in order.cardContents"
          :key="i"
          style="
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px;
            background: rgba(128, 128, 128, 0.06);
            border-radius: 8px;
            margin-bottom: 8px;
          "
        >
          <code
            style="
              font-size: 14px;
              word-break: break-all;
            "
          >
            {{ card }}
          </code>
          <n-button
            size="small"
            type="primary"
            style="margin-left: 12px; white-space: nowrap"
            @click="copyCard(card)"
          >
            Copy
          </n-button>
        </div>
      </n-card>

      <n-card>
        <n-descriptions label-placement="top" :column="2" bordered>
          <n-descriptions-item v-if="order.contactInfo" label="Contact Info">
            {{ order.contactInfo }}
          </n-descriptions-item>
          <n-descriptions-item v-if="order.remark" label="Remark">
            {{ order.remark }}
          </n-descriptions-item>
          <n-descriptions-item
            v-if="order.deliveryInfo"
            label="Delivery Info"
          >
            {{ order.deliveryInfo }}
          </n-descriptions-item>
          <n-descriptions-item label="Created At">
            {{ order.createdAt }}
          </n-descriptions-item>
        </n-descriptions>
      </n-card>
    </template>
  </div>
</template>
