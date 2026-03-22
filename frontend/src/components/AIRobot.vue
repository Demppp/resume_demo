<template>
  <div class="ai-robot-container">
    <div
      class="ai-robot"
      :class="{ 'robot-talking': isTalking }"
      @click="handleRobotClick"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
    >
      <div class="robot-head">
        <svg width="68" height="68" viewBox="0 0 68 68" fill="none" xmlns="http://www.w3.org/2000/svg" class="robot-svg">
          <defs>
            <radialGradient id="bgSphere" cx="40%" cy="35%" r="60%">
              <stop offset="0%" stop-color="#2563eb"/>
              <stop offset="55%" stop-color="#1d4ed8"/>
              <stop offset="100%" stop-color="#0f172a"/>
            </radialGradient>
            <radialGradient id="innerGlow" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="#38bdf8" stop-opacity="0.3"/>
              <stop offset="100%" stop-color="#38bdf8" stop-opacity="0"/>
            </radialGradient>
            <linearGradient id="ringGrad" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#38bdf8"/>
              <stop offset="50%" stop-color="#818cf8"/>
              <stop offset="100%" stop-color="#38bdf8"/>
            </linearGradient>
            <linearGradient id="eyeL" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#e0f2fe"/>
              <stop offset="100%" stop-color="#7dd3fc"/>
            </linearGradient>
            <filter id="eyeBloom" x="-50%" y="-50%" width="200%" height="200%">
              <feGaussianBlur stdDeviation="1.8" result="b"/>
              <feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
            <filter id="sphereShadow">
              <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="#1d4ed8" flood-opacity="0.7"/>
            </filter>
          </defs>

          <!-- 主球体 -->
          <circle cx="34" cy="34" r="31" fill="url(#bgSphere)" filter="url(#sphereShadow)"/>

          <!-- 球体高光 -->
          <ellipse cx="26" cy="20" rx="10" ry="7" fill="white" opacity="0.08"/>

          <!-- 内发光 -->
          <circle cx="34" cy="34" r="28" fill="url(#innerGlow)"/>

          <!-- 旋转装饰弧线（静态，动画用 CSS）-->
          <circle cx="34" cy="34" r="30" fill="none" stroke="url(#ringGrad)" stroke-width="1.2"
            stroke-dasharray="18 8 6 8" stroke-linecap="round" opacity="0.6" class="orbit-ring"/>

          <!-- 眼睛：两个发光六边形改为圆角菱形 -->
          <!-- 左眼 -->
          <rect x="17" y="26" width="13" height="13" rx="6.5" fill="url(#eyeL)" filter="url(#eyeBloom)" class="eye-left"/>
          <circle cx="23.5" cy="32.5" r="3.8" fill="#0369a1"/>
          <circle cx="25" cy="30.5" r="1.8" fill="white" opacity="0.9"/>
          <circle cx="23.5" cy="32.5" r="1.2" fill="#075985"/>

          <!-- 右眼 -->
          <rect x="38" y="26" width="13" height="13" rx="6.5" fill="url(#eyeL)" filter="url(#eyeBloom)" class="eye-right"/>
          <circle cx="44.5" cy="32.5" r="3.8" fill="#0369a1"/>
          <circle cx="46" cy="30.5" r="1.8" fill="white" opacity="0.9"/>
          <circle cx="44.5" cy="32.5" r="1.2" fill="#075985"/>

          <!-- 嘴巴：简洁弧线 -->
          <path d="M25 44 Q34 50 43 44" stroke="url(#eyeL)" stroke-width="2.2" stroke-linecap="round" fill="none" opacity="0.85" class="mouth-arc"/>

          <!-- 额头装饰：三条细线 -->
          <rect x="28" y="17" width="12" height="1.5" rx="0.75" fill="#38bdf8" opacity="0.5"/>
          <rect x="30" y="20" width="8" height="1" rx="0.5" fill="#818cf8" opacity="0.4"/>

          <!-- 两侧耳朵：简洁圆 -->
          <circle cx="5" cy="34" r="3.5" fill="#1d4ed8" stroke="#38bdf8" stroke-width="1"/>
          <circle cx="5" cy="34" r="1.5" fill="#38bdf8" opacity="0.8"/>
          <circle cx="63" cy="34" r="3.5" fill="#1d4ed8" stroke="#38bdf8" stroke-width="1"/>
          <circle cx="63" cy="34" r="1.5" fill="#38bdf8" opacity="0.8"/>
        </svg>
        <div class="ai-status-ring" :class="{ talking: isTalking }"></div>
      </div>
      <transition name="bubble">
        <div v-if="showBubble" class="speech-bubble">{{ bubbleText }}</div>
      </transition>
    </div>

    <!-- 侧边栏 -->
    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="420px"
      :modal="true"
      :close-on-click-modal="true"
      class="ai-chat-drawer"
    >
      <template #header>
        <div class="drawer-header">
          <div class="drawer-title">
            <span class="title-dot"></span>
            AI 智能助手
            <span class="model-badge">RAG · 意图识别</span>
          </div>
          <el-button size="small" text @click="activeTab === 'chat' ? clearMessages() : clearRag()">清空</el-button>
        </div>
      </template>

      <!-- Tab 切换 -->
      <div class="tab-bar">
        <div class="tab-item" :class="{ active: activeTab === 'chat' }" @click="activeTab = 'chat'">💬 智能对话</div>
        <div class="tab-item" :class="{ active: activeTab === 'rag' }" @click="activeTab = 'rag'">🔍 学情分析</div>
      </div>

      <!-- 对话 Tab -->
      <template v-if="activeTab === 'chat'">
        <div class="message-list" ref="messageListRef">
          <div v-if="messages.length === 0" class="welcome-area">
            <div class="welcome-icon">🤖</div>
            <p class="welcome-title">你好，我是 AI 助手</p>
            <p class="welcome-sub">说人话，我来帮你跳转或分析数据</p>
            <div class="quick-questions">
              <div v-for="q in quickQuestions" :key="q" class="quick-item" @click="sendQuick(q)">{{ q }}</div>
            </div>
          </div>

          <transition-group name="msg">
            <div v-for="(msg, idx) in messages" :key="idx" class="message-item" :class="msg.role">
              <div v-if="msg.role === 'assistant'" class="msg-avatar">🤖</div>
              <div class="msg-bubble">
                <span v-if="msg.loading" class="typing-dots"><span></span><span></span><span></span></span>
                <span v-if="msg.role === 'user'" style="white-space:pre-wrap">{{ msg.content }}</span>
                <div v-else class="md-body" v-html="renderMarkdown(msg.content)"></div>
              </div>
              <div v-if="msg.role === 'user'" class="msg-avatar user">👤</div>
            </div>
          </transition-group>
        </div>

        <div class="input-area">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="输入消息，Enter 发送；Shift+Enter 换行"
            resize="none"
            @keydown.enter.exact.prevent="sendMessage()"
            @keydown.shift.enter.exact="() => {}"
            class="chat-input"
          />
          <div class="input-actions">
            <span class="input-hint">Enter 发送 · Shift+Enter 换行</span>
            <el-button type="primary" :loading="isLoading" @click="sendMessage()" class="send-btn">发送</el-button>
          </div>
        </div>
      </template>

      <!-- RAG 学情分析 Tab -->
      <template v-if="activeTab === 'rag'">
        <div class="rag-panel">
          <div class="rag-intro">
            <div class="rag-intro-title">🧠 pgvector 语义检索 + 千问大模型</div>
            <div class="rag-intro-sub">基于学情画像向量数据库，用自然语言查询最相关学生</div>
          </div>

          <div class="rag-quick-list">
            <div v-for="q in ragQuickQuestions" :key="q" class="rag-quick-item" @click="ragInput = q">{{ q }}</div>
          </div>

          <div class="rag-input-area">
            <el-input
              v-model="ragInput"
              type="textarea"
              :rows="2"
              placeholder="例如：找出近三次考试退步明显且缺勤较多的学生"
              resize="none"
              @keydown.enter.exact.prevent="sendRagQuery()"
              class="rag-input"
            />
            <div class="rag-input-actions">
              <el-button size="small" plain @click="rebuildIndex" :loading="isRebuilding">重建索引</el-button>
              <el-button type="primary" :loading="isRagLoading" @click="sendRagQuery()" class="send-btn">分析</el-button>
            </div>
          </div>

          <!-- 结果区 -->
          <div v-if="ragResult" class="rag-result-area">
            <!-- 召回学生卡片 -->
            <div class="rag-section-title">
              <span class="rag-section-icon">📋</span>
              语义检索召回（{{ ragResult.retrievedCount }} 名学生）
              <span class="rag-timing">向量化 {{ ragResult.embedTimeMs }}ms · 总耗时 {{ ragResult.totalTimeMs }}ms</span>
            </div>
            <div class="rag-profile-list">
              <div v-for="(p, i) in ragResult.retrievedProfiles" :key="i" class="rag-profile-card" :class="'risk-' + p.riskLevel">
                <div class="profile-header">
                  <span class="profile-name">{{ p.studentName }}</span>
                  <span class="profile-class">{{ p.className }}</span>
                  <span class="risk-badge" :class="'risk-' + p.riskLevel">{{ formatRisk(p.riskLevel) }}</span>
                  <span class="similarity-badge">相似度 {{ (p.similarity * 100).toFixed(1) }}%</span>
                </div>
                <div class="profile-text">{{ p.profileText }}</div>
              </div>
            </div>

            <!-- AI 分析结果 -->
            <div class="rag-section-title" style="margin-top:16px">
              <span class="rag-section-icon">✨</span>
              千问 AI 分析报告
            </div>
            <div class="rag-ai-analysis md-body" v-html="renderMarkdown(ragResult.aiAnalysis)"></div>
          </div>

          <div v-if="ragError" class="rag-error">{{ ragError }}</div>

          <!-- 加载状态 -->
          <div v-if="isRagLoading" class="rag-loading">
            <div class="rag-loading-step" :class="{ active: ragStep >= 1, done: ragStep >= 2 }">
              <span class="step-icon">{{ ragStep >= 2 ? '✅' : ragStep >= 1 ? '⏳' : '○' }}</span>
              千问 text-embedding-v3 向量化中...
            </div>
            <div class="rag-loading-step" :class="{ active: ragStep >= 2, done: ragStep >= 3 }">
              <span class="step-icon">{{ ragStep >= 3 ? '✅' : ragStep >= 2 ? '⏳' : '○' }}</span>
              pgvector 余弦距离语义检索中...
            </div>
            <div class="rag-loading-step" :class="{ active: ragStep >= 3 }">
              <span class="step-icon">{{ ragStep >= 3 ? '⏳' : '○' }}</span>
              千问 qwen-turbo 生成分析报告中...
            </div>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {nextTick, onMounted, onUnmounted, ref} from 'vue'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import {useRouter} from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 配置 marked
