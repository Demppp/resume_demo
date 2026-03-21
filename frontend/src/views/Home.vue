<template>
  <div class="home-container">
    <el-container>
      <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar-aside">
        <div class="logo" v-if="!isCollapsed">
          <h2>班级管理系统</h2>
          <p>2013届高三年级</p>
        </div>
        <div class="logo logo-mini" v-else>
          <h2>班</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          :collapse="isCollapsed"
          :collapse-transition="true"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.65)"
          active-text-color="#fff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
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
          <el-menu-item index="/warning">
            <el-icon><Warning /></el-icon>
            <span>预警系统</span>
          </el-menu-item>
          <el-menu-item index="/analytics">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </el-menu-item>
          <el-menu-item index="/operation-log">
            <el-icon><List /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/semester">
            <el-icon><Clock /></el-icon>
            <span>学期管理</span>
          </el-menu-item>
        </el-menu>

        <!-- 折叠按钮 -->
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="18">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <h3>广州市高三年级管理系统</h3>
            <div class="header-actions">
              <NotificationBell />
              <!-- 语言切换 -->
              <el-dropdown trigger="click" @command="handleLangChange">
                <el-button circle size="small" class="header-btn">
                  <el-icon :size="16"><Flag /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="zh-CN" :disabled="currentLang === 'zh-CN'">中文</el-dropdown-item>
                    <el-dropdown-item command="en-US" :disabled="currentLang === 'en-US'">English</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <!-- 深色模式切换 -->
              <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
                <el-button circle size="small" @click="toggleTheme" class="header-btn theme-btn">
                  <el-icon :size="16">
                    <Sunny v-if="isDark" />
                    <Moon v-else />
                  </el-icon>
                </el-button>
              </el-tooltip>
              <!-- 数据大屏入口 -->
              <el-tooltip content="数据大屏" placement="bottom">
                <el-button circle size="small" @click="openDataScreen" class="header-btn">
                  <el-icon :size="16"><Monitor /></el-icon>
                </el-button>
              </el-tooltip>
              <span class="header-label">年级主任管理平台</span>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useThemeStore} from '../stores/theme'
import NotificationBell from '../components/NotificationBell.vue'
import {
  Calendar,
  Clock,
  DataAnalysis,
  DataBoard,
  Document,
  Expand,
  Flag,
  Fold,
  List,
  Monitor,
  Moon,
  Notebook,
  Sunny,
  User,
  Warning
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()

const activeMenu = ref('/dashboard')
const isCollapsed = ref(false)
const currentLang = ref('zh-CN')

const isDark = computed(() => themeStore.isDark)

function toggleTheme() {
  themeStore.toggleTheme()
}

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

function openDataScreen() {
  const routeData = router.resolve({ path: '/data-screen' })
  window.open(routeData.href, '_blank')
}

function handleLangChange(lang) {
  currentLang.value = lang
}

// 初始化时跳转到dashboard
if (route.path === '/home' || route.path === '/') {
  router.push('/dashboard')
}

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })

// 响应式: 窗口较小时折叠侧边栏
function checkScreenSize() {
  if (window.innerWidth < 768) {
    isCollapsed.value = true
  }
}
checkScreenSize()
window.addEventListener('resize', checkScreenSize)
</script>

<style scoped>
.home-container {
  height: 100vh;
  overflow: hidden;
  background: var(--bg-primary);
}

.el-container {
  height: 100%;
}

.sidebar-aside {
  background: var(--bg-sidebar);
  color: #fff;
  overflow-x: hidden;
  box-shadow: var(--shadow-sidebar);
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  position: relative;
}

.logo {
  padding: 24px 16px;
  text-align: center;
  background: var(--bg-sidebar-deep);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  font-size: 20px;
  margin-bottom: 4px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.logo-mini {
  padding: 20px 8px;
}

.logo-mini h2 {
  font-size: 24px;
  margin-bottom: 0;
}

.el-menu {
  border: none;
  background: transparent !important;
  flex: 1;
}

:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
  margin: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-menu--collapse .el-menu-item) {
  margin: 4px;
  padding: 0 20px !important;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background: #1890ff !important;
  color: #fff;
}

.collapse-btn {
  padding: 12px;
  text-align: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.65);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.collapse-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.el-header {
  background: var(--bg-header);
  box-shadow: var(--shadow-header);
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 10;
  transition: background 0.3s;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-content h3 {
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 500;
  transition: color 0.3s;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-btn {
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-regular);
  transition: all 0.3s;
}

.header-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.theme-btn {
  background: var(--bg-card);
}

.header-label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-left: 4px;
}

.el-main {
  background: var(--bg-primary);
  padding: 24px;
  overflow-y: auto;
  transition: background 0.3s;
}

@media (max-width: 768px) {
  .header-content h3 {
    font-size: 14px;
  }
  .header-label {
    display: none;
  }
  .el-main {
    padding: 12px;
  }
}
</style>
