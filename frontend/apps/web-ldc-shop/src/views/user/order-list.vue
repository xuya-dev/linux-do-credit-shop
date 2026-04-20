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
  { label: '全部订单', value: null },
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
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>首页</span> &gt; <span>我的订单</span>
    </div>

    <!-- 订单查询面板 -->
    <div class="faka-card filter-card">
      <div class="card-header">订单查询</div>
      <div class="card-body">
        <div class="filter-tabs">
          <span 
            v-for="tab in statusTabs" 
            :key="String(tab.value)"
            :class="['filter-tab', { active: statusFilter === tab.value }]"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
          </span>
        </div>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="faka-card mt-24">
      <div class="card-body">
        <n-spin v-if="loading" size="large" class="loading-spin" />

        <template v-else-if="orders.length">
          <div class="order-list">
            <div
              v-for="order in orders"
              :key="order.id"
              class="order-item-box"
            >
              <div class="order-meta-head">
                <div class="meta-left">
                  <span class="order-no">订单号: {{ order.orderNo }}</span>
                  <span class="order-date">{{ order.createdAt }}</span>
                </div>
                <div class="meta-right">
                  <span :class="['faka-status-tag', 'status-' + order.paymentStatus]">
                    {{ PAYMENT_STATUS_MAP[order.paymentStatus]?.label }}
                  </span>
                </div>
              </div>

              <div class="order-content" @click="goDetail(order.id)">
                <div class="product-info">
                  <div class="product-name">{{ order.productName }}</div>
                  <div class="product-sku">数量: {{ order.quantity }}件 
                    <span v-if="order.paymentStatus === 1" class="dlv-tag">
                      [{{ DELIVERY_STATUS_MAP[order.deliveryStatus]?.label }}]
                    </span>
                  </div>
                </div>
                <div class="order-price">
                  <span>实付款：</span>
                  <span class="price-num">{{ order.totalAmount }} 积分</span>
                </div>
              </div>

              <div class="order-actions">
                <button 
                  v-if="order.paymentStatus === 0" 
                  class="faka-btn primary"
                  @click.stop="goPay(order)"
                >去支付</button>
                <button class="faka-btn plain" @click="goDetail(order.id)">查看详情</button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="total > 10" class="pagination-wrapper">
            <n-pagination
              :page="page"
              :page-count="Math.ceil(total / 10)"
              @update:page="handlePageChange"
            />
          </div>
        </template>

        <div v-else class="empty-state">
          未查询到相关订单记录
        </div>
      </div>
    </div>
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

.faka-card {
  background: var(--faka-bg-header, #ffffff);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  color: var(--faka-text-main, #333);
}

.mt-24 { margin-top: 24px; }

.card-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid var(--faka-border, #f0f0f0);
}

.card-body {
  padding: 20px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}
.filter-tab {
  padding: 6px 16px;
  font-size: 13px;
  color: var(--faka-text-sub, #595959);
  cursor: pointer;
  border-radius: 2px;
  border: 1px solid var(--faka-border, #d9d9d9);
  transition: all 0.2s;
}
.filter-tab.active {
  background: #1890ff;
  color: #fff;
  border-color: #1890ff;
}
.filter-tab:hover:not(.active) {
  color: #1890ff;
  border-color: #1890ff;
}

.loading-spin {
  display: flex;
  justify-content: center;
  padding: 60px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item-box {
  border: 1px solid var(--faka-border, #e8e8e8);
  border-radius: 2px;
  transition: box-shadow 0.2s;
}
.order-item-box:hover {
  border-color: var(--faka-border, #d9d9d9);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.order-meta-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: var(--faka-tag-bg, #fafafa);
  border-bottom: 1px solid var(--faka-border, #e8e8e8);
  font-size: 13px;
}
.meta-left {
  display: flex;
  gap: 16px;
  color: var(--faka-text-sub, #595959);
}
.order-no { font-weight: 500; color: var(--faka-text-main, #333); }

.faka-status-tag {
  font-size: 12px;
  font-weight: 500;
}
.status-0 { color: #faad14; }
.status-1 { color: #52c41a; }
.status-2 { color: #f5222d; }

.order-content {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  cursor: pointer;
}
.product-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--faka-text-main, #333);
}
.product-sku {
  font-size: 13px;
  color: var(--faka-text-sub, #8c8c8c);
}
.dlv-tag { color: #1890ff; font-size: 12px; margin-left: 8px; }

.order-price {
  font-size: 13px;
  color: var(--faka-text-sub, #595959);
  text-align: right;
}
.price-num {
  font-size: 18px;
  font-weight: 700;
  color: #f5222d;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 10px 16px;
  border-top: 1px dashed var(--faka-border, #f0f0f0);
}

.faka-btn {
  padding: 4px 16px;
  font-size: 13px;
  border-radius: 2px;
  cursor: pointer;
  border: 1px solid transparent;
}
.faka-btn.primary {
  background: #1890ff;
  color: #fff;
}
.faka-btn.plain {
  background: transparent;
  border-color: var(--faka-border, #d9d9d9);
  color: var(--faka-text-main, #333);
}
.faka-btn.plain:hover { color: #1890ff; border-color: #1890ff; }

.empty-state { text-align: center; padding: 60px; color: var(--faka-text-sub); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 24px; }
</style>
