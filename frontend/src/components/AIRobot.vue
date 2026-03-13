<template>
  <div class="ai-robot-container">
    <!-- AI机器人头像 -->
    <div 
      class="ai-robot" 
      :class="{ 'robot-talking': isTalking, 'robot-demo': isDemoMode }"
      @click="handleRobotClick"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
    >
      <div class="robot-head">
        <!-- 外圈光环 -->
        <div class="head-ring"></div>
        
        <!-- 头部主体 -->
        <div class="head-face">
          <!-- 天线 -->
          <div class="antenna">
            <div class="antenna-ball"></div>
          </div>
          
          <!-- 眼睛 -->
          <div class="eyes">
            <div class="eye left" :class="{ 'eye-blink': isBlinking || isHovering }">
              <div class="pupil"></div>
            </div>
            <div class="eye right" :class="{ 'eye-blink': isBlinking || isHovering }">
              <div class="pupil"></div>
            </div>
          </div>
          
          <!-- 嘴巴 -->
          <div class="mouth" :class="{ 'mouth-smile': isTalking }"></div>
          
          <!-- 腮红 -->
          <div class="cheek left"></div>
          <div class="cheek right"></div>
        </div>
        
        <!-- AI标识 -->
        <div class="ai-badge">AI</div>
      </div>
      
      <!-- 提示气泡 -->
      <transition name="bubble">
        <div v-if="showBubble" class="speech-bubble">
          {{ bubbleText }}
        </div>
      </transition>
    </div>

    <!-- AI搜索对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="AI智能助手" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="ai-dialog-content">
        <!-- 演示模式提示 -->
        <el-alert
          v-if="showDemoHint"
          title="请选择一个演示场景"
          type="success"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div style="color: #67c23a; font-weight: 500;">点击下方任意示例开始演示 👇</div>
          </template>
        </el-alert>
        
        <!-- 正常模式提示 -->
        <el-alert
          v-else
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div>您可以用自然语言描述您想查看的内容，例如：</div>
          </template>
        </el-alert>
        
        <!-- 可点击的示例列表 -->
        <div class="demo-examples">
          <div 
            v-for="(demo, index) in demoList" 
            :key="index"
            class="demo-item"
            :class="{ 'demo-hint-mode': showDemoHint }"
            @click="handleDemoItemClick(demo.command)"
          >
            <el-icon :size="18" style="margin-right: 8px;">
              <component :is="demo.icon" />
            </el-icon>
            <span>{{ demo.text }}</span>
          </div>
        </div>
        
        <el-input
          v-if="!showDemoHint"
          v-model="searchQuery"
          type="textarea"
          :rows="4"
          placeholder="请输入您想查看的内容..."
          @keyup.enter.ctrl="handleSearch"
          style="margin-top: 15px;"
        />
        
        <div v-if="isSearching" style="margin-top: 20px; text-align: center;">
          <el-icon class="is-loading" :size="30"><Loading /></el-icon>
          <div style="margin-top: 10px; color: #909399;">AI正在理解您的需求...</div>
        </div>
        
        <div v-if="searchResult" style="margin-top: 20px;">
          <el-alert
            :title="searchResult.message"
            :type="searchResult.success ? 'success' : 'warning'"
            :closable="false"
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="handleCancelDialog">取消</el-button>
        <el-button 
          v-if="!showDemoHint"
          type="primary" 
          @click="handleSearch" 
          :loading="isSearching"
        >
          <el-icon><MagicStick /></el-icon>
          AI搜索
        </el-button>
        <el-button 
          type="success" 
          @click="toggleDemoMode"
          :loading="isDemoMode"
        >
          <el-icon><VideoPlay /></el-icon>
          {{ showDemoHint ? '返回搜索' : '演示功能' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, MagicStick, VideoPlay, TrendCharts, User, Calendar, Notebook } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const dialogVisible = ref(false)
const searchQuery = ref('')
const isSearching = ref(false)
const searchResult = ref(null)
const isTalking = ref(false)
const isBlinking = ref(false)
const isHovering = ref(false)
const showBubble = ref(false)
const bubbleText = ref('点我试试！')
const isDemoMode = ref(false)
const showDemoHint = ref(false)

// 演示场景列表
const demoList = [
  { command: 'demo1', text: '查看张伟的第一次月考成绩', icon: TrendCharts },
  { command: 'demo2', text: '显示高三1班的学生列表', icon: User },
  { command: 'demo3', text: '查看李明的最近考勤记录', icon: Calendar },
  { command: 'demo4', text: '打开高三2班的班干部日志', icon: Notebook }
]

let blinkInterval = null
let bubbleTimeout = null

// 鼠标悬停
const handleMouseEnter = () => {
  isHovering.value = true
  showSpeechBubble('点我打开AI助手！', 2000)
}

const handleMouseLeave = () => {
  isHovering.value = false
}

// 眨眼动画
const startBlinking = () => {
  blinkInterval = setInterval(() => {
    isBlinking.value = true
    setTimeout(() => {
      isBlinking.value = false
    }, 200)
  }, 3000)
}

// 显示气泡
const showSpeechBubble = (text, duration = 3000) => {
  bubbleText.value = text
  showBubble.value = true
  if (bubbleTimeout) clearTimeout(bubbleTimeout)
  bubbleTimeout = setTimeout(() => {
    showBubble.value = false
  }, duration)
}

// 点击机器人
const handleRobotClick = () => {
  isTalking.value = true
  showSpeechBubble('你好！我是AI助手，有什么可以帮你的吗？', 2000)
  setTimeout(() => {
    isTalking.value = false
    dialogVisible.value = true
    showDemoHint.value = false
  }, 500)
}

// 切换演示模式
const toggleDemoMode = () => {
  showDemoHint.value = !showDemoHint.value
  searchResult.value = null
}

// 取消对话框
const handleCancelDialog = () => {
  dialogVisible.value = false
  showDemoHint.value = false
}

// 点击演示项
const handleDemoItemClick = (command) => {
  if (!showDemoHint.value) {
    // 正常模式下，点击示例自动填充到搜索框
    const demo = demoList.find(d => d.command === command)
    if (demo) {
      searchQuery.value = demo.text
    }
  } else {
    // 演示模式下，直接执行演示
    handleDemoCommand(command)
  }
}

// AI搜索
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    return
  }
  
  isSearching.value = true
  searchResult.value = null
  
  try {
    const response = await axios.post('/api/ai/search', {
      query: searchQuery.value
    })
    
    const result = response.data.data
    
    if (result.success) {
      searchResult.value = {
        success: true,
        message: `已为您找到：${result.description}`
      }
      
      setTimeout(() => {
        dialogVisible.value = false
        router.push(result.route)
      }, 1500)
    } else {
      searchResult.value = {
        success: false,
        message: result.message || '抱歉，无法理解您的需求，请换个方式描述'
      }
    }
  } catch (error) {
    searchResult.value = {
      success: false,
      message: '搜索失败，请稍后重试'
    }
  } finally {
    isSearching.value = false
  }
}

