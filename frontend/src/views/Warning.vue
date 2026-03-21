<template>
  <div class="warning-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Warning /></el-icon>
            预警系统
          </span>
          <div style="display:flex;align-items:center;gap:10px;">
            <el-tag type="danger" size="large">实时监控</el-tag>
            <el-button v-if="handledCount > 0" size="small" plain type="info" @click="clearAllHandled">清除已处理 ({{ handledCount }})</el-button>
          </div>
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
              <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);">
                <el-icon :size="28"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ handledCount }}</div>
                <div class="stat-label">已处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 预警列表 -->
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="全部预警" name="all">
          <el-table :data="allWarnings" stripe style="width: 100%">
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
            <el-table-column label="处理状态" width="110" align="center">
              <template #default="{ row }">
                <div v-if="isHandled(row)" class="handled-status">
                  <el-tag type="success" size="small" effect="dark">✓ 已处理</el-tag>
                  <div class="handled-time">{{ getHandledTime(row) }}</div>
                </div>
                <el-tag v-else type="danger" size="small" effect="plain">待处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">查看详情</el-button>
                <el-button type="warning" size="small" link @click="openAiReport(row)">🤖 AI诊断</el-button>
                <el-button v-if="!isHandled(row)" type="success" size="small" link @click="handleWarning(row)">处理</el-button>
                <el-button v-else type="info" size="small" link @click="undoHandled(row)">撤销</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadWarnings"
              @current-change="loadWarnings"
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
            <el-table-column label="处理状态" width="110" align="center">
              <template #default="{ row }">
                <div v-if="isHandled(row)" class="handled-status">
                  <el-tag type="success" size="small" effect="dark">✓ 已处理</el-tag>
                  <div class="handled-time">{{ getHandledTime(row) }}</div>
                </div>
                <el-tag v-else type="danger" size="small" effect="plain">待处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">查看详情</el-button>
                <el-button v-if="!isHandled(row)" type="success" size="small" link @click="handleWarning(row)">处理</el-button>
                <el-button v-else type="info" size="small" link @click="undoHandled(row)">撤销</el-button>
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
            <el-table-column label="处理状态" width="110" align="center">
              <template #default="{ row }">
                <div v-if="isHandled(row)" class="handled-status">
                  <el-tag type="success" size="small" effect="dark">✓ 已处理</el-tag>
                  <div class="handled-time">{{ getHandledTime(row) }}</div>
                </div>
                <el-tag v-else type="danger" size="small" effect="plain">待处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">查看详情</el-button>
                <el-button v-if="!isHandled(row)" type="success" size="small" link @click="handleWarning(row)">处理</el-button>
                <el-button v-else type="info" size="small" link @click="undoHandled(row)">撤销</el-button>
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
            <el-table-column label="处理状态" width="110" align="center">
              <template #default="{ row }">
                <div v-if="isHandled(row)" class="handled-status">
                  <el-tag type="success" size="small" effect="dark">✓ 已处理</el-tag>
                  <div class="handled-time">{{ getHandledTime(row) }}</div>
                </div>
                <el-tag v-else type="danger" size="small" effect="plain">待处理</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewDetail(row)">查看详情</el-button>
                <el-button v-if="!isHandled(row)" type="success" size="small" link @click="handleWarning(row)">处理</el-button>
                <el-button v-else type="info" size="small" link @click="undoHandled(row)">撤销</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    <!-- AI 学情诊断报告弹窗 -->
  <AIReportDialog
    v-model="reportDialogVisible"
    :student-id="currentStudentId"
    :student-name="currentStudentName"
  />
</div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Calendar, CircleCheck, TrendCharts, Warning} from '@element-plus/icons-vue'
import {getWarningList} from '@/api/warning'
import AIReportDialog from '@/components/AIReportDialog.vue'

const router = useRouter()
const activeTab = ref('all')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
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

// ── 处理状态持久化（localStorage）──
const STORAGE_KEY = 'warning_handled_map'
const handledMap = ref({})
const loadHandledMap = () => {
  try { handledMap.value = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') } catch { handledMap.value = {} }
}
const saveHandledMap = () => localStorage.setItem(STORAGE_KEY, JSON.stringify(handledMap.value))
const warningKey = (row) => `${row.studentId}_${row.warningType}_${row.className}`
const isHandled = (row) => !!handledMap.value[warningKey(row)]
const getHandledTime = (row) => handledMap.value[warningKey(row)]?.time || ''
const handledCount = computed(() => Object.keys(handledMap.value).length)

const handleWarning = (row) => {
  handledMap.value = { ...handledMap.value, [warningKey(row)]: { time: new Date().toLocaleString('zh-CN', { hour12: false }).slice(0, 16) } }
  saveHandledMap()
  ElMessage.success(`已将 ${row.studentName} 的「${row.warningType}」预警标记为已处理`)
}
const undoHandled = (row) => {
  const m = { ...handledMap.value }; delete m[warningKey(row)]; handledMap.value = m
  saveHandledMap(); ElMessage.info(`已撤销 ${row.studentName} 的处理状态`)
}
const clearAllHandled = async () => {
  try {
    await (await import('element-plus')).ElMessageBox.confirm(`确定清除全部 ${handledCount.value} 条已处理记录？`, '清除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    handledMap.value = {}; saveHandledMap(); ElMessage.success('已清除全部处理记录')
  } catch {}
}
const rowClassName = ({ row }) => isHandled(row) ? 'handled-row' : ''
// ───────────────────────────────────────────────────────────

const loadWarnings = async () => {
  try {
    // 加载当前页数据
    const res = await getWarningList({ pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    allWarnings.value = res.data.records
    pagination.total = res.data.total
    warningStats.totalWarnings = res.data.total
    // 加载全量数据用于统计各类型数量
    const allRes = await getWarningList({ pageNum: 1, pageSize: 9999 })
    const allRecords = allRes.data.records || []
    warningStats.scoreDecline = allRecords.filter(w => w.warningType === '成绩下降' || w.warningType === '单科下降').length
    warningStats.attendanceIssue = allRecords.filter(w => w.warningType === '考勤异常').length
    warningStats.lowScore = allRecords.filter(w => w.warningType === '成绩偏低').length
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



// AI 诊断报告
const reportDialogVisible = ref(false)
const currentStudentId = ref(null)
const currentStudentName = ref('')

const openAiReport = (row) => {
  if (!row.studentId) {
    ElMessage.warning('该预警记录缺少学生ID，无法生成报告')
    return
  }
  currentStudentId.value = row.studentId
  currentStudentName.value = row.studentName
  reportDialogVisible.value = true
}

onMounted(() => {
  loadHandledMap()
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

/* 已处理行样式 */
:deep(.handled-row td) { opacity: 0.55; }
.handled-status { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.handled-time { font-size: 10px; color: #aaa; line-height: 1.2; }
</style>

