<template>
  <div class="warning-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Warning /></el-icon>
            预警系统
          </span>
          <el-tag type="danger" size="large">实时监控</el-tag>
        </div>
      </template>

      <!-- 预警统计 -->
      <el-row :gutter="20" style="margin-bottom: 24px;">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card danger-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);">
                <el-icon :size="28"><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ warningStats.totalWarnings }}</div>
                <div class="stat-label">预警总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #faad14 0%, #d48806 100%);">
                <el-icon :size="28"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ warningStats.scoreDecline }}</div>
                <div class="stat-label">成绩下降</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #ff7a45 0%, #ff4d4f 100%);">
                <el-icon :size="28"><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ warningStats.attendanceIssue }}</div>
                <div class="stat-label">考勤异常</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);">
                <el-icon :size="28"><DataLine /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ warningStats.lowScore }}</div>
                <div class="stat-label">成绩偏低</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 预警列表 -->
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="全部预警" name="all">
          <el-table :data="paginatedWarnings" stripe style="width: 100%">
            <el-table-column prop="studentName" label="学生姓名" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 600;">{{ row.studentName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.className }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningType" label="预警类型" width="150" align="center">
              <template #default="{ row }">
                <el-tag :type="getWarningTypeColor(row.warningType)" size="large">
                  {{ getWarningTypeIcon(row.warningType) }} {{ row.warningType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningContent" label="预警内容" min-width="300" />
            <el-table-column prop="warningLevel" label="严重程度" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.warningLevel)" size="large">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningTime" label="预警时间" width="180" align="center" />
            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">
                  查看详情
                </el-button>
                <el-button type="success" size="small" link @click="handleWarning(row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="allWarnings.length"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="成绩下降" name="score">
          <el-table :data="scoreWarnings" stripe style="width: 100%">
            <el-table-column prop="studentName" label="学生姓名" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 600;">{{ row.studentName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.className }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningContent" label="预警内容" min-width="300" />
            <el-table-column prop="warningLevel" label="严重程度" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.warningLevel)" size="large">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="考勤异常" name="attendance">
          <el-table :data="attendanceWarnings" stripe style="width: 100%">
            <el-table-column prop="studentName" label="学生姓名" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 600;">{{ row.studentName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.className }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningContent" label="预警内容" min-width="300" />
            <el-table-column prop="warningLevel" label="严重程度" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.warningLevel)" size="large">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="成绩偏低" name="lowscore">
          <el-table :data="lowScoreWarnings" stripe style="width: 100%">
            <el-table-column prop="studentName" label="学生姓名" width="100" align="center">
              <template #default="{ row }">
                <span style="font-weight: 600;">{{ row.studentName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.className }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningContent" label="预警内容" min-width="300" />
            <el-table-column prop="warningLevel" label="严重程度" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.warningLevel)" size="large">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, TrendCharts, Calendar, DataLine } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const activeTab = ref('all')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const warningStats = reactive({
  totalWarnings: 0,
  scoreDecline: 0,
  attendanceIssue: 0,
  lowScore: 0
})

const allWarnings = ref([])
const scoreWarnings = computed(() => allWarnings.value.filter(w => w.warningType === '成绩下降'))
const attendanceWarnings = computed(() => allWarnings.value.filter(w => w.warningType === '考勤异常'))
const lowScoreWarnings = computed(() => allWarnings.value.filter(w => w.warningType === '成绩偏低'))

const paginatedWarnings = computed(() => {
  const start = (pagination.pageNum - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return allWarnings.value.slice(start, end)
})

const loadWarnings = async () => {
  try {
    const res = await axios.get('/api/warning/list')
    allWarnings.value = res.data.data
    
    // 统计各类预警数量
    warningStats.totalWarnings = allWarnings.value.length
    warningStats.scoreDecline = scoreWarnings.value.length
    warningStats.attendanceIssue = attendanceWarnings.value.length
    warningStats.lowScore = lowScoreWarnings.value.length
  } catch (error) {
    ElMessage.error('加载预警数据失败')
  }
}

const getWarningTypeColor = (type) => {
  const colorMap = {
    '成绩下降': 'warning',
    '考勤异常': 'danger',
    '成绩偏低': 'info',
    '单科下降': 'warning'
  }
  return colorMap[type] || 'info'
}

const getWarningTypeIcon = (type) => {
  const iconMap = {
    '成绩下降': '📉',
    '考勤异常': '❌',
    '成绩偏低': '📊',
    '单科下降': '📉'
  }
  return iconMap[type] || '⚠️'
}

const getLevelType = (level) => {
  const typeMap = {
    '严重': 'danger',
    '警告': 'warning',
    '提示': 'info'
  }
  return typeMap[level] || 'info'
}

const viewDetail = (row) => {
  router.push(`/exam?studentName=${row.studentName}`)
}

const handleWarning = (row) => {
  ElMessage.success('预警已标记为已处理')
}

onMounted(() => {
  loadWarnings()
})
</script>

<style scoped>
.warning-container {
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

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.danger-card {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(245, 34, 45, 0.2);
  }
  50% {
    box-shadow: 0 4px 16px rgba(245, 34, 45, 0.4);
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #262626;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

:deep(.el-tabs__item) {
  font-weight: 500;
}
</style>

