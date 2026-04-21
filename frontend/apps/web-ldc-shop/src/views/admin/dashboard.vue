<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useMessage } from 'naive-ui';
import { useI18n } from '@vben/locales';

import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, PieChart, BarChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components';
import VChart from 'vue-echarts';

import { dashboardApi } from '#/api/modules';

const { t } = useI18n();
const message = useMessage();

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
]);

const dashboard = ref<any>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    dashboard.value = await dashboardApi.getData();
  } catch (e: any) {
    console.error(e);
    message.error(e.message || '加载仪表盘数据失败');
  } finally {
    loading.value = false;
  }
});

const statCards = computed(() => {
  const d = dashboard.value;
  if (!d) return [];
  return [
    { label: t('page.admin.todayOrders'), value: d.todayOrderCount, color: '#3b82f6' },
    { label: t('page.admin.todaySales'), value: `${d.todaySalesAmount} ${t('page.shop.credits')}`, color: '#2563eb' },
    { label: t('page.admin.totalUsers'), value: d.totalUserCount, color: '#059669' },
    { label: t('page.admin.totalOrders'), value: d.totalOrderCount, color: '#7c3aed' },
    { label: t('page.admin.pendingDisputes'), value: d.pendingDisputeCount, color: '#d97706' },
    { label: t('page.admin.pendingDeliveries'), value: d.pendingDeliveryCount, color: '#dc2626' },
  ];
});

const salesTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: dashboard.value?.salesTrend?.map((i: any) => i.date) || [],
  },
  yAxis: { type: 'value' },
  series: [
    {
      data: dashboard.value?.salesTrend?.map((i: any) => i.amount) || [],
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.1 },
      lineStyle: { color: '#3b82f6' },
      itemStyle: { color: '#3b82f6' },
    },
  ],
}));

const statusPieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      data:
        dashboard.value?.paymentStatusDistribution?.map((i: any) => ({
          name: i.statusName,
          value: i.count,
        })) || [],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0,0,0,0.3)',
        },
      },
    },
  ],
}));

const productRankOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value' },
  yAxis: {
    type: 'category',
    data:
      dashboard.value?.productSalesRank?.map((i: any) => i.productName).reverse() ||
      [],
  },
  series: [
    {
      type: 'bar',
      data:
        dashboard.value?.productSalesRank?.map((i: any) => i.totalQuantity).reverse() ||
        [],
      itemStyle: { color: '#3b82f6', borderRadius: [0, 4, 4, 0] },
    },
  ],
}));
</script>

<template>
  <div class="p-5">
    <n-spin :show="loading">
      <template v-if="dashboard">
        <n-grid :cols="3" :x-gap="12" :y-gap="12" style="margin-bottom: 16px">
          <n-gi v-for="item in statCards" :key="item.label">
            <n-card size="small" style="text-align: center">
              <p
                style="
                  font-size: 11px;
                  font-weight: 600;
                  text-transform: uppercase;
                  letter-spacing: 0.08em;
                  color: #6b7280;
                  margin-bottom: 8px;
                "
              >
                {{ item.label }}
              </p>
              <p :style="{ fontSize: '24px', fontWeight: 700, color: item.color }">
                {{ item.value }}
              </p>
            </n-card>
          </n-gi>
        </n-grid>

        <n-grid :cols="24" :x-gap="24" :y-gap="24" style="margin-bottom: 24px">
          <n-gi :span="16">
            <n-card :title="t('page.admin.salesTrend')">
              <v-chart :option="salesTrendOption" style="height: 300px" autoresize />
            </n-card>
          </n-gi>
          <n-gi :span="8">
            <n-card :title="t('page.admin.paymentDistribution')">
              <v-chart :option="statusPieOption" style="height: 300px" autoresize />
            </n-card>
          </n-gi>
        </n-grid>

        <n-grid :cols="24" :x-gap="24" :y-gap="24">
          <n-gi :span="12">
            <n-card :title="t('page.admin.productRank')">
              <v-chart :option="productRankOption" style="height: 300px" autoresize />
            </n-card>
          </n-gi>
          <n-gi :span="12">
            <n-card :title="t('page.admin.categoryDistribution')">
              <div
                v-for="cat in dashboard.categorySalesDistribution"
                :key="cat.categoryName"
                style="
                  display: flex;
                  justify-content: space-between;
                  padding: 10px 0;
                  border-bottom: 1px solid #f0f0f0;
                "
              >
                <span style="font-size: 14px">{{ cat.categoryName }}</span>
                <span style="font-size: 14px; font-weight: 600; color: #3b82f6">
                  {{ cat.totalAmount }} {{ t('page.shop.credits') }}
                </span>
              </div>
            </n-card>
          </n-gi>
        </n-grid>
      </template>
    </n-spin>
  </div>
</template>
