<template>
  <header class="header" :class="{ scrolled: isScrolled }">
    <div class="logo" @click="goHome">
      <span class="logo-text">星空动漫</span>
    </div>
    <div class="tab-list">
      <div
        v-for="item in TabList"
        :key="item.id"
        class="tab-item"
        :class="{ active: isTabActive(item.content) }"
        @click="selectTab(item)"
      >
        {{ item.content }}
      </div>
    </div>
    <!-- “更多”下拉普通面板（非气泡） -->
    <div
      class="more-wrapper"
      :style="{ color: showMorePanel ? '#00ffff' : '#fff' }"
      @mouseenter="showMorePanel = true"
      @mouseleave="showMorePanel = false"
    >
      <div class="more">
        更多
        <el-icon v-if="showMorePanel"><ArrowUp /></el-icon>
        <el-icon v-else><ArrowDown /></el-icon>
      </div>
      <div class="panel" v-show="showMorePanel">
        <button class="el-btn" @click="goRank">排行榜</button>
        <button class="el-btn" @click="goCommunity">留言板</button>
        <button class="el-btn" @click="goProfile">我的追番</button>
        <!-- 隐藏区域（已按要求注释掉）
        <div class="el-btn" @click="goTonext">隐藏区域</div>
        -->
      </div>
    </div>
    <div class="header-end">
      <div class="tubiao">
        <div class="search-box">
          <el-icon class="search-icon" @click="onSearch"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="晴彩内容等你来看"
            class="search-input"
            @keyup.enter="onSearch"
          />
        </div>
        <ElIcon class="history-icon" title="观看历史" @click="goHistory"><Clock /></ElIcon>
        <!-- 已登录：头像下拉菜单 -->
        <ElDropdown v-if="userStore.isLoggedIn" trigger="click" @command="onCommand">
          <div class="avatar-btn" :title="userStore.username">
            {{ userStore.avatarText }}
          </div>
          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem command="profile">个人中心</ElDropdownItem>
              <ElDropdownItem command="logout" divided>退出登录</ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>
        <!-- 未登录：登录入口 -->
        <div v-else class="login-btn" @click="router.push('/login')">登录</div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ArrowDown, ArrowUp, Clock, Search } from '@element-plus/icons-vue'
import { ElDropdown, ElDropdownItem, ElDropdownMenu, ElIcon } from 'element-plus'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const TabList = ref([
  { id: 1, content: '日漫' },
  { id: 2, content: '国漫' },
  { id: 3, content: '欧美动漫' },
  { id: 4, content: '电影' },
])

const showMorePanel = ref(false)
const searchKeyword = ref('')

function goHome() {
  router.push('/home')
}
// 隐藏区域跳转（已按要求注释掉）
// function goTonext(){
//   router.push('/next')
// }
// tab 点击 → 跳首页并带分类
function selectTab(item) {
  router.push({ path: '/home', query: { ...route.query, category: item.content } })
}
const isTabActive = (content) => route.query.category === content

// 搜索 → 跳首页并带关键词
function onSearch() {
  const q = searchKeyword.value.trim()
  router.push({ path: '/home', query: { ...route.query, q } })
}

// 排行榜 / 历史记录 / 留言板 / 我的追番
function goRank() {
  router.push('/rank')
}
function goHistory() {
  router.push('/history')
}
function goCommunity() {
  router.push('/community')
}
function goProfile() {
  router.push('/profile')
}

// 头像下拉菜单
function onCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

// header 透明 → 实色：滚动超过 100px 就变成黑色实底
const isScrolled = ref(false)
const onScroll = () => {
  const top = window.scrollY || document.documentElement.scrollTop
  isScrolled.value = top > 100
}
onMounted(() => {
  window.addEventListener('scroll', onScroll)
  onScroll()
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 76px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.45), rgba(0, 0, 0, 0));
  display: flex;
  flex-direction: row;
  align-items: center;
  padding-left: 50px;
  padding-right: 24px;
  transition: background 0.3s;
}
.header.scrolled {
  background: rgba(0, 0, 0, 0.92);
  backdrop-filter: blur(10px);
}
/* logo 渐变文字 */
.logo {
  margin-right: 30px;
  cursor: pointer;
  user-select: none;
}
.logo-text {
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #00ffff, #ab6fff, #ff7eb3, #00ffff);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: logoFlow 4s linear infinite;
}
@keyframes logoFlow {
  to { background-position: 200% center; }
}
.tab-list {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
}
.tab-item {
  padding: 6px 14px;
  font-size: 15px;
  color: #ddd;
  cursor: pointer;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
}
.tab-item:hover {
  color: #00ffff;
}
.tab-item.active {
  color: #00ffff;
  background: rgba(0, 255, 255, 0.12);
  font-weight: 600;
}
.more-wrapper {
  position: relative;
  margin-left: 16px;
}
.more {
  width: 80px;
  font-size: 18px;
  cursor: pointer;
  user-select: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}
.panel {
  width: 380px;
  min-height: 60px;
  position: absolute;
  top: 100%;
  margin-top: 30px;
  background: rgba(10, 12, 18, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  left: 0;
  z-index: 999;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 10px 14px;
  gap: 6px;
}
.panel::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: -30px;
  height: 30px;
}
.el-btn {
  padding: 8px 14px;
  border: none;
  border-radius: 8px;
  color: #ddd;
  background: transparent;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s, background 0.2s;
}
.el-btn:hover {
  color: #00ffff;
  background: rgba(0, 255, 255, 0.1);
}
.header-end {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.tubiao {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 22px;
}
.search-box {
  position: relative;
  width: 200px;
}
.search-input {
  width: 100%;
  height: 32px;
  padding: 0 34px 0 14px;
  box-sizing: border-box;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.search-input:focus {
  border-color: #00ffff;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.25);
}
.search-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}
.search-icon {
  position: absolute;
  right: 12px;
  transform: translateY(-50%);
  top: 50%;
  color: #bbb;
  cursor: pointer;
  transition: color 0.2s;
}
.search-icon:hover {
  color: #00ffff;
}
.history-icon {
  font-size: 22px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.2s;
}
.history-icon:hover {
  color: #00ffff;
}
.avatar-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ab6fff, #ff7eb3);
  cursor: pointer;
  user-select: none;
  box-shadow: 0 0 12px rgba(171, 111, 255, 0.45);
  transition: transform 0.2s, box-shadow 0.2s;
}
.avatar-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 0 18px rgba(0, 255, 255, 0.55);
}
.login-btn {
  padding: 5px 16px;
  border-radius: 16px;
  font-size: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
  cursor: pointer;
  user-select: none;
  transition: background 0.2s, color 0.2s;
}
.login-btn:hover {
  background: #00ffff;
  color: #000;
}
</style>
