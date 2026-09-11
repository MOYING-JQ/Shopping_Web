<template>
  <div class="admin-page animate-fade-in-up dashboard-page">
    <!-- 页头 -->
    <header class="admin-header stagger-1">
      <div>
        <h1 class="admin-title">
          <el-icon :size="22" color="var(--gold-300)"><DataAnalysis /></el-icon>
          数据看板
        </h1>
        <div class="admin-subtitle">实时掌握经营核心数据，洞察业务趋势</div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" round @click="handleRefresh">刷新数据</el-button>
      </div>
    </header>

    <!-- 统计卡片 -->
    <div class="stat-grid stagger-2">
      <div class="stat-card" v-for="(item, i) in statList" :key="i" :style="{ animationDelay: (i * 60) + 'ms' }">
        <div class="stat-inner">
          <div class="icon-box" :style="{ background: item.grad }">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-meta">
            <div class="stat-value" :data-test="item.value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
          <div class="deco">
            <el-icon :size="60"><component :is="item.icon" /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid stagger-3">
      <div class="chart-card chart-wide">
        <div class="chart-head">
          <h3 class="chart-title"><span />近 7 天销售趋势</h3>
          <div class="chart-tools">
            <div class="legend-chip chip-gold">销售额</div>
            <div class="legend-chip chip-indigo">订单量</div>
          </div>
        </div>
        <div ref="salesTrendRef" class="chart-box" style="height: 320px"></div>
      </div>
    </div>

    <div class="chart-grid stagger-3">
      <div class="chart-card">
        <div class="chart-head">
          <h3 class="chart-title"><span />订单状态分布</h3>
        </div>
        <div ref="orderStatusRef" class="chart-box" style="height: 320px"></div>
      </div>
      <div class="chart-card">
        <div class="chart-head">
          <h3 class="chart-title"><span />商品销量 TOP 10</h3>
        </div>
        <div ref="topProductsRef" class="chart-box" style="height: 320px"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { Money, List, User, Goods, DataAnalysis, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOverview, getSalesTrend, getOrderStatusDist } from '../api/dashboard'
import { getTopSalesProducts } from '../api/product'

const overview = ref({})
const salesTrendRef = ref(null)
const orderStatusRef = ref(null)
const topProductsRef = ref(null)

const statList = computed(() => [
  {
    label: '总销售额 (¥)',
    value: formatMoney(overview.value.totalSales),
    icon: Money,
    grad: 'linear-gradient(135deg, #f2d06b 0%, #d4af37 50%, #b8941f 100%)'
  },
  {
    label: '订单总数',
    value: overview.value.totalOrders || 0,
    icon: List,
    grad: 'linear-gradient(135deg, #a0b0d4 0%, #7c8db5 50%, #5a6a8a 100%)'
  },
  {
    label: '用户总数',
    value: overview.value.totalCustomers || 0,
    icon: User,
    grad: 'linear-gradient(135deg, #8fbcd4 0%, #6a9fb5 50%, #4a7a9a 100%)'
  },
  {
    label: '在售商品',
    value: overview.value.totalProducts || 0,
    icon: Goods,
    grad: 'linear-gradient(135deg, #a8c898 0%, #8aad7a 50%, #6a8d5a 100%)'
  }
])

let chartInstances = []

const statusMap = {
  0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消'
}

function formatMoney(val) {
  if (!val) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/* ================ 图表 ================ */
function initSalesTrendChart(data) {
  const chart = echarts.init(salesTrendRef.value)
  chartInstances.push(chart)
  const dates = data.map(d => d.date)
  const amounts = data.map(d => Number(d.amount))
  const counts = data.map(d => d.orderCount)

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26,26,30,0.96)',
      borderColor: 'rgba(212,175,55,0.3)',
      textStyle: { color: '#e8e8ec' }
    },
    legend: {
      show: false
    },
    grid: { left: '2%', right: '3%', bottom: '3%', top: 36, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLabel: { color: '#8f8fa0', fontSize: 12 },
      axisLine: { lineStyle: { color: '#2d2d36' } },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额',
        axisLabel: { formatter: '{value}', color: '#8f8fa0', fontSize: 12 },
        nameTextStyle: { color: '#8f8fa0', fontSize: 12, padding: [0, 0, 0, -8] },
        splitLine: { lineStyle: { color: '#2d2d36', type: 'dashed' } },
        axisLine: { show: false },
        axisTick: { show: false }
      },
      {
        type: 'value',
        name: '订单量',
        position: 'right',
        axisLabel: { color: '#8f8fa0', fontSize: 12 },
        nameTextStyle: { color: '#8f8fa0', fontSize: 12 },
        splitLine: { show: false },
        axisLine: { show: false },
        axisTick: { show: false }
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 3, color: '#d4af37' },
        itemStyle: { color: '#d4af37', borderColor: '#1c1c22', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(212,175,55,0.45)' },
            { offset: 1, color: 'rgba(212,175,55,0.02)' }
          ])
        },
        data: amounts
      },
      {
        name: '订单量',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 16,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#7c8db5' },
            { offset: 1, color: '#5a6a8a' }
          ])
        },
        data: counts
      }
    ]
  })
}

function initOrderStatusChart(data) {
  const chart = echarts.init(orderStatusRef.value)
  chartInstances.push(chart)
  const pieData = data.map(d => ({
    name: d.name || statusMap[d.status] || '未知',
    value: d.value
  }))
  const empty = pieData.length === 0 || pieData.every(x => !x.value)

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
      backgroundColor: 'rgba(26,26,30,0.96)',
      borderColor: 'rgba(212,175,55,0.3)',
      textStyle: { color: '#e8e8ec' }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 14,
      textStyle: { fontSize: 12, color: '#a0a0ae' }
    },
    graphic: empty ? [{
      type: 'text',
      left: 'center', top: 'middle',
      style: { text: '暂无数据', fill: '#6a6a74', fontSize: 14 }
    }] : [],
    series: [{
      name: '订单状态',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['68%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#1c1c22',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 13, fontWeight: '700', color: '#e8e8ec' },
        scale: true,
        scaleSize: 8
      },
      labelLine: { show: false },
      data: pieData
    }],
    color: ['#d4af37', '#7c8db5', '#8aad7a', '#b685c9', '#d18a7c']
  })
}

