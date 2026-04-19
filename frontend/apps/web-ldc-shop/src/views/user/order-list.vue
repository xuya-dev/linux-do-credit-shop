<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { orderApi } from '#/api/modules';

const router = useRouter();

const orders = ref<any[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const statusTabs = [
  { label: 'All', value: null },
  { label: 'Pending', value: 0 },
  { label: 'Paid', value: 1 },
  { label: 'Refunded', value: 2 },
];

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

async function loadOrders() {
  loading.value = true;
  try {
    const res = await orderApi.userList({
      page: page.value,
      size: 10,
      paymentStatus: statusFilter.value ?? undefined,
    });
    orders.value = res?.records || [];
    total.value = res?.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

onMounted(loadOrders);

function handleTabChange(value: number | null) {
  statusFilter.value = value;
  page.value = 1;
  loadOrders();
}

function handlePageChange(p: number) {
  page.value = p;
  loadOrders();
}
</script>

<template>
  <div style="max-width: 1120px; margin: 0 auto; padding: 40px 24px">
    <div
      style="
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
      "
    >
      <h1
        style="
          font-size: 28px;
          font-weight: 700;
        "
      >
        My Orders
      </h1>
      <n-space>
        <n-button
          v-for="s in statusTabs"
          :key="String(s.value)"
          :type="statusFilter === s.value ? 'primary' : 'default'"
          size="small"
          @click="handleTabChange(s.value)"
        >
          {{ s.label }}
        </n-button>
      </n-space>
    </div>

    <n-spin v-if="loading" style="width: 100%; padding: 60px" />

    <template v-else>
      <n-card
        v-for="order in orders"
        :key="order.id"
        hoverable
        style="margin-bottom: 12px"
      >
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <div>
            <span style="font-size: 13px; opacity: 0.5">
              {{ order.orderNo }}
            </span>
            <h3
              style="
                font-size: 16px;
                font-weight: 600;
                margin: 4px 0 0;
              "
            >
              {{ order.productName }}
            </h3>
          </div>
          <div style="text-align: right">
            <span style="font-size: 18px; font-weight: 700; color: #18a058">
              {{ order.totalAmount }}
              <small style="font-size: 12px">credits</small>
            </span>
          </div>
        </div>
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px solid rgba(128, 128, 128, 0.1);
          "
        >
          <n-space>
            <n-tag
              :type="getPaymentType(order.paymentStatus)"
              size="small"
            >
              {{ getPaymentLabel(order.paymentStatus) }}
            </n-tag>
            <n-tag
              v-if="order.paymentStatus === 1"
              :type="getDeliveryType(order.deliveryStatus)"
              size="small"
            >
              {{ getDeliveryLabel(order.deliveryStatus) }}
            </n-tag>
          </n-space>
          <n-space align="center">
            <span style="font-size: 13px; opacity: 0.5">
              {{ order.createdAt }}
            </span>
            <n-button
              text
              type="primary"
              @click="router.push('/order/' + order.id)"
            >
              Detail &rarr;
            </n-button>
          </n-space>
        </div>
      </n-card>

      <n-empty
        v-if="!orders.length"
        description="No orders found"
        style="padding: 60px"
      />
    </template>

    <div
      v-if="total > 10"
      style="display: flex; justify-content: center; margin-top: 32px"
    >
      <n-pagination
        :page="page"
        :page-count="Math.ceil(total / 10)"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>
