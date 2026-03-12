<template>
  <div class="log-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><List /></el-icon>
            操作日志
          </span>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true">
          <el-form-item label="操作类型">
            <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 120px;">
              <el-option label="新增" value="CREATE" />
              <el-option label="修改" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作模块">
            <el-select v-model="searchForm.operationModule" placeholder="全部" clearable style="width: 120px;">
              <el-option label="学生" value="student" />
              <el-option label="成绩" value="exam" />
              <el-option label="考勤" value="attendance" />
              <el-option label="日志" value="diary" />
              <el-option label="学期" value="semester" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
              start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
              style="width: 260px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.operationType)" size="small">{{ getTypeLabel(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationModule" label="模块" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ getModuleLabel(row.operationModule) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
        <el-table-column prop="requestMethod" label="方法" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.requestMethod === 'DELETE' ? 'danger' : 'primary'" size="small" effect="plain">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP" width="130" align="center" />
        <el-table-column prop="executionTime" label="耗时(ms)" width="90" align="center" />
        <el-table-column prop="createdTime" label="操作时间" width="170" align="center" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { List } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const dateRange = ref(null)

const searchForm = reactive({
  operationType: '',
  operationModule: '',
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      operationType: searchForm.operationType || undefined,
      operationModule: searchForm.operationModule || undefined,
      startDate: dateRange.value ? dateRange.value[0] : undefined,
      endDate: dateRange.value ? dateRange.value[1] : undefined,
    }
    const res = await request.get('/operation-log/list', { params })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.operationType = ''
  searchForm.operationModule = ''
  dateRange.value = null
  pagination.pageNum = 1
  loadData()
}

function getTypeColor(type) {
  return { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger' }[type] || 'info'
}

function getTypeLabel(type) {
  return { CREATE: '新增', UPDATE: '修改', DELETE: '删除' }[type] || type
}

function getModuleLabel(module) {
  return { student: '学生', exam: '成绩', attendance: '考勤', diary: '日志', semester: '学期', ai: 'AI', notification: '通知' }[module] || module
}

onMounted(() => loadData())
</script>

<style scoped>
.log-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 600; color: var(--text-primary); display: flex; align-items: center; gap: 8px; }
.search-bar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
:deep(.el-card) { border-radius: 8px; border: none; box-shadow: var(--shadow-card); }
:deep(.el-table__header th) { background: var(--bg-table-header) !important; color: var(--text-primary); font-weight: 600; }
</style>
