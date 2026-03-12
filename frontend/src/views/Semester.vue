<template>
  <div class="semester-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Clock /></el-icon>
            学期管理
          </span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 添加学期
          </el-button>
        </div>
      </template>

      <el-table :data="semesters" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="semesterName" label="学期名称" min-width="200">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.semesterName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="130" align="center" />
        <el-table-column prop="endDate" label="结束日期" width="130" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isCurrent" type="success" size="large" effect="dark">当前学期</el-tag>
            <el-tag v-else type="info" size="small">历史学期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="handleSetCurrent(row)" v-if="!row.isCurrent">设为当前</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学期名称" required>
          <el-input v-model="form.semesterName" placeholder="如：2013年春季学期" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
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
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const semesters = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加学期')
const isEdit = ref(false)

const form = reactive({
  id: null,
  semesterName: '',
  startDate: '',
  endDate: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/semester/list')
    semesters.value = res.data
  } catch (e) {
    ElMessage.error('加载学期列表失败')
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  isEdit.value = false
  dialogTitle.value = '添加学期'
  Object.assign(form, { id: null, semesterName: '', startDate: '', endDate: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑学期'
  Object.assign(form, { id: row.id, semesterName: row.semesterName, startDate: row.startDate, endDate: row.endDate })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.semesterName) { ElMessage.warning('请输入学期名称'); return }
  try {
    if (isEdit.value) {
      await request.put('/semester/update', form)
      ElMessage.success('学期更新成功')
    } else {
      await request.post('/semester/add', form)
      ElMessage.success('学期添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleSetCurrent(row) {
  try {
    await ElMessageBox.confirm(`确定将"${row.semesterName}"设为当前学期？`, '提示')
    await request.put(`/semester/set-current/${row.id}`)
    ElMessage.success('设置成功')
    loadData()
  } catch (e) { /* cancelled */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.semesterName}"？`, '提示', { type: 'warning' })
    await request.delete(`/semester/delete/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* cancelled */ }
}

onMounted(() => loadData())
</script>

<style scoped>
.semester-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 600; color: var(--text-primary); display: flex; align-items: center; gap: 8px; }
:deep(.el-card) { border-radius: 8px; border: none; box-shadow: var(--shadow-card); }
:deep(.el-table__header th) { background: var(--bg-table-header) !important; color: var(--text-primary); font-weight: 600; }
</style>
