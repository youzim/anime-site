<template>
  <div class="profile">
    <div class="profile__inner">
      <!-- 用户信息卡：头像 + 资料 + 统计 -->
      <div class="user-card">
        <div class="user-avatar">{{ userStore.avatarText }}</div>
        <div class="user-meta">
          <h2 class="user-name">{{ userStore.username || '未登录' }}</h2>
          <p class="user-id">用户 ID：{{ userStore.userInfo?.id ?? '-' }}</p>
          <p class="user-joined" v-if="stats.createdAt">注册于 {{ formatDate(stats.createdAt) }}</p>
        </div>
      </div>

      <!-- 数据统计 -->
      <div class="stats-card">
        <div class="stat-item" @click="goHistory">
          <span class="stat-num">{{ stats.historyCount }}</span>
          <span class="stat-label">观看历史</span>
        </div>
        <div class="stat-item" @click="scrollTo('fav')">
          <span class="stat-num">{{ stats.favoriteCount }}</span>
          <span class="stat-label">追番</span>
        </div>
        <div class="stat-item" @click="scrollTo('comment')">
          <span class="stat-num">{{ stats.commentCount }}</span>
          <span class="stat-label">评论</span>
        </div>
        <div class="stat-item" @click="goCommunity">
          <span class="stat-num">{{ stats.postCount }}</span>
          <span class="stat-label">发帖</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.danmakuCount }}</span>
          <span class="stat-label">弹幕</span>
        </div>
      </div>

      <!-- 最近观看 -->
      <div class="panel">
        <div class="panel__head">
          <h3 class="panel__title">最近观看</h3>
          <button v-if="recentHistory.length" class="panel__more" @click="goHistory">查看全部 →</button>
        </div>
        <div v-if="recentHistory.length" class="recent-list">
          <div class="recent-item" v-for="h in recentHistory" :key="h.animeId" @click="continueWatch(h)">
            <div class="recent-cover">
              <img :src="h.cover" :alt="h.title" />
            </div>
            <div class="recent-main">
              <span class="recent-name">{{ h.title }}</span>
              <span class="recent-cat">{{ h.category }} · 看到第 {{ h.ep }} 集</span>
              <span class="recent-time">{{ formatTime(h.updatedAt) }}</span>
            </div>
            <button class="recent-play" @click.stop="continueWatch(h)">
              <el-icon><CaretRight /></el-icon>
              继续观看
            </button>
          </div>
        </div>
        <div v-else class="panel__empty">还没有观看记录，去首页逛逛吧～</div>
      </div>

      <!-- 我的追番 -->
      <div class="panel" ref="favRef">
        <div class="panel__head">
          <h3 class="panel__title">我的追番 <span class="fav-count">{{ favorites.length }}</span></h3>
          <button v-if="favorites.length" class="panel__more" @click="goHome">去逛逛 →</button>
        </div>
        <div v-if="favorites.length" class="fav-grid">
          <div class="fav-item" v-for="f in favorites" :key="f.animeId" @click="goDetail(f.animeId)">
            <div class="fav-img">
              <img :src="f.cover" :alt="f.title" />
              <span class="fav-rating">{{ f.rating }}</span>
              <button class="fav-del" title="取消追番" @click.stop="unfavorite(f.animeId)">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div class="fav-info">
              <span class="fav-title">{{ f.title }}</span>
              <span class="fav-category">{{ f.category }}</span>
            </div>
          </div>
        </div>
        <div v-else class="panel__empty">
          还没有追番，去首页逛逛吧～
          <button class="fav-go" @click="goHome">去逛逛</button>
        </div>
      </div>

      <!-- 我的评论 -->
      <div class="panel">
        <div class="panel__head">
          <h3 class="panel__title">我的评论 <span class="fav-count">{{ myComments.length }}</span></h3>
        </div>
        <div v-if="myComments.length" class="comment-list">
          <div class="comment-item" v-for="c in myComments" :key="c.id" @click="goDetail(c.animeId)">
            <div class="comment-content">{{ c.content }}</div>
            <div class="comment-meta">
              <span class="comment-anime">{{ c.animeTitle }}</span>
              <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
              <span class="comment-likes">👍 {{ c.likes }}</span>
            </div>
          </div>
        </div>
        <div v-else class="panel__empty">还没有发表过评论</div>
      </div>

      <!-- 修改用户名 -->
      <div class="panel">
        <h3 class="panel__title">修改用户名</h3>
        <p class="panel__desc">新的用户名将同步到后端数据库</p>
        <div class="form-row">
          <el-input
            v-model="nameForm.username"
            placeholder="请输入新的用户名（2-16 个字符）"
            maxlength="16"
            clearable
            class="form-input"
          />
          <el-button type="primary" class="form-btn" :loading="nameLoading" @click="saveName">
            保存
          </el-button>
        </div>
      </div>

      <!-- 修改密码 -->
      <div class="panel">
        <h3 class="panel__title">修改密码</h3>
        <p class="panel__desc">修改成功后需要重新登录</p>
        <el-form label-position="top" class="pwd-form">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </el-form>
        <el-button type="primary" class="form-btn" :loading="pwdLoading" @click="savePassword">
          修改密码
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { CaretRight, Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  deleteHistoryRecord,
  getComments,
  getFavorites,
  getHistory,
  getUserInfo,
  getUserStats,
  toggleFavorite,
  updatePassword,
  updateUserInfo
} from '@/api/api'

