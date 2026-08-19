<template>
  <div class="detail" v-if="anime">
    <!-- 顶部 Banner -->
    <div class="banner">
      <img class="banner-img" :src="anime.cover" :alt="anime.title" />
      <div class="banner-mask"></div>
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </button>
    </div>

    <div class="detail-main">
      <!-- 信息头 -->
      <div class="detail-head">
        <h1 class="detail-title">{{ anime.title }}</h1>
        <div class="detail-meta">
          <span class="detail-rating"><el-icon><StarFilled /></el-icon>{{ anime.rating }}</span>
          <span class="dot"></span>
          <span>{{ anime.category }}</span>
          <span class="dot"></span>
          <span>{{ anime.year }}</span>
          <span class="dot"></span>
          <span>{{ anime.episodes > 1 ? `全${anime.episodes}话` : '剧场版' }}</span>
          <span class="dot"></span>
          <span>{{ anime.views }} 播放</span>
        </div>
        <div class="detail-tags">
          <span v-for="tag in anime.tags" :key="tag" class="detail-tag">{{ tag }}</span>
        </div>
        <div class="source-line">
          片源：直链正片（完整长视频，选集可切换）
        </div>
        <div class="detail-btns">
          <button class="play-btn" @click="play">
            <el-icon><CaretRight /></el-icon>
            立即播放
          </button>
          <button class="fav-btn" :class="{ favorited }" @click="onToggleFavorite">
            <el-icon><StarFilled v-if="favorited" /><Star v-else /></el-icon>
            {{ favorited ? '已追番' : '追番' }}
          </button>
        </div>
      </div>

      <!-- 简介 -->
      <div class="detail-section">
        <h3 class="section-label">简介</h3>
        <p class="detail-desc">{{ anime.desc }}</p>
        <div class="detail-author">制作：{{ anime.author }}</div>
      </div>

      <!-- 选集 -->
      <div class="detail-section">
        <h3 class="section-label">选集</h3>
        <div class="ep-grid">
          <button
            v-for="n in epList"
            :key="n"
            class="ep-btn"
            @click="goPlay(n)"
          >
            {{ anime.episodes > 1 ? `第${n}集` : '正片' }}
          </button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="detail-section">
        <h3 class="section-label">
          评论
          <span class="comment-count">{{ comments.length }}</span>
        </h3>
        <div class="comment-input-row">
          <input
            v-model="commentText"
            class="comment-input"
            placeholder="说说你对这部番的看法吧~"
            maxlength="500"
            @keyup.enter="sendComment"
          />
          <button class="comment-send" @click="sendComment">发表评论</button>
        </div>

        <div class="comment-list" v-if="comments.length">
          <div class="comment-item" v-for="c in comments" :key="c.id">
            <div class="comment-avatar">{{ (c.username || '客').slice(0, 1).toUpperCase() }}</div>
            <div class="comment-main">
              <div class="comment-head">
                <span class="comment-user">{{ c.username }}</span>
                <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
                <button v-if="c.username === userStore.username" class="comment-del" title="删除" @click="delCommentFn(c.id)">
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
              <p class="comment-content">{{ c.content }}</p>
              <button class="comment-like" @click="likeCommentFn(c.id)">
                <el-icon><Pointer /></el-icon>
                {{ c.likes }}
              </button>
            </div>
          </div>
        </div>
        <div class="comment-empty" v-else>还没有评论，来抢沙发！</div>
      </div>

      <!-- 相关推荐 -->
      <div class="detail-section">
        <h3 class="section-label">相关推荐</h3>
        <div class="related-grid" v-if="related.length">
          <div class="related-item" v-for="item in related" :key="item.id" @click="goDetail(item.id)">
            <div class="related-img">
              <img :src="item.cover" :alt="item.title" />
            </div>
            <div class="related-info">
              <span class="related-title">{{ item.title }}</span>
              <span class="related-rating">{{ item.rating }}</span>
            </div>
          </div>
        </div>
        <div class="related-empty" v-else>暂无更多推荐</div>
      </div>
    </div>
  </div>

  <!-- 加载中 -->
  <div class="detail-loading" v-else>
    <el-icon class="loading-icon" :class="{ spin: true }"><Loading /></el-icon>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CaretRight, Delete, Loading, Pointer, Star, StarFilled } from '@element-plus/icons-vue'
