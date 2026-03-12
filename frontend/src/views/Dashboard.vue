<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-card-link" @click="goToStudents">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStats.studentCount }}</div>
              <div class="stat-label">在校学生 <el-icon :size="12" style="vertical-align: middle;"><Right /></el-icon></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);">
              <el-icon :size="32"><School /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStats.classCount }}</div>
              <div class="stat-label">班级数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #faad14 0%, #d48806 100%);">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStats.avgScore }}</div>
              <div class="stat-label">年级平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-card-link" @click="goToExcellentStudents">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);">
              <el-icon :size="32"><Trophy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStats.excellentCount }}</div>
              <div class="stat-label">优秀学生 <el-icon :size="12" style="vertical-align: middle;"><Right /></el-icon></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" v-loading="loading">
      <!-- 左侧：班级排名 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="module-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Histogram /></el-icon>
                班级排名
              </span>
              <el-tag type="info" size="small">最近一次考试</el-tag>
            </div>
          </template>
          <el-table :data="classRankings" stripe style="width: 100%" @row-click="handleClassClick">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="danger" size="large">🥇</el-tag>
                <el-tag v-else-if="$index === 1" type="warning" size="large">🥈</el-tag>
                <el-tag v-else-if="$index === 2" type="success" size="large">🥉</el-tag>
                <span v-else style="font-weight: 600;">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="primary" size="large">{{ row.className }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="classType" label="科类" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.classType === '理科' ? 'warning' : 'info'" size="small">
                  {{ row.classType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avgScore" label="平均分" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 700; font-size: 16px; color: #e74c3c;">
                  {{ row.avgScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="studentCount" label="人数" width="80" align="center" />
            <el-table-column label="操作" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click.stop="viewClassStudents(row)">
                  查看学生
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：三好学生 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="module-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Medal /></el-icon>
                三好学生
              </span>
              <el-tag type="danger" size="small">年级前10名</el-tag>
            </div>
          </template>
          <el-table :data="topStudents" stripe style="width: 100%">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="danger" size="large">🥇</el-tag>
                <el-tag v-else-if="$index === 1" type="warning" size="large">🥈</el-tag>
                <el-tag v-else-if="$index === 2" type="success" size="large">🥉</el-tag>
                <span v-else style="font-weight: 600;">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="studentName" label="姓名" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 600; color: #409eff;">{{ row.studentName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.className }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 700; font-size: 16px; color: #67c23a;">
                  {{ row.totalScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="{ row }">
                <el-button type="success" size="small" link @click="viewStudentProfile(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 单科成绩排名 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="module-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><DataAnalysis /></el-icon>
                单科成绩排名 - {{ getSubjectName(selectedSubject) }}
              </span>
              <el-select v-model="selectedSubject" size="small" style="width: 120px;" @change="loadSubjectRankings">
                <el-option label="语文" value="chinese" />
                <el-option label="数学" value="math" />
                <el-option label="英语" value="english" />
                <el-option label="文综/理综" value="comprehensive" />
              </el-select>
            </div>
          </template>
          <el-table :data="subjectRankings" stripe style="width: 100%">
            <el-table-column type="index" label="排名" width="60" align="center" />
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
            <el-table-column label="科目" width="100" align="center">
              <template #default>
                <el-tag type="warning" size="small">{{ getSubjectName(selectedSubject) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="分数" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 700; font-size: 16px; color: #409eff;">
                  {{ row.score }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewStudentDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 考勤预警 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="module-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Warning /></el-icon>
                考勤预警
              </span>
              <el-tag type="warning" size="small">最近7天</el-tag>
            </div>
          </template>
          <el-table :data="attendanceWarnings" stripe style="width: 100%">
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
            <el-table-column prop="abnormalCount" label="异常次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.abnormalCount >= 3 ? 'danger' : 'warning'" size="large">
                  {{ row.abnormalCount }} 次
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="latestStatus" label="最近状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.latestStatus)" size="small">
                  {{ row.latestStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="{ row }">
                <el-button type="warning" size="small" link @click="viewAttendance(row)">
                  查看考勤
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const loading = ref(false)

const router = useRouter()

const totalStats = reactive({
  studentCount: 0,
  classCount: 6,
  avgScore: 0,
  excellentCount: 0
})

const classRankings = ref([])
const topStudents = ref([])
const subjectRankings = ref([])
const attendanceWarnings = ref([])
const selectedSubject = ref('chinese')

const loadDashboardData = async () => {
  loading.value = true
  try {
    // 加载统计数据
    const statsRes = await axios.get('/api/dashboard/stats')
    Object.assign(totalStats, statsRes.data.data)

    // 加载班级排名
    const classRes = await axios.get('/api/dashboard/class-rankings')
    classRankings.value = classRes.data.data

    // 加载三好学生
    const topRes = await axios.get('/api/dashboard/top-students')
    topStudents.value = topRes.data.data

    // 加载考勤预警
    const attendanceRes = await axios.get('/api/dashboard/attendance-warnings')
    attendanceWarnings.value = attendanceRes.data.data

    // 加载单科排名
    loadSubjectRankings()
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadSubjectRankings = async () => {
  try {
    const res = await axios.get(`/api/dashboard/subject-rankings?subject=${selectedSubject.value}`)
    subjectRankings.value = res.data.data
  } catch (error) {
    ElMessage.error('加载单科排名失败')
  }
}

const goToStudents = () => {
  router.push('/student')
}

const goToExcellentStudents = () => {
  router.push('/exam?minScore=600')
}

const viewClassStudents = (row) => {
  router.push(`/student?className=${row.className}`)
}

const viewStudentDetail = (row) => {
  router.push(`/exam?studentName=${row.studentName}`)
}

const viewStudentProfile = (row) => {
  if (row.studentId || row.id) {
    router.push(`/student-profile/${row.studentId || row.id}`)
  } else {
    router.push(`/exam?studentName=${row.studentName}`)
  }
}

const viewAttendance = (row) => {
  router.push(`/attendance?studentName=${row.studentName}`)
}

const handleClassClick = (row) => {
  viewClassStudents(row)
}

const getStatusType = (status) => {
  const typeMap = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '请假': 'info',
    '旷课': 'danger'
  }
  return typeMap[status] || 'info'
}

const getSubjectName = (subject) => {
  const nameMap = {
    'chinese': '语文',
    'math': '数学',
    'english': '英语',
    'comprehensive': '文综/理综'
  }
  return nameMap[subject] || subject
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
  border: none;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.stat-card-link:hover {
  box-shadow: 0 12px 24px rgba(24, 144, 255, 0.18);
}

.stat-card-link:active {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 400;
}

.module-card {
  border-radius: 8px;
  min-height: 420px;
  border: none;
  box-shadow: var(--shadow-card);
  transition: all 0.3s;
}

.module-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
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
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table__header th) {
  background: var(--bg-table-header) !important;
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-table__row) {
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.el-table__row:hover) {
  background-color: var(--bg-hover) !important;
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-lighter);
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 优化标签样式 */
:deep(.el-tag) {
  border: none;
  font-weight: 500;
}

/* 优化按钮样式 */
:deep(.el-button--primary.is-link) {
  color: #1890ff;
}

:deep(.el-button--success.is-link) {
  color: #52c41a;
}

:deep(.el-button--warning.is-link) {
  color: #faad14;
}
</style>

