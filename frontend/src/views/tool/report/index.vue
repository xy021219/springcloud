<template>
  <div class="app-container">
    <el-row class="mb8">
      <el-col :span="24">
        <span class="report-title">{{ report.title }}</span>
        <span class="report-period">{{ report.period }}</span>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb8">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in report.cards" :key="card.label">
        <el-card shadow="hover" class="report-card">
          <div class="report-label">{{ card.label }}</div>
          <div class="report-value">{{ card.value }}<span>{{ card.unit }}</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14" class="card-box">
        <el-card>
          <template #header><span>{{ text.dailyTitle }}</span></template>
          <div ref="dailyChart" style="height: 320px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10" class="card-box">
        <el-card>
          <template #header><span>{{ text.moduleTitle }}</span></template>
          <div v-for="item in report.modules" :key="item.name" class="module-row">
            <div class="module-name">
              <span>{{ item.name }}</span>
              <el-tag :type="tagType(item.status)" size="small">{{ item.status }}</el-tag>
            </div>
            <el-progress :percentage="item.progress" :status="item.progress === 100 ? 'success' : undefined" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="card-box">
      <template #header><span>{{ text.taskTitle }}</span></template>
      <el-table :data="report.tasks" border>
        <el-table-column :label="text.taskName" prop="name" min-width="220" />
        <el-table-column :label="text.owner" prop="owner" width="120" />
        <el-table-column :label="text.status" prop="status" width="120">
          <template #default="scope">
            <el-tag :type="tagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="text.date" prop="updatedAt" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="AiReport">
import * as echarts from 'echarts'
import { getReportOverview } from '@/api/tool/report'

const text = {
  dailyTitle: '\u6bcf\u65e5\u63d0\u4ea4\u4e0e\u5b8c\u6210\u9700\u6c42',
  moduleTitle: '\u6a21\u5757\u8fdb\u5ea6',
  taskTitle: '\u4e8b\u9879',
  taskName: '\u4e8b\u9879',
  owner: '\u8d1f\u8d23\u4eba',
  status: '\u72b6\u6001',
  date: '\u65e5\u671f',
  done: '\u5df2\u5b8c\u6210',
  doing: '\u8fdb\u884c\u4e2d',
  commits: '\u4ee3\u7801\u63d0\u4ea4',
  features: '\u5b8c\u6210\u9700\u6c42'
}

const report = ref({ cards: [], daily: [], modules: [], tasks: [], title: '', period: '' })
const dailyChart = ref(null)
let chart

function tagType(status) {
  if (status === text.done) return 'success'
  if (status === text.doing) return 'warning'
  return 'info'
}

function renderChart() {
  if (!dailyChart.value) return
  if (!chart) {
    chart = echarts.init(dailyChart.value)
    window.addEventListener('resize', () => chart && chart.resize())
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: [text.commits, text.features] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: report.value.daily.map(item => item.day) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: text.commits, type: 'bar', data: report.value.daily.map(item => item.commits) },
      { name: text.features, type: 'bar', data: report.value.daily.map(item => item.features) }
    ]
  })
}

function load() {
  getReportOverview().then(response => {
    report.value = response.data
    nextTick(() => renderChart())
  })
}

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
})

load()
</script>

<style scoped>
.report-title {
  font-size: 18px;
  font-weight: 600;
}
.report-period {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
}
.report-card {
  margin-bottom: 16px;
}
.report-label {
  color: var(--el-text-color-secondary);
}
.report-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
}
.report-value span {
  margin-left: 4px;
  font-size: 14px;
  font-weight: 400;
}
.module-row {
  margin-bottom: 14px;
}
.module-name {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
</style>