marked.setOptions({ breaks: true, gfm: true })
const renderMarkdown = (text) => DOMPurify.sanitize(marked.parse(text || ''))

const isTalking = ref(false)
const isBlinking = ref(false)
const isHovering = ref(false)
const showBubble = ref(false)
const bubbleText = ref('点我试试！')
const drawerVisible = ref(false)
const messageListRef = ref(null)
const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)

const activeTab = ref('chat')

const quickQuestions = [
  '查看刘梦的考勤记录',
  '帮我看高三1班的学生列表',
  '迟到最多的学生是谁？',
  '分析一下年级整体成绩趋势'
]

// RAG 相关状态
const ragInput = ref('')
const isRagLoading = ref(false)
const isRebuilding = ref(false)
const ragResult = ref(null)
const ragError = ref('')
const ragStep = ref(0) // 0=idle 1=embedding 2=searching 3=generating

const ragQuickQuestions = [
  '找出近三次考试退步明显且缺勤较多的学生',
  '哪些学生有较高学业风险',
  '谁最近成绩下降最快',
  '需要重点干预的学生有哪些'
]

const formatRisk = (level) => {
  const map = { high: '高风险', medium: '中风险', low: '低风险' }
  return map[level] || level
}

const clearRag = () => {
  ragResult.value = null
  ragError.value = ''
  ragInput.value = ''
}

