<template>
  <div class="Layout_Page">
    <HeaderView />
    <main class="main">
      <RouterView />
    </main>
    <footer class="footer">
      <div class="footer-links">
        <span>关于我们</span>
        <span>联系我们</span>
        <span>帮助中心</span>
        <span>服务条款</span>
        <span>隐私政策</span>
      </div>
      <div class="footer-copyright">© 2026 星空动漫 版权所有 · 京ICP备XXXXXXXX号</div>
    </footer>
  </div>
</template>

<script setup>
import HeaderView from '@/components/HeaderView.vue'
</script>

<style scoped>
.Layout_Page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main {
  flex: 1;      /* 关键：flex:1 放在 <main> 这个直接子元素上 */
  width: 100%;
}
.footer {
  min-height: 80px;
  background: linear-gradient(180deg, #0a0c10, #05060a);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: #999;
  font-size: 13px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 0;
  box-sizing: border-box;
}
.footer-links {
  display: flex;
  gap: 24px;
}
.footer-links span {
  cursor: pointer;
  transition: color 0.2s;
}
.footer-links span:hover {
  color: #00ffff;                 /* hover 青色，和全站交互色统一 */
}
.footer-copyright {
  opacity: 0.8;
}
</style>
