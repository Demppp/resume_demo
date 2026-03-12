<template>
  <div id="app" :class="{ 'dark-mode': isDark }">
    <router-view />
    <AIRobot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AIRobot from './components/AIRobot.vue'
import { useThemeStore } from './stores/theme'

const themeStore = useThemeStore()
const isDark = computed(() => themeStore.isDark)
</script>

<style>
:root {
  /* Light theme variables */
  --bg-primary: #f0f2f5;
  --bg-card: #ffffff;
  --bg-sidebar: #001529;
  --bg-sidebar-deep: #002140;
  --bg-header: #ffffff;
  --bg-hover: #f5f7fa;
  --bg-table-header: #fafafa;
  --bg-table-stripe: #fafafa;
  --bg-input: #ffffff;
  --bg-dialog: #ffffff;
  --bg-popover: #ffffff;
  --bg-tag: #ecf5ff;
  --bg-skeleton: #f2f2f2;

  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;
  --text-white: #ffffff;

  --border-color: #dcdfe6;
  --border-light: #e4e7ed;
  --border-lighter: #ebeef5;

  --shadow-card: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-header: 0 1px 4px rgba(0, 21, 41, 0.08);
  --shadow-sidebar: 2px 0 8px rgba(0, 0, 0, 0.15);

  --color-primary: #409eff;
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  --stat-icon-bg-1: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --stat-icon-bg-2: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --stat-icon-bg-3: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  --stat-icon-bg-4: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

[data-theme="dark"] {
  --bg-primary: #141414;
  --bg-card: #1f1f1f;
  --bg-sidebar: #0d1117;
  --bg-sidebar-deep: #010409;
  --bg-header: #1f1f1f;
  --bg-hover: #2a2a2a;
  --bg-table-header: #262626;
  --bg-table-stripe: #1a1a1a;
  --bg-input: #2a2a2a;
  --bg-dialog: #1f1f1f;
  --bg-popover: #2a2a2a;
  --bg-tag: rgba(64, 158, 255, 0.15);
  --bg-skeleton: #2a2a2a;

  --text-primary: #e5eaf3;
  --text-regular: #cfd3dc;
  --text-secondary: #a3a6ad;
  --text-placeholder: #6c6e72;
  --text-white: #ffffff;

  --border-color: #4c4d4f;
  --border-light: #414243;
  --border-lighter: #363637;

  --shadow-card: 0 2px 12px rgba(0, 0, 0, 0.3);
  --shadow-header: 0 1px 4px rgba(0, 0, 0, 0.3);
  --shadow-sidebar: 2px 0 8px rgba(0, 0, 0, 0.4);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: background 0.3s, color 0.3s;
}

/* Global dark mode overrides for Element Plus */
[data-theme="dark"] .el-card {
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

[data-theme="dark"] .el-table {
  background: var(--bg-card);
  color: var(--text-primary);
}

[data-theme="dark"] .el-table tr {
  background: var(--bg-card);
}

[data-theme="dark"] .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: var(--bg-table-stripe);
}

[data-theme="dark"] .el-table th.el-table__cell {
  background: var(--bg-table-header) !important;
  color: var(--text-primary);
}

[data-theme="dark"] .el-table td.el-table__cell {
  border-color: var(--border-lighter);
}

[data-theme="dark"] .el-table__inner-wrapper::before {
  background: var(--border-lighter);
}

[data-theme="dark"] .el-table--border .el-table__inner-wrapper::after {
  background: var(--border-lighter);
}

[data-theme="dark"] .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: var(--bg-hover);
}

[data-theme="dark"] .el-input__wrapper {
  background: var(--bg-input);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

[data-theme="dark"] .el-input__inner {
  color: var(--text-primary);
}

[data-theme="dark"] .el-select .el-input__wrapper {
  background: var(--bg-input);
}

[data-theme="dark"] .el-dialog {
  background: var(--bg-dialog);
  border-color: var(--border-color);
}

[data-theme="dark"] .el-dialog__header {
  color: var(--text-primary);
}

[data-theme="dark"] .el-dialog__title {
  color: var(--text-primary);
}

[data-theme="dark"] .el-form-item__label {
  color: var(--text-primary);
}

[data-theme="dark"] .el-pagination {
  color: var(--text-primary);
}

[data-theme="dark"] .el-pagination button {
  background: var(--bg-card);
  color: var(--text-primary);
}

[data-theme="dark"] .el-pager li {
  background: var(--bg-card);
  color: var(--text-primary);
}

[data-theme="dark"] .el-tabs__item {
  color: var(--text-secondary);
}

[data-theme="dark"] .el-tabs__item.is-active {
  color: var(--color-primary);
}

[data-theme="dark"] .el-alert {
  border-color: var(--border-color);
}

[data-theme="dark"] .el-menu {
  border-color: var(--border-color);
}

[data-theme="dark"] .el-textarea__inner {
  background: var(--bg-input);
  color: var(--text-primary);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

[data-theme="dark"] .el-select-dropdown {
  background: var(--bg-card);
  border-color: var(--border-color);
}

[data-theme="dark"] .el-select-dropdown__item {
  color: var(--text-primary);
}

[data-theme="dark"] .el-select-dropdown__item.hover {
  background: var(--bg-hover);
}

[data-theme="dark"] .el-popover.el-popper {
  background: var(--bg-popover);
  border-color: var(--border-color);
  color: var(--text-primary);
}

[data-theme="dark"] .el-date-picker {
  background: var(--bg-card);
}

[data-theme="dark"] .el-picker-panel {
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

[data-theme="dark"] ::-webkit-scrollbar-track {
  background: #2a2a2a;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

[data-theme="dark"] ::-webkit-scrollbar-thumb {
  background: #555;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

[data-theme="dark"] ::-webkit-scrollbar-thumb:hover {
  background: #777;
}
</style>
