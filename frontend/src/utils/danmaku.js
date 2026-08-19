// 弹幕数据（localStorage 本地持久化，按 番剧id:集数 存储）
const KEY = 'danmaku_v1'
const MAX_PER_EP = 300

function all() {
  try {
    return JSON.parse(localStorage.getItem(KEY) || '{}')
  } catch {
    return {}
  }
}

function save(map) {
  localStorage.setItem(KEY, JSON.stringify(map))
}

function epKey(animeId, ep) {
  return `${animeId}:${ep}`
}

// 取某集的弹幕列表
export function getDanmaku(animeId, ep) {
  return all()[epKey(animeId, ep)] || []
}

// 新增一条弹幕：{ text, time(视频秒), color, user }，返回新弹幕对象
export function addDanmaku(animeId, ep, item) {
  const map = all()
  const key = epKey(animeId, ep)
  const list = map[key] || []
  const danmaku = {
    id: Date.now() + Math.random().toString(36).slice(2, 6),
    text: String(item.text).slice(0, 50),
    time: Number(item.time) || 0,
    color: item.color || '#ffffff',
    user: item.user || '游客',
    ts: Date.now()
  }
  list.push(danmaku)
  map[key] = list.slice(-MAX_PER_EP)
  save(map)
  return danmaku
}

// 弹幕颜色池（随机取）
export const DANMAKU_COLORS = ['#ffffff', '#ffd86b', '#7ee8ff', '#ff9ed6', '#aef08a', '#ff8a80', '#c4a6ff']
