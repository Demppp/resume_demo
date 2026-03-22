<template>
  <el-dialog
    v-model="visible"
    width="620px"
    :close-on-click-modal="false"
    class="ai-report-dialog"
    destroy-on-close
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <span class="header-icon">🤖</span>
          <div>
            <div class="header-title">AI 学情诊断报告</div>
            <div class="header-sub">{{ studentName }} · 基于真实数据生成</div>
          </div>
        </div>
        <div class="header-badge">RAG · LLM</div>
      </div>
    </template>

    <!-- 加载中 -->
    <div v-if="loading" class="report-loading">
      <div class="loading-robot">
        <div class="robot-eye left"></div>
        <div class="robot-eye right"></div>
      </div>
      <p class="loading-text">AI 正在分析学生数据...</p>
      <p class="loading-sub">检索成绩趋势 · 考勤记录 · 班级排名</p>
      <div class="loading-bar"><div class="loading-bar-inner"></div></div>
    </div>

    <!-- 报告内容 -->
    <div v-else-if="report" class="report-content">

      <!-- 风险等级横幅 -->
      <div class="urgency-banner" :class="'urgency-' + urgencyLevel">
        <div class="urgency-left">
          <span class="urgency-icon">{{ urgencyIcon }}</span>
          <div>
            <div class="urgency-label">综合风险等级</div>
            <div class="urgency-value">{{ report.urgency }}风险</div>
          </div>
        </div>
        <div class="urgency-meter">
          <div class="meter-track">
            <div class="meter-fill" :style="{ width: urgencyPercent + '%' }"></div>
          </div>
          <div class="meter-labels">
            <span>低</span><span>中</span><span>高</span>
          </div>
        </div>
      </div>

      <!-- 成绩趋势 -->
      <div class="report-section">
        <div class="section-title">
          <span class="section-icon">📈</span>
          成绩趋势分析
        </div>
        <div class="section-body">
          <div class="md-body" v-html="renderMarkdown(report.trend)"></div>
        </div>
      </div>

      <!-- 风险因素 -->
      <div class="report-section">
        <div class="section-title">
          <span class="section-icon">⚠️</span>
          风险因素
          <span class="risk-count">{{ report.risk_factors?.length || 0 }} 项</span>
        </div>
        <div class="section-body risk-body">
          <div
            v-for="(factor, index) in report.risk_factors"
            :key="index"
            class="risk-item"
          >
            <span class="risk-index">{{ index + 1 }}</span>
            <span class="risk-text">{{ factor }}</span>
          </div>
        </div>
      </div>

      <!-- 干预建议 -->
      <div class="report-section suggestion-section">
        <div class="section-title">
          <span class="section-icon">💡</span>
          干预建议
        </div>
        <div class="section-body">
          <div class="md-body" v-html="renderMarkdown(report.suggestion)"></div>
        </div>
      </div>

      <div class="report-footer">
        <span>⚡ 由通义千问 qwen-turbo 基于 RAG 检索生成</span>
        <span class="footer-time">{{ generateTime }}</span>
      </div>
    </div>

    <!-- 错误 -->
    <div v-else-if="error" class="report-error">
      <div class="error-icon">⚠️</div>
      <p>{{ error }}</p>
      <el-button type="primary" size="small" @click="fetchReport">重试</el-button>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button v-if="report" @click="copyReport" plain>📋 复制报告</el-button>
        <el-button v-if="report" type="primary" :loading="loading" @click="fetchReport">🔄 重新生成</el-button>
      </div>
    </template>
  </el-dialog>
</template>


<script setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import axios from 'axios'

marked.setOptions({ breaks: true, gfm: true })
const renderMarkdown = (text) => DOMPurify.sanitize(marked.parse(text || ''))

