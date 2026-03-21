<template>
  <div class="print-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">🖨️ 打印成绩单</span>
          <div>
            <el-select v-model="selectedClass" placeholder="选择班级" clearable style="width: 120px; margin-right: 12px;" @change="loadScores">
              <el-option v-for="c in ['高三1班','高三2班','高三3班','高三4班','高三5班','高三6班']" :key="c" :label="c" :value="c" />
            </el-select>
            <el-select v-model="selectedExam" placeholder="选择考试" clearable style="width: 150px; margin-right: 12px;" @change="loadScores">
              <el-option v-for="e in examNames" :key="e" :label="e" :value="e" />
            </el-select>
            <el-button type="primary" @click="handlePrint">
              <el-icon><Printer /></el-icon> 打印
            </el-button>
          </div>
        </div>
      </template>

      <!-- 打印预览 -->
      <div ref="printArea" class="print-area">
        <div class="print-header">
          <h1>广州市高三年级成绩单</h1>
          <div class="print-info">
            <span v-if="selectedClass">班级：{{ selectedClass }}</span>
            <span v-if="selectedExam">考试：{{ selectedExam }}</span>
            <span>打印日期：{{ new Date().toLocaleDateString('zh-CN') }}</span>
          </div>
        </div>
        <table class="print-table" v-if="scores.length > 0">
          <thead>
            <tr>
              <th>排名</th>
              <th>姓名</th>
              <th>班级</th>
              <th>语文</th>
              <th>数学</th>
              <th>英语</th>
              <th>文综/理综</th>
              <th>总分</th>
              <th>年级排名</th>
              <th>预测大学</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in scores" :key="idx">
              <td>{{ idx + 1 }}</td>
              <td>{{ row.studentName }}</td>
              <td>{{ row.className }}</td>
              <td>{{ row.chineseScore }}</td>
              <td>{{ row.mathScore }}</td>
              <td>{{ row.englishScore }}</td>
              <td>{{ row.comprehensiveScore }}</td>
              <td class="total-score">{{ row.totalScore }}</td>
              <td>{{ row.gradeRank }}</td>
              <td>{{ row.predictedUniversity }}</td>
            </tr>
          </tbody>
        </table>
        <el-empty v-else description="请选择班级和考试查看成绩单" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import axios from 'axios'
import {ElMessage} from 'element-plus'

const selectedClass = ref('')
const selectedExam = ref('')
const scores = ref([])
const examNames = ref([])
const printArea = ref(null)

async function loadExamNames() {
  try {
    const res = await axios.get('/api/exam/list', { params: { pageNum: 1, pageSize: 1000 } })
    const records = res.data.data.records || []
    examNames.value = [...new Set(records.map(s => s.examName))]
  } catch (e) { /* ignore */ }
}

async function loadScores() {
  if (!selectedExam.value) return
  try {
    const params = {
      pageNum: 1,
      pageSize: 200,
      className: selectedClass.value || undefined,
      examName: selectedExam.value,
    }
    const res = await axios.get('/api/exam/list', { params })
    scores.value = (res.data.data.records || []).sort((a, b) => (a.gradeRank || 999) - (b.gradeRank || 999))
  } catch (e) {
    ElMessage.error('加载成绩失败')
  }
}

function handlePrint() {
  if (scores.value.length === 0) {
    ElMessage.warning('请先选择考试加载成绩')
    return
  }
  window.print()
}

onMounted(() => loadExamNames())
</script>

<style scoped>
.print-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
:deep(.el-card) { border-radius: 8px; border: none; box-shadow: var(--shadow-card); }

.print-area { padding: 20px; }
.print-header { text-align: center; margin-bottom: 24px; }
.print-header h1 { font-size: 22px; color: var(--text-primary); margin-bottom: 8px; }
.print-info { color: var(--text-secondary); font-size: 14px; display: flex; justify-content: center; gap: 24px; }

.print-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.print-table th, .print-table td { border: 1px solid var(--border-color, #dcdfe6); padding: 8px 12px; text-align: center; }
.print-table th { background: var(--bg-table-header, #fafafa); font-weight: 600; color: var(--text-primary); }
.print-table td { color: var(--text-regular); }
.total-score { font-weight: 700; color: var(--color-danger, #f56c6c); }

/* 打印样式 */
@media print {
  body * { visibility: hidden; }
  .print-area, .print-area * { visibility: visible; }
  .print-area { position: absolute; left: 0; top: 0; width: 100%; padding: 20px; }
  .print-table th { background: #f5f5f5 !important; -webkit-print-color-adjust: exact; }
}
</style>