import {
  addComment,
  addHistoryRecord,
  deleteComment,
  getAnimeDetail,
  getComments,
  getFavoriteStatus,
  getRelated,
  likeComment,
  toggleFavorite
} from '@/api/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const anime = ref(null)
const relatedList = ref([])

// 评论
const comments = ref([])
const commentText = ref('')

// 追番（收藏）
const favorited = ref(false)

// 选集
const epList = computed(() => Array.from({ length: anime.value?.episodes ?? 1 }, (_, i) => i + 1))

// 相关推荐（后端同分类返回）
const related = computed(() => relatedList.value)

// 时间格式化：2026-08-16 12:30:00 → 今天 12:30 / 8月16日
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
  return `${d.getMonth() + 1}月${d.getDate()}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const [detail, rel, cms] = await Promise.all([
      getAnimeDetail(id),
      getRelated(id),
      getComments(id)
    ])
    anime.value = detail
    relatedList.value = rel
    comments.value = cms
  } catch {}
  if (userStore.isLoggedIn) {
    try {
      const s = await getFavoriteStatus(id)
      favorited.value = s.favorited
    } catch {}
  }
})

// 立即播放 / 选集播放（顺手上报观看历史）
function play() {
  goPlay(1)
}
function goPlay(ep) {
  if (userStore.isLoggedIn) {
    addHistoryRecord({ animeId: anime.value.id, ep, progress: 0 }).catch(() => {})
  }
  router.push({ path: `/player/${anime.value.id}`, query: { ep } })
}

// 追番 / 取消追番
async function onToggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再追番')
    router.push('/login')
    return
  }
  try {
    const res = await toggleFavorite(anime.value.id)
    favorited.value = res.favorited
    ElMessage.success(res.favorited ? '已加入追番' : '已取消追番')
  } catch {}
}

// 发表评论
async function sendComment() {
  const content = commentText.value.trim()
  if (!content) {
    ElMessage.info('先写点内容再发表吧')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再评论')
    router.push('/login')
    return
  }
  try {
    await addComment({ animeId: anime.value.id, content })
    comments.value = await getComments(anime.value.id)
    commentText.value = ''
    ElMessage.success('评论成功')
  } catch {}
}

// 点赞
async function likeCommentFn(commentId) {
  try {
    const res = await likeComment(commentId)
    const target = comments.value.find((c) => c.id === commentId)
    if (target) target.likes = res.likes
  } catch {}
}

// 删除评论（后端仅允许本人删除）
async function delCommentFn(commentId) {
  try {
    await deleteComment(commentId)
    comments.value = comments.value.filter((c) => c.id !== commentId)
    ElMessage.success('已删除评论')
  } catch {}
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

// 返回（有历史则返回上页，无历史兜底到首页）
function goBack() {
  if (window.history.state?.back) {
    router.back()
  } else {
    router.replace('/home')
  }
}
</script>

<style scoped>
.detail {
  min-height: 100vh;
  background:
    radial-gradient(700px 500px at 85% 20%, rgba(171, 111, 255, 0.14), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
}

/* ===== Banner ===== */
.banner {
  position: relative;
  height: 52vh;
  min-height: 320px;
  overflow: hidden;
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 20%;
}
.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(5, 6, 10, 0.25) 0%, rgba(5, 6, 10, 0.55) 55%, #07080d 100%);
}
.back-btn {
  position: absolute;
  top: 90px;
  left: 30px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  font-size: 14px;
  color: #fff;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s;
}
.back-btn:hover {
  background: #00ffff;
  color: #000;
  border-color: #00ffff;
}

/* ===== 主体 ===== */
.detail-main {
  max-width: 1100px;
  margin: -70px auto 0;
  padding: 0 30px 60px;
  position: relative;
  z-index: 1;
}
.detail-head {
  padding: 28px 32px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
}
.detail-title {
  margin: 0 0 14px;
  font-size: 34px;
  font-weight: 900;
  color: #fff;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 14px;
  color: #bbb;
  margin-bottom: 16px;
}
.detail-rating {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 17px;
  font-weight: 700;
  color: #ffd86b;
}
.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #555;
}
.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.detail-tag {
  font-size: 12px;
  color: #00ffff;
  background: rgba(0, 255, 255, 0.12);
  padding: 3px 12px;
  border-radius: 12px;
}
.source-line {
  font-size: 12px;
  color: #888;
  margin-bottom: 20px;
}
.source-link {
  color: #00ffff;
  text-decoration: none;
  margin-left: 4px;
}
.source-link:hover {
  text-decoration: underline;
}
.detail-btns {
  display: flex;
  align-items: center;
  gap: 14px;
}
.fav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  color: #ffd86b;
  background: rgba(255, 216, 107, 0.1);
  border: 1px solid rgba(255, 216, 107, 0.45);
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.25s;
}
.fav-btn:hover {
  background: rgba(255, 216, 107, 0.22);
  box-shadow: 0 0 18px rgba(255, 216, 107, 0.25);
}
.fav-btn.favorited {
  color: #000;
  background: #ffd86b;
  border-color: #ffd86b;
}
.play-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 11px 34px;
  font-size: 15px;
  font-weight: bold;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.play-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 255, 255, 0.45);
}

/* 区块 */
.detail-section {
  margin-top: 26px;
  padding: 24px 28px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.section-label {
  margin: 0 0 14px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  padding-left: 10px;
  border-left: 3px solid #00ffff;
}
.comment-count {
  margin-left: 8px;
  font-size: 13px;
  color: #888;
  font-weight: 400;
}
.detail-desc {
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.9;
  color: #ccc;
  white-space: pre-line;
}
.detail-author {
  font-size: 13px;
  color: #888;
}

/* 选集 */
.ep-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 10px;
}
.ep-btn {
  padding: 10px 0;
  font-size: 13px;
  color: #ccc;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
}
.ep-btn:hover {
  color: #fff;
  border-color: rgba(0, 255, 255, 0.55);
  transform: translateY(-2px);
}

/* 评论区 */
.comment-input-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.comment-input {
  flex: 1;
  height: 42px;
  padding: 0 16px;
  font-size: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 20px;
  outline: none;
  transition: all 0.25s;
  box-sizing: border-box;
}
.comment-input:focus {
  border-color: #00ffff;
  box-shadow: 0 0 12px rgba(0, 255, 255, 0.25);
}
.comment-input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}
.comment-send {
  padding: 0 22px;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s;
}
.comment-send:hover {
  box-shadow: 0 0 16px rgba(0, 255, 255, 0.4);
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.comment-avatar {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ab6fff, #ff7eb3);
}
.comment-main {
  flex: 1;
  min-width: 0;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.comment-user {
  font-size: 13px;
  font-weight: 600;
  color: #00ffff;
}
.comment-time {
  font-size: 12px;
  color: #666;
}
.comment-del {
  margin-left: auto;
  padding: 2px 6px;
  font-size: 13px;
  color: #888;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}
.comment-del:hover {
  color: #ff7eb3;
}
.comment-content {
  margin: 0 0 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #ddd;
  word-break: break-word;
}
.comment-like {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 12px;
  font-size: 12px;
  color: #999;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.comment-like:hover {
  color: #ff7eb3;
  border-color: rgba(255, 126, 179, 0.5);
}
.comment-like.liked {
  color: #ff7eb3;
  border-color: rgba(255, 126, 179, 0.6);
}
.comment-empty {
  padding: 30px 0;
  text-align: center;
  color: #666;
  font-size: 14px;
}

/* 相关推荐 */
.related-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.related-item {
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.25s, box-shadow 0.25s;
}
.related-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5), 0 0 14px rgba(0, 255, 255, 0.18);
}
.related-img {
  aspect-ratio: 16 / 9;
  overflow: hidden;
}
.related-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.related-item:hover .related-img img {
  transform: scale(1.06);
}
.related-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 12px;
}
.related-title {
  font-size: 13px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.related-rating {
  font-size: 13px;
  font-weight: 700;
  color: #ffd86b;
}
.related-empty {
  padding: 30px 0;
  text-align: center;
  color: #666;
  font-size: 14px;
}

/* 加载中 */
.detail-loading {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #0b0d12, #05060a);
}
.loading-icon {
  font-size: 36px;
  color: #00ffff;
}
.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 820px) {
  .related-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