const rebuildIndex = async () => {
  isRebuilding.value = true
  try {
    await axios.post('/api/ai/rag/rebuild-index')
    ElMessage.success('向量索引重建中，约15秒后自动查询...')
    // 等待异步重建完成后自动触发查询
    setTimeout(async () => {
      if (ragInput.value.trim()) {
        await sendRagQuery()
      } else {
        ElMessage.success('向量索引重建完成')
      }
    }, 15000)
  } catch (e) {
    ElMessage.error('重建失败：' + (e.message || '未知错误'))
  } finally {
    isRebuilding.value = false
  }
}

const sendRagQuery = async () => {
  const query = ragInput.value.trim()
  if (!query || isRagLoading.value) return
  isRagLoading.value = true
  ragResult.value = null
  ragError.value = ''
  ragStep.value = 1 // 向量化中
  try {
    // 模拟步骤进度（后端约 300ms 向量化，600ms 检索，3s 生成）
    const step2Timer = setTimeout(() => { ragStep.value = 2 }, 400)  // 检索中
    const step3Timer = setTimeout(() => { ragStep.value = 3 }, 900)  // 生成中
    const res = await axios.post('/api/ai/rag/query', { query })
    clearTimeout(step2Timer)
    clearTimeout(step3Timer)
    const data = res.data?.data || res.data
    if (data.success) {
      ragResult.value = data
    } else {
      ragError.value = data.message || '查询失败'
    }
  } catch (e) {
    ragError.value = '请求失败：' + (e.message || '未知错误')
  } finally {
    isRagLoading.value = false
    ragStep.value = 0
  }
}

