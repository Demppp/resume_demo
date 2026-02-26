<template>
  <div class="home-container">
    <el-container>
      <el-aside width="220px">
        <div class="logo">
          <h2>班级管理系统</h2>
          <p>2013届高三年级</p>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#2c3e50"
          text-color="#ecf0f1"
          active-text-color="#3498db"
        >
          <el-menu-item index="/student">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="/attendance">
            <el-icon><Calendar /></el-icon>
            <span>考勤管理</span>
          </el-menu-item>
          <el-menu-item index="/exam">
            <el-icon><Document /></el-icon>
            <span>成绩管理</span>
          </el-menu-item>
          <el-menu-item index="/diary">
            <el-icon><Notebook /></el-icon>
            <span>班干部日志</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-content">
            <h3>广州市高三年级管理系统</h3>
            <div class="header-info">
              <el-button type="primary" circle @click="showAiSearch">
                <el-icon><Search /></el-icon>
              </el-button>
              <span style="margin-left: 15px;">年级主任管理平台</span>
            </div>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <!-- AI搜索对话框 -->
    <el-dialog 
      v-model="aiSearchVisible" 
      title="AI智能搜索" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="ai-search-container">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div>您可以用自然语言描述您想查看的内容，例如：</div>
            <ul style="margin: 10px 0 0 20px; padding: 0;">
              <li>查看张三的第一周周考成绩</li>
              <li>显示一班的学生列表</li>
              <li>查看李娜最近的考勤记录</li>
              <li>打开五班的班干部日志</li>
            </ul>
          </template>
        </el-alert>
        
        <el-input
          v-model="aiSearchQuery"
          type="textarea"
          :rows="4"
          placeholder="请输入您想查看的内容..."
          @keyup.enter.ctrl="handleAiSearch"
        />
        
        <div v-if="aiSearchLoading" style="margin-top: 20px; text-align: center;">
          <el-icon class="is-loading" :size="30"><Loading /></el-icon>
          <div style="margin-top: 10px; color: #909399;">AI正在理解您的需求...</div>
        </div>
        
        <div v-if="aiSearchResult" style="margin-top: 20px;">
          <el-alert
            :title="aiSearchResult.message"
            :type="aiSearchResult.success ? 'success' : 'warning'"
            :closable="false"
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="aiSearchVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleAiSearch" 
          :loading="aiSearchLoading"
        >
          <el-icon><MagicStick /></el-icon>
          AI搜索
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const activeMenu = ref('/student')
const aiSearchVisible = ref(false)
const aiSearchQuery = ref('')
const aiSearchLoading = ref(false)
const aiSearchResult = ref(null)

const showAiSearch = () => {
  aiSearchVisible.value = true
  aiSearchQuery.value = ''
  aiSearchResult.value = null
}

const handleAiSearch = async () => {
  if (!aiSearchQuery.value.trim()) {
    return
  }
  
  aiSearchLoading.value = true
  aiSearchResult.value = null
  
  try {
    // 调用后端AI解析接口
    const response = await axios.post('/api/ai/search', {
      query: aiSearchQuery.value
    })
    
    const result = response.data.data
    
    if (result.success) {
      aiSearchResult.value = {
        success: true,
        message: `已为您找到：${result.description}`
      }
      
      // 延迟跳转，让用户看到结果
      setTimeout(() => {
        aiSearchVisible.value = false
        router.push(result.route)
      }, 1500)
    } else {
      aiSearchResult.value = {
        success: false,
        message: result.message || '抱歉，无法理解您的需求，请换个方式描述'
      }
    }
  } catch (error) {
    aiSearchResult.value = {
      success: false,
      message: '搜索失败，请稍后重试'
    }
  } finally {
    aiSearchLoading.value = false
  }
}

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })
</script>

<style scoped>
.home-container {
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #2c3e50;
  color: #ecf0f1;
  overflow-x: hidden;
}

.logo {
  padding: 30px 20px;
  text-align: center;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  border-bottom: 3px solid #2980b9;
}

.logo h2 {
  font-size: 22px;
  margin-bottom: 8px;
  color: #fff;
  font-weight: 600;
}

.logo p {
  font-size: 13px;
  color: #ecf0f1;
  opacity: 0.9;
}

.el-menu {
  border: none;
}

.el-header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-content h3 {
  font-size: 20px;
  color: #2c3e50;
  font-weight: 600;
}

.header-info {
  color: #7f8c8d;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.ai-search-container {
  padding: 10px 0;
}

.ai-search-container ul {
  list-style: none;
}

.ai-search-container ul li {
  padding: 5px 0;
  color: #606266;
}

.ai-search-container ul li:before {
  content: "• ";
  color: #409eff;
  font-weight: bold;
  margin-right: 5px;
}

.el-main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>

