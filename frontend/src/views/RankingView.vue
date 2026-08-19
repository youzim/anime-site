<template>
  <div class="rank">
    <div class="rank-inner">
      <!-- 标题 + tab -->
      <div class="rank-head">
        <h1 class="rank-title">排行榜</h1>
        <div class="rank-tabs">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="rank-tab"
            :class="{ active: sort === t.key }"
            @click="switchSort(t.key)"
          >
            {{ t.label }}
          </button>
        </div>
      </div>

      <!-- TOP 3 大卡 -->
      <div class="top3" v-if="list.length">
        <div class="top3-item" v-for="(item, i) in list.slice(0, 3)" :key="item.id" @click="goDetail(item.id)">
          <div class="top3-badge" :class="`top3-badge--${i + 1}`">{{ i + 1 }}</div>
          <div class="top3-img">
            <img :src="item.cover" :alt="item.title" />
            <div class="top3-mask"></div>
            <div class="top3-info">
              <h3 class="top3-name">{{ item.title }}</h3>
              <span class="top3-rating">{{ item.rating }} 分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 4-16 列表 -->
      <div class="rank-list" v-if="list.length > 3">
        <div class="rank-row" v-for="(item, i) in list.slice(3)" :key="item.id" @click="goDetail(item.id)">
          <span class="rank-no">{{ i + 4 }}</span>
          <div class="rank-cover">
            <img :src="item.cover" :alt="item.title" />
          </div>
          <div class="rank-main">
            <div class="rank-name-row">
              <span class="rank-name">{{ item.title }}</span>
              <span class="rank-category">{{ item.category }}</span>
            </div>
            <div class="rank-bar">
              <div
                class="rank-bar-fill"
                :style="{ width: barWidth(item) + '%' }"
              ></div>
            </div>
          </div>
          <div class="rank-stat">
            <span class="rank-stat-num" v-if="sort === 'views'">{{ formatViews(item.views) }}</span>
            <span class="rank-stat-num" v-else>{{ item.rating }}</span>
            <span class="rank-stat-label">{{ sort === 'views' ? '播放' : '评分' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { formatViews, getRankList } from '@/api/api'

const router = useRouter()

const tabs = [
  { key: 'rating', label: '评分榜' },
  { key: 'views', label: '热播榜' }
]
const sort = ref('rating')
const animeList = ref([])

const list = computed(() => animeList.value)

// 热度条：评分按 10 分制，播放量按最大归一化（views 已是数字）
const barWidth = (item) => {
  if (sort.value === 'views') {
    const max = Math.max(...animeList.value.map((a) => Number(a.views) || 0))
    return max > 0 ? (Number(item.views) / max) * 100 : 0
  }
  return (Number(item.rating) / 10) * 100
}

async function switchSort(key) {
  sort.value = key
  animeList.value = await getRankList(key)
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

onMounted(async () => {
  try {
    animeList.value = await getRankList('rating')
  } catch {}
})
</script>

<style scoped>
.rank {
  min-height: 100vh;
  padding: 110px 30px 60px;
  box-sizing: border-box;
  background:
    radial-gradient(600px 400px at 15% 25%, rgba(0, 255, 255, 0.12), transparent 65%),
    radial-gradient(700px 500px at 85% 70%, rgba(171, 111, 255, 0.16), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
}
.rank-inner {
  max-width: 1100px;
  margin: 0 auto;
}

/* 头部 */
.rank-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 26px;
}
.rank-title {
  margin: 0;
  font-size: 30px;
  font-weight: 900;
  background: linear-gradient(90deg, #00ffff, #ab6fff, #ff7eb3);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.rank-tabs {
  display: flex;
  gap: 8px;
}
.rank-tab {
  padding: 7px 20px;
  font-size: 14px;
  color: #bbb;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s;
}
.rank-tab:hover {
  color: #fff;
  border-color: rgba(0, 255, 255, 0.5);
}
.rank-tab.active {
  color: #000;
  background: #00ffff;
  border-color: #00ffff;
  font-weight: 600;
  box-shadow: 0 0 16px rgba(0, 255, 255, 0.35);
}

/* TOP3 */
.top3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-bottom: 26px;
}
.top3-item {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.14);
  transition: transform 0.3s, box-shadow 0.3s;
}
.top3-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.6), 0 0 24px rgba(0, 255, 255, 0.18);
}
.top3-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 900;
  color: #1a1206;
  border-radius: 50%;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.4);
}
.top3-badge--1 {
  background: linear-gradient(135deg, #ffe28a, #ffc94d);
}
.top3-badge--2 {
  background: linear-gradient(135deg, #e8eaf0, #b8bcc6);
}
.top3-badge--3 {
  background: linear-gradient(135deg, #e8b98f, #c98d5f);
}
.top3-img {
  position: relative;
  aspect-ratio: 3 / 4;
}
.top3-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s;
}
.top3-item:hover .top3-img img {
  transform: scale(1.06);
}
.top3-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(5, 6, 10, 0.9) 0%, transparent 55%);
}
.top3-info {
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 14px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
}
.top3-name {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
}
.top3-rating {
  font-size: 14px;
  font-weight: 700;
  color: #ffd86b;
}

/* 列表 */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rank-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: background 0.25s, transform 0.25s, border-color 0.25s;
}
.rank-row:hover {
  background: rgba(255, 255, 255, 0.09);
  border-color: rgba(0, 255, 255, 0.4);
  transform: translateX(4px);
}
.rank-no {
  width: 34px;
  text-align: center;
  font-size: 18px;
  font-weight: 800;
  color: #888;
}
.rank-cover {
  width: 64px;
  aspect-ratio: 16 / 9;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 6px;
}
.rank-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.rank-main {
  flex: 1;
  min-width: 0;
}
.rank-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.rank-name {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rank-category {
  flex-shrink: 0;
  font-size: 11px;
  color: #00ffff;
  background: rgba(0, 255, 255, 0.12);
  padding: 1px 8px;
  border-radius: 4px;
}
.rank-bar {
  height: 5px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.1);
  overflow: hidden;
}
.rank-bar-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #00ffff, #ab6fff);
  transition: width 0.5s ease;
}
.rank-stat {
  width: 90px;
  text-align: right;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.rank-stat-num {
  font-size: 16px;
  font-weight: 800;
  color: #ffd86b;
}
.rank-stat-label {
  font-size: 11px;
  color: #777;
}

@media (max-width: 820px) {
  .top3 { grid-template-columns: 1fr; }
}
</style>
