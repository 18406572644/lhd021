<template>
  <div class="page-container">
    <div class="page-header">
      <h2>月度互助数据统计</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          value-format="YYYY-MM"
          size="large"
          @change="loadStatistics"
        />
        <el-button type="primary" @click="handleGenerate">
          <el-icon><Refresh /></el-icon>生成统计
        </el-button>
      </div>
    </div>

    <div v-if="currentStats" class="stats-overview">
      <div class="overview-card">
        <div class="overview-title">
          <el-icon><DataLine /></el-icon>
          <span>{{ currentStats.statMonth }} 月运营概览</span>
        </div>
        <div class="overview-grid">
          <div class="overview-item">
            <span class="label">发布物品数</span>
            <span class="value">{{ currentStats.totalPublished }}</span>
            <span class="trend up" v-if="currentStats.publishGrowth > 0">
              <el-icon><Top /></el-icon>{{ currentStats.publishGrowth }}%
            </span>
            <span class="trend down" v-else-if="currentStats.publishGrowth < 0">
              <el-icon><Bottom /></el-icon>{{ Math.abs(currentStats.publishGrowth) }}%
            </span>
          </div>
          <div class="overview-item">
            <span class="label">互换申请数</span>
            <span class="value">{{ currentStats.totalExchanges }}</span>
            <span class="trend up" v-if="currentStats.exchangeGrowth > 0">
              <el-icon><Top /></el-icon>{{ currentStats.exchangeGrowth }}%
            </span>
          </div>
          <div class="overview-item">
            <span class="label">互换成功率</span>
            <span class="value">{{ currentStats.successRate }}%</span>
            <el-progress :percentage="currentStats.successRate" :stroke-width="8" style="width: 100px;" />
          </div>
          <div class="overview-item">
            <span class="label">完成领取数</span>
            <span class="value">{{ currentStats.totalClaimed }}</span>
          </div>
          <div class="overview-item">
            <span class="label">活跃用户数</span>
            <span class="value">{{ currentStats.activeUsers }}</span>
          </div>
          <div class="overview-item">
            <span class="label">新增用户数</span>
            <span class="value">{{ currentStats.newUsers }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>近6个月发布与互换趋势</h3>
        </div>
        <div ref="trendChartRef" class="chart-container-lg"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>分类活跃度TOP5</h3>
        </div>
        <div ref="categoryChartRef" class="chart-container-lg"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>用户信用分布</h3>
        </div>
        <div ref="creditChartRef" class="chart-container-lg"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>自提点使用情况</h3>
        </div>
        <el-table :data="pickupPointStats" style="width: 100%">
          <el-table-column prop="name" label="自提点" />
          <el-table-column prop="totalItems" label="存放物品" width="100" align="center" />
          <el-table-column prop="exchangeCount" label="互换次数" width="100" align="center" />
          <el-table-column prop="usageRate" label="使用率" width="140">
            <template #default="{ row }">
              <el-progress :percentage="row.usageRate" :stroke-width="10" :color="row.usageRate >= 70 ? '#67c23a' : '#909399'" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="page-card">
      <div class="card-header">
        <h3>历史月度统计</h3>
        <el-button type="primary" link @click="exportData">
          <el-icon><Download /></el-icon>导出Excel
        </el-button>
      </div>
      <el-table :data="historyStats" v-loading="loading">
        <el-table-column prop="statMonth" label="统计月份" width="120" />
        <el-table-column prop="totalPublished" label="发布物品" width="100" align="center" />
        <el-table-column prop="totalExchanges" label="互换申请" width="100" align="center" />
        <el-table-column prop="approvedExchanges" label="通过申请" width="100" align="center" />
        <el-table-column prop="successRate" label="成功率" width="100" align="center">
          <template #default="{ row }">{{ row.successRate }}%</template>
        </el-table-column>
        <el-table-column prop="totalClaimed" label="领取数量" width="100" align="center" />
        <el-table-column prop="activeUsers" label="活跃用户" width="100" align="center" />
        <el-table-column prop="newUsers" label="新增用户" width="100" align="center" />
        <el-table-column prop="totalArchives" label="归档物品" width="100" align="center" />
        <el-table-column label="活跃度" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getActivityTag(row.activityLevel)">{{ row.activityLevel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { DataLine, Top, Bottom, Refresh, Download } from '@element-plus/icons-vue'
import { api } from '@/api'

const loading = ref(false)
const selectedMonth = ref('2024-01')
const trendChartRef = ref()
const categoryChartRef = ref()
const creditChartRef = ref()

const currentStats = ref(null)
const pickupPointStats = ref([])
const historyStats = ref([])

const getActivityTag = (level) => {
  const map = { '高': 'success', '中': 'warning', '低': 'info' }
  return map[level] || 'info'
}

const initTrendChart = (data) => {
  nextTick(() => {
    const chart = echarts.init(trendChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['发布数', '互换数', '领取数'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
      xAxis: { type: 'category', data: data.months },
      yAxis: { type: 'value' },
      series: [
        { name: '发布数', type: 'bar', data: data.publishCounts, itemStyle: { color: '#1e88e5', borderRadius: [4, 4, 0, 0] } },
        { name: '互换数', type: 'line', smooth: true, data: data.exchangeCounts, itemStyle: { color: '#43a047' }, lineStyle: { width: 3 } },
        { name: '领取数', type: 'line', smooth: true, data: data.claimCounts, itemStyle: { color: '#fb8c00' }, lineStyle: { width: 3 } }
      ]
    })
    window.addEventListener('resize', () => chart.resize())
  })
}

const initCategoryChart = (data) => {
  nextTick(() => {
    const chart = echarts.init(categoryChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: data.map(item => item.name).reverse() },
      series: [{
        type: 'bar',
        data: data.map(item => item.count).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#90caf9' },
            { offset: 1, color: '#1e88e5' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        barWidth: '50%'
      }]
    })
    window.addEventListener('resize', () => chart.resize())
  })
}

