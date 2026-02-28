<template>
  <div class="exam-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-label">参考学生</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-clickable" :class="{ 'stat-card-active': activeStatFilter === 'excellent' }" @click="toggleStatFilter('excellent')">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);">
              <el-icon :size="28"><Trophy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.excellentCount }}</div>
              <div class="stat-label">优秀学生(≥600分)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #faad14 0%, #d48806 100%);">
              <el-icon :size="28"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.averageScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-clickable" :class="{ 'stat-card-active': activeStatFilter === 'needAttention' }" @click="toggleStatFilter('needAttention')">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.needAttentionCount }}</div>
              <div class="stat-label">需关注学生(<500分)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">成绩管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            录入成绩
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true">
          <el-form-item label="学生姓名">
            <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable style="width: 150px;" />
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="searchForm.className" placeholder="请选择班级" clearable style="width: 120px;">
              <el-option label="一班" value="一班" />
              <el-option label="二班" value="二班" />
              <el-option label="三班" value="三班" />
              <el-option label="四班" value="四班" />
              <el-option label="五班" value="五班" />
              <el-option label="六班" value="六班" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试名称">
            <el-input v-model="searchForm.examName" placeholder="请输入考试名称" clearable style="width: 150px;" />
          </el-form-item>
          <el-form-item label="重点关注">
            <el-select v-model="focusFilter" placeholder="筛选学生" clearable @change="handleSearch" style="width: 140px;">
              <el-option label="全部学生" value="" />
              <el-option label="年级前10名" value="top10" />
              <el-option label="年级后20名" value="bottom20" />
              <el-option label="班级前3名" value="classTop3" />
              <el-option label="班级后5名" value="classBottom5" />
            </el-select>
          </el-form-item>
          <el-form-item label="分数范围">
            <div style="display: flex; align-items: center; gap: 6px;">
              <el-input-number v-model="searchForm.minScore" :min="0" :max="750" :controls="false" placeholder="最低分" style="width: 100px;" />
              <span style="color: #999;">~</span>
              <el-input-number v-model="searchForm.maxScore" :min="0" :max="750" :controls="false" placeholder="最高分" style="width: 100px;" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table 
        :data="tableData" 
        stripe 
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
        :row-class-name="getRowClassName"
      >
        <el-table-column type="index" label="序号" width="60" align="center" fixed />
        <el-table-column prop="studentName" label="学生姓名" width="100" fixed align="center">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.studentName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.className }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="classType" label="科类" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.classType === '理科' ? 'warning' : 'info'" size="small">
              {{ row.classType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="examName" label="考试名称" width="130" align="center" />
        <el-table-column prop="examDate" label="考试日期" width="110" align="center" />
        <el-table-column prop="chineseScore" label="语文" width="70" align="center" />
        <el-table-column prop="mathScore" label="数学" width="70" align="center" />
        <el-table-column prop="englishScore" label="英语" width="70" align="center" />
        <el-table-column prop="comprehensiveScore" label="文综/理综" width="100" align="center">
          <template #default="{ row }">
            <el-popover
              placement="top"
              :width="200"
              trigger="hover"
            >
              <template #reference>
                <span style="cursor: pointer; color: #409eff;">{{ row.comprehensiveScore }}</span>
              </template>
              <div v-if="row.classType === '理科'" style="line-height: 1.8;">
                <div><strong>理综详情：</strong></div>
                <div>物理：{{ row.physicsScore }}</div>
                <div>化学：{{ row.chemistryScore }}</div>
                <div>生物：{{ row.biologyScore }}</div>
                <div style="border-top: 1px solid #eee; margin-top: 5px; padding-top: 5px;">
                  <strong>总分：{{ row.comprehensiveScore }}</strong>
                </div>
              </div>
              <div v-else style="line-height: 1.8;">
                <div><strong>文综详情：</strong></div>
                <div>政治：{{ row.politicsScore }}</div>
                <div>历史：{{ row.historyScore }}</div>
                <div>地理：{{ row.geographyScore }}</div>
                <div style="border-top: 1px solid #eee; margin-top: 5px; padding-top: 5px;">
                  <strong>总分：{{ row.comprehensiveScore }}</strong>
                </div>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="90" align="center" sortable>
          <template #default="{ row }">
            <span style="font-weight: 700; font-size: 16px;" :style="{ color: getScoreColor(row.totalScore) }">
              {{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="与上次对比" width="120" align="center">
          <template #default="{ row }">
            <div v-if="row.scoreChange !== undefined">
              <el-tag v-if="row.scoreChange > 0" type="success" size="small">
                <el-icon><CaretTop /></el-icon>
                {{ row.scoreChange }}
              </el-tag>
              <el-tag v-else-if="row.scoreChange < 0" type="danger" size="small">
                <el-icon><CaretBottom /></el-icon>
                {{ Math.abs(row.scoreChange) }}
              </el-tag>
              <el-tag v-else type="info" size="small">
                持平
              </el-tag>
            </div>
            <span v-else style="color: #8c8c8c;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="classRank" label="班级排名" width="90" align="center" sortable>
          <template #default="{ row }">
            <el-tag :type="getRankType(row.classRank)" size="small">
              第{{ row.classRank }}名
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gradeRank" label="年级排名" width="90" align="center" sortable>
          <template #default="{ row }">
            <el-tag :type="getRankType(row.gradeRank)" size="small">
              第{{ row.gradeRank }}名
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="predictedUniversity" label="预测大学" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.predictedUniversity" placement="top" :show-after="300">
              <el-tag :type="getUniversityType(row.predictedUniversity)" effect="plain" style="max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                {{ row.predictedUniversity }}
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)" link>
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" link>
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生姓名" required>
              <el-input v-model="form.studentName" placeholder="请输入学生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级" required>
              <el-select v-model="form.className" placeholder="请选择班级" style="width: 100%;">
                <el-option label="一班" value="一班" />
                <el-option label="二班" value="二班" />
                <el-option label="三班" value="三班" />
                <el-option label="四班" value="四班" />
                <el-option label="五班" value="五班" />
                <el-option label="六班" value="六班" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="科类" required>
              <el-select v-model="form.classType" placeholder="请选择科类" style="width: 100%;">
                <el-option label="文科" value="文科" />
                <el-option label="理科" value="理科" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试日期" required>
              <el-date-picker
                v-model="form.examDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="考试名称" required>
          <el-input v-model="form.examName" placeholder="例如：第一周周考" />
        </el-form-item>

        <el-divider content-position="left">成绩录入</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="语文" required>
              <el-input-number v-model="form.chineseScore" :min="0" :max="150" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数学" required>
              <el-input-number v-model="form.mathScore" :min="0" :max="150" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="英语" required>
              <el-input-number v-model="form.englishScore" :min="0" :max="150" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="form.classType === '文科' ? '文综' : '理综'" required>
              <el-input-number v-model="form.comprehensiveScore" :min="0" :max="300" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamScoreList, addExamScore, updateExamScore, deleteExamScore, getExamStats } from '@/api/exam'
import { useRoute } from 'vue-router'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'

const route = useRoute()

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('录入成绩')
const focusFilter = ref('')
const allData = ref([]) // 存储所有数据用于筛选
const activeStatFilter = ref('') // 当前激活的统计卡片过滤器

const stats = reactive({
  totalStudents: 0,
  excellentCount: 0,
  averageScore: 0,
  needAttentionCount: 0
})

const searchForm = reactive({
  studentName: '',
  className: '',
  examName: '',
  minScore: undefined,
  maxScore: undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  studentName: '',
  className: '',
  classType: '理科',
  examName: '',
  examDate: '',
  chineseScore: 0,
  mathScore: 0,
  englishScore: 0,
  comprehensiveScore: 0
})

const getScoreColor = (score) => {
  if (score >= 650) return '#67c23a' // 绿色 - 优秀
  if (score >= 600) return '#409eff' // 蓝色 - 良好
  if (score >= 550) return '#e6a23c' // 橙色 - 中等
  return '#f56c6c' // 红色 - 需努力
}

const getRankType = (rank) => {
  if (rank <= 3) return 'danger' // 前三名
  if (rank <= 10) return 'warning' // 前十名
  return 'info'
}

const getUniversityType = (university) => {
  if (!university) return ''
  if (university.includes('985')) return 'danger'
  if (university.includes('211')) return 'warning'
  if (university.includes('一本')) return 'success'
  if (university.includes('二本')) return 'info'
  return ''
}

const getRowClassName = ({ row }) => {
  if (row.gradeRank <= 10) return 'top-student-row'
  if (row.gradeRank >= 50) return 'need-attention-row'
  return ''
}

const loadData = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    // 清理 undefined 值
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    
    const [listRes, statsRes] = await Promise.all([
      getExamScoreList(params),
      getExamStats({
        studentName: searchForm.studentName || undefined,
        className: searchForm.className || undefined,
        examName: searchForm.examName || undefined
      })
    ])
    
    allData.value = listRes.data.records
    pagination.total = listRes.data.total
    
    // 使用后端返回的精确统计数据
    stats.totalStudents = statsRes.data.totalStudents
    stats.excellentCount = statsRes.data.excellentCount
    stats.needAttentionCount = statsRes.data.needAttentionCount
    stats.averageScore = statsRes.data.averageScore
    
    // 应用重点关注筛选
    applyFocusFilter()
    
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 点击统计卡片切换过滤
const toggleStatFilter = (filterType) => {
  if (activeStatFilter.value === filterType) {
    // 再次点击取消筛选
    activeStatFilter.value = ''
    searchForm.minScore = undefined
    searchForm.maxScore = undefined
  } else {
    activeStatFilter.value = filterType
    if (filterType === 'excellent') {
      searchForm.minScore = 600
      searchForm.maxScore = undefined
    } else if (filterType === 'needAttention') {
      searchForm.minScore = undefined
      searchForm.maxScore = 499
    }
  }
  pagination.pageNum = 1
  loadData()
}

const applyFocusFilter = () => {
  let filteredData = [...allData.value]
  
  if (focusFilter.value === 'top10') {
    // 年级前10名
    filteredData = filteredData.filter(item => item.gradeRank <= 10)
  } else if (focusFilter.value === 'bottom20') {
    // 年级后20名
    const maxRank = Math.max(...filteredData.map(item => item.gradeRank))
    filteredData = filteredData.filter(item => item.gradeRank > maxRank - 20)
  } else if (focusFilter.value === 'classTop3') {
    // 班级前3名
    filteredData = filteredData.filter(item => item.classRank <= 3)
  } else if (focusFilter.value === 'classBottom5') {
    // 班级后5名
    const classMaxRanks = {}
    filteredData.forEach(item => {
      if (!classMaxRanks[item.className] || item.classRank > classMaxRanks[item.className]) {
        classMaxRanks[item.className] = item.classRank
      }
    })
    filteredData = filteredData.filter(item => 
      item.classRank > classMaxRanks[item.className] - 5
    )
  }
  
  tableData.value = filteredData
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.studentName = ''
  searchForm.className = ''
  searchForm.examName = ''
  searchForm.minScore = undefined
  searchForm.maxScore = undefined
  focusFilter.value = ''
  activeStatFilter.value = ''
  handleSearch()
}

const showAddDialog = () => {
  dialogTitle.value = '录入成绩'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑成绩'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该成绩记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteExamScore(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!form.studentName || !form.className || !form.examName || !form.examDate) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  try {
    if (form.id) {
      await updateExamScore(form)
      ElMessage.success('更新成功')
    } else {
      await addExamScore(form)
      ElMessage.success('添加成功，已自动计算总分和预测大学')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetForm = () => {
  form.id = null
  form.studentName = ''
  form.className = ''
  form.classType = '理科'
  form.examName = ''
  form.examDate = ''
  form.chineseScore = 0
  form.mathScore = 0
  form.englishScore = 0
  form.comprehensiveScore = 0
}

onMounted(() => {
  // 从URL参数中获取筛选条件
  if (route.query.examName) {
    searchForm.examName = route.query.examName
  }
  if (route.query.className) {
    searchForm.className = route.query.className
  }
  if (route.query.studentName) {
    searchForm.studentName = route.query.studentName
  }
  if (route.query.minScore) {
    searchForm.minScore = Number(route.query.minScore)
    if (searchForm.minScore === 600) {
      activeStatFilter.value = 'excellent'
    }
  }
  if (route.query.maxScore) {
    searchForm.maxScore = Number(route.query.maxScore)
    if (searchForm.maxScore === 499) {
      activeStatFilter.value = 'needAttention'
    }
  }
  
  loadData()
  
  // 监听AI演示事件
  window.addEventListener('ai-demo-search', (event) => {
    const { studentName, examName } = event.detail
    searchForm.studentName = studentName
    searchForm.examName = examName
    handleSearch()
  })
})
</script>

<style scoped>
.exam-container {
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
  color: #2c3e50;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

.stat-card-clickable {
  cursor: pointer;
  user-select: none;
}

.stat-card-active {
  border: 2px solid #409eff !important;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.25) !important;
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

:deep(.top-student-row) {
  background-color: #e6f7ff !important;
}

:deep(.need-attention-row) {
  background-color: #fff1f0 !important;
}

:deep(.el-table__row:hover) {
  background-color: #fafafa !important;
}

:deep(.el-card) {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

:deep(.el-table__header th) {
  background: #fafafa !important;
  color: #262626;
  font-weight: 600;
}
</style>

