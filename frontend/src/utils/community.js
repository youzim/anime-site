// 社区数据（localStorage 本地持久化）：视频评论 + 留言板帖子，均支持点赞

const COMMENTS_KEY = 'community_comments_v1'
const POSTS_KEY = 'community_posts_v1'

// ---------- 通用 ----------
function load(key, fallback) {
  try {
    return JSON.parse(localStorage.getItem(key)) ?? fallback
  } catch {
    return fallback
  }
}
function save(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

function newId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

// 点赞切换：返回是否已点赞
function toggleLike(likeList, user) {
  const i = likeList.indexOf(user)
  if (i >= 0) {
    likeList.splice(i, 1)
    return false
  }
  likeList.push(user)
  return true
}

// ================= 视频评论 =================
export function getVideoComments(animeId) {
  const map = load(COMMENTS_KEY, {})
  return map[animeId] || []
}

export function addVideoComment(animeId, { content, user }) {
  const map = load(COMMENTS_KEY, {})
  const list = map[animeId] || []
  list.unshift({
    id: newId(),
    user: user || '游客',
    content: String(content).slice(0, 500),
    likes: 0,
    likedBy: [],
    time: Date.now()
  })
  map[animeId] = list
  save(COMMENTS_KEY, map)
  return list
}

export function toggleVideoCommentLike(animeId, commentId, user) {
  const map = load(COMMENTS_KEY, {})
  const list = map[animeId] || []
  const comment = list.find((c) => c.id === commentId)
  if (!comment) return false
  const liked = toggleLike(comment.likedBy, user)
  comment.likes = comment.likedBy.length
  map[animeId] = list
  save(COMMENTS_KEY, map)
  return liked
}

export function deleteVideoComment(animeId, commentId) {
  const map = load(COMMENTS_KEY, {})
  map[animeId] = (map[animeId] || []).filter((c) => c.id !== commentId)
  save(COMMENTS_KEY, map)
}

// ================= 留言板帖子 =================
export function getPosts() {
  return load(POSTS_KEY, [])
}

export function addPost({ title, content, user }) {
  const posts = load(POSTS_KEY, [])
  posts.unshift({
    id: newId(),
    user: user || '游客',
    title: String(title).slice(0, 60),
    content: String(content).slice(0, 2000),
    likes: 0,
    likedBy: [],
    comments: [],
    time: Date.now()
  })
  save(POSTS_KEY, posts)
  return posts
}

export function deletePost(postId, user) {
  const posts = load(POSTS_KEY, [])
  const post = posts.find((p) => p.id === postId)
  if (!post || post.user !== user) return false
  save(POSTS_KEY, posts.filter((p) => p.id !== postId))
  return true
}

export function togglePostLike(postId, user) {
  const posts = load(POSTS_KEY, [])
  const post = posts.find((p) => p.id === postId)
  if (!post) return false
  const liked = toggleLike(post.likedBy, user)
  post.likes = post.likedBy.length
  save(POSTS_KEY, posts)
  return liked
}

export function addPostComment(postId, { content, user }) {
  const posts = load(POSTS_KEY, [])
  const post = posts.find((p) => p.id === postId)
  if (!post) return null
  const comment = {
    id: newId(),
    user: user || '游客',
    content: String(content).slice(0, 500),
    likes: 0,
    likedBy: [],
    time: Date.now()
  }
  post.comments.unshift(comment)
  save(POSTS_KEY, posts)
  return comment
}

export function togglePostCommentLike(postId, commentId, user) {
  const posts = load(POSTS_KEY, [])
  const post = posts.find((p) => p.id === postId)
  if (!post) return false
  const comment = post.comments.find((c) => c.id === commentId)
  if (!comment) return false
  const liked = toggleLike(comment.likedBy, user)
  comment.likes = comment.likedBy.length
  save(POSTS_KEY, posts)
  return liked
}

// ================= 时间格式化 =================
export function formatTime(ts) {
  const d = new Date(ts)
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `今天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (d.getFullYear() === now.getFullYear()) return `${d.getMonth() + 1}月${d.getDate()}日 ${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
