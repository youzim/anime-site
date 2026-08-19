<template>
  <div class="not-found">
    <div class="code">404</div>
    <h1 class="title">页面走丢了</h1>
    <p class="desc">你访问的页面不存在，或已被移动到别处。</p>
    <button class="back-btn" @click="goHome">返回首页</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/home')
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background:
    radial-gradient(600px 400px at 50% 30%, rgba(171, 111, 255, 0.2), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
  color: #fff;
  text-align: center;
}
.code {
  font-size: 120px;
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(135deg, #00ffff, #ab6fff, #ff7eb3);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: float 3s ease-in-out infinite;
}
.title {
  margin: 0;
  font-size: 26px;
}
.desc {
  margin: 0;
  color: #888;
  font-size: 14px;
}
.back-btn {
  margin-top: 16px;
  padding: 10px 30px;
  font-size: 15px;
  font-weight: bold;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 255, 255, 0.4);
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}
</style>