const props = defineProps({
  modelValue: Boolean,
  studentId: [Number, String],
  studentName: String
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const report = ref(null)
const error = ref(null)
const generateTime = ref('')

const urgencyLevel = computed(() => ({ '高': 'high', '中': 'mid', '低': 'low' }[report.value?.urgency] || 'low'))
const urgencyIcon = computed(() => ({ '高': '🔴', '中': '🟡', '低': '🟢' }[report.value?.urgency] || '⚪'))
const urgencyPercent = computed(() => ({ '高': 90, '中': 55, '低': 20 }[report.value?.urgency] || 20))

const fetchReport = async () => {
  if (!props.studentId) return
  loading.value = true
  report.value = null
  error.value = null
  try {
    const res = await axios.post('/api/ai-chat/student-report', { studentId: props.studentId })
    const data = res.data.data
    if (data.success) {
      report.value = data
      generateTime.value = new Date().toLocaleString('zh-CN')
    } else {
      error.value = data.message || '生成报告失败'
    }
  } catch (e) {
    error.value = '请求失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const copyReport = () => {
  if (!report.value) return
  const text = [
    `AI 学情诊断报告 - ${props.studentName}`,
    `生成时间：${generateTime.value}`,
    `风险等级：${report.value.urgency}风险`,
    `\n【成绩趋势】\n${report.value.trend}`,
    `\n【风险因素】\n${report.value.risk_factors?.join('\n')}`,
    `\n【干预建议】\n${report.value.suggestion}`
  ].join('\n')
  navigator.clipboard.writeText(text).then(() => ElMessage.success('报告已复制到剪贴板'))
}

watch(visible, (val) => { if (val) fetchReport() })
</script>


<style scoped>
.ai-report-dialog :deep(.el-dialog__header) { padding: 0; }
.ai-report-dialog :deep(.el-dialog__body) { padding: 0 24px 8px; }
.ai-report-dialog :deep(.el-dialog) { border-radius: 12px; overflow: hidden; }

.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 24px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}
.header-left { display: flex; align-items: center; gap: 14px; }
.header-icon { font-size: 32px; }
.header-title { color: #fff; font-size: 16px; font-weight: 700; }
.header-sub { color: rgba(255,255,255,0.6); font-size: 12px; margin-top: 2px; }
.header-badge {
  font-size: 11px; background: rgba(255,255,255,0.12); color: rgba(255,255,255,0.8);
  padding: 3px 10px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.2);
}

/* 加载 */
.report-loading { display: flex; flex-direction: column; align-items: center; padding: 48px 20px; gap: 14px; }
.loading-robot { width: 64px; height: 44px; background: linear-gradient(135deg, #fbbf24, #f59e0b); border-radius: 14px; display: flex; align-items: center; justify-content: center; gap: 12px; animation: pulse 1.2s ease-in-out infinite; }
.robot-eye { width: 10px; height: 10px; background: #1a1a2e; border-radius: 50%; animation: blink 1.2s ease-in-out infinite; }
.robot-eye.right { animation-delay: 0.2s; }
@keyframes pulse { 0%,100%{transform:scale(1);}50%{transform:scale(1.06);} }
@keyframes blink { 0%,100%{transform:scaleY(1);}50%{transform:scaleY(0.1);} }
.loading-text { font-size: 15px; font-weight: 600; color: #303133; margin: 0; }
.loading-sub { font-size: 12px; color: #909399; margin: 0; }
.loading-bar { width: 200px; height: 4px; background: #ebeef5; border-radius: 2px; overflow: hidden; }
.loading-bar-inner { height: 100%; width: 40%; background: linear-gradient(90deg, #fbbf24, #f59e0b); border-radius: 2px; animation: bar-slide 1.4s ease-in-out infinite; }
@keyframes bar-slide { 0%{transform:translateX(-100%);}100%{transform:translateX(350%);} }

/* 风险横幅 */
.urgency-banner {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-radius: 10px; margin: 16px 0 8px;
  gap: 16px;
}
.urgency-high { background: linear-gradient(135deg, #fff1f0, #ffe4e6); border: 1px solid #ffa39e; }
.urgency-mid { background: linear-gradient(135deg, #fffbe6, #fff7cd); border: 1px solid #ffd666; }
.urgency-low { background: linear-gradient(135deg, #f6ffed, #d9f7be); border: 1px solid #95de64; }
.urgency-left { display: flex; align-items: center; gap: 12px; }
.urgency-icon { font-size: 28px; }
.urgency-label { font-size: 12px; color: #606266; }
.urgency-value { font-size: 18px; font-weight: 700; color: #1a1a2e; margin-top: 2px; }
.urgency-meter { flex: 1; max-width: 160px; }
.meter-track { height: 8px; background: rgba(0,0,0,0.08); border-radius: 4px; overflow: hidden; }
.meter-fill { height: 100%; border-radius: 4px; transition: width 0.8s ease; }
.urgency-high .meter-fill { background: linear-gradient(90deg, #ff7875, #f5222d); }
.urgency-mid .meter-fill { background: linear-gradient(90deg, #ffd666, #faad14); }
.urgency-low .meter-fill { background: linear-gradient(90deg, #95de64, #52c41a); }
.meter-labels { display: flex; justify-content: space-between; font-size: 10px; color: #909399; margin-top: 4px; }

/* 报告区块 */
.report-content { display: flex; flex-direction: column; gap: 12px; padding-bottom: 8px; }
.report-section { border: 1px solid #ebeef5; border-radius: 10px; overflow: hidden; }
.suggestion-section { border-color: #d9f7be; }
.section-title {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 16px; background: #fafafa;
  font-size: 13px; font-weight: 700; color: #1a1a2e;
  border-bottom: 1px solid #ebeef5;
}
.suggestion-section .section-title { background: #f6ffed; border-bottom-color: #d9f7be; }
.section-icon { font-size: 15px; }
.risk-count { margin-left: auto; font-size: 11px; background: #fff2f0; color: #f5222d; padding: 1px 8px; border-radius: 10px; font-weight: 500; }
.section-body { padding: 14px 16px; }
.risk-body { display: flex; flex-direction: column; gap: 8px; }
.risk-item { display: flex; align-items: flex-start; gap: 10px; }
.risk-index { width: 20px; height: 20px; border-radius: 50%; background: #f5222d; color: #fff; font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 1px; }
.risk-text { font-size: 13px; line-height: 1.6; color: #303133; }

/* Markdown */
.md-body { font-size: 13px; line-height: 1.75; color: #303133; }
.md-body :deep(p) { margin: 0 0 6px 0; }
.md-body :deep(p:last-child) { margin-bottom: 0; }
.md-body :deep(strong) { color: #1a1a2e; font-weight: 600; }
.md-body :deep(ul),.md-body :deep(ol) { margin: 4px 0 6px 16px; padding: 0; }
.md-body :deep(li) { margin: 3px 0; }
.md-body :deep(code) { background: rgba(99,102,241,0.08); color: #4f46e5; padding: 1px 5px; border-radius: 4px; font-size: 12px; }
.suggestion-section .section-body { background: #f6ffed; }

/* 页脚 */
.report-footer { display: flex; justify-content: space-between; align-items: center; font-size: 11px; color: #c0c4cc; padding: 4px 2px 0; }
.footer-time { color: #bfbfbf; }

/* 错误 */
.report-error { display: flex; flex-direction: column; align-items: center; padding: 48px 20px; gap: 12px; }
.error-icon { font-size: 40px; }
.report-error p { font-size: 14px; color: #f56c6c; margin: 0; }

/* 底部按钮 */
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
</style>

