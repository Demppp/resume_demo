<template>
  <div class="ai-robot-container">
    <!-- AI机器人 -->
    <div 
      class="ai-robot" 
      :class="{ 'robot-talking': isTalking, 'robot-demo': isDemoMode }"
      @click="handleRobotClick"
    >
      <div class="robot-body">
        <div class="robot-antenna">
          <div class="antenna-ball"></div>
        </div>
        <div class="robot-head">
          <div class="robot-eyes">
            <div class="eye left" :class="{ 'eye-blink': isBlinking }"></div>
            <div class="eye right" :class="{ 'eye-blink': isBlinking }"></div>
          </div>
          <div class="robot-mouth" :class="{ 'mouth-smile': isTalking }"></div>
        </div>
        <div class="robot-arms">
          <div class="arm left"></div>
          <div class="arm right"></div>
        </div>
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
  width: 80px;
  height: 100px;
  cursor: pointer;
  position: relative;
  animation: float 3s ease-in-out infinite;
  transition: transform 0.3s;
}

.ai-robot:hover {
  transform: scale(1.1);
}

.ai-robot.robot-demo {
  animation: float 3s ease-in-out infinite, pulse 1s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.7);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(24, 144, 255, 0);
  }
}

.robot-body {
  position: relative;
}

.robot-antenna {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: 15px;
  background: #1890ff;
}

.antenna-ball {
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.robot-head {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 15px;
  margin: 0 auto;
  position: relative;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.robot-eyes {
  display: flex;
  justify-content: space-around;
  padding: 15px 10px 0;
}

.eye {
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  position: relative;
  transition: all 0.2s;
}

.eye::after {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  background: #262626;
  border-radius: 50%;
  top: 3px;
  left: 3px;
}

.eye-blink {
  height: 2px;
}

.robot-mouth {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 8px;
  border: 2px solid white;
  border-top: none;
  border-radius: 0 0 10px 10px;
  transition: all 0.3s;
}

.mouth-smile {
  width: 30px;
  height: 12px;
}

.robot-arms {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
}

.arm {
  width: 15px;
  height: 30px;
  background: #1890ff;
  border-radius: 8px;
  animation: wave 2s ease-in-out infinite;
}

.arm.left {
  animation-delay: 0s;
}

.arm.right {
  animation-delay: 1s;
}

@keyframes wave {
  0%, 100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(20deg);
  }
}

.speech-bubble {
  position: absolute;
  bottom: 110px;
  right: 0;
  background: white;
  padding: 10px 15px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  white-space: nowrap;
  font-size: 14px;
  color: #262626;
}

.speech-bubble::after {
  content: '';
  position: absolute;
  bottom: -8px;
  right: 20px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-top: 8px solid white;
}

.bubble-enter-active, .bubble-leave-active {
  transition: all 0.3s;
}

.bubble-enter-from, .bubble-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.ai-dialog-content ul {
  list-style: none;
}

.ai-dialog-content ul li {
  padding: 5px 0;
  color: #606266;
}

.ai-dialog-content ul li:before {
  content: "• ";
  color: #1890ff;
  font-weight: bold;
  margin-right: 5px;
}
</style>