// 演示功能 - 根据选择的场景
const handleDemoCommand = (command) => {
  switch (command) {
    case 'demo1':
      startDemo1()
      break
    case 'demo2':
      startDemo2()
      break
    case 'demo3':
      startDemo3()
      break
    case 'demo4':
      startDemo4()
      break
  }
}

// 演示1：查看张伟的第一次月考成绩
const startDemo1 = async () => {
  isDemoMode.value = true
  dialogVisible.value = false
  
  try {
    showSpeechBubble('让我演示一下如何查看张伟的成绩', 3000)
    await sleep(3000)
    
    showSpeechBubble('首先，打开成绩管理页面', 2000)
    await sleep(2000)
    router.push('/exam')
    await sleep(2000)
    
    showSpeechBubble('然后，搜索"张伟"的"第一次月考"', 2000)
    await sleep(2000)
    
    window.dispatchEvent(new CustomEvent('ai-demo-search', { 
      detail: { studentName: '张伟', examName: '第一次月考' } 
    }))
    
    await sleep(2000)
    showSpeechBubble('完成！这就是张伟的第一次月考成绩', 3000)
    
  } catch (error) {
    ElMessage.error('演示失败')
  } finally {
    isDemoMode.value = false
  }
}

// 演示2：显示高三1班的学生列表
const startDemo2 = async () => {
  isDemoMode.value = true
  dialogVisible.value = false
  
  try {
    showSpeechBubble('让我演示如何查看高三1班的学生', 3000)
    await sleep(3000)
    
    showSpeechBubble('打开学生管理页面', 2000)
    await sleep(2000)
    router.push('/student?className=高三1班')
    await sleep(2000)
    
    showSpeechBubble('完成！这是高三1班的所有学生', 3000)
    
  } catch (error) {
    ElMessage.error('演示失败')
  } finally {
    isDemoMode.value = false
  }
}

