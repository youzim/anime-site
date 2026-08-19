// 本地 mock 层：localStorage 模拟用户表 + 番剧数据，让前端脱离后端独立运行。
// 通过 axios 自定义 adapter 拦截请求，返回与后端一致的 {code, msg, data} 结构。
// 接入真实后端时，只需在 request.js 关闭 mock（VITE_USE_MOCK=false）。
import animeList from './anime'

const USER_KEY = 'mock_users'
const TOKEN_PREFIX = 'mock-token-'

// ---------- 用户表（localStorage 持久化） ----------
function seedUsers() {
  const users = [{ id: 1, username: 'admin', password: '123456' }]
  writeUsers(users)
  return users
}

function readUsers() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : seedUsers()
  } catch {
    return seedUsers()
  }
}

function writeUsers(users) {
  localStorage.setItem(USER_KEY, JSON.stringify(users))
}

// ---------- 统一返回体 ----------
function ok(data = null, msg = 'success') {
  return { code: 200, msg, data }
}
function fail(msg) {
  return { code: 500, msg, data: null }
}

// ---------- token 生成 / 解析（演示用，非真实 JWT） ----------
function makeToken(id, username) {
  const payload = JSON.stringify({ id, username, ts: Date.now() })
  return TOKEN_PREFIX + btoa(encodeURIComponent(payload))
}

function parseToken(token) {
  if (!token || !token.startsWith(TOKEN_PREFIX)) return null
  try {
    const json = decodeURIComponent(atob(token.slice(TOKEN_PREFIX.length)))
    return JSON.parse(json)
  } catch {
    return null
  }
}

// ---------- 各接口处理 ----------
function handleRegister({ body }) {
  const username = String(body?.username ?? '').trim()
  const password = String(body?.password ?? '')
  if (!username || !password) return { status: 200, data: fail('用户名和密码不能为空') }
  if (username.length < 2 || username.length > 16) return { status: 200, data: fail('用户名需为 2-16 个字符') }
  if (password.length < 6) return { status: 200, data: fail('密码至少 6 位') }
  const users = readUsers()
  if (users.some((u) => u.username === username)) return { status: 200, data: fail('用户名已存在') }
  const id = users.length ? Math.max(...users.map((u) => u.id)) + 1 : 1
  users.push({ id, username, password })
  writeUsers(users)
  return { status: 200, data: ok(null, '注册成功') }
}

function handleLogin({ body }) {
  const username = String(body?.username ?? '').trim()
  const password = String(body?.password ?? '')
  if (!username || !password) return { status: 200, data: fail('用户名和密码不能为空') }
  const user = readUsers().find((u) => u.username === username && u.password === password)
  if (!user) return { status: 200, data: fail('用户名或密码错误') }
  return {
    status: 200,
    data: ok({ id: user.id, username: user.username, token: makeToken(user.id, user.username) })
  }
}

function resolveUser(config) {
  const auth = config.headers?.Authorization || config.headers?.authorization || ''
  const token = String(auth).replace(/^Bearer\s+/i, '')
  const payload = parseToken(token)
  if (!payload) return null
  const users = readUsers()
  const user = users.find((u) => u.id === payload.id)
  return user ? { users, user } : null
}

function handleMe(config) {
  const resolved = resolveUser(config)
  if (!resolved) return { status: 401, data: { code: 401, msg: '未登录或登录已过期', data: null } }
  return { status: 200, data: ok({ id: resolved.user.id, username: resolved.user.username }) }
}

function handleUpdateUser(config, body) {
  const resolved = resolveUser(config)
  if (!resolved) return { status: 401, data: { code: 401, msg: '未登录或登录已过期', data: null } }
  const { users, user } = resolved
  const username = String(body?.username ?? '').trim()
  if (!username) return { status: 200, data: fail('用户名不能为空') }
  if (username.length < 2 || username.length > 16) return { status: 200, data: fail('用户名需为 2-16 个字符') }
  if (users.some((u) => u.username === username && u.id !== user.id)) {
    return { status: 200, data: fail('用户名已存在') }
  }
  user.username = username
  writeUsers(users)
  return { status: 200, data: ok({ id: user.id, username: user.username }, '修改成功') }
}

