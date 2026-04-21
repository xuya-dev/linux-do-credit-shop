<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import type { Order } from '#/api/types';
import {
  DELIVERY_STATUS_MAP,
  PAYMENT_STATUS_MAP,
} from '#/api/types';

import { orderApi } from '#/api/modules';
import '#/styles/faka-common.css';

const router = useRouter();
const message = useMessage();
const { t } = useI18n();

const orders = ref<Order[]>([]);
const loading = ref(true);
const page = ref(1);
const total = ref(0);
const statusFilter = ref<number | null>(null);

const statusTabs = [
  { labelKey: 'page.user.allOrders', value: null },
  { labelKey: 'page.user.pendingPayment', value: 0 },
  { labelKey: 'page.user.paid', value: 1 },
  { labelKey: 'page.user.refunded', value: 2 },
  { labelKey: 'page.user.cancelled', value: 3 },
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
  } catch (e: any) {
    console.error(e);
    message.error(e.message || t('page.user.loadOrdersFailed'));
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
    }).catch((err) => {
      message.error(err.message || t('page.user.payInitFailed'));
    });
  }
}
</script>

<template>
  <div class="faka-container">
    <div class="breadcrumb" @click="router.push('/home')">
      <span>{{ t('page.user.home') }}</span> &gt; <span>{{ t('page.user.myOrders') }}</span>
    </div>

    <!-- 订单查询面板 -->
    <div class="faka-card filter-card">
      <div class="card-header">{{ t('page.user.orderQuery') }}</div>
      <div class="card-body">
        <div class="filter-tabs">
          <span 
            v-for="tab in statusTabs" 
            :key="String(tab.value)"
            :class="['filter-tab', { active: statusFilter === tab.value }]"
            @click="handleTabChange(tab.value)"
          >
            {{ t(tab.labelKey) }}
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
                  <span class="order-no">{{ t('page.user.orderNo') }}: {{ order.orderNo }}</span>
                  <span class="order-date">{{ order.createdAt }}</span>
                </div>
                <div class="meta-right">
                  <span :class="['faka-status-tag', 'status-' + order.paymentStatus]">
                    {{ t(PAYMENT_STATUS_MAP[order.paymentStatus]?.i18nKey || '') }}
                  </span>
                </div>
              </div>

              <div class="order-content" @click="goDetail(order.id)">
                <div class="product-info">
                  <div class="product-name">{{ order.productName }}</div>
                  <div class="product-sku">{{ t('page.user.quantity') }}: {{ order.quantity }}{{ t('page.user.items') }}
                    <span v-if="order.paymentStatus === 1" class="dlv-tag">
                      [{{ t(DELIVERY_STATUS_MAP[order.deliveryStatus]?.i18nKey || '') }}]
                    </span>
                  </div>
                </div>
                <div class="order-price">
                  <span>{{ t('page.user.actualPayment') }}</span>
                  <span class="price-num">{{ order.totalAmount }} {{ t('page.user.credits') }}</span>
                </div>
              </div>

              <div class="order-actions">
                <button 
                  v-if="order.paymentStatus === 0" 
                  class="faka-btn primary"
                  @click.stop="goPay(order)"
                >{{ t('page.user.goPay') }}</button>
                <button class="faka-btn plain" @click="goDetail(order.id)">{{ t('page.user.viewDetail') }}</button>
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
          {{ t('page.user.noOrdersFound') }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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

/* Override faka-btn for order list context */
.faka-btn {
  padding: 4px 16px;
  font-size: 13px;
}
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 24px; }
</style>
