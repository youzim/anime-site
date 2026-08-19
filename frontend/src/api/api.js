import request from '@/request/request'

// ---------- 认证 ----------
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)

// ---------- 用户信息 ----------
export const getUserInfo = () => request.get('/user/me')
export const getUserStats = () => request.get('/user/stats')
export const updateUserInfo = (data) => request.put('/user/update', data)
export const updatePassword = (data) => request.put('/user/password', data)

// ---------- 番剧（公开） ----------
// 分页列表：{ page, size, category, keyword, sort: views|rating|newest }
export const getAnimeList = (params = {}) => request.get('/anime/list', { params })
export const getAnimeDetail = (id) => request.get(`/anime/${id}`)
export const getRankList = (sort = 'rating', limit = 20) =>
  request.get('/anime/rank', { params: { sort, limit } })
export const getBanner = () => request.get('/anime/banner')
export const getRelated = (id) => request.get(`/anime/related/${id}`)

// ---------- 弹幕 ----------
export const getDanmaku = (animeId, ep) =>
  request.get('/danmaku', { params: { animeId, ep } })
export const sendDanmaku = (data) => request.post('/danmaku', data)

// ---------- 观看历史（需登录） ----------
export const getHistory = () => request.get('/history')
export const addHistoryRecord = (data) => request.post('/history', data)
export const deleteHistoryRecord = (animeId) => request.delete(`/history/${animeId}`)
export const clearHistoryRecord = () => request.delete('/history')

// ---------- 收藏（需登录） ----------
export const getFavorites = () => request.get('/favorite')
export const toggleFavorite = (animeId) => request.post(`/favorite/${animeId}`)
export const getFavoriteStatus = (animeId) => request.get(`/favorite/status/${animeId}`)

// ---------- 评论 ----------
export const getComments = (animeId) => request.get('/comment', { params: { animeId } })
export const addComment = (data) => request.post('/comment', data)
export const deleteComment = (id) => request.delete(`/comment/${id}`)
export const likeComment = (id) => request.post(`/comment/${id}/like`)

// ---------- 社区留言板 ----------
export const getPosts = (page = 1, size = 20) =>
  request.get('/post', { params: { page, size } })
export const getPostDetail = (id) => request.get(`/post/${id}`)
export const addPost = (data) => request.post('/post', data)
export const deletePost = (id) => request.delete(`/post/${id}`)
export const likePost = (id) => request.post(`/post/${id}/like`)
export const addPostComment = (postId, data) => request.post(`/post/${postId}/comment`, data)
export const deletePostComment = (id) => request.delete(`/post/comment/${id}`)
export const likePostComment = (id) => request.post(`/post/comment/${id}/like`)

// ---------- 工具：播放量数字格式化（123456789 → 1.2亿 / 34万） ----------
export function formatViews(v) {
  const n = Number(v || 0)
  if (n >= 100000000) return (n / 100000000).toFixed(1).replace(/\.0$/, '') + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  return String(n)
}