function handleUpdatePassword(config, body) {
  const resolved = resolveUser(config)
  if (!resolved) return { status: 401, data: { code: 401, msg: '未登录或登录已过期', data: null } }
  const { users, user } = resolved
  const oldPassword = String(body?.oldPassword ?? '')
  const newPassword = String(body?.newPassword ?? '')
  if (!oldPassword || !newPassword) return { status: 200, data: fail('请填写完整') }
  if (user.password !== oldPassword) return { status: 200, data: fail('原密码错误') }
  if (newPassword.length < 6) return { status: 200, data: fail('新密码至少 6 位') }
  user.password = newPassword
  writeUsers(users)
  return { status: 200, data: ok(null, '密码修改成功，请重新登录') }
}

function handleAnimeList() {
  return { status: 200, data: ok(animeList) }
}

// 播放量字符串（"3.6亿"）→ 数字
function parseViews(v) {
  const m = /^([\d.]+)([亿万])?$/.exec(String(v ?? ''))
  if (!m) return 0
  let n = parseFloat(m[1])
  if (m[2] === '亿') n *= 1e8
  else if (m[2] === '万') n *= 1e4
  return n
}

function handleAnimeDetail({ path }) {
  const id = Number(path.split('/').filter(Boolean).pop())
  const item = animeList.find((a) => a.id === id)
  if (!item) return { status: 200, data: fail('番剧不存在') }
  return { status: 200, data: ok(item) }
}

function handleAnimeRank({ query }) {
  const sort = query.sort === 'views' ? 'views' : 'rating'
  const list = [...animeList].sort((a, b) =>
    sort === 'views' ? parseViews(b.views) - parseViews(a.views) : b.rating - a.rating
  )
  return { status: 200, data: ok(list) }
}

// ---------- 路由表（支持 :id 路径参数） ----------
const routes = [
  { method: 'POST', pattern: '/auth/register', handler: handleRegister },
  { method: 'POST', pattern: '/auth/login', handler: handleLogin },
  { method: 'GET', pattern: '/user/me', handler: handleMe },
  { method: 'PUT', pattern: '/user/update', handler: handleUpdateUser },
  { method: 'PUT', pattern: '/user/password', handler: handleUpdatePassword },
  { method: 'GET', pattern: '/anime/list', handler: handleAnimeList },
  { method: 'GET', pattern: '/anime/rank', handler: handleAnimeRank },
  { method: 'GET', pattern: '/anime/:id', handler: handleAnimeDetail }
]

function matchRoute(method, path) {
  const exact = routes.find((r) => r.method === method && r.pattern === path)
  if (exact) return exact
  const segments = path.split('/').filter(Boolean)
  for (const r of routes) {
    if (r.method !== method || !r.pattern.includes(':')) continue
    const patternSegs = r.pattern.split('/').filter(Boolean)
    if (segments.length !== patternSegs.length) continue
    let matched = true
    for (let i = 0; i < segments.length; i++) {
      if (!patternSegs[i].startsWith(':') && patternSegs[i] !== segments[i]) {
        matched = false
        break
      }
    }
    if (matched) return r
  }
  return null
}

function parseQuery(url) {
  const query = {}
  try {
    new URL(url, 'http://localhost').searchParams.forEach((v, k) => {
      query[k] = v
    })
  } catch {}
  return query
}

// ---------- mock adapter（axios 自定义 adapter 约定） ----------
export function mockAdapter(config) {
  return new Promise((resolve) => {
    const method = (config.method || 'get').toUpperCase()
    let path = '/'
    try {
      path = new URL(config.url, 'http://localhost').pathname.replace(/^\/api/, '') || '/'
    } catch {
      path = '/'
    }
    let body = {}
    try {
      body = JSON.parse(config.data || '{}')
    } catch {
      body = {}
    }
    const query = parseQuery(config.url)

    const route = matchRoute(method, path)
    // 模拟网络延迟，让加载态/交互更真实
    setTimeout(() => {
      if (!route) {
        resolve({
          data: { code: 404, msg: `mock: 未定义接口 ${method} ${path}`, data: null },
          status: 404,
          statusText: 'Not Found',
          headers: {},
          config
        })
        return
      }
      const { status, data } = route.handler({ config, body, path, query })
      resolve({
        data,
        status,
        statusText: status === 200 ? 'OK' : 'Error',
        headers: {},
        config
      })
    }, 220)
  })
}

export default mockAdapter
