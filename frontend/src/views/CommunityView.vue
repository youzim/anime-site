<template>
  <div class="community">
    <div class="community-inner">
      <!-- 头部 -->
      <div class="community-head">
        <h1 class="community-title">留言板</h1>
        <p class="community-sub">分享你对某部番剧的看法，或对网站的意见建议，畅所欲言～</p>
      </div>

      <!-- 发布区 -->
      <div class="post-form">
        <input v-model="form.title" class="post-title-input" placeholder="标题：说点什么？（选填）" maxlength="60" />
        <textarea
          v-model="form.content"
          class="post-content-input"
          placeholder="内容：写下你的想法…"
          maxlength="2000"
          rows="3"
        ></textarea>
        <div class="post-form-foot">
          <span class="post-form-tip">文明发言，友善交流</span>
          <button class="post-submit" :disabled="!form.content.trim()" @click="submitPost">发表留言</button>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="post-list" v-if="posts.length">
        <div class="post-item" v-for="post in posts" :key="post.id">
          <div class="post-head">
            <span class="post-avatar">{{ (post.username || '客').slice(0, 1).toUpperCase() }}</span>
            <span class="post-user">{{ post.username }}</span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            <button v-if="post.username === userStore.username" class="post-del" title="删除" @click="delPost(post.id)">
              <el-icon><Delete /></el-icon>
            </button>
          </div>
          <h3 class="post-title" v-if="post.title">{{ post.title }}</h3>
          <p class="post-content">{{ post.content }}</p>
          <div class="post-actions">
            <button class="act-btn" @click="likePostFn(post.id)">
              <el-icon><Pointer /></el-icon>
              {{ post.likes }} 点赞
            </button>
            <button class="act-btn" @click="toggleComments(post.id)">
              <el-icon><ChatDotRound /></el-icon>
              {{ post.commentCount }} 评论
            </button>
          </div>

          <!-- 评论展开区 -->
          <div class="post-comments" v-if="expandedId === post.id">
            <div class="post-comment" v-for="c in post.comments" :key="c.id">
              <span class="pc-user">{{ c.username }}</span>
              <span class="pc-content">{{ c.content }}</span>
              <button class="pc-like" @click="likePostCommentFn(post.id, c.id)">
                <el-icon><Pointer /></el-icon>{{ c.likes }}
              </button>
              <button v-if="c.username === userStore.username" class="pc-del" title="删除" @click="delPostCommentFn(post.id, c.id)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
            <div class="pc-input-row">
              <input
                v-model="commentText"
                class="pc-input"
                placeholder="回复一下…"
                maxlength="500"
                @keyup.enter="sendPostComment(post.id)"
              />
              <button class="pc-send" @click="sendPostComment(post.id)">回复</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空态 -->
      <div class="community-empty" v-else>
        <el-icon class="empty-icon"><ChatDotRound /></el-icon>
        <p>还没有留言，来发表第一条吧！</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Delete, Pointer } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  addPost,
  addPostComment,
  deletePost,
  deletePostComment,
  getPostDetail,
  getPosts,
  likePost,
  likePostComment
} from '@/api/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const posts = ref([])
const form = reactive({ title: '', content: '' })
const commentText = ref('')
const expandedId = ref(null)

onMounted(load)

async function load() {
  try {
    const data = await getPosts(1, 50)
    posts.value = data.list
  } catch {
    posts.value = []
  }
}

// 展开评论：懒加载帖子详情（含评论列表）
async function toggleComments(postId) {
  if (expandedId.value === postId) {
    expandedId.value = null
    return
  }
  try {
    const data = await getPostDetail(postId)
    const target = posts.value.find((p) => p.id === postId)
    if (target) {
      target.comments = data.comments || []
      target.commentCount = data.comments?.length || 0
    }
    expandedId.value = postId
  } catch {}
}

async function submitPost() {
  const content = form.content.trim()
  if (!content) {
    ElMessage.info('写点内容再发表吧')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再发帖')
    router.push('/login')
    return
  }
  try {
    await addPost({ title: form.title.trim(), content })
    form.title = ''
    form.content = ''
    await load()
    ElMessage.success('留言发表成功')
  } catch {}
}

async function delPost(postId) {
  try {
    await deletePost(postId)
    posts.value = posts.value.filter((p) => p.id !== postId)
    ElMessage.success('已删除')
  } catch {}
}

async function likePostFn(postId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再点赞')
    router.push('/login')
    return
  }
  try {
    const res = await likePost(postId)
    const target = posts.value.find((p) => p.id === postId)
    if (target) target.likes = res.likes
  } catch {}
}

async function sendPostComment(postId) {
  const content = commentText.value.trim()
  if (!content) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再回复')
    router.push('/login')
    return
  }
  try {
    await addPostComment(postId, { content })
    commentText.value = ''
    const data = await getPostDetail(postId)
    const target = posts.value.find((p) => p.id === postId)
    if (target) {
      target.comments = data.comments || []
      target.commentCount = data.comments?.length || 0
    }
  } catch {}
}

