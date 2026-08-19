<template>
  <RouterView />
</template>

<script setup>
</script>

<style>
/* 全局基础样式：统一深色主题 + 交互色 */
:root {
  --ds-accent: #00ffff;
  --ds-accent-soft: rgba(0, 255, 255, 0.35);
  --ds-purple: #ab6fff;
  --ds-pink: #ff7eb3;
  --ds-bg-top: #0b0d12;
  --ds-bg-bottom: #05060a;
  --ds-card-bg: rgba(255, 255, 255, 0.06);
  --ds-card-border: rgba(255, 255, 255, 0.12);
  --ds-text: #fff;
  --ds-text-dim: #888;
}

body {
  margin: 0;
  overflow-x: hidden;
  font-family: 'Quicksand', 'PingFang SC', 'Microsoft YaHei', 'Segoe UI', sans-serif;
  background: linear-gradient(180deg, var(--ds-bg-top), var(--ds-bg-bottom));
  color: var(--ds-text);
  -webkit-font-smoothing: antialiased;
}

/* 全站统一滚动条（暗色） */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: #0a0c10;
}
::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 255, 255, 0.45);
}

/* 文本选中 */
::selection {
  background: rgba(0, 255, 255, 0.35);
  color: #000;
}

/* Element Plus 暗色弹层跟随主题 */
.el-popper.is-light {
  background: #14161c !important;
  border-color: rgba(255, 255, 255, 0.12) !important;
}
</style>
