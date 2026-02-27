<template>
  <div class="student-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">学生信息管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog">
              <el-icon><Plus /></el-icon>
              添加学生
            </el-button>
            <el-button type="success" @click="showAiDialog">
              <el-icon><MagicStick /></el-icon>
              AI添加学生
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true">
          <el-form-item label="班级">
            <el-select v-model="searchForm.className" placeholder="请选择班级" clearable>
              <el-option label="一班" value="一班" />
              <el-option label="二班" value="二班" />
              <el-option label="三班" value="三班" />
              <el-option label="四班" value="四班" />
              <el-option label="五班" value="五班" />
              <el-option label="六班" value="六班" />
            </el-select>
          </el-form-item>
          <el-form-item label="科类">
            <el-select v-model="searchForm.classType" placeholder="请选择科类" clearable>
              <el-option label="文科" value="文科" />
              <el-option label="理科" value="理科" />
            </el-select>
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
        :row-style="{ height: '55px' }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="姓名" width="100" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #409eff;">{{ row.studentName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? 'primary' : 'danger'" size="small">
              {{ row.gender }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.className }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="classType" label="科类" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.classType === '理科' ? 'warning' : 'info'">
              {{ row.classType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="parentPhone" label="家长电话" width="130" align="center" />
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleViewScores(row)" link>
              <el-icon><DataLine /></el-icon>
              查看成绩
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.studentName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级" required>
          <el-select v-model="form.className" placeholder="请选择班级">
            <el-option label="一班" value="一班" />
            <el-option label="二班" value="二班" />
            <el-option label="三班" value="三班" />
            <el-option label="四班" value="四班" />
            <el-option label="五班" value="五班" />
            <el-option label="六班" value="六班" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入家庭地址" />
        </el-form-item>
        <el-form-item label="家长电话">
          <el-input v-model="form.parentPhone" placeholder="请输入家长电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI添加对话框 -->
    <el-dialog v-model="aiDialogVisible" title="AI智能添加学生" width="600px">
      <el-form>
        <el-form-item label="描述信息">
          <el-input
            v-model="aiDescription"
            type="textarea"
            :rows="4"
            placeholder="例如：张三，男，广州市天河区天河路123号，家长电话13800138000，一班，理科"
          />
        </el-form-item>
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-top: 10px"
        >
          请用一句话描述学生信息，包含：姓名、性别、地址、家长电话、班级、科类
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAiSubmit" :loading="aiLoading">
          AI解析并添加
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看成绩对话框 -->
    <el-dialog v-model="scoresDialogVisible" title="学生成绩记录" width="900px">
      <div v-if="studentScores.length > 0">
        <el-table :data="studentScores" stripe>
          <el-table-column prop="examName" label="考试名称" width="150" />
          <el-table-column prop="examDate" label="考试日期" width="120" />
          <el-table-column prop="chineseScore" label="语文" width="80" align="center" />
          <el-table-column prop="mathScore" label="数学" width="80" align="center" />
          <el-table-column prop="englishScore" label="英语" width="80" align="center" />
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
          <el-table-column prop="totalScore" label="总分" width="90" align="center">
            <template #default="{ row }">
              <span style="font-weight: 700; color: #e74c3c;">{{ row.totalScore }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="classRank" label="班级排名" width="90" align="center" />
          <el-table-column prop="gradeRank" label="年级排名" width="90" align="center" />
        </el-table>
      </div>
      <el-empty v-else description="暂无成绩记录" />
      <template #footer>
        <el-button type="primary" @click="scoresDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, addStudent, addStudentByAi, updateStudent, deleteStudent } from '@/api/student'
import { getStudentScores } from '@/api/exam'

const tableData = ref([])
const dialogVisible = ref(false)
const aiDialogVisible = ref(false)
const scoresDialogVisible = ref(false)
const dialogTitle = ref('添加学生')
const aiDescription = ref('')
const aiLoading = ref(false)
const studentScores = ref([])

const searchForm = reactive({
  className: '',
  classType: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  studentName: '',
  gender: '男',
  className: '',
  address: '',
  parentPhone: ''
})

const loadData = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const res = await getStudentList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.className = ''
  searchForm.classType = ''
  handleSearch()
}

const showAddDialog = () => {
  dialogTitle.value = '添加学生'
  resetForm()
  dialogVisible.value = true
}

const showAiDialog = () => {
  aiDescription.value = ''
  aiDialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学生'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleViewScores = async (row) => {
  try {
    const res = await getStudentScores(row.id)
    studentScores.value = res.data
    scoresDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载成绩失败')
  }
}

const handleSubmit = async () => {
  if (!form.studentName || !form.className) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  try {
    if (form.id) {
      await updateStudent(form)
      ElMessage.success('更新成功')
    } else {
      await addStudent(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleAiSubmit = async () => {
  if (!aiDescription.value.trim()) {
    ElMessage.warning('请输入学生描述信息')
    return
  }
  
  aiLoading.value = true
  try {
    await addStudentByAi({ description: aiDescription.value })
    ElMessage.success('AI添加学生成功')
    aiDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('AI解析失败，请检查输入格式')
  } finally {
    aiLoading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.studentName = ''
  form.gender = '男'
  form.className = ''
  form.address = ''
  form.parentPhone = ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.student-container {
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

.header-actions {
  display: flex;
  gap: 10px;
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