async function likePostCommentFn(postId, commentId) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录再点赞')
    router.push('/login')
    return
  }
  try {
    const res = await likePostComment(commentId)
    const target = posts.value.find((p) => p.id === postId)
    const c = target?.comments?.find((x) => x.id === commentId)
    if (c) c.likes = res.likes
  } catch {}
}

async function delPostCommentFn(postId, commentId) {
  try {
    await deletePostComment(commentId)
    const target = posts.value.find((p) => p.id === postId)
    if (target) target.comments = target.comments.filter((x) => x.id !== commentId)
    ElMessage.success('已删除评论')
  } catch {}
}

// 时间格式化
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
</script>

<style scoped>
.community {
  min-height: 100vh;
  padding: 110px 30px 60px;
  box-sizing: border-box;
  background:
    radial-gradient(600px 400px at 15% 25%, rgba(0, 255, 255, 0.12), transparent 65%),
    radial-gradient(700px 500px at 85% 70%, rgba(171, 111, 255, 0.16), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
}
.community-inner {
  max-width: 900px;
  margin: 0 auto;
}

/* 头部 */
.community-head {
  text-align: center;
  margin-bottom: 26px;
}
.community-title {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 900;
  background: linear-gradient(90deg, #00ffff, #ab6fff, #ff7eb3);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.community-sub {
  margin: 0;
  font-size: 14px;
  color: #888;
}

/* 发布区 */
.post-form {
  padding: 20px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 24px;
}
.post-title-input,
.post-content-input {
  width: 100%;
  font-size: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 10px;
  outline: none;
  transition: all 0.25s;
  box-sizing: border-box;
  font-family: inherit;
}
.post-title-input {
  height: 40px;
  padding: 0 14px;
  margin-bottom: 10px;
}
.post-content-input {
  padding: 10px 14px;
  resize: vertical;
  min-height: 70px;
}
.post-title-input:focus,
.post-content-input:focus {
  border-color: #00ffff;
  box-shadow: 0 0 10px rgba(0, 255, 255, 0.2);
}
.post-title-input::placeholder,
.post-content-input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}
.post-form-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}
.post-form-tip {
  font-size: 12px;
  color: #666;
}
.post-submit {
  padding: 9px 26px;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s;
}
.post-submit:hover {
  box-shadow: 0 0 16px rgba(0, 255, 255, 0.4);
}
.post-submit:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 帖子 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.post-item {
  padding: 18px 20px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: border-color 0.25s;
}
.post-item:hover {
  border-color: rgba(0, 255, 255, 0.25);
}
.post-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.post-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #ab6fff, #ff7eb3);
}
.post-user {
  font-size: 14px;
  font-weight: 600;
  color: #00ffff;
}
.post-time {
  font-size: 12px;
  color: #666;
}
.post-del {
  margin-left: auto;
  padding: 2px 6px;
  font-size: 14px;
  color: #888;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}
.post-del:hover {
  color: #ff7eb3;
}
.post-title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}
.post-content {
  margin: 0 0 14px;
  font-size: 14px;
  line-height: 1.8;
  color: #ccc;
  word-break: break-word;
  white-space: pre-wrap;
}
.post-actions {
  display: flex;
  gap: 10px;
}
.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  font-size: 13px;
  color: #999;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.act-btn:hover {
  color: #00ffff;
  border-color: rgba(0, 255, 255, 0.5);
}
.act-btn.liked {
  color: #ff7eb3;
  border-color: rgba(255, 126, 179, 0.6);
}

/* 评论展开 */
.post-comments {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.07);
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.post-comment {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
}
.pc-user {
  color: #00ffff;
  font-weight: 600;
  flex-shrink: 0;
}
.pc-content {
  flex: 1;
  color: #ccc;
  word-break: break-word;
}
.pc-del {
  margin-left: auto;
  padding: 0;
  font-size: 12px;
  color: #888;
  background: transparent;
  border: none;
  cursor: pointer;
}
.pc-del:hover {
  color: #ff7eb3;
}
.pc-like {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 8px;
  font-size: 12px;
  color: #888;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}
.pc-like:hover,
.pc-like.liked {
  color: #ff7eb3;
}
.pc-input-row {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.pc-input {
  flex: 1;
  height: 34px;
  padding: 0 12px;
  font-size: 13px;
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  outline: none;
  box-sizing: border-box;
}
.pc-input:focus {
  border-color: #00ffff;
}
.pc-input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}
.pc-send {
  padding: 0 16px;
  font-size: 13px;
  font-weight: 600;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 16px;
  cursor: pointer;
}

/* 空态 */
.community-empty {
  padding: 80px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #666;
}
.empty-icon {
  font-size: 46px;
  color: #444;
}
.community-empty p {
  margin: 0;
  font-size: 14px;
}
</style>
