<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { dashboardApi } from '@/api'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const { t } = useI18n()
const dashboard = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await dashboardApi.getData()
    dashboard.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

const salesTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: dashboard.value?.salesTrend?.map((i: any) => i.date) || [] },
  yAxis: { type: 'value' },
  series: [{ data: dashboard.value?.salesTrend?.map((i: any) => i.amount) || [], type: 'line', smooth: true, areaStyle: { opacity: 0.1 }, lineStyle: { color: '#3b82f6' }, itemStyle: { color: '#3b82f6' } }],
}))

const statusPieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: ['40%', '70%'], data: dashboard.value?.paymentStatusDistribution?.map((i: any) => ({ name: i.statusName, value: i.count })) || [], emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' } } }],
}))

const productRankOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: dashboard.value?.productSalesRank?.map((i: any) => i.productName).reverse() || [] },
  series: [{ type: 'bar', data: dashboard.value?.productSalesRank?.map((i: any) => i.totalQuantity).reverse() || [], itemStyle: { color: '#3b82f6', borderRadius: [0, 4, 4, 0] } }],
}))
</script>

<template>
  <div>
    <div v-if="loading" style="text-align: center; padding: 60px;">{{ t('app.loading') }}</div>

    <div v-else-if="dashboard">
      <!-- 统计卡片 / Stat Cards -->
      <div style="display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; margin-bottom: 24px;">
        <div v-for="item in [
          { label: t('admin.dashboard.todayOrders'), value: dashboard.todayOrderCount, color: '#3b82f6' },
          { label: t('admin.dashboard.todaySales'), value: dashboard.todaySalesAmount + ' 积分', color: '#2563eb' },
          { label: t('admin.dashboard.totalUsers'), value: dashboard.totalUserCount, color: '#059669' },
          { label: t('admin.dashboard.totalOrders'), value: dashboard.totalOrderCount, color: '#7c3aed' },
          { label: t('admin.dashboard.pendingDisputes'), value: dashboard.pendingDisputeCount, color: '#d97706' },
          { label: t('admin.dashboard.pendingDelivery'), value: dashboard.pendingDeliveryCount, color: '#dc2626' },
        ]" :key="item.label" class="card" style="text-align: center;">
          <p style="font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; color: #6b7280; margin-bottom: 8px;">{{ item.label }}</p>
          <p :style="{ fontSize: '24px', fontWeight: 700, color: item.color }">{{ item.value }}</p>
        </div>
      </div>

      <!-- 图表 / Charts -->
      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 24px; margin-bottom: 24px;">
        <div class="card">
          <h3 style="font-size: 14px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 16px;">{{ t('admin.dashboard.salesTrend') }}</h3>
          <v-chart :option="salesTrendOption" style="height: 300px;" autoresize />
        </div>
        <div class="card">
          <h3 style="font-size: 14px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 16px;">{{ t('admin.dashboard.statusDistribution') }}</h3>
          <v-chart :option="statusPieOption" style="height: 300px;" autoresize />
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
        <div class="card">
          <h3 style="font-size: 14px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 16px;">{{ t('admin.dashboard.productRank') }}</h3>
          <v-chart :option="productRankOption" style="height: 300px;" autoresize />
        </div>
        <div class="card">
          <h3 style="font-size: 14px; font-weight: 600; color: var(--color-text-dark); margin-bottom: 16px;">{{ t('admin.dashboard.categoryDistribution') }}</h3>
          <div v-for="cat in dashboard.categorySalesDistribution" :key="cat.categoryName" style="display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--admin-card-border);">
            <span style="font-size: 14px; color: var(--color-text-dark);">{{ cat.categoryName }}</span>
            <span style="font-size: 14px; font-weight: 600; color: var(--color-primary);">{{ cat.totalAmount }} 积分</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