let blinkInterval = null
let bubbleTimeout = null

const handleMouseEnter = () => {
  isHovering.value = true
  showSpeechBubble('我是智能助手，可以帮你分析学情、查询成绩、识别风险学生～', 3000)
}
const handleMouseLeave = () => { isHovering.value = false }

const startBlinking = () => {
  blinkInterval = setInterval(() => {
    isBlinking.value = true
    setTimeout(() => { isBlinking.value = false }, 200)
  }, 3000)
}

const showSpeechBubble = (text, duration = 3000) => {
  bubbleText.value = text
  showBubble.value = true
  if (bubbleTimeout) clearTimeout(bubbleTimeout)
  bubbleTimeout = setTimeout(() => { showBubble.value = false }, duration)
}

const handleRobotClick = () => {
  isTalking.value = true
  showSpeechBubble('你好！有什么可以帮你？', 2000)
  setTimeout(() => { isTalking.value = false; drawerVisible.value = true }, 400)
}

const sendMessage = async (quickText) => {
  const text = quickText || inputText.value.trim()
  if (!text || isLoading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isLoading.value = true
  messages.value.push({ role: 'assistant', content: '', loading: true })
  scrollToBottom()

  try {
    const searchRes = await axios.post('/api/ai/search', { query: text })
    const searchData = searchRes.data.data

    if (searchData && searchData.success && searchData.route) {
      const desc = searchData.description || '已为您找到对应页面'
      replaceLastAssistant('✅ ' + desc + '，正在跳转...')

      const routeStr = searchData.route
      const pathPart = routeStr.split('?')[0]
      const queryStr = routeStr.split('?')[1] || ''
      const params = {}
      if (queryStr) {
        queryStr.split('&').forEach(pair => {
          const eqIdx = pair.indexOf('=')
          if (eqIdx > 0) {
            const k = decodeURIComponent(pair.slice(0, eqIdx))
            const v = decodeURIComponent(pair.slice(eqIdx + 1))
            params[k] = v
          }
        })
      }
      // 关键修复：先 push 路由（await 等待路由完成，目标页面 onMounted 已读取 query）
      // 再广播 ai-demo-search 事件尔（针对已在同一页面的情况）
      await router.push({ path: pathPart, query: params })
      window.dispatchEvent(new CustomEvent('ai-demo-search', { detail: params }))
      setTimeout(() => { drawerVisible.value = false }, 300)
    } else {
      // 意图未识别成功，改用 SSE 流式接口，体验与 ChatGPT 一致
      const history = messages.value
        .slice(0, -1)
        .filter(m => !m.loading)
        .map(m => ({ role: m.role, content: m.content }))
      await streamChat(text, history)
    }
  } catch (e) {
    replaceLastAssistant('请求失败，请稍后重试')
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}


const streamChat = async (text, history) => {
  const url = `/api/ai/chat/stream?message=${encodeURIComponent(text)}&history=${encodeURIComponent(JSON.stringify(history))}`
  let response
  try { response = await fetch(url) } catch(e) { replaceLastAssistant('请求失败，请稍后重试'); return }
  if (!response.ok) { replaceLastAssistant('请求失败，请稍后重试'); return }
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  const idx = messages.value.findLastIndex(m => m.loading)
  if (idx !== -1) messages.value[idx] = { role: 'assistant', content: '', loading: false }
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    const chunk = decoder.decode(value, { stream: true })
    chunk.split('\n').forEach(line => {
      if (line.startsWith('data:')) {
        const token = line.slice(5)
        if (token === '[DONE]') return
        const i = messages.value.findLastIndex(m => m.role === 'assistant')
        if (i !== -1) { messages.value[i].content += token; scrollToBottom() }
      }
    })
  }
}

const sendQuick = (q) => {
  if (!drawerVisible.value) drawerVisible.value = true
  setTimeout(() => sendMessage(q), 100)
}

const replaceLastAssistant = (content) => {
  const idx = messages.value.findLastIndex(m => m.loading)
  if (idx !== -1) messages.value[idx] = { role: 'assistant', content, loading: false }
}

const clearMessages = () => { messages.value = [] }

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  })
}

