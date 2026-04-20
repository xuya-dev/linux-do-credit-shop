<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import type { Order } from '#/api/types';
import {
  DELIVERY_STATUS_MAP,
  PAYMENT_STATUS_MAP,
} from '#/api/types';

import { orderApi } from '#/api/modules';

const router = useRouter();

const orders = ref<Order[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const statusTabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已退款', value: 2 },
];

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

function goDetail(id: number) {
  router.push(`/order/${id}`);
}

function goPay(order: Order) {
  if (order.paymentStatus === 0) {
    orderApi.pay(order.id).then((res) => {
      if (res?.payUrl) {
        window.location.href = res.payUrl;
      }
    });
  }
}
</script>

<template>
  <div class="order-list-page">
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">查看和管理您的所有订单</p>
    </div>

    <!-- 状态筛选 -->
    <n-tabs
      :value="statusFilter"
      type="segment"
      class="status-tabs"
      @update:value="handleTabChange"
    >
      <n-tab-pane
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        :name="tab.value"
        :tab="tab.label"
      />
    </n-tabs>

    <!-- 订单列表 -->
    <div v-if="loading" class="order-skeleton-list">
      <n-card v-for="i in 3" :key="i" class="order-skeleton">
        <n-skeleton text :repeat="4" />
      </n-card>
    </div>

    <template v-else-if="orders.length">
      <div class="order-list">
        <n-card
          v-for="order in orders"
          :key="order.id"
          hoverable
          class="order-card"
        >
          <div class="order-header">
            <div class="order-meta">
              <span class="order-no">{{ order.orderNo }}</span>
              <span class="order-date">{{ order.createdAt }}</span>
            </div>
            <n-tag
              :type="PAYMENT_STATUS_MAP[order.paymentStatus]?.type || 'default'"
              size="small"
            >
              {{ PAYMENT_STATUS_MAP[order.paymentStatus]?.label }}
            </n-tag>
          </div>

          <div class="order-body" @click="goDetail(order.id)">
            <div class="order-product">
              <div class="product-cover-small">
                <img
                  v-if="order.productCoverImage"
                  :src="order.productCoverImage"
                  :alt="order.productName"
                />
                <div v-else>📦</div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ order.productName }}</h3>
                <div class="product-detail">
                  <span>数量: {{ order.quantity }}</span>
                  <n-tag
                    v-if="order.paymentStatus === 1"
                    :type="DELIVERY_STATUS_MAP[order.deliveryStatus]?.type || 'default'"
                    size="small"
                  >
                    {{ DELIVERY_STATUS_MAP[order.deliveryStatus]?.label }}
                  </n-tag>
                </div>
              </div>
            </div>
            <div class="order-amount">
              <span class="amount-value">{{ order.totalAmount }}</span>
              <span class="amount-unit">积分</span>
            </div>
          </div>

          <div class="order-footer">
            <n-button
              v-if="order.paymentStatus === 0"
              type="primary"
              size="small"
              @click="goPay(order)"
            >
              去支付
            </n-button>
            <n-button text size="small" @click="goDetail(order.id)">
              查看详情
            </n-button>
          </div>
        </n-card>
      </div>

      <div v-if="total > 10" class="pagination-wrapper">
        <n-pagination
          :page="page"
          :page-count="Math.ceil(total / 10)"
          @update:page="handlePageChange"
        />
      </div>
    </template>

    <n-empty v-else description="暂无订单" />
  </div>
</template>

<style scoped>
.order-list-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 15px;
  opacity: 0.5;
  margin: 0;
}

.status-tabs {
  margin-bottom: 24px;
}

.order-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card :deep(.n-card__content) {
  padding: 16px 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(128, 128, 128, 0.08);
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-size: 13px;
  font-weight: 600;
  opacity: 0.8;
  font-family: monospace;
}

.order-date {
  font-size: 12px;
  opacity: 0.4;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  padding: 4px 0;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.product-cover-small {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(128, 128, 128, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.product-cover-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-detail {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  opacity: 0.6;
}

.order-amount {
  text-align: right;
  flex-shrink: 0;
}

.amount-value {
  font-size: 20px;
  font-weight: 700;
  color: #18a058;
}

.amount-unit {
  font-size: 12px;
  opacity: 0.6;
  margin-left: 2px;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(128, 128, 128, 0.08);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .order-list-page {
    padding: 20px 16px;
  }

  .order-body {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-amount {
    width: 100%;
    text-align: left;
    padding-top: 8px;
    border-top: 1px solid rgba(128, 128, 128, 0.06);
  }
}
</style>