// 演示3：查看李明的最近考勤记录
const startDemo3 = async () => {
  isDemoMode.value = true
  dialogVisible.value = false
  
  try {
    showSpeechBubble('让我演示如何查看李明的考勤记录', 3000)
    await sleep(3000)
    
    showSpeechBubble('打开考勤管理页面', 2000)
    await sleep(2000)
    router.push('/attendance?studentName=李明')
    await sleep(2000)
    
    showSpeechBubble('完成！这是李明的考勤记录', 3000)
    
  } catch (error) {
    ElMessage.error('演示失败')
  } finally {
    isDemoMode.value = false
  }
}

// 演示4：查看高三2班的班干部日志
const startDemo4 = async () => {
  isDemoMode.value = true
  dialogVisible.value = false
  
  try {
    showSpeechBubble('让我演示如何查看高三2班的班干部日志', 3000)
    await sleep(3000)
    
    showSpeechBubble('打开班干部日志页面', 2000)
    await sleep(2000)
    router.push('/diary?className=高三2班')
    await sleep(2000)
    
    showSpeechBubble('完成！这是高三2班的班干部日志', 3000)
    
  } catch (error) {
    ElMessage.error('演示失败')
  } finally {
    isDemoMode.value = false
  }
}

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

onMounted(() => {
  startBlinking()
  
  // 定期显示提示
  setTimeout(() => {
    showSpeechBubble('需要帮助吗？', 2000)
  }, 5000)
})

onUnmounted(() => {
  if (blinkInterval) clearInterval(blinkInterval)
  if (bubbleTimeout) clearTimeout(bubbleTimeout)
})
</script>

<style scoped>
.ai-robot-container {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 9999;
}

.ai-robot {
  cursor: pointer;
  position: relative;
  animation: float 3s ease-in-out infinite;
  transition: transform 0.3s;
}

.ai-robot:hover {
  transform: scale(1.1);
}

.ai-robot:hover .head-ring {
  opacity: 1;
  transform: scale(1.2);
}