const router = useRouter()
const userStore = useUserStore()

const nameForm = reactive({ username: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const nameLoading = ref(false)
const pwdLoading = ref(false)

// 统计
const stats = ref({ favoriteCount: 0, historyCount: 0, commentCount: 0, postCount: 0, danmakuCount: 0, createdAt: '' })
// 追番 / 历史 / 我的评论
const favorites = ref([])
const recentHistory = ref([])
const myComments = ref([])
const favRef = ref(null)

onMounted(async () => {
  try {
    const info = await getUserInfo()
    userStore.setUserInfo(info)
    nameForm.username = info.username
  } catch {}
  await Promise.all([loadStats(), loadFavorites(), loadHistory(), loadMyComments()])
})

async function loadStats() {
  try {
    stats.value = await getUserStats()
  } catch {}
}
async function loadFavorites() {
  try {
    favorites.value = await getFavorites()
  } catch {}
}
async function loadHistory() {
  try {
    recentHistory.value = (await getHistory()).slice(0, 5)
  } catch {}
}
async function loadMyComments() {
  try {
    // 后端没有"我的评论"接口：取每部番剧评论太重，先展示最近追番番剧的评论（含自己），或留空引导
    // 简化：直接显示 0，等后端补接口。这里从追番番剧里查自己发的评论
    const favs = await getFavorites()
    const all = []
    for (const f of favs.slice(0, 10)) {
      try {
        const cms = await getComments(f.animeId)
        cms
          .filter((c) => c.username === userStore.username)
          .forEach((c) => all.push({ ...c, animeId: f.animeId, animeTitle: f.title }))
      } catch {}
    }
    myComments.value = all.sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt))).slice(0, 10)
  } catch {}
}

// 取消追番
async function unfavorite(animeId) {
  try {
    await toggleFavorite(animeId)
    favorites.value = favorites.value.filter((f) => f.animeId !== animeId)
    ElMessage.success('已取消追番')
    loadStats()
  } catch {}
}

// 继续观看
function continueWatch(h) {
  router.push({ path: `/player/${h.animeId}`, query: { ep: h.ep } })
}

