// 观看历史（localStorage 本地持久化，最多保留 50 条）
const KEY = 'watch_history'
const MAX = 50

export function getHistory() {
  try {
    return JSON.parse(localStorage.getItem(KEY) || '[]')
  } catch {
    return []
  }
}

// 新增/更新一条历史：同 id 去重置顶
export function addHistory(item) {
  const list = getHistory().filter((h) => h.animeId !== item.animeId)
  list.unshift({ ...item, watchedAt: Date.now() })
  localStorage.setItem(KEY, JSON.stringify(list.slice(0, MAX)))
}

export function removeHistory(animeId) {
  const list = getHistory().filter((h) => h.animeId !== animeId)
  localStorage.setItem(KEY, JSON.stringify(list))
}

export function clearHistory() {
  localStorage.removeItem(KEY)
}

// 格式化观看时间：今天 HH:mm / 昨天 / M月D日
export function formatWatchedAt(ts) {
  const d = new Date(ts)
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  const yesterday = new Date(now.getTime() - 86400000)
  const isYesterday = d.toDateString() === yesterday.toDateString()
  const pad = (n) => String(n).padStart(2, '0')
  if (sameDay) return `今天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (isYesterday) return `昨天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getMonth() + 1}月${d.getDate()}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