.ai-robot.robot-demo {
  animation: float 3s ease-in-out infinite, glow-pulse 1.5s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

@keyframes glow-pulse {
  0%, 100% { filter: drop-shadow(0 0 12px rgba(251, 146, 60, 0.4)); }
  50% { filter: drop-shadow(0 0 28px rgba(251, 146, 60, 0.8)); }
}

/* ===== 机器人头像 ===== */
.robot-head {
  position: relative;
  width: 70px;
  height: 70px;
}

.head-ring {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  border: 3px solid rgba(251, 146, 60, 0.3);
  animation: ring-rotate 8s linear infinite;
  opacity: 0.6;
  transition: all 0.3s;
}

.head-ring::before {
  content: '';
  position: absolute;
  top: -3px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background: #fbbf24;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(251, 191, 36, 0.8);
}

@keyframes ring-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.head-face {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 50%, #fb923c 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 6px 24px rgba(251, 146, 60, 0.4),
    0 0 40px rgba(245, 158, 11, 0.2),
    inset 0 -4px 12px rgba(0, 0, 0, 0.15),
    inset 0 3px 8px rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: visible;
}

/* 天线 */
.antenna {
  position: absolute;
  top: -18px;
  left: 50%;
  transform: translateX(-50%);
  width: 3px;
  height: 18px;
  background: linear-gradient(to bottom, #f59e0b, #fb923c);
  border-radius: 2px;
}

.antenna-ball {
  position: absolute;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border-radius: 50%;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.6);
  animation: antenna-blink 2s ease-in-out infinite;
}

@keyframes antenna-blink {
  0%, 100% { opacity: 1; transform: translateX(-50%) scale(1); }
  50% { opacity: 0.4; transform: translateX(-50%) scale(0.8); }
}

/* 眼睛 */
.eyes {
  display: flex;
  gap: 18px;
  margin-top: 18px;
  margin-bottom: 8px;
}

.eye {
  width: 14px;
  height: 14px;
  background: white;
  border-radius: 50%;
  position: relative;
  transition: all 0.2s;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.eye-blink {
  height: 3px;
  background: #78350f;
}

.eye-blink .pupil {
  opacity: 0;
}

.pupil {
  position: absolute;
  width: 7px;
  height: 7px;
  background: #78350f;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: opacity 0.2s;
}

.pupil::after {
  content: '';
  position: absolute;
  width: 3px;
  height: 3px;
  background: white;
  border-radius: 50%;
  top: 2px;
  left: 2px;
}

/* 嘴巴 */
.mouth {
  width: 20px;
  height: 10px;
  border: 2px solid white;
  border-top: none;
  border-radius: 0 0 12px 12px;
  transition: all 0.3s;
}

.mouth-smile {
  width: 28px;
  height: 14px;
  animation: mouth-talk 0.5s ease-in-out infinite;
}

@keyframes mouth-talk {
  0%, 100% { height: 14px; }
  50% { height: 8px; }
}

/* 腮红 */
.cheek {
  position: absolute;
  width: 12px;
  height: 8px;
  background: rgba(239, 68, 68, 0.35);
  border-radius: 50%;
  top: 42px;
}

.cheek.left {
  left: 8px;
}

.cheek.right {
  right: 8px;
}

.ai-badge {
  position: absolute;
  bottom: -3px;
  right: -3px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 800;
  color: white;
  letter-spacing: -0.5px;
  box-shadow: 0 3px 8px rgba(34, 197, 94, 0.5);
  border: 3px solid white;
}

/* ===== 气泡 ===== */
.speech-bubble {
  position: absolute;
  bottom: 78px;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  padding: 10px 16px;
  border-radius: 14px;
  box-shadow: 0 6px 24px rgba(99, 102, 241, 0.12);
  white-space: nowrap;
  font-size: 13px;
  color: #312e81;
  font-weight: 500;
  border: 1px solid rgba(99, 102, 241, 0.12);
}

.speech-bubble::after {
  content: '';
  position: absolute;
  bottom: -8px;
  right: 22px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-top: 8px solid rgba(255, 255, 255, 0.95);
}

.bubble-enter-active, .bubble-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.bubble-enter-from, .bubble-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.9);
}

/* ===== 对话框列表样式 ===== */
.demo-examples {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.demo-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 2px solid transparent;
}

.demo-item:hover {
  background: #ecf5ff;
  color: #409eff;
  border-color: #409eff;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.demo-item.demo-hint-mode {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-color: #67c23a;
  animation: pulse-hint 2s ease-in-out infinite;
}

.demo-item.demo-hint-mode:hover {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  border-color: #67c23a;
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

@keyframes pulse-hint {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(103, 194, 58, 0);
  }
}

.demo-item span {
  font-size: 14px;
  font-weight: 500;
}
</style>
