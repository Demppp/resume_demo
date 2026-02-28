<template>
  <div class="diary-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">班干部日志</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加日志
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true">
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
        <el-table-column prop="className" label="班级" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.className }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diaryDate" label="日期" width="120" align="center" />
        <el-table-column prop="recorderName" label="记录人" width="120" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.recorderName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diaryContent" label="日志内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="aiSummary" label="AI总结" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.aiSummary" type="success" effect="plain" size="large">
              <el-icon><MagicStick /></el-icon>
              {{ row.aiSummary }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleView(row)" link>
              <el-icon><View /></el-icon>
              查看
            </el-button>
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
      <el-form :model="form" label-width="100px">
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
        <el-form-item label="日期" required>
          <el-date-picker
            v-model="form.diaryDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="记录人" required>
          <el-input v-model="form.recorderName" placeholder="请输入班干部姓名" />
        </el-form-item>
        <el-form-item label="日志内容" required>
          <el-input
            v-model="form.diaryContent"
            type="textarea"
            :rows="8"
            placeholder="请输入今日班级情况..."
          />
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          :closable="false"
        >
          保存后将自动使用AI生成一句话总结
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewDialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="班级">{{ viewData.className }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ viewData.diaryDate }}</el-descriptions-item>
        <el-descriptions-item label="记录人">{{ viewData.recorderName }}</el-descriptions-item>
        <el-descriptions-item label="日志内容">
          <div style="white-space: pre-wrap;">{{ viewData.diaryContent }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="AI总结">
          <el-tag type="success" size="large">{{ viewData.aiSummary }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiaryList, addDiary, updateDiary, deleteDiary } from '@/api/diary'
import { useRoute } from 'vue-router'

const route = useRoute()

const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('添加日志')
const dateRange = ref([])
const submitLoading = ref(false)

const searchForm = reactive({
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
  className: '',
  diaryDate: '',
  recorderName: '',
  diaryContent: ''
})

const viewData = reactive({
  className: '',
  diaryDate: '',
  recorderName: '',
  diaryContent: '',
  aiSummary: ''
})

const loadData = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      className: searchForm.className,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    }
    const res = await getDiaryList(params)
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
  searchForm.className = ''
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  handleSearch()
}

const showAddDialog = () => {
  dialogTitle.value = '添加日志'
  resetForm()
  dialogVisible.value = true
}

const handleView = (row) => {
  Object.assign(viewData, row)
  viewDialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑日志'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDiary(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!form.className || !form.diaryDate || !form.recorderName || !form.diaryContent) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  submitLoading.value = true
  try {
    if (form.id) {
      await updateDiary(form)
      ElMessage.success('更新成功，AI已重新生成摘要')
    } else {
      await addDiary(form)
      ElMessage.success('添加成功，AI已生成摘要')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.className = ''
  form.diaryDate = ''
  form.recorderName = ''
  form.diaryContent = ''
}

onMounted(() => {
  // 从URL参数中获取筛选条件
  if (route.query.className) {
    searchForm.className = route.query.className
  }
  
  // 如果有URL参数，自动触发搜索
  if (route.query.className) {
    handleSearch()
  } else {
    loadData()
  }
})
</script>

<style scoped>
.diary-container {
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