// 时间/日期格式化
function formatDate(ts) {
  if (!ts) return ''
  const d = new Date(String(ts).includes('T') ? ts : String(ts).replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(ts).slice(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(String(ts).includes('T') ? ts : String(ts).replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(ts).slice(0, 16)
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const sameDay = d.toDateString() === now.toDateString()
  const yesterday = new Date(now.getTime() - 86400000)
  const isYesterday = d.toDateString() === yesterday.toDateString()
  if (sameDay) return `今天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (isYesterday) return `昨天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}
function goHome() {
  router.push('/home')
}
function goHistory() {
  router.push('/history')
}
function goCommunity() {
  router.push('/community')
}
function scrollTo(which) {
  if (which === 'fav') favRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function saveName() {
  const username = nameForm.username.trim()
  if (!username) {
    ElMessage.error('用户名不能为空')
    return
  }
  if (username.length < 2 || username.length > 16) {
    ElMessage.error('用户名需为 2-16 个字符')
    return
  }
  nameLoading.value = true
  try {
    const info = await updateUserInfo({ username })
    userStore.setUserInfo(info)
    nameForm.username = info.username
    ElMessage.success('用户名修改成功')
  } catch {
  } finally {
    nameLoading.value = false
  }
}

async function savePassword() {
  const { oldPassword, newPassword, confirmPassword } = pwdForm
  if (!oldPassword || !newPassword) {
    ElMessage.error('请填写完整')
    return
  }
  if (newPassword.length < 6) {
    ElMessage.error('新密码至少 6 位')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  pwdLoading.value = true
  try {
    await updatePassword({ oldPassword, newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch {
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.profile {
  min-height: 100vh;
  padding: 110px 20px 40px;
  box-sizing: border-box;
  background:
    radial-gradient(600px 400px at 15% 25%, rgba(0, 255, 255, 0.12), transparent 65%),
    radial-gradient(700px 500px at 85% 70%, rgba(171, 111, 255, 0.16), transparent 65%),
    radial-gradient(500px 400px at 60% 15%, rgba(255, 126, 179, 0.1), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
  display: flex;
  justify-content: center;
}
.profile__inner {
  width: 100%;
  max-width: 640px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
/* 用户信息卡 */
.user-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(171, 111, 255, 0.18), rgba(255, 126, 179, 0.12));
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.45);
}
.user-avatar {
  width: 76px;
  height: 76px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ab6fff, #ff7eb3);
  box-shadow: 0 0 24px rgba(171, 111, 255, 0.5);
}
.user-name {
  margin: 0 0 4px;
  font-size: 24px;
  color: #fff;
}
.user-id {
  margin: 0 0 4px;
  font-size: 13px;
  color: #aaa;
}
.user-joined {
  margin: 0;
  font-size: 12px;
  color: #888;
}

/* 统计卡 */
.stats-card {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}
.stat-item {
  padding: 16px 8px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.25s;
}
.stat-item:hover {
  background: rgba(0, 255, 255, 0.1);
  border-color: rgba(0, 255, 255, 0.4);
  transform: translateY(-3px);
}
.stat-num {
  font-size: 24px;
  font-weight: 800;
  color: #00ffff;
}
.stat-label {
  font-size: 12px;
  color: #aaa;
}

/* 面板 */
.panel {
  padding: 22px 26px 26px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.45);
}
.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.panel__title {
  margin: 0;
  font-size: 18px;
  color: #fff;
}
.panel__more {
  font-size: 13px;
  color: #00ffff;
  background: transparent;
  border: none;
  cursor: pointer;
}
.panel__more:hover {
  text-decoration: underline;
}
.panel__desc {
  margin: 0 0 18px;
  font-size: 13px;
  color: #888;
}
.panel__empty {
  padding: 16px 0;
  text-align: center;
  font-size: 13px;
  color: #888;
}

/* 最近观看 */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.recent-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: all 0.25s;
}
.recent-item:hover {
  border-color: rgba(0, 255, 255, 0.4);
  background: rgba(0, 255, 255, 0.06);
}
.recent-cover {
  width: 56px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 3 / 4;
}
.recent-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.recent-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.recent-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-cat {
  font-size: 12px;
  color: #888;
}
.recent-time {
  font-size: 11px;
  color: #666;
}
.recent-play {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  flex-shrink: 0;
}
.recent-play:hover {
  opacity: 0.85;
}

/* 追番 */
.fav-count {
  font-size: 13px;
  color: #00ffff;
  margin-left: 6px;
}
.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.fav-item {
  border-radius: 10px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: transform 0.25s, border-color 0.25s, box-shadow 0.25s;
}
.fav-item:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 255, 255, 0.45);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.5);
}
.fav-img {
  position: relative;
  aspect-ratio: 3 / 4;
  overflow: hidden;
}
.fav-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.fav-rating {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 1px 7px;
  font-size: 12px;
  font-weight: 700;
  color: #ffd86b;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 6px;
}
.fav-del {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}
.fav-item:hover .fav-del {
  opacity: 1;
}
.fav-del:hover {
  background: #ff5c7a;
}
.fav-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 8px 10px;
}
.fav-title {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fav-category {
  font-size: 11px;
  color: #888;
}
.fav-go {
  display: block;
  margin: 12px auto 0;
  padding: 7px 22px;
  font-size: 13px;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 18px;
  cursor: pointer;
}
.fav-go:hover {
  opacity: 0.85;
}

/* 我的评论 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.comment-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: all 0.25s;
}
.comment-item:hover {
  border-color: rgba(0, 255, 255, 0.4);
}
.comment-content {
  font-size: 13px;
  color: #ddd;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.comment-meta {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: #888;
}
.comment-anime {
  color: #00ffff;
}
.comment-likes {
  color: #ffd86b;
}

/* 表单 */
.form-row {
  display: flex;
  gap: 12px;
}
.form-input {
  flex: 1;
}
.form-btn {
  min-width: 96px;
  background: linear-gradient(135deg, #ab6fff, #ff7eb3);
  border: none;
}
.form-btn:hover {
  opacity: 0.9;
}
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.07);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.14) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #00ffff inset, 0 0 12px rgba(0, 255, 255, 0.25);
}
:deep(.el-input__inner) {
  color: #fff;
}
:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.35);
}
:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 600px) {
  .stats-card {
    grid-template-columns: repeat(3, 1fr);
  }
  .fav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
