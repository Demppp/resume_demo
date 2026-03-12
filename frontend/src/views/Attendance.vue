<template>
  <div class="attendance-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">考勤管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加考勤记录
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
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
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
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.studentName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.className }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attendanceDate" label="考勤日期" width="120" align="center" />
        <el-table-column prop="attendanceStatus" label="考勤状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.attendanceStatus)" size="large">
              {{ row.attendanceStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因说明" min-width="200" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学生姓名" required>
          <el-input v-model="form.studentName" placeholder="请输入学生姓名" />
        </el-form-item>
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
        <el-form-item label="考勤日期" required>
          <el-date-picker
            v-model="form.attendanceDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考勤状态" required>
          <el-select v-model="form.attendanceStatus" placeholder="请选择考勤状态" style="width: 100%;">
            <el-option label="正常" value="正常" />
            <el-option label="迟到" value="迟到" />
            <el-option label="早退" value="早退" />
            <el-option label="请假" value="请假" />
            <el-option label="旷课" value="旷课" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因说明">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入原因说明"
          />
        </el-form-item>
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
import { getAttendanceList, addAttendance, updateAttendance, deleteAttendance } from '@/api/attendance'
import { useRoute } from 'vue-router'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const route = useRoute()

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加考勤记录')
const dateRange = ref([])

const searchForm = reactive({
  studentName: '',
  className: '',
  startDate: '',
  endDate: ''
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
  attendanceDate: '',
  attendanceStatus: '正常',
  reason: ''
})

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

const loadData = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      studentName: searchForm.studentName,
      className: searchForm.className,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    }
    const res = await getAttendanceList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startDate = dateRange.value[0]
    searchForm.endDate = dateRange.value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.studentName = ''
  searchForm.className = ''
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  handleSearch()
}

const showAddDialog = () => {
  dialogTitle.value = '添加考勤记录'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑考勤记录'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该考勤记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAttendance(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!form.studentName || !form.className || !form.attendanceDate || !form.attendanceStatus) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  try {
    if (form.id) {
      await updateAttendance(form)
      ElMessage.success('更新成功')
    } else {
      await addAttendance(form)
      ElMessage.success('添加成功')
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
  form.attendanceDate = ''
  form.attendanceStatus = '正常'
  form.reason = ''
}

onMounted(() => {
  // 从 URL 参数中获取筛选条件
  if (route.query.studentName) {
    searchForm.studentName = route.query.studentName
  }
  if (route.query.className) {
    searchForm.className = route.query.className
  }
  
  // 如果有URL参数，自动触发搜索
  if (route.query.studentName || route.query.className) {
    handleSearch()
  } else {
    loadData()
  }
})
</script>

<style scoped>
.attendance-container {
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
</style>

