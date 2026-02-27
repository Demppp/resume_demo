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
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-content">
            <h3>广州市高三年级管理系统</h3>
            <div class="header-info">
              <span>年级主任管理平台</span>
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
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeMenu = ref('/dashboard')

// 初始化时跳转到dashboard
if (route.path === '/home' || route.path === '/') {
  router.push('/dashboard')
}

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })
</script>

<style scoped>
.home-container {
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

.el-container {
  height: 100%;
}

.el-aside {
  background: #001529;
  color: #fff;
  overflow-x: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo {
  padding: 24px 16px;
  text-align: center;
  background: #002140;
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

.el-menu {
  border: none;
  background: #001529;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
  margin: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background: #1890ff !important;
  color: #fff;
}

.el-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-content h3 {
  font-size: 18px;
  color: #001529;
  font-weight: 500;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-info span {
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}

.el-main {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>