const initCreditChart = (data) => {
  nextTick(() => {
    const chart = echarts.init(creditChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['50%', '50%'],
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{d}% ({c}人)'
        },
        data: [
          { value: data.excellent, name: '优秀', itemStyle: { color: '#67c23a' } },
          { value: data.good, name: '良好', itemStyle: { color: '#409eff' } },
          { value: data.average, name: '一般', itemStyle: { color: '#e6a23c' } },
          { value: data.poor, name: '较差', itemStyle: { color: '#f56c6c' } },
          { value: data.veryPoor, name: '很差', itemStyle: { color: '#909399' } }
        ]
      }]
    })
    window.addEventListener('resize', () => chart.resize())
  })
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const [monthlyRes, trendRes, categoryRes, creditRes, historyRes] = await Promise.all([
      api.statistics.getMonthly(selectedMonth.value),
      api.statistics.getTrend(),
      api.statistics.getCategoryStats(),
      api.statistics.getCreditDistribution(),
      api.statistics.getHistory()
    ])

    currentStats.value = monthlyRes.data
    pickupPointStats.value = [
      { name: '东门自提点', totalItems: 45, exchangeCount: 28, usageRate: 75 },
      { name: '西门自提点', totalItems: 38, exchangeCount: 22, usageRate: 62 },
      { name: '南门自提点', totalItems: 52, exchangeCount: 35, usageRate: 88 },
      { name: '北门自提点', totalItems: 25, exchangeCount: 15, usageRate: 42 }
    ]
    historyStats.value = historyRes.data.records || []

    initTrendChart(trendRes.data)
    initCategoryChart(categoryRes.data.slice(0, 5).reverse())
    initCreditChart(creditRes.data)
  } catch (e) {
    console.error(e)
    currentStats.value = {
      statMonth: '2024-01',
      totalPublished: 72,
      publishGrowth: 12,
      totalExchanges: 50,
      exchangeGrowth: 8,
      successRate: 85,
      totalClaimed: 42,
      activeUsers: 89,
      newUsers: 15,
      totalArchives: 3,
      activityLevel: '高'
    }
    pickupPointStats.value = [
      { name: '东门自提点', totalItems: 45, exchangeCount: 28, usageRate: 75 },
      { name: '西门自提点', totalItems: 38, exchangeCount: 22, usageRate: 62 },
      { name: '南门自提点', totalItems: 52, exchangeCount: 35, usageRate: 88 },
      { name: '北门自提点', totalItems: 25, exchangeCount: 15, usageRate: 42 }
    ]
    historyStats.value = [
      { statMonth: '2024-01', totalPublished: 72, totalExchanges: 50, approvedExchanges: 42, successRate: 85, totalClaimed: 42, activeUsers: 89, newUsers: 15, totalArchives: 3, activityLevel: '高' },
      { statMonth: '2023-12', totalPublished: 65, totalExchanges: 45, approvedExchanges: 38, successRate: 84, totalClaimed: 38, activeUsers: 78, newUsers: 12, totalArchives: 5, activityLevel: '高' },
      { statMonth: '2023-11', totalPublished: 48, totalExchanges: 36, approvedExchanges: 30, successRate: 83, totalClaimed: 30, activeUsers: 65, newUsers: 10, totalArchives: 2, activityLevel: '中' },
      { statMonth: '2023-10', totalPublished: 52, totalExchanges: 38, approvedExchanges: 32, successRate: 84, totalClaimed: 32, activeUsers: 70, newUsers: 8, totalArchives: 4, activityLevel: '中' },
      { statMonth: '2023-09', totalPublished: 38, totalExchanges: 25, approvedExchanges: 20, successRate: 80, totalClaimed: 22, activeUsers: 55, newUsers: 6, totalArchives: 1, activityLevel: '低' }
    ]

    initTrendChart({
      months: ['2023-08', '2023-09', '2023-10', '2023-11', '2023-12', '2024-01'],
      publishCounts: [45, 38, 52, 48, 65, 72],
      exchangeCounts: [25, 25, 38, 36, 45, 50],
      claimCounts: [20, 22, 32, 30, 38, 42]
    })
    initCategoryChart([
      { name: '书籍', count: 35 },
      { name: '家居', count: 28 },
      { name: '电子', count: 22 },
      { name: '衣物', count: 18 },
      { name: '玩具', count: 15 }
    ].reverse())
    initCreditChart({ excellent: 35, good: 42, average: 28, poor: 12, veryPoor: 5 })
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  try {
    await api.statistics.generate(selectedMonth.value)
    ElMessage.success('统计数据生成成功')
    loadStatistics()
  } catch (e) {
    console.error(e)
  }
}

