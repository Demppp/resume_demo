<template>
  <div class="student-profile-container">
    <el-page-header @back="goBack" style="margin-bottom: 20px;">
      <template #content>
        <span class="page-title">学生个人档案 - {{ student.studentName }}</span>
      </template>
    </el-page-header>

    <div v-loading="loading">
      <!-- 基本信息卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="24" :md="8">
          <el-card shadow="hover" class="info-card">
            <div class="student-avatar">
              <el-avatar :size="80" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); font-size: 32px;">
                {{ student.studentName ? student.studentName[0] : '' }}
              </el-avatar>
              <h2>{{ student.studentName }}</h2>
              <div class="student-tags">
                <el-tag type="primary" size="large">{{ student.className }}</el-tag>
                <el-tag :type="student.classType === '理科' ? 'warning' : 'info'" size="large">{{ student.classType }}</el-tag>
                <el-tag :type="student.gender === '男' ? '' : 'danger'" size="large">{{ student.gender }}</el-tag>
              </div>
            </div>
            <el-descriptions :column="1" border style="margin-top: 16px;">
              <el-descriptions-item label="学号">{{ student.studentNumber }}</el-descriptions-item>
              <el-descriptions-item label="家长电话">{{ student.parentPhone }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ student.address }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 最新成绩 -->
        <el-col :xs="24" :sm="24" :md="16">
          <el-card shadow="hover" class="info-card" v-if="latestScore">
            <template #header>
              <div class="card-header">
                <span class="card-title">📊 最近一次考试 - {{ latestScore.examName }}</span>
                <el-tag type="success">{{ latestScore.examDate }}</el-tag>
              </div>
            </template>
            <el-row :gutter="16">
              <el-col :span="4" v-for="(item, key) in scoreItems" :key="key">
                <div class="score-item">
                  <div class="score-label">{{ item.label }}</div>
                  <div class="score-value" :style="{ color: item.color }">{{ latestScore[item.key] || '-' }}</div>
                </div>
              </el-col>
            </el-row>
            <el-divider />
            <el-row :gutter="16">
              <el-col :span="8">
                <el-statistic title="年级排名" :value="latestScore.gradeRank" prefix="第" suffix="名" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="班级排名" :value="latestScore.classRank" prefix="第" suffix="名" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="预测大学" :value="latestScore.predictedUniversity || '待评估'" />
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>

      <!-- 成绩趋势图 + 考勤统计 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="24" :md="16">
          <el-card shadow="hover" class="info-card">
            <template #header>
              <span class="card-title">📈 成绩趋势</span>
            </template>
            <div ref="scoreChart" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-card shadow="hover" class="info-card">
            <template #header>
              <span class="card-title">📅 考勤统计</span>
            </template>
            <div ref="attendanceChart" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 历次考试成绩表 -->
      <el-card shadow="hover" class="info-card">
        <template #header>
          <span class="card-title">📝 历次考试成绩</span>
        </template>
        <el-table :data="scores" stripe>
          <el-table-column prop="examName" label="考试名称" width="150" />
          <el-table-column prop="examDate" label="考试日期" width="120" />
          <el-table-column prop="chineseScore" label="语文" width="80" align="center" />
          <el-table-column prop="mathScore" label="数学" width="80" align="center" />
          <el-table-column prop="englishScore" label="英语" width="80" align="center" />
          <el-table-column prop="comprehensiveScore" label="文综/理综" width="100" align="center" />
          <el-table-column prop="totalScore" label="总分" width="90" align="center">
            <template #default="{ row }">
              <span style="font-weight: 700; color: var(--color-danger);">{{ row.totalScore }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="classRank" label="班级排名" width="90" align="center" />
          <el-table-column prop="gradeRank" label="年级排名" width="90" align="center" />
          <el-table-column prop="predictedUniversity" label="预测大学" min-width="120" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import {nextTick, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import request from '@/api/request'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const loading = ref(true)

const student = ref({})
const scores = ref([])
const latestScore = ref(null)
const scoreTrend = ref([])
const attendanceStats = ref({})

const scoreChart = ref(null)
const attendanceChart = ref(null)

const scoreItems = [
  { label: '语文', key: 'chineseScore', color: '#409eff' },
  { label: '数学', key: 'mathScore', color: '#67c23a' },
  { label: '英语', key: 'englishScore', color: '#e6a23c' },
  { label: '文综/理综', key: 'comprehensiveScore', color: '#f56c6c' },
  { label: '总分', key: 'totalScore', color: '#303133' },
]

function goBack() {
  router.back()
}

async function loadProfile() {
  loading.value = true
  try {
    const studentId = route.params.id
    const res = await request.get(`/student-profile/${studentId}`)
    const data = res.data

    student.value = data.student || {}
    scores.value = data.scores || []
    latestScore.value = data.latestScore || null
    scoreTrend.value = data.scoreTrend || []
    attendanceStats.value = data.attendanceStats || {}

    await nextTick()
    renderScoreChart()
    renderAttendanceChart()
  } catch (e) {
    ElMessage.error('加载学生档案失败')
  } finally {
    loading.value = false
  }
}

function renderScoreChart() {
  if (!scoreChart.value || scoreTrend.value.length === 0) return
  const chart = echarts.init(scoreChart.value)
  const names = scoreTrend.value.map(s => s.examName)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['总分', '语文', '数学', '英语', '文综/理综'], textStyle: { color: 'var(--text-regular)' } },
    grid: { left: 40, right: 20, bottom: 30, top: 40 },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value', name: '分数' },
    series: [
      { name: '总分', type: 'line', data: scoreTrend.value.map(s => s.totalScore), lineStyle: { width: 3 }, symbolSize: 8 },
      { name: '语文', type: 'line', data: scoreTrend.value.map(s => s.chineseScore) },
      { name: '数学', type: 'line', data: scoreTrend.value.map(s => s.mathScore) },
      { name: '英语', type: 'line', data: scoreTrend.value.map(s => s.englishScore) },
      { name: '文综/理综', type: 'line', data: scoreTrend.value.map(s => s.comprehensiveScore) },
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderAttendanceChart() {
  if (!attendanceChart.value) return
  const chart = echarts.init(attendanceChart.value)
  const stats = attendanceStats.value
  const data = Object.entries(stats).map(([name, value]) => ({ name, value }))

  if (data.length === 0) {
    data.push({ name: '全勤', value: 1 })
  }

  const colorMap = { '正常': '#67c23a', '迟到': '#e6a23c', '早退': '#f56c6c', '请假': '#909399', '旷课': '#f56c6c', '全勤': '#67c23a' }

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: 'var(--bg-card)', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}次' },
      data: data.map(d => ({ ...d, itemStyle: { color: colorMap[d.name] || '#409eff' } }))
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.student-profile-container {
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.info-card {
  border-radius: 8px;
  border: none;
  box-shadow: var(--shadow-card);
}

.student-avatar {
  text-align: center;
  padding: 20px 0;
}

.student-avatar h2 {
  margin-top: 12px;
  font-size: 22px;
  color: var(--text-primary);
}

.student-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.score-item {
  text-align: center;
  padding: 12px 0;
}

.score-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.score-value {
  font-size: 24px;
  font-weight: 700;
}

:deep(.el-table__header th) {
  background: var(--bg-table-header) !important;
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-lighter);
}

:deep(.el-descriptions__label) {
  color: var(--text-secondary);
}
</style>
