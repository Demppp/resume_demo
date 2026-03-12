<template>
  <div class="data-screen">
    <!-- 顶部标题 -->
    <div class="screen-header">
      <h1>🎓 广州市高三年级数据大屏</h1>
      <div class="header-right">
        <span class="time">{{ currentTime }}</span>
        <el-button type="primary" size="small" @click="exitFullScreen">返回系统</el-button>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="screen-body" v-loading="loading">
      <el-row :gutter="16">
        <!-- 左侧: 年级排行榜 -->
        <el-col :span="8">
          <div class="panel">
            <div class="panel-header">🏆 年级排名实时榜</div>
            <div class="ranking-list">
              <div v-for="(s, i) in topStudents" :key="i" class="ranking-item" :class="{ 'top3': i < 3 }">
                <span class="rank" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
                <span class="name">{{ s.studentName }}</span>
                <span class="class-tag">{{ s.className }}</span>
                <span class="score">{{ s.totalScore }}</span>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 中间: 统计 + 雷达图 -->
        <el-col :span="8">
          <!-- 统计数字 -->
          <div class="stats-row">
            <div class="stat-box">
              <div class="stat-num">{{ stats.studentCount }}</div>
              <div class="stat-lbl">学生总数</div>
            </div>
            <div class="stat-box">
              <div class="stat-num">{{ stats.classCount }}</div>
              <div class="stat-lbl">班级数量</div>
            </div>
            <div class="stat-box">
              <div class="stat-num">{{ stats.avgScore }}</div>
              <div class="stat-lbl">年级均分</div>
            </div>
            <div class="stat-box">
              <div class="stat-num">{{ stats.excellentCount }}</div>
              <div class="stat-lbl">优秀人数</div>
            </div>
          </div>
          <!-- 雷达图 -->
          <div class="panel" style="margin-top: 16px;">
            <div class="panel-header">📊 各班成绩雷达图</div>
            <div ref="radarChart" style="width: 100%; height: 300px;"></div>
          </div>
        </el-col>

        <!-- 右侧: 考勤 + 预警 -->
        <el-col :span="8">
          <div class="panel" style="margin-bottom: 16px;">
            <div class="panel-header">📅 考勤出席率</div>
            <div ref="gaugeChart" style="width: 100%; height: 200px;"></div>
          </div>
          <div class="panel">
            <div class="panel-header">⚠️ 最新预警</div>
            <div class="warning-list">
              <div v-for="(w, i) in warnings" :key="i" class="warning-item">
                <el-tag :type="w.warningLevel === '严重' ? 'danger' : 'warning'" size="small">{{ w.warningLevel }}</el-tag>
                <span class="warning-name">{{ w.studentName }}</span>
                <span class="warning-desc">{{ w.warningMessage }}</span>
              </div>
              <div v-if="warnings.length === 0" style="text-align: center; color: #666; padding: 20px;">暂无预警</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import * as echarts from 'echarts'

const router = useRouter()
const loading = ref(true)
const currentTime = ref('')
const topStudents = ref([])
const warnings = ref([])
const radarChart = ref(null)
const gaugeChart = ref(null)

const stats = reactive({ studentCount: 0, classCount: 6, avgScore: 0, excellentCount: 0 })

let timer = null

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { hour12: false })
}

function exitFullScreen() {
  router.push('/dashboard')
}

async function loadData() {
  loading.value = true
  try {
    const [statsRes, topRes, warningRes] = await Promise.all([
      request.get('/dashboard/stats'),
      request.get('/dashboard/top-students'),
      request.get('/warning/list', { params: { pageNum: 1, pageSize: 20 } }).catch(() => ({ data: { records: [] } })),
    ])
    Object.assign(stats, statsRes.data)
    topStudents.value = (topRes.data || []).slice(0, 15)
    warnings.value = (warningRes.data.records || []).slice(0, 8)
    renderRadarChart()
    renderGaugeChart()
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

function renderRadarChart() {
  if (!radarChart.value) return
  const chart = echarts.init(radarChart.value)
  chart.setOption({
    radar: {
      indicator: [
        { name: '语文', max: 150 }, { name: '数学', max: 150 },
        { name: '英语', max: 150 }, { name: '文/理综', max: 300 },
      ],
      shape: 'circle',
      axisName: { color: '#aaa' },
      splitArea: { areaStyle: { color: ['rgba(100,200,255,0.05)', 'rgba(100,200,255,0.1)'] } },
    },
    tooltip: {},
    series: [{
      type: 'radar',
      data: [
        { value: [110, 115, 105, 220], name: '理科平均', areaStyle: { opacity: 0.2 } },
        { value: [115, 100, 110, 210], name: '文科平均', areaStyle: { opacity: 0.2 } },
      ]
    }]
  })
}

function renderGaugeChart() {
  if (!gaugeChart.value) return
  const chart = echarts.init(gaugeChart.value)
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 180, endAngle: 0,
      min: 0, max: 100,
      progress: { show: true, width: 18 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 18 } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      title: { offsetCenter: [0, '30%'], fontSize: 14, color: '#aaa' },
      detail: { offsetCenter: [0, '-10%'], fontSize: 36, fontWeight: 700, color: '#67c23a', formatter: '{value}%' },
      data: [{ value: 94.5, name: '出席率' }]
    }]
  })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadData()
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.data-screen {
  position: fixed;
  inset: 0;
  background: #0a0e27;
  color: #e0e0e0;
  overflow-y: auto;
  z-index: 9999;
}

.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: linear-gradient(90deg, rgba(30,60,114,0.9), rgba(42,82,152,0.9));
  border-bottom: 1px solid rgba(100,200,255,0.2);
}

.screen-header h1 {
  font-size: 22px;
  background: linear-gradient(135deg, #64b5f6, #42a5f5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-right { display: flex; align-items: center; gap: 16px; }
.time { color: #64b5f6; font-size: 14px; font-family: monospace; }

.screen-body { padding: 16px 24px; }

.panel {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(100,200,255,0.15);
  border-radius: 8px;
  padding: 16px;
}

.panel-header {
  font-size: 15px;
  font-weight: 600;
  color: #64b5f6;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(100,200,255,0.1);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-box {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(100,200,255,0.15);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.stat-num { font-size: 28px; font-weight: 700; color: #64b5f6; }
.stat-lbl { font-size: 12px; color: #90a0b0; margin-top: 4px; }

.ranking-list { max-height: 500px; overflow-y: auto; }

.ranking-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.2s;
  gap: 12px;
}

.ranking-item:hover { background: rgba(100,200,255,0.1); }
.ranking-item.top3 { background: rgba(255,215,0,0.08); }

.rank { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 13px; background: rgba(255,255,255,0.1); }
.rank-1 { background: #ffd700; color: #000; }
.rank-2 { background: #c0c0c0; color: #000; }
.rank-3 { background: #cd7f32; color: #fff; }
.name { flex: 1; font-weight: 500; }
.class-tag { color: #90a0b0; font-size: 12px; }
.score { font-weight: 700; color: #67c23a; font-size: 16px; }

.warning-list { max-height: 250px; overflow-y: auto; }
.warning-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }
.warning-name { font-weight: 500; }
.warning-desc { flex: 1; font-size: 12px; color: #90a0b0; text-align: right; }
</style>
