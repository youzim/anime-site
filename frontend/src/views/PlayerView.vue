<template>
  <div class="player">
    <!-- 顶部 -->
    <div class="player-top">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </button>
      <div class="player-title">
        <span class="title">{{ anime?.title }}</span>
        <span class="ep-label">{{ epLabel }}</span>
      </div>
      <div class="top-actions">
        <span class="src-label">片源：直链正片</span>
        <button class="src-btn" @click="switchSource">
          换源 {{ sourceIndex + 1 }}/{{ sourcePool.length }}
        </button>
      </div>
    </div>

    <div class="player-body">
      <!-- 播放区 -->
      <div class="video-area">
        <div class="video-box">
          <!-- 加载中占位：保持 16:9 稳定布局 -->
          <div v-if="!anime" class="player-loading">
            <el-icon class="loading-icon" :class="{ spin: true }"><Loading /></el-icon>
            <span>视频加载中…</span>
          </div>

          <!-- 直链长视频播放器（HTML5 + 本站弹幕） -->
          <DanPlayer
            v-else
            ref="playerRef"
            :key="'ep-' + ep + '-src-' + sourceIndex"
            :src="directUrl"
            :comments="cclComments"
            :autoplay-on-comment-load="false"
            :additional-functions="['picture-in-picture']"
            show-comment-sender
            locale="zh"
            @send-comment="onSendComment"
          />
        </div>
        <p class="player-tip">
          正片直链为可播放的长视频（公版动画/开源电影，完整正片），选集切换自动换集 · 支持本站弹幕 · 卡顿可点「换源」切换线路
        </p>
      </div>

      <!-- 选集面板 -->
      <div class="ep-panel">
        <h3 class="ep-title">选集</h3>
        <div class="ep-grid">
          <button
            v-for="n in epList"
            :key="n"
            class="ep-btn"
            :class="{ active: n === ep }"
            @click="goEp(n)"
          >
            {{ episodes > 1 ? '第' + n + '集' : '正片' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 猜你喜欢 -->
    <div class="recommend" v-if="relatedList.length">
      <h3 class="recommend-title">猜你喜欢</h3>
      <div class="recommend-row">
        <div class="recommend-card" v-for="item in relatedList" :key="item.id" @click="goDetail(item.id)">
          <div class="recommend-img">
            <img :src="item.cover" :alt="item.title" />
            <span class="recommend-rating">{{ item.rating }}</span>
          </div>
          <div class="recommend-info">
            <span class="recommend-name">{{ item.title }}</span>
            <span class="recommend-cat">{{ item.category }} · {{ item.year }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { DanPlayer } from '@wiidede/dan-player'
import '@wiidede/dan-player/index.css'
import { addHistoryRecord, getAnimeDetail, getDanmaku, getHistory, getRelated, sendDanmaku } from '@/api/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const anime = ref(null)
const ep = ref(Number(route.query.ep) || 1)
const episodes = computed(() => anime.value?.episodes ?? 1)
const epLabel = computed(() => (episodes.value > 1 ? `第 ${ep.value} 集` : '正片'))
// 选集列表
const epList = computed(() => Array.from({ length: episodes.value }, (_, i) => i + 1))

const playerRef = ref(null)

// ---------- 直链长视频源池（可播放的完整长视频，公版动画/开源电影） ----------
const DIRECT_SOURCES = [
  'https://upload.wikimedia.org/wikipedia/commons/e/eb/%E5%B0%8F%E8%9D%8C%E8%9A%AA%E6%89%BE%E5%A6%88%E5%A6%88_1960.webm',
  'https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm',
  'https://upload.wikimedia.org/wikipedia/commons/c/cb/Tears_of_Steel_1080p.webm',
  'https://upload.wikimedia.org/wikipedia/commons/0/02/Elephants_Dream%28HQ%29.webm',
  'https://media.w3.org/2010/05/video/movie_300.mp4',
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4'
]

const sourceIndex = ref(0)
// 源池 = 该番剧配置的正片直链（第1位，主题最贴近） + 通用长视频池
const sourcePool = computed(() => {
  const pool = []
  if (anime.value?.videoUrl) pool.push(anime.value.videoUrl)
  DIRECT_SOURCES.forEach((u) => {
    if (!pool.includes(u)) pool.push(u)
  })
  return pool
})
// 当前集直链：按集数轮换（切集=换集，保证每集可播），换源再偏移
const directUrl = computed(() => {
  if (!sourcePool.value.length) return ''
  const base = (ep.value - 1 + sourceIndex.value) % sourcePool.value.length
  return sourcePool.value[base]
})

function switchSource() {
  sourceIndex.value = (sourceIndex.value + 1) % sourcePool.value.length
  ElMessage.info(`已切换源 ${sourceIndex.value + 1}/${sourcePool.value.length}`)
}

// ---------- 本站弹幕 ----------
const danmakuList = ref([])
const cclComments = computed(() =>
  danmakuList.value.map((d) => ({
    text: d.text,
    stime: Number(d.time) || 0,
    color: parseInt(String(d.color).replace('#', ''), 16) || 0xffffff,
    mode: 0,
    size: 25,
    self: false
  }))
)

async function onSendComment(text) {
  const content = String(text || '').trim()
  if (!content) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再发弹幕')
    router.push('/login')
    return
  }
  const currentTimeRef = playerRef.value?.currentTime
  const time = typeof currentTimeRef === 'number' ? currentTimeRef : (currentTimeRef?.value ?? 0)
  try {
    await sendDanmaku({ animeId: anime.value.id, ep: ep.value, time, text: content, color: '#ffffff' })
    playerRef.value?.addSelfComment?.(content)
  } catch {}
}

async function loadDanmaku() {
  try {
    danmakuList.value = await getDanmaku(anime.value.id, ep.value)
  } catch {
    danmakuList.value = []
  }
}

// ---------- 观看历史（进度上报） ----------
let progressTimer = null
let lastProgress = 0

function reportHistory(progress = 0) {
  if (!userStore.isLoggedIn || !anime.value) return
  addHistoryRecord({ animeId: anime.value.id, ep: ep.value, progress }).catch(() => {})
}

function startProgressReport() {
  stopProgressReport()
  progressTimer = setInterval(() => {
    if (playerRef.value?.currentTime != null) {
      const t =
        typeof playerRef.value.currentTime === 'number'
          ? playerRef.value.currentTime
          : (playerRef.value.currentTime?.value ?? 0)
      if (t - lastProgress >= 10) {
        lastProgress = t
        reportHistory(Math.round(t * 10) / 10)
      }
    }
  }, 10000)
}
function stopProgressReport() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

// ---------- 选集 / 导航 ----------
function goEp(n) {
  if (n === ep.value) return
  router.push({ path: `/player/${anime.value.id}`, query: { ep: n } })
}

function goBack() {
  if (window.history.state?.back) {
    router.back()
  } else {
    router.replace(`/detail/${route.params.id}`)
  }
}

// 猜你喜欢（同分类推荐）
const relatedList = ref([])
async function loadRelated() {
  try {
    relatedList.value = await getRelated(anime.value.id)
  } catch {
    relatedList.value = []
  }
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

// ---------- 加载 ----------
async function loadData() {
  ep.value = Number(route.query.ep) || 1
  sourceIndex.value = 0
  try {
    anime.value = await getAnimeDetail(route.params.id)
    await loadDanmaku()
    reportHistory(0)
    startProgressReport()
    resumeEpisode()
    loadRelated()
  } catch {}
}

// 续播：用户没指定集数时，续到上次看的集
async function resumeEpisode() {
  if (!userStore.isLoggedIn) return
  try {
    const history = await getHistory()
    const rec = (history || []).find((h) => h.animeId === anime.value.id)
    if (!rec) return
    if (!route.query.ep && rec.ep > 1) {
      ep.value = rec.ep
      router.replace({ path: `/player/${anime.value.id}`, query: { ep: rec.ep } })
      loadDanmaku()
    }
    // 直链可读进度：上次进度 > 5 秒时跳转续播
    const progress = Number(rec.progress) || 0
    lastProgress = progress
    if (progress > 5) seekTo(progress)
  } catch {}
}

function seekTo(seconds) {
  let tries = 0
  const timer = setInterval(() => {
    tries++
    const ct = playerRef.value?.currentTime
    if (typeof ct === 'number') {
      playerRef.value.currentTime = seconds
      clearInterval(timer)
    } else if (ct && typeof ct.value === 'number') {
      ct.value = seconds
      clearInterval(timer)
    } else if (tries > 30) {
      clearInterval(timer)
    }
  }, 500)
}

// 选集切换 → 重载该集弹幕 + 上报历史（直链源随集数轮换自动换集）
watch(
  () => route.query.ep,
  () => {
    if (!anime.value) return
    const next = Number(route.query.ep) || 1
    if (next !== ep.value) {
      ep.value = next
      lastProgress = 0
      loadDanmaku()
      reportHistory(0)
    }
  }
)

onMounted(() => {
  loadData()
})

onBeforeUnmount(() => {
  stopProgressReport()
  reportHistory(lastProgress)
})
</script>

<style scoped>
.player {
  min-height: 100vh;
  padding: 90px 30px 50px;
  box-sizing: border-box;
  background:
    radial-gradient(700px 500px at 85% 20%, rgba(171, 111, 255, 0.14), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
}

/* 顶部 */
.player-top {
  max-width: 1200px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  font-size: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s;
}

.back-btn:hover {
  background: #00ffff;
  color: #000;
  border-color: #00ffff;
}

.player-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.player-title .title {
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.player-title .ep-label {
  flex-shrink: 0;
  font-size: 14px;
  color: #00ffff;
  background: rgba(0, 255, 255, 0.12);
  padding: 3px 12px;
  border-radius: 12px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.src-label {
  font-size: 13px;
  color: #00ffff;
  background: rgba(0, 255, 255, 0.1);
  padding: 5px 12px;
  border-radius: 14px;
}

.src-btn {
  padding: 6px 14px;
  font-size: 13px;
  color: #ddd;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s;
}

.src-btn:hover {
  color: #00ffff;
  border-color: #00ffff;
}

.player-body {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 240px;
  gap: 20px;
  align-items: start;
}

/* 播放区 */
.video-area {
  min-width: 0;
}

.video-box {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.6);
  /* 关键修复：让子元素居中且不撑破比例 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 关键修复：video-box 的直接子元素全部绝对填满 */
.video-box > * {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

/* 深度穿透 DanPlayer 及其内部 video，强制填满 */
.video-box :deep(.dan-player-root),
.video-box :deep(.dan-player-container),
.video-box :deep(video) {
  width: 100% !important;
  height: 100% !important;
}

.video-box :deep(video) {
  object-fit: contain;   /* 改成 cover 如果需要裁切铺满 */
  display: block;
}

/* 加载占位：稳定 16:9，避免视频未加载时布局塌陷 */
.player-loading {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #888;
  font-size: 14px;
  background: #05060a;
}

.loading-icon {
  font-size: 34px;
  color: #00ffff;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.player-tip {
  margin: 10px 2px 0;
  font-size: 12px;
  color: #666;
}

/* 选集 */
.ep-panel {
  padding: 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.ep-title {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.ep-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  max-height: 420px;
  overflow-y: auto;
}

.ep-btn {
  padding: 9px 0;
  font-size: 13px;
  color: #ccc;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.ep-btn:hover {
  color: #fff;
  border-color: rgba(0, 255, 255, 0.5);
}

.ep-btn.active {
  color: #000;
  background: #00ffff;
  border-color: #00ffff;
  font-weight: 700;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.35);
}

@media (max-width: 900px) {
  .player-body {
    grid-template-columns: 1fr;
  }
  .ep-panel {
    order: -1;
  }
}

/* 猜你喜欢 */
.recommend {
  max-width: 1200px;
  margin: 34px auto 0;
}

.recommend-title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  padding-left: 12px;
  border-left: 4px solid #00ffff;
}

.recommend-row {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.recommend-card {
  width: 160px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  cursor: pointer;
  transition: transform 0.3s, border-color 0.3s, box-shadow 0.3s;
}

.recommend-card:hover {
  transform: translateY(-5px);
  border-color: rgba(0, 255, 255, 0.45);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.5), 0 0 14px rgba(0, 255, 255, 0.15);
}

.recommend-img {
  position: relative;
  aspect-ratio: 3 / 4;
  overflow: hidden;
}

.recommend-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-rating {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 1px 7px;
  font-size: 12px;
  font-weight: 700;
  color: #ffd86b;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 6px;
}

.recommend-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 9px 11px;
}

.recommend-name {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommend-cat {
  font-size: 11px;
  color: #888;
}
</style>