function initTopProductsChart(data) {
  const chart = echarts.init(topProductsRef.value)
  chartInstances.push(chart)
  const sorted = [...data].reverse()
  const names = sorted.map(d => d.name?.length > 10 ? d.name.substring(0, 10) + '…' : d.name)
  const sales = sorted.map(d => d.sales)

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(26,26,30,0.96)',
      borderColor: 'rgba(212,175,55,0.3)',
      textStyle: { color: '#e8e8ec' }
    },
    grid: { left: '3%', right: '10%', bottom: '3%', top: 20, containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#8f8fa0', fontSize: 11 },
      splitLine: { lineStyle: { color: '#2d2d36', type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { fontSize: 11, color: '#a0a0ae' },
      axisLine: { lineStyle: { color: '#2d2d36' } },
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      barWidth: 14,
      data: sales,
      itemStyle: {
        borderRadius: [0, 7, 7, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#a0b0d4' },
          { offset: 1, color: '#5a6a8a' }
        ])
      },
      label: { show: true, position: 'right', formatter: '{c}件', color: '#c8c8d0', fontSize: 11 }
    }]
  })
}

function handleResize() {
  chartInstances.forEach(c => c && c.resize())
}

async function loadData() {
  const results = await Promise.allSettled([
    getOverview(),
    getSalesTrend(),
    getOrderStatusDist(),
    getTopSalesProducts(10)
  ])
  const [overviewRes, trendRes, statusRes, topRes] = results

  if (overviewRes.status === 'fulfilled') {
    overview.value = overviewRes.value.data || {}
  } else {
    console.error('[看板] 概览数据失败:', overviewRes.reason)
  }

  chartInstances.forEach(c => c && c.dispose())
  chartInstances = []
  await nextTick()

  if (trendRes.status === 'fulfilled') {
    initSalesTrendChart(trendRes.value.data || [])
  } else {
    console.error('[看板] 销售趋势失败:', trendRes.reason)
  }
  if (statusRes.status === 'fulfilled') {
    initOrderStatusChart(statusRes.value.data || [])
  } else {
    console.error('[看板] 订单状态失败:', statusRes.reason)
  }
if (topRes.status === 'fulfilled') {
    initTopProductsChart(topRes.value.data || [])
  } else {
    console.error('[看板] 热销榜失败:', topRes.reason)
  }
}

async function handleRefresh() {
  ElMessage.success('正在刷新数据…')
  await loadData()
  ElMessage.success('数据已更新')
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(c => c && c.dispose())
  chartInstances = []
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

/* 页头 */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
}
.admin-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-emphasis);
  margin: 0 0 4px;
  letter-spacing: -0.01em;
}
.admin-subtitle {
  font-size: var(--fs-sm);
  color: var(--text-muted);
}
.header-actions { display: flex; gap: var(--space-sm); }

/* 统计卡 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}
.stat-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  position: relative;
  overflow: hidden;
  transition: var(--transition-slow);
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
  box-shadow: var(--shadow-sm);
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg), 0 22px 50px -24px var(--gold-glow);
  border-color: var(--gold-tint-2);
}
.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 0% 0%, rgba(212,175,55,0.06), transparent 60%);
  pointer-events: none;
}
.stat-card::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--gold-300), transparent);
  opacity: 0;
  transition: opacity var(--dur-normal);
}
.stat-card:hover::after { opacity: 0.7; }

.stat-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-md);
  z-index: 1;
}
.icon-box {
  width: 56px;
  height: 56px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 10px 24px -8px rgba(0,0,0,0.4);
  flex-shrink: 0;
}
.stat-meta { flex: 1; min-width: 0; }
.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-emphasis);
  line-height: 1.2;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: var(--fs-sm);
  color: var(--text-muted);
  margin-top: 4px;
  letter-spacing: 0.04em;
}
.deco {
  position: absolute;
  right: -10px;
  bottom: -18px;
  color: rgba(255,255,255,0.03);
  pointer-events: none;
  z-index: 0;
}

/* 图表 */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-lg);
}
.chart-wide { grid-column: span 2; }
.chart-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp var(--dur-slow) var(--ease-out-quart) both;
  transition: var(--transition-normal);
}
.chart-card:hover {
  border-color: var(--gold-tint-2);
  box-shadow: var(--shadow-md);
}

.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}
.chart-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: var(--fs-md);
  font-weight: 700;
  color: var(--text-emphasis);
  margin: 0;
}
.chart-title span {
  width: 3px;
  height: 16px;
  background: linear-gradient(180deg, var(--gold-200), var(--gold-500));
  border-radius: 2px;
}
.chart-tools {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}
.legend-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fs-xs);
  color: var(--text-secondary);
  padding: 3px 8px 3px 4px;
  border-radius: var(--radius-pill);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
}
.legend-chip::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 2px;
}
.chip-gold::before { background: linear-gradient(135deg, var(--gold-300), var(--gold-500)); }
.chip-indigo::before { background: linear-gradient(135deg, #7c8db5, #5a6a8a); }

.chart-box { width: 100%; }

@media (max-width: 1200px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}
@media (max-width: 560px) {
  .stat-grid { grid-template-columns: 1fr; }
  .admin-header { flex-direction: column; align-items: flex-start; }
}
</style>
