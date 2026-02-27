<template>
  <div class="analytics-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><DataAnalysis /></el-icon>
            数据分析与可视化
          </span>
          <el-select v-model="selectedExam" size="default" style="width: 200px;" @change="loadAnalytics">
            <el-option 
              v-for="exam in examList" 
              :key="exam" 
              :label="exam" 
              :value="exam" 
            />
          </el-select>
        </div>
      </template>

      <!-- 分析模块 -->
      <el-row :gutter="20">
        <!-- 班级平均分趋势 -->
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <span class="chart-title">📈 班级平均分趋势</span>
            </template>
            <div ref="trendChart" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>

        <!-- 各科成绩分布 -->
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <span class="chart-title">📊 各科成绩分布</span>
            </template>
            <div ref="distributionChart" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>

        <!-- 学生排名变化曲线 -->
        <el-col :span="12" style="margin-top: 20px;">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span class="chart-title">📉 学生排名变化曲线</span>
                <el-select v-model="selectedStudent" size="small" style="width: 150px;" @change="loadRankingTrend">
                  <el-option 
                    v-for="student in studentList" 
                    :key="student" 
                    :label="student" 
                    :value="student" 
                  />
                </el-select>
              </div>
            </template>
            <div ref="rankingChart" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>

        <!-- 风险学生名单 -->
        <el-col :span="12" style="margin-top: 20px;">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <span class="chart-title">⚠️ 风险学生名单</span>
            </template>
            <el-table :data="riskStudents" stripe style="width: 100%; height: 350px;" max-height="350">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="studentName" label="姓名" width="100" align="center">
                <template #default="{ row }">
                  <span style="font-weight: 600;">{{ row.studentName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="className" label="班级" width="80" align="center">
                <template #default="{ row }">
                  <el-tag type="success" size="small">{{ row.className }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getRiskLevelType(row.riskLevel)" size="large">
                    {{ row.riskLevel }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="riskReason" label="风险原因" min-width="200" />
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="viewStudentDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()
const selectedExam = ref('第一周周考')
const selectedStudent = ref('')
const examList = ref(['第一周周考', '第二周周考'])
const studentList = ref([])
const riskStudents = ref([])

const trendChart = ref(null)
const distributionChart = ref(null)
const rankingChart = ref(null)

let trendChartInstance = null
let distributionChartInstance = null
let rankingChartInstance = null

// 加载分析数据
const loadAnalytics = async () => {
  try {
    const res = await axios.get('/api/analytics/data', {
      params: { examName: selectedExam.value }
    })
    
    const data = res.data.data
    
    // 渲染图表
    renderTrendChart(data.trendData)
    renderDistributionChart(data.distributionData)
    loadRiskStudents()
    
    // 加载学生列表
    if (data.studentList && data.studentList.length > 0) {
      studentList.value = data.studentList
      selectedStudent.value = data.studentList[0]
      loadRankingTrend()
    }
  } catch (error) {
    ElMessage.error('加载分析数据失败')
  }
}

// 班级平均分趋势图
const renderTrendChart = (data) => {
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#262626' }
    },
    legend: {
      data: data.classes,
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.exams,
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#595959' }
    },
    yAxis: {
      type: 'value',
      name: '平均分',
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#595959' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: data.series.map((item, index) => ({
      name: item.name,
      type: 'line',
      smooth: true,
      data: item.data,
      lineStyle: { width: 3 },
      itemStyle: {
        color: ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1'][index % 5]
      }
    }))
  }
  
  trendChartInstance.setOption(option)
}

// 各科成绩分布图
const renderDistributionChart = (data) => {
  if (!distributionChartInstance) {
    distributionChartInstance = echarts.init(distributionChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#262626' }
    },
    legend: {
      data: ['语文', '数学', '英语', '文综/理综'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.ranges,
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#595959' }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#595959' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [
      {
        name: '语文',
        type: 'bar',
        data: data.chinese,
        itemStyle: { color: '#1890ff' }
      },
      {
        name: '数学',
        type: 'bar',
        data: data.math,
        itemStyle: { color: '#52c41a' }
      },
      {
        name: '英语',
        type: 'bar',
        data: data.english,
        itemStyle: { color: '#faad14' }
      },
      {
        name: '文综/理综',
        type: 'bar',
        data: data.comprehensive,
        itemStyle: { color: '#722ed1' }
      }
    ]
  }
  
  distributionChartInstance.setOption(option)
}

// 学生排名变化曲线
const loadRankingTrend = async () => {
  try {
    const res = await axios.get('/api/analytics/ranking-trend', {
      params: { studentName: selectedStudent.value }
    })
    
    renderRankingChart(res.data.data)
  } catch (error) {
    ElMessage.error('加载排名趋势失败')
  }
}

const renderRankingChart = (data) => {
  if (!rankingChartInstance) {
    rankingChartInstance = echarts.init(rankingChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#262626' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.exams,
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#595959' }
    },
    yAxis: {
      type: 'value',
      name: '排名',
      inverse: true,
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#595959' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [
      {
        name: '班级排名',
        type: 'line',
        smooth: true,
        data: data.classRanking,
        lineStyle: { width: 3, color: '#1890ff' },
        itemStyle: { color: '#1890ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ])
        }
      },
      {
        name: '年级排名',
        type: 'line',
        smooth: true,
        data: data.gradeRanking,
        lineStyle: { width: 3, color: '#52c41a' },
        itemStyle: { color: '#52c41a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ])
        }
      }
    ]
  }
  
  rankingChartInstance.setOption(option)
}

// 加载风险学生
const loadRiskStudents = async () => {
  try {
    const res = await axios.get('/api/analytics/risk-students', {
      params: { examName: selectedExam.value }
    })
    riskStudents.value = res.data.data
  } catch (error) {
    ElMessage.error('加载风险学生失败')
  }
}

const getRiskLevelType = (level) => {
  const typeMap = {
    '高风险': 'danger',
    '中风险': 'warning',
    '低风险': 'info'
  }
  return typeMap[level] || 'info'
}

const viewStudentDetail = (row) => {
  router.push(`/exam?studentName=${row.studentName}`)
}

onMounted(() => {
  nextTick(() => {
    loadAnalytics()
    
    // 响应式调整
    window.addEventListener('resize', () => {
      trendChartInstance?.resize()
      distributionChartInstance?.resize()
      rankingChartInstance?.resize()
    })
  })
})
</script>

<style scoped>
.analytics-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.chart-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

:deep(.el-card) {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-table__header th) {
  background: #fafafa !important;
  color: #262626;
  font-weight: 600;
}
</style>