onMounted(() => {
  startBlinking()
  setTimeout(() => showSpeechBubble('需要帮助吗？', 2000), 5000)
})

onUnmounted(() => {
  if (blinkInterval) clearInterval(blinkInterval)
  if (bubbleTimeout) clearTimeout(bubbleTimeout)
})
</script>

<style scoped>
/* Markdown 渲染样式 */
.md-body { font-size: 14px; line-height: 1.7; color: #303133; word-break: break-word; }
.md-body :deep(p) { margin: 0 0 6px 0; }
.md-body :deep(p:last-child) { margin-bottom: 0; }
.md-body :deep(strong) { color: #1a1a2e; font-weight: 600; }
.md-body :deep(ul), .md-body :deep(ol) { margin: 4px 0 6px 0; padding-left: 18px; }
.md-body :deep(li) { margin: 2px 0; }
.md-body :deep(code) { background: rgba(99,102,241,0.08); color: #4f46e5; padding: 1px 5px; border-radius: 4px; font-size: 12px; font-family: 'Fira Code', monospace; }
.md-body :deep(pre) { background: #1a1a2e; color: #e2e8f0; padding: 10px 14px; border-radius: 8px; overflow-x: auto; margin: 6px 0; }
.md-body :deep(pre code) { background: none; color: inherit; padding: 0; font-size: 12px; }
.md-body :deep(blockquote) { border-left: 3px solid #fbbf24; margin: 6px 0; padding: 4px 10px; background: rgba(251,191,36,0.06); color: #78716c; border-radius: 0 6px 6px 0; }
.md-body :deep(table) { border-collapse: collapse; width: 100%; margin: 6px 0; font-size: 13px; }
.md-body :deep(th) { background: #f0f4ff; padding: 6px 10px; border: 1px solid #dde3f0; font-weight: 600; text-align: left; }
.md-body :deep(td) { padding: 5px 10px; border: 1px solid #ebeef5; }
.md-body :deep(tr:nth-child(even)) { background: #fafbff; }
.md-body :deep(h1),.md-body :deep(h2),.md-body :deep(h3) { margin: 8px 0 4px 0; font-size: 14px; color: #1a1a2e; }
.md-body :deep(hr) { border: none; border-top: 1px solid #ebeef5; margin: 8px 0; }

.ai-robot-container { position: fixed; right: 30px; bottom: 30px; z-index: 9999; }

.ai-robot { cursor: pointer; position: relative; animation: float 3s ease-in-out infinite; transition: transform 0.3s; }
.ai-robot:hover { transform: scale(1.1); }
.ai-robot.robot-talking .head-face { animation: talking 0.4s ease-in-out infinite alternate; }

@keyframes float { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
@keyframes talking { from { box-shadow: 0 6px 24px rgba(251,146,60,0.4); } to { box-shadow: 0 6px 32px rgba(251,146,60,0.8); } }

.robot-head { position: relative; width: 68px; height: 68px; }

.robot-svg { filter: drop-shadow(0 6px 22px rgba(29,78,216,0.6)); transition: filter 0.3s; }
.ai-robot:hover .robot-svg { filter: drop-shadow(0 10px 32px rgba(56,189,248,0.75)); }
.ai-robot.robot-talking .robot-svg { filter: drop-shadow(0 6px 28px rgba(56,189,248,0.9)); }

/* 轨道环旋转 */
.robot-svg .orbit-ring { transform-origin: 34px 34px; animation: orbit-spin 6s linear infinite; }
@keyframes orbit-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* 眼睛呼吸 */
.robot-svg .eye-left, .robot-svg .eye-right { animation: eye-breathe 3s ease-in-out infinite; }
.robot-svg .eye-right { animation-delay: 0.3s; }
@keyframes eye-breathe { 0%,100% { opacity: 1; } 50% { opacity: 0.75; } }

/* 说话时嘴巴动效 */
.robot-talking .mouth-arc { animation: mouth-wave 0.45s ease-in-out infinite alternate; }
@keyframes mouth-wave { from { d: path('M25 44 Q34 50 43 44'); } to { d: path('M25 46 Q34 48 43 46'); } }

.antenna-dot { animation: antenna-blink 2s ease-in-out infinite; }
@keyframes antenna-blink { 0%,100% { opacity:1; r:3; } 50% { opacity:0.5; r:2; } }

.robot-mouth { transition: d 0.3s; }
.robot-talking .robot-mouth { animation: mouth-talk 0.5s ease-in-out infinite; }
@keyframes mouth-talk { 0%,100% { stroke-width: 2; } 50% { stroke-width: 3; } }

.ai-status-ring { position: absolute; inset: -5px; border-radius: 50%; border: 2px solid transparent; background: linear-gradient(white, white) padding-box, linear-gradient(135deg, #6366f1, #a855f7, #6366f1) border-box; animation: ring-spin 4s linear infinite; opacity: 0.7; }
.ai-status-ring.talking { opacity: 1; animation-duration: 1.5s; border-width: 3px; }
@keyframes ring-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.speech-bubble { position: absolute; bottom: 78px; right: 0; background: rgba(255,255,255,0.95); backdrop-filter: blur(12px); padding: 10px 16px; border-radius: 14px; box-shadow: 0 6px 24px rgba(99,102,241,0.12); white-space: nowrap; font-size: 13px; color: #312e81; font-weight: 500; border: 1px solid rgba(99,102,241,0.12); }
.speech-bubble::after { content:''; position:absolute; bottom:-8px; right:22px; width:0; height:0; border-left:8px solid transparent; border-right:8px solid transparent; border-top:8px solid rgba(255,255,255,0.95); }
.bubble-enter-active,.bubble-leave-active { transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); }
.bubble-enter-from,.bubble-leave-to { opacity:0; transform:translateY(10px) scale(0.9); }

.ai-chat-drawer :deep(.el-drawer__header) { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); margin-bottom: 0; padding: 16px 20px; }
.drawer-header { display: flex; align-items: center; justify-content: space-between; width: 100%; }
.drawer-title { display: flex; align-items: center; gap: 8px; color: #fff; font-size: 15px; font-weight: 600; }
.title-dot { width: 8px; height: 8px; background: #22c55e; border-radius: 50%; box-shadow: 0 0 6px #22c55e; animation: dot-pulse 2s ease-in-out infinite; }
@keyframes dot-pulse { 0%,100% { opacity:1; } 50% { opacity:0.4; } }
.model-badge { font-size: 10px; background: rgba(255,255,255,0.15); color: rgba(255,255,255,0.8); padding: 2px 8px; border-radius: 10px; font-weight: 400; }
.ai-chat-drawer :deep(.el-drawer__body) { display: flex; flex-direction: column; padding: 0; height: calc(100% - 57px); overflow: hidden; }

.message-list { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.welcome-area { display: flex; flex-direction: column; align-items: center; padding: 32px 16px; gap: 12px; text-align: center; }
.welcome-icon { font-size: 48px; }
.welcome-title { font-size: 16px; font-weight: 600; color: #303133; margin: 0; }
.welcome-sub { font-size: 13px; color: #909399; margin: 0; }
.quick-questions { display: flex; flex-direction: column; gap: 8px; width: 100%; margin-top: 8px; }
.quick-item { padding: 10px 14px; background: #f5f7fa; border-radius: 8px; font-size: 13px; color: #606266; cursor: pointer; text-align: left; transition: all 0.2s; border: 1px solid transparent; }
.quick-item:hover { background: #ecf5ff; color: #409eff; border-color: #409eff; }

.message-item { display: flex; align-items: flex-start; gap: 10px; }
.message-item.user { flex-direction: row-reverse; }
.msg-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; background: #f0f0f0; }
.msg-avatar.user { background: #ecf5ff; }
.msg-bubble { max-width: 85%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; }
.message-item.assistant .msg-bubble { background: #f5f7fa; color: #303133; border-radius: 4px 12px 12px 12px; }
.message-item.user .msg-bubble { background: linear-gradient(135deg, #409eff, #2d8cf0); color: #fff; border-radius: 12px 4px 12px 12px; }

.typing-dots { display: inline-flex; gap: 4px; align-items: center; height: 20px; }
.typing-dots span { width: 6px; height: 6px; background: #909399; border-radius: 50%; animation: typing 1.2s ease-in-out infinite; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%,80%,100% { transform:scale(0.6); opacity:0.4; } 40% { transform:scale(1); opacity:1; } }

.msg-enter-active { transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); }
.msg-enter-from { opacity:0; transform:translateY(10px) scale(0.95); }

.input-area { padding: 12px 16px 16px; border-top: 1px solid #ebeef5; background: #fff; flex-shrink: 0; }
.chat-input :deep(.el-textarea__inner) { border-radius: 8px; font-size: 14px; resize: none; }
.input-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
.input-hint { font-size: 12px; color: #c0c4cc; }
.send-btn { border-radius: 8px; padding: 8px 20px; }

/* Tab 切换 */
.tab-bar { display: flex; border-bottom: 1px solid #ebeef5; background: #fff; flex-shrink: 0; }
.tab-item { flex: 1; text-align: center; padding: 10px 0; font-size: 13px; color: #909399; cursor: pointer; transition: all 0.2s; border-bottom: 2px solid transparent; }
.tab-item.active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }
.tab-item:hover { color: #409eff; background: #f5f7fa; }

/* RAG 面板 */
.rag-panel { flex: 1; overflow-y: auto; display: flex; flex-direction: column; padding: 14px; gap: 12px; }
.rag-intro { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); border-radius: 10px; padding: 12px 16px; }
.rag-intro-title { color: #fff; font-size: 13px; font-weight: 600; margin-bottom: 4px; }
.rag-intro-sub { color: rgba(255,255,255,0.6); font-size: 12px; }

.rag-quick-list { display: flex; flex-direction: column; gap: 6px; }
.rag-quick-item { padding: 8px 12px; background: #f5f7fa; border-radius: 8px; font-size: 12px; color: #606266; cursor: pointer; border: 1px solid transparent; transition: all 0.2s; }
.rag-quick-item:hover { background: #ecf5ff; color: #409eff; border-color: #c6e2ff; }

.rag-input-area { display: flex; flex-direction: column; gap: 8px; }
.rag-input :deep(.el-textarea__inner) { border-radius: 8px; font-size: 13px; }
.rag-input-actions { display: flex; justify-content: flex-end; gap: 8px; }

/* RAG 结果 */
.rag-result-area { display: flex; flex-direction: column; gap: 8px; }
.rag-section-title { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #303133; }
.rag-section-icon { font-size: 14px; }
.rag-timing { font-size: 11px; color: #909399; font-weight: 400; margin-left: auto; }

.rag-profile-list { display: flex; flex-direction: column; gap: 8px; }
.rag-profile-card { border-radius: 10px; padding: 10px 14px; border-left: 4px solid #ddd; background: #fafafa; }
.rag-profile-card.risk-high { border-left-color: #f56c6c; background: #fff5f5; }
.rag-profile-card.risk-medium { border-left-color: #e6a23c; background: #fffbf0; }
.rag-profile-card.risk-low { border-left-color: #67c23a; background: #f5fff0; }

.profile-header { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin-bottom: 6px; }
.profile-name { font-size: 14px; font-weight: 700; color: #303133; }
.profile-class { font-size: 12px; color: #909399; }
.risk-badge { font-size: 11px; padding: 1px 7px; border-radius: 10px; font-weight: 600; }
.risk-badge.risk-high { background: #fef0f0; color: #f56c6c; }
.risk-badge.risk-medium { background: #fdf6ec; color: #e6a23c; }
.risk-badge.risk-low { background: #f0f9eb; color: #67c23a; }
.similarity-badge { margin-left: auto; font-size: 11px; color: #409eff; background: #ecf5ff; padding: 1px 7px; border-radius: 10px; }
.profile-text { font-size: 12px; color: #606266; line-height: 1.6; }

.rag-ai-analysis { background: linear-gradient(135deg, #f0f4ff 0%, #f5f0ff 100%); border-radius: 10px; padding: 14px 16px; border: 1px solid #e0e7ff; }
.rag-error { color: #f56c6c; font-size: 13px; padding: 10px; background: #fff5f5; border-radius: 8px; }

/* RAG 加载步骤 */
.rag-loading { display: flex; flex-direction: column; gap: 10px; padding: 16px; background: linear-gradient(135deg, #f0f4ff, #f5f0ff); border-radius: 10px; border: 1px solid #e0e7ff; }
.rag-loading-step { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #909399; transition: all 0.3s; }
.rag-loading-step.active { color: #409eff; font-weight: 600; }
.rag-loading-step.done { color: #67c23a; }
.step-icon { font-size: 14px; width: 18px; text-align: center; }
</style>
