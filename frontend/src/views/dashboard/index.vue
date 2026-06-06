<template>
  <div class="dashboard-page">
    <div class="stats-cards">
      <div class="stat-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-icon" :style="{ background: stat.color + '15', color: stat.color }">
          <el-icon :size="28"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.title }}</div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            <el-icon><CaretTop v-if="stat.trend > 0" /><CaretBottom v-else /></el-icon>
            {{ Math.abs(stat.trend) }}% 较上月
          </div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>月度互助趋势</h3>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>物品分类分布</h3>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>用户信用分布</h3>
        </div>
        <div ref="creditChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>最近活动</h3>
        </div>
        <el-table :data="recentActivities" style="width: 100%">
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.tagType" size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="time" label="时间" width="160" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import * as echarts from 'echarts'
import { Goods, RefreshRight, User, Location, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { api } from '@/api'

const stats = ref([
  { title: '在线物品', value: 0, icon: markRaw(Goods), color: '#1e88e5', trend: 12 },
  { title: '本月互换', value: 0, icon: markRaw(RefreshRight), color: '#2196f3', trend: 8 },
  { title: '活跃用户', value: 0, icon: markRaw(User), color: '#42a5f5', trend: 15 },
  { title: '自提点', value: 0, icon: markRaw(Location), color: '#64b5f6', trend: 0 }
])

const recentActivities = ref([])
const trendChartRef = ref()
const categoryChartRef = ref()
const creditChartRef = ref()

const initTrendChart = (data) => {
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['发布数', '互换数', '领取数'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: data.months },
    yAxis: { type: 'value' },
    series: [
      { name: '发布数', type: 'line', smooth: true, data: data.publishCounts, itemStyle: { color: '#1e88e5' }, areaStyle: { opacity: 0.1 } },
      { name: '互换数', type: 'line', smooth: true, data: data.exchangeCounts, itemStyle: { color: '#43a047' }, areaStyle: { opacity: 0.1 } },
      { name: '领取数', type: 'line', smooth: true, data: data.claimCounts, itemStyle: { color: '#fb8c00' }, areaStyle: { opacity: 0.1 } }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

const initCategoryChart = (data) => {
  const chart = echarts.init(categoryChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {d}%' },
      data: data.map(item => ({ value: item.count, name: item.name }))
    }],
    color: ['#1e88e5', '#42a5f5', '#90caf9', '#64b5f6', '#1976d2']
  })
  window.addEventListener('resize', () => chart.resize())
}

const initCreditChart = (data) => {
  const chart = echarts.init(creditChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['优秀', '良好', '一般', '较差', '很差'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [data.excellent, data.good, data.average, data.poor, data.veryPoor],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#1e88e5' },
          { offset: 1, color: '#90caf9' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const loadDashboardData = async () => {
  try {
    const [overview, trend, category, credit] = await Promise.all([
      api.statistics.getOverview(),
      api.statistics.getTrend(),
      api.statistics.getCategoryStats(),
      api.statistics.getCreditDistribution()
    ])
    
    stats.value[0].value = overview.data.totalItems
    stats.value[1].value = overview.data.monthlyExchanges
    stats.value[2].value = overview.data.activeUsers
    stats.value[3].value = overview.data.totalPickupPoints

    initTrendChart(trend.data)
    initCategoryChart(category.data)
    initCreditChart(credit.data)

    recentActivities.value = [
      { type: '发布', content: '张三发布了「闲置书籍一批」', time: '10分钟前', tagType: 'primary' },
      { type: '互换', content: '李四与王五完成物品互换', time: '30分钟前', tagType: 'success' },
      { type: '审核', content: '管理员通过了互换申请', time: '1小时前', tagType: 'warning' },
      { type: '领取', content: '赵六领取了「厨房用具」', time: '2小时前', tagType: 'info' },
      { type: '归档', content: '系统自动归档3件闲置物品', time: '3小时前', tagType: 'danger' }
    ]
  } catch (e) {
    console.error(e)
    stats.value[0].value = 128
    stats.value[1].value = 45
    stats.value[2].value = 89
    stats.value[3].value = 4
    
    initTrendChart({
      months: ['1月', '2月', '3月', '4月', '5月', '6月'],
      publishCounts: [45, 52, 38, 65, 48, 72],
      exchangeCounts: [28, 35, 25, 42, 36, 50],
      claimCounts: [20, 28, 22, 35, 30, 42]
    })
    initCategoryChart([
      { name: '书籍', count: 35 },
      { name: '家居', count: 28 },
      { name: '电子', count: 22 },
      { name: '衣物', count: 18 },
      { name: '其他', count: 25 }
    ])
    initCreditChart({ excellent: 35, good: 42, average: 28, poor: 12, veryPoor: 5 })
    
    recentActivities.value = [
      { type: '发布', content: '张三发布了「闲置书籍一批」', time: '10分钟前', tagType: 'primary' },
      { type: '互换', content: '李四与王五完成物品互换', time: '30分钟前', tagType: 'success' },
      { type: '审核', content: '管理员通过了互换申请', time: '1小时前', tagType: 'warning' },
      { type: '领取', content: '赵六领取了「厨房用具」', time: '2小时前', tagType: 'info' },
      { type: '归档', content: '系统自动归档3件闲置物品', time: '3小时前', tagType: 'danger' }
    ]
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: $page-padding;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;

  .stat-card {
    @include card-style;
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 24px;

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: $text-primary;
        line-height: 1.2;
      }
      .stat-label {
        color: $text-secondary;
        font-size: 14px;
        margin-top: 4px;
      }
      .stat-trend {
        font-size: 12px;
        margin-top: 6px;
        display: flex;
        align-items: center;
        gap: 2px;
        &.up { color: $success-color; }
        &.down { color: $danger-color; }
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
    .chart-container {
      height: 320px;
      padding: 10px;
    }
  }
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
