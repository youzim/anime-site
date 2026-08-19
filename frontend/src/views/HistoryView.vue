<template>
  <div class="history">
    <div class="history-inner">
      <!-- 头部 -->
      <div class="history-head">
        <h1 class="history-title">观看历史</h1>
        <button v-if="list.length" class="clear-btn" @click="onClear">
          <el-icon><Delete /></el-icon>
          清空全部
        </button>
      </div>

      <!-- 列表 -->
      <div class="history-list" v-if="list.length">
        <div class="history-item" v-for="item in list" :key="item.animeId" @click="goDetail(item.animeId)">
          <div class="history-cover">
            <img :src="item.cover" :alt="item.title" />
          </div>
          <div class="history-main">
            <span class="history-name">{{ item.title }}</span>
            <span class="history-category">{{ item.category }}</span>
            <span class="history-time">{{ formatWatchedAt(item.updatedAt) }}</span>
            <span v-if="item.progress > 0" class="history-progress">看到 {{ item.ep }} 集 · {{ Math.floor(item.progress / 60) }}分{{ Math.floor(item.progress % 60) }}秒</span>
          </div>
          <button class="continue-btn" @click.stop="goDetail(item.animeId)">
            <el-icon><CaretRight /></el-icon>
            继续观看
          </button>
          <button class="del-btn" title="删除" @click.stop="onRemove(item.animeId)">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </div>

      <!-- 空态 -->
      <div class="history-empty" v-else>
        <el-icon class="empty-icon"><Clock /></el-icon>
        <p>还没有观看记录</p>
        <p class="empty-sub">去首页逛逛，点开喜欢的番剧吧</p>
        <button class="go-btn" @click="goHome">去逛逛</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CaretRight, Clock, Close, Delete } from '@element-plus/icons-vue'
import { clearHistoryRecord, deleteHistoryRecord, getHistory } from '@/api/api'

const router = useRouter()
const list = ref([])

// 时间格式化：数据库返回 updatedAt（"2026-08-16 12:30:00"）
function formatWatchedAt(ts) {
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

async function load() {
  try {
    list.value = await getHistory()
  } catch {
    list.value = []
  }
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

function goHome() {
  router.push('/home')
}

async function onRemove(animeId) {
  try {
    await deleteHistoryRecord(animeId)
    list.value = list.value.filter((h) => h.animeId !== animeId)
    ElMessage.success('已删除该条记录')
  } catch {}
}

async function onClear() {
  try {
    await ElMessageBox.confirm('确定清空全部观看历史吗？', '清空历史', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await clearHistoryRecord()
    list.value = []
    ElMessage.success('已清空观看历史')
  } catch {
    // 取消
  }
}

onMounted(load)
</script>

<style scoped>
.history {
  min-height: 100vh;
  padding: 110px 30px 60px;
  box-sizing: border-box;
  background:
    radial-gradient(600px 400px at 15% 25%, rgba(0, 255, 255, 0.12), transparent 65%),
    radial-gradient(700px 500px at 85% 70%, rgba(171, 111, 255, 0.16), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
}
.history-inner {
  max-width: 900px;
  margin: 0 auto;
}

.history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.history-title {
  margin: 0;
  font-size: 30px;
  font-weight: 900;
  background: linear-gradient(90deg, #00ffff, #ab6fff, #ff7eb3);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.clear-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  font-size: 13px;
  color: #bbb;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s;
}
.clear-btn:hover {
  color: #ff7eb3;
  border-color: rgba(255, 126, 179, 0.5);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: background 0.25s, transform 0.25s, border-color 0.25s;
}
.history-item:hover {
  background: rgba(255, 255, 255, 0.09);
  border-color: rgba(0, 255, 255, 0.35);
  transform: translateX(4px);
}
.history-cover {
  width: 120px;
  aspect-ratio: 16 / 9;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 8px;
}
.history-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.history-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.history-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.history-category {
  font-size: 12px;
  color: #00ffff;
  background: rgba(0, 255, 255, 0.1);
  padding: 1px 8px;
  border-radius: 4px;
  align-self: flex-start;
}
.history-time {
  font-size: 12px;
  color: #777;
}
.history-progress {
  font-size: 12px;
  color: #00ffff;
  opacity: 0.9;
}
.continue-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 18px;
  font-size: 13px;
  font-weight: 600;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 18px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  flex-shrink: 0;
}
.continue-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 255, 255, 0.4);
}
.del-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #888;
  background: transparent;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}
.del-btn:hover {
  color: #ff7eb3;
  background: rgba(255, 126, 179, 0.15);
}

/* 空态 */
.history-empty {
  padding: 100px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #666;
}
.empty-icon {
  font-size: 52px;
  color: #444;
}
.history-empty p {
  margin: 0;
  font-size: 15px;
}
.empty-sub {
  font-size: 13px !important;
}
.go-btn {
  margin-top: 14px;
  padding: 9px 30px;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  background: #00ffff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.go-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 255, 255, 0.4);
}

@media (max-width: 640px) {
  .history-cover { width: 90px; }
  .continue-btn { display: none; }
}
</style>