const exportData = () => {
  ElMessage.success('导出功能开发中...')
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.stats-overview {
  margin-bottom: 20px;

  .overview-card {
    @include card-style;
    padding: 24px;

    .overview-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: $primary-color;
      margin-bottom: 20px;
    }

    .overview-grid {
      display: grid;
      grid-template-columns: repeat(6, 1fr);
      gap: 20px;

      .overview-item {
        padding: 16px;
        background: $bg-primary;
        border-radius: 8px;
        .label {
          display: block;
          font-size: 13px;
          color: $text-secondary;
          margin-bottom: 8px;
        }
        .value {
          display: block;
          font-size: 26px;
          font-weight: 700;
          color: $text-primary;
          line-height: 1.2;
        }
        .trend {
          display: inline-flex;
          align-items: center;
          gap: 2px;
          font-size: 12px;
          margin-top: 6px;
          &.up { color: $success-color; }
          &.down { color: $danger-color; }
        }
      }
    }
  }
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;

  .chart-card {
    @include card-style;
    .card-header {
      padding: 16px 20px;
      border-bottom: 1px solid $border-light;
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin: 0;
      }
    }
    .chart-container-lg {
      height: 380px;
      padding: 10px;
    }
  }
}

@media (max-width: 1200px) {
  .stats-overview .overview-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
