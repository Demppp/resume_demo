<template>
  <el-popover
    placement="bottom-end"
    :width="360"
    trigger="click"
    popper-class="notification-popover"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <el-button circle size="small" class="header-btn">
          <el-icon :size="16"><Bell /></el-icon>
        </el-button>
      </el-badge>
    </template>
    <div class="notification-header">
      <span class="notification-title">通知</span>
      <el-button type="primary" link size="small" @click="markAllRead" v-if="unreadCount > 0">
        全部已读
      </el-button>
    </div>
    <el-scrollbar max-height="400px">
      <div v-if="notifications.length === 0" class="notification-empty">
        <el-empty description="暂无通知" :image-size="60" />
      </div>
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ 'is-unread': !item.isRead }"
        @click="handleClick(item)"
      >
        <div class="notification-icon">
          <el-icon :size="20" :color="getTypeColor(item.type)">
            <WarningFilled v-if="item.type === 'warning'" />
            <InfoFilled v-else-if="item.type === 'info'" />
            <CircleCheckFilled v-else />
          </el-icon>
        </div>
        <div class="notification-content">
          <div class="notification-msg">{{ item.title }}</div>
          <div class="notification-time">{{ item.createdTime }}</div>
        </div>
      </div>
    </el-scrollbar>
  </el-popover>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { Bell, WarningFilled, InfoFilled, CircleCheckFilled } from '@element-plus/icons-vue'

const notifications = ref([])
const unreadCount = ref(0)

async function loadNotifications() {
  try {
    const res = await getNotificationList({ pageNum: 1, pageSize: 20 })
    if (res.code === 200) {
      notifications.value = res.data.records || []
    }
    const countRes = await getUnreadCount()
    if (countRes.code === 200) {
      unreadCount.value = countRes.data.count || 0
    }
  } catch (e) {
    // Notification service may not be ready yet
  }
}

function markAllRead() {
  markAllAsRead().then(() => {
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
  }).catch(() => {})
}

function handleClick(item) {
  if (!item.isRead) {
    markAsRead(item.id).catch(() => {})
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

function getTypeColor(type) {
  const map = { warning: '#e6a23c', danger: '#f56c6c', info: '#909399', success: '#67c23a' }
  return map[type] || '#409eff'
}

onMounted(() => {
  loadNotifications()
  // 轮询
  setInterval(loadNotifications, 60000)
})
</script>

<style scoped>
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 12px;
  border-bottom: 1px solid var(--border-lighter, #ebeef5);
  margin-bottom: 8px;
}

.notification-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary, #303133);
}

.notification-empty {
  padding: 20px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: var(--bg-hover, #f5f7fa);
}

.notification-item.is-unread {
  background: var(--bg-tag, #ecf5ff);
}

.notification-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.notification-msg {
  font-size: 13px;
  color: var(--text-primary, #303133);
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: var(--text-secondary, #909399);
  margin-top: 4px;
}

.header-btn {
  border: 1px solid var(--border-color, #dcdfe6);
  background: var(--bg-card, #fff);
  color: var(--text-regular, #606266);
  transition: all 0.3s;
}

.header-btn:hover {
  border-color: var(--color-primary, #409eff);
  color: var(--color-primary, #409eff);
}
</style>
