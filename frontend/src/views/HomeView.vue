<template>
  <div class="home">
    <!-- ===== Hero 轮播 ===== -->
    <ElCarousel
      ref="carouselRef"
      height="100vh"
      :interval="4000"
      arrow="never"
      indicator-position="none"
      @change="onChange"
    >
      <ElCarouselItem v-for="item in Swiper_list" :key="item.id">
        <div class="swiper-item">
          <img class="swiper-img" :src="item.imgurl" :alt="item.title" />
          <div class="swiper-mask"></div>
          <div class="hero-info">
            <h2 class="title">{{ item.title }}</h2>
            <p class="subtitle">{{ item.subtitle }}</p>
            <div class="tags">
              <span v-for="tag in item.tags" :key="tag">{{ tag }}</span>
            </div>
            <button class="play-btn" @click="goDetail(item.id)">
              <el-icon><CaretRight /></el-icon>
              立即观看
            </button>
          </div>
        </div>
      </ElCarouselItem>
    </ElCarousel>

    <!-- 右下角横排缩略图切换区 -->
    <div class="thumb-list">
      <div
        v-for="(item, i) in Swiper_list"
        :key="item.id"
        class="thumb-item"
        :class="{ active: activeIndex === i }"
        @click="switchTo(i)"
      >
        <img :src="item.imgurl" :alt="item.title" />
        <span>{{ item.title }}</span>
      </div>
    </div>
  </div>

  <!-- ===== 内容区 ===== -->
  <div class="main">
    <!-- ===== 板块浏览视图（默认首页） ===== -->
    <template v-if="viewMode === 'sections'">
      <div class="sections-head">
        <h2 class="sections-title">番剧精选</h2>
        <p class="sections-sub">分类浏览，发现更多好番</p>
      </div>

      <div class="section-block" v-for="cat in sectionCats" :key="cat">
        <div class="block-head">
          <h3 class="block-title">{{ cat }}</h3>
          <button class="more-btn" @click="enterCategory(cat)">
            查看更多
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <div class="block-row">
          <div class="mini-card" v-for="item in byCategory(cat)" :key="item.id" @click="goDetail(item.id)">
            <div class="mini-img">
              <img :src="item.cover" :alt="item.title" />
              <span class="mini-rating">{{ item.rating }}</span>
            </div>
            <div class="mini-info">
              <span class="mini-title">{{ item.title }}</span>
              <span class="mini-author">{{ item.author }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ===== 完整列表视图（分类 tab / 查看更多 / 搜索进入） ===== -->
    <template v-else>
      <div class="back-row">
        <button class="back-sections" @click="backSections">
          <el-icon><ArrowLeft /></el-icon>
          板块浏览
        </button>
        <h2 class="list-title">{{ category === '全部' ? '全部番剧' : category }}</h2>
      </div>

      <!-- 分类筛选 + 搜索 + 排序 -->
      <div class="filter-bar">
        <div class="filter-tabs">
          <button
            v-for="c in categories"
            :key="c"
            class="filter-tab"
            :class="{ active: category === c }"
            @click="selectCategory(c)"
          >
            {{ c }}
          </button>
        </div>
        <div class="filter-right">
          <div class="sort-tabs">
            <button
              v-for="s in sortOptions"
              :key="s.key"
              class="sort-tab"
              :class="{ active: sort === s.key }"
              @click="selectSort(s.key)"
            >
              {{ s.label }}
            </button>
          </div>
          <div class="filter-search">
            <el-icon class="filter-search-icon"><Search /></el-icon>
            <input
              v-model="keyword"
              class="filter-search-input"
              type="text"
              placeholder="搜索番剧 / 作者"
              @keyup.enter="applyKeyword"
            />
          </div>
        </div>
      </div>

      <div class="result-info">
        共 <b>{{ total }}</b> 部 · 当前分类：{{ category }}
        <span v-if="keyword" class="result-keyword">关键词「{{ keyword }}」</span>
      </div>

      <div class="grid">
        <div class="grid-item" v-for="item in shownList" :key="item.id" @click="goDetail(item.id)">
          <div class="grid-img">
            <img :src="item.cover" :alt="item.title" />
            <span class="grid-rating">{{ item.rating }}</span>
            <div class="grid-play">
              <el-icon><CaretRight /></el-icon>
              <span>播放</span>
            </div>
            <span class="grid-episodes">{{ item.episodes > 1 ? `全${item.episodes}话` : '剧场版' }}</span>
          </div>
          <div class="grid-content">
            <span class="grid-title">{{ item.title }}</span>
            <div class="grid-tags">
              <span v-for="tag in item.tags.slice(0, 2)" :key="tag" class="grid-tag">{{ tag }}</span>
            </div>
            <div class="grid-meta">
              <span class="grid-category">{{ item.category }} · {{ item.year }}</span>
              <span class="grid-author">{{ item.author }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty" v-if="filtered.length === 0">
        <el-icon class="empty-icon"><Search /></el-icon>
        <p>没有找到相关内容，换个关键词试试</p>
      </div>

      <div class="load-more-wrap" v-if="shownList.length < total">
        <button class="load-more" :disabled="loading" @click="loadMore">
          {{ loading ? '加载中…' : `加载更多（${total - shownList.length}）` }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ElCarousel, ElCarouselItem } from 'element-plus'
import { ArrowLeft, ArrowRight, CaretRight, Search } from '@element-plus/icons-vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAnimeList, getBanner } from '@/api/api'

const route = useRoute()
const router = useRouter()

// 分类定义（首页 tab / 板块用）
const categories = ['全部', '国漫', '日漫', '欧美动漫', '电影']
const sectionCats = categories.filter((c) => c !== '全部')
const sortOptions = [
  { key: 'views', label: '热门' },
  { key: 'rating', label: '评分' },
  { key: 'newest', label: '最新' }
]

// 视图模式：sections 板块浏览（默认） | grid 完整列表
const viewMode = ref('sections')

// 筛选状态
const category = ref('全部')
const keyword = ref('')
const sort = ref('views')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)

// 轮播 + 板块数据
const bannerList = ref([])
const sectionMap = ref({})

const Swiper_list = computed(() =>
  bannerList.value.map((item) => {
    const desc = String(item.desc || '')
    return {
      id: item.id,
      title: item.title,
      subtitle: desc.length > 60 ? desc.slice(0, 60) + '…' : desc,
      tags: item.tags,
      imgurl: item.cover
    }
  })
)

// 各分类板块的卡片（后端按分类查，取前 6）
const byCategory = (cat) => sectionMap.value[cat] || []

// 完整列表（后端分页）
const gridList = ref([])
const filtered = computed(() => gridList.value)
const shownList = computed(() => gridList.value)

// 拉取完整列表（reset=true 从头，false 追加下一页）
async function fetchList(reset = true) {
  if (loading.value) return
  loading.value = true
  try {
    const p = reset ? 1 : page.value + 1
    const data = await getAnimeList({
      page: p,
      size: pageSize.value,
      category: category.value,
      keyword: keyword.value,
      sort: sort.value
    })
    total.value = data.total
    page.value = data.page
    gridList.value = reset ? data.list : [...gridList.value, ...data.list]
  } catch {
  } finally {
    loading.value = false
  }
}

// 拉取首页轮播 + 各分类板块
async function fetchSections() {
  try {
    bannerList.value = await getBanner()
  } catch {}
  await Promise.all(
    sectionCats.map(async (cat) => {
      try {
        const data = await getAnimeList({ page: 1, size: 6, category: cat, sort: 'views' })
        sectionMap.value[cat] = data.list
      } catch {}
    })
  )
}

const activeIndex = ref(0)
const carouselRef = ref(null)

// 响应 Header tab / 搜索 / 查看更多 带来的 query → 进入完整列表视图
watch(
  () => route.query,
  (q) => {
    if (q.category && categories.includes(q.category)) {
      viewMode.value = 'grid'
      category.value = q.category
      page.value = 1
      fetchList(true)
    }
    if (q.q !== undefined) {
      viewMode.value = 'grid'
      keyword.value = String(q.q)
      page.value = 1
      fetchList(true)
    }
  },
  { immediate: true }
)

onMounted(() => {
  fetchSections()
})

// 板块「查看更多」→ 进入该分类完整列表（与顶部 tab 行为一致）
function enterCategory(cat) {
  router.push({ path: '/home', query: { category: cat } })
}

// 返回板块浏览
function backSections() {
  viewMode.value = 'sections'
  category.value = '全部'
  keyword.value = ''
  router.replace({ path: '/home' })
}

function selectCategory(c) {
  category.value = c
  fetchList(true)
}
function selectSort(s) {
  sort.value = s
  fetchList(true)
}
function applyKeyword() {
  fetchList(true)
}
function loadMore() {
  fetchList(false)
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

// 缩略图切换
const switchTo = (i) => {
  activeIndex.value = i
  carouselRef.value?.setActiveItem(i)
}
const onChange = (current) => {
  activeIndex.value = current
}
</script>

<style scoped>
.home {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background-color: #0f1014;
}

/* ===== 轮播 ===== */
.swiper-item {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}
.swiper-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.swiper-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.72) 0%, rgba(0, 0, 0, 0.25) 35%, transparent 60%);
  pointer-events: none;
}
.hero-info {
  position: absolute;
  left: 60px;
  bottom: 120px;
  z-index: 2;
  color: #fff;
  max-width: 520px;
}
.hero-info .title {
  font-size: 46px;
  font-weight: 900;
  letter-spacing: 2px;
  margin: 0 0 10px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
}
.hero-info .subtitle {
  font-size: 16px;
  opacity: 0.85;
  margin: 0 0 14px;
}
.hero-info .tags {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}
.hero-info .tags span {
  font-size: 12px;
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  padding: 2px 10px;
  border-radius: 4px;
}
.play-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px;
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
  box-shadow: 0 8px 24px rgba(0, 255, 255, 0.4);
}

