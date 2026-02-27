<template>
  <div class="ai-robot-container">
    <!-- AI智能助手 -->
    <div 
      class="ai-robot" 
      :class="{ 'robot-talking': isTalking, 'robot-demo': isDemoMode }"
      @click="handleRobotClick"
    >
      <div class="ai-orb">
        <!-- 外圈光环 -->
        <div class="orb-ring"></div>
        <!-- 主体 -->
        <div class="orb-core">
          <!-- 脑回路图标 -->
          <svg class="brain-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 6C14 6 9 10 9 16c0 4 2 7 5 9v4c0 1 1 2 2 2h8c1 0 2-1 2-2v-4c3-2 5-5 5-9 0-6-5-10-11-10z" stroke="white" stroke-width="2" stroke-linecap="round" fill="none"/>
            <path d="M15 21v4M20 21v6M25 21v4" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="16" cy="15" r="1.5" fill="white" opacity="0.9"/>
            <circle cx="24" cy="15" r="1.5" fill="white" opacity="0.9"/>
            <circle cx="20" cy="12" r="1" fill="white" opacity="0.6"/>
            <path d="M14 14c2-1 4 1 6 0s4 1 6 0" stroke="white" stroke-width="1" opacity="0.5" stroke-linecap="round"/>
          </svg>
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
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div>您可以用自然语言描述您想查看的内容，例如：</div>
            <ul style="margin: 10px 0 0 20px; padding: 0;">
              <li>查看张伟的第一周周考成绩</li>
              <li>显示一班的学生列表</li>
              <li>查看李娜最近的考勤记录</li>
              <li>打开二班的班干部日志</li>
            </ul>
          </template>
        </el-alert>
        
        <el-input
          v-model="searchQuery"
          type="textarea"
          :rows="4"
          placeholder="请输入您想查看的内容..."
          @keyup.enter.ctrl="handleSearch"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSearch" :loading="isSearching">
          <el-icon><MagicStick /></el-icon>
          AI搜索
        </el-button>
        <el-button type="success" @click="startDemo" :loading="isDemoMode">
          <el-icon><VideoPlay /></el-icon>
          演示功能
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, MagicStick, VideoPlay } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const dialogVisible = ref(false)
const searchQuery = ref('')
const isSearching = ref(false)
const searchResult = ref(null)
const isTalking = ref(false)
const isBlinking = ref(false)
const showBubble = ref(false)
const bubbleText = ref('点我试试！')
const isDemoMode = ref(false)

let blinkInterval = null
let bubbleTimeout = null

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
  }, 500)
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

// 演示功能
const startDemo = async () => {
  isDemoMode.value = true
  dialogVisible.value = false
  
  try {
    // 步骤1：显示提示
    showSpeechBubble('让我演示一下如何查看张伟的成绩', 3000)
    await sleep(3000)
    
    // 步骤2：导航到成绩管理
    showSpeechBubble('首先，打开成绩管理页面', 2000)
    await sleep(2000)
    router.push('/exam')
    await sleep(2000)
    
    // 步骤3：模拟搜索
    showSpeechBubble('然后，搜索"张伟"', 2000)
    await sleep(2000)
    
    // 触发搜索（通过事件）
    window.dispatchEvent(new CustomEvent('ai-demo-search', { 
      detail: { studentName: '张伟', examName: '第一周周考' } 
    }))
    
    await sleep(2000)
    showSpeechBubble('完成！这就是张伟的第一周周考成绩', 3000)
    
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

.ai-robot:hover .orb-ring {
  opacity: 1;
  transform: scale(1.15);
}

.ai-robot.robot-demo {
  animation: float 3s ease-in-out infinite, glow-pulse 1.5s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

@keyframes glow-pulse {
  0%, 100% { filter: drop-shadow(0 0 12px rgba(99, 102, 241, 0.4)); }
  50% { filter: drop-shadow(0 0 28px rgba(99, 102, 241, 0.8)); }
}

/* ===== AI 光球主体 ===== */
.ai-orb {
  position: relative;
  width: 64px;
  height: 64px;
}

.orb-ring {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  border: 2px solid rgba(129, 140, 248, 0.4);
  animation: ring-rotate 6s linear infinite;
  opacity: 0.7;
  transition: all 0.3s;
}

.orb-ring::before {
  content: '';
  position: absolute;
  top: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 6px;
  background: #a5b4fc;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(165, 180, 252, 0.8);
}

@keyframes ring-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.orb-core {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 40%, #a78bfa 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4px 20px rgba(99, 102, 241, 0.35),
    0 0 40px rgba(139, 92, 246, 0.15),
    inset 0 -3px 8px rgba(0, 0, 0, 0.15),
    inset 0 2px 4px rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.orb-core::before {
  content: '';
  position: absolute;
  top: 6px;
  left: 10px;
  width: 20px;
  height: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: rotate(-30deg);
}

.brain-icon {
  width: 32px;
  height: 32px;
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.15));
}

.ai-badge {
  position: absolute;
  bottom: -2px;
  right: -4px;
  width: 22px;
  height: 22px;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: 800;
  color: white;
  letter-spacing: -0.3px;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.4);
  border: 2px solid white;
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
.ai-dialog-content ul {
  list-style: none;
}

.ai-dialog-content ul li {
  padding: 5px 0;
  color: #606266;
}

.ai-dialog-content ul li:before {
  content: "• ";
  color: #6366f1;
  font-weight: bold;
  margin-right: 5px;
}
</style>