/* 缩略图 */
.thumb-list {
  position: absolute;
  right: 40px;
  bottom: 80px;
  z-index: 2;
  display: flex;
  flex-direction: row;
  gap: 10px;
}
.thumb-item {
  width: 150px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(0, 0, 0, 0.55);
  border: 2px solid transparent;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}
.thumb-item img {
  display: block;
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}
.thumb-item span {
  display: block;
  padding: 6px 10px;
  font-size: 12px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.thumb-item:hover {
  transform: translateY(-4px);
}
.thumb-item.active {
  border-color: #00ffff;
  transform: translateY(-6px);
  box-shadow: 0 0 16px rgba(0, 255, 255, 0.35);
}

/* ===== 内容区 ===== */
.main {
  width: 100%;
  min-height: 100vh;
  background:
    radial-gradient(600px 400px at 15% 25%, rgba(0, 255, 255, 0.12), transparent 65%),
    radial-gradient(700px 500px at 85% 70%, rgba(171, 111, 255, 0.16), transparent 65%),
    radial-gradient(500px 400px at 60% 15%, rgba(255, 126, 179, 0.1), transparent 65%),
    linear-gradient(180deg, #0b0d12, #05060a);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0 60px;
  box-sizing: border-box;
}
.sections-head {
  text-align: center;
  margin-bottom: 30px;
}
.sections-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: #fff;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #00ffff, #ab6fff, #ff7eb3);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.sections-sub {
  margin: 0;
  font-size: 14px;
  color: #888;
}

/* ===== 分类板块 ===== */
.section-block {
  width: 90%;
  max-width: 1400px;
  margin-bottom: 38px;
}
.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.block-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  padding-left: 12px;
  border-left: 4px solid #00ffff;
}
.more-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  font-size: 13px;
  color: #bbb;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s;
}
.more-btn:hover {
  color: #00ffff;
  border-color: #00ffff;
  box-shadow: 0 0 12px rgba(0, 255, 255, 0.2);
}
/* 横向滚动行 */
.block-row {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 12px;
  scrollbar-width: thin;
}
.block-row::-webkit-scrollbar {
  height: 6px;
}
.block-row::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
}
.mini-card {
  width: 172px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s, border-color 0.3s;
}
.mini-card:hover {
  transform: translateY(-6px);
  border-color: rgba(0, 255, 255, 0.45);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.55), 0 0 16px rgba(0, 255, 255, 0.18);
}
.mini-img {
  position: relative;
  aspect-ratio: 3 / 4;
  overflow: hidden;
}
.mini-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s;
}
.mini-card:hover .mini-img img {
  transform: scale(1.07);
}
.mini-rating {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 1px 7px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 6px;
}
.mini-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 10px 12px;
}
.mini-title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mini-author {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 完整列表视图 ===== */
.back-row {
  width: 90%;
  max-width: 1400px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}
.back-sections {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  font-size: 13px;
  color: #bbb;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s;
}
.back-sections:hover {
  color: #00ffff;
  border-color: #00ffff;
}
.list-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
}
.filter-bar {
  width: 90%;
  max-width: 1400px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 18px;
  padding: 12px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-sizing: border-box;
}
.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-tab {
  padding: 7px 18px;
  font-size: 14px;
  color: #bbb;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s;
}
.filter-tab:hover {
  color: #fff;
  border-color: rgba(0, 255, 255, 0.5);
}
.filter-tab.active {
  color: #000;
  background: #00ffff;
  border-color: #00ffff;
  font-weight: 600;
  box-shadow: 0 0 16px rgba(0, 255, 255, 0.35);
}
.filter-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.sort-tabs {
  display: flex;
  gap: 6px;
}
.sort-tab {
  padding: 7px 14px;
  font-size: 13px;
  color: #bbb;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s;
}
.sort-tab:hover {
  color: #fff;
  border-color: rgba(171, 111, 255, 0.55);
}
.sort-tab.active {
  color: #fff;
  background: rgba(171, 111, 255, 0.28);
  border-color: #ab6fff;
  font-weight: 600;
}
.filter-search {
  position: relative;
  display: flex;
  align-items: center;
}
.filter-search-icon {
  position: absolute;
  left: 12px;
  color: #888;
  font-size: 15px;
}
.filter-search-input {
  width: 220px;
  height: 36px;
  padding: 0 14px 0 36px;
  font-size: 13px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 18px;
  outline: none;
  transition: all 0.25s;
  box-sizing: border-box;
}
.filter-search-input:focus {
  border-color: #00ffff;
  box-shadow: 0 0 12px rgba(0, 255, 255, 0.25);
}
.filter-search-input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}
.result-info {
  width: 90%;
  max-width: 1400px;
  margin-bottom: 20px;
  font-size: 13px;
  color: #888;
  text-align: left;
}
.result-info b {
  color: #00ffff;
}
.result-keyword {
  margin-left: 8px;
  color: #ab6fff;
}
.grid {
  width: 90%;
  max-width: 1400px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.grid-item {
  aspect-ratio: 3 / 4;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.18);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  animation: cardIn 0.5s ease both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
.grid-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.6), 0 0 24px rgba(0, 255, 255, 0.2);
}
.grid-img {
  position: relative;
  height: 74%;
  overflow: hidden;
  flex-shrink: 0;
}
.grid-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transform-origin: center;
  transition: transform 0.35s;
}
.grid-item:hover .grid-img img {
  transform: scale(1.07);
}
.grid-rating {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 8px;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 8px;
  backdrop-filter: blur(4px);
}
.grid-episodes {
  position: absolute;
  left: 10px;
  bottom: 8px;
  padding: 1px 8px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 6px;
}
.grid-play {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.25s;
}
.grid-play .el-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #000;
  background: #00ffff;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
}
.grid-play span {
  font-size: 13px;
}
.grid-item:hover .grid-play {
  opacity: 1;
}
.grid-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 12px 14px;
  box-sizing: border-box;
  background: rgba(8, 10, 16, 0.92);
}
.grid-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.grid-tags {
  display: flex;
  gap: 6px;
}
.grid-tag {
  font-size: 11px;
  color: #00ffff;
  background: rgba(0, 255, 255, 0.12);
  padding: 1px 7px;
  border-radius: 4px;
}
.grid-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.grid-category {
  font-size: 12px;
  color: #aaa;
}
.grid-author {
  font-size: 12px;
  color: #777;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.empty {
  padding: 80px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #666;
}
.empty-icon {
  font-size: 40px;
}
.empty p {
  margin: 0;
  font-size: 14px;
}
.load-more-wrap {
  margin-top: 30px;
}
.load-more {
  padding: 10px 40px;
  font-size: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.25s;
}
.load-more:hover {
  color: #000;
  background: #00ffff;
  border-color: #00ffff;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.35);
}

@media (max-width: 1200px) {
  .grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 820px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
