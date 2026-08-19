<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import AuthCard from '@/components/AuthCard.vue'
import { login, register } from '@/api/api'
import { useUserStore } from '@/stores/user'
import bgImg from '@/assets/login.jpg'
import registerBg from '@/assets/register-winter.jpg'
import { useRouter } from 'vue-router'
// ============ 登录 / 注册切换 ============
const isLogin = ref(true)

// 撕开式切换：登录层(顶层)的 clip-path 由 JS 驱动，一条斜线从右下往左上扫过
const loginLayer = ref(null)   // 登录层 DOM
let clipC = 300                // 斜线位置 x+y=clipC：300=登录全显，-50=完全露出注册
let peelAnim = null            // 当前缓动：{ from, to, start, dur }
const PEEL_DUR = 1600          // 切换时长(ms)：点了立刻动，收尾放缓

const router=useRouter()

const userData = ref({
  username: '',
  password: '',
  confirmPassword: '',
  remember: false
})

async function loginTo() {
  if (!userData.value.username || !userData.value.password) {
    ElMessage.error('用户名或密码不能为空')
    return
  }
  try {
    const res = await login(userData.value)
    // 记住我：保存用户名密码（练习项目明文存储，正式项目请勿照做）
    if (userData.value.remember) {
      localStorage.setItem(
        'remembered',
        JSON.stringify({ username: userData.value.username, password: userData.value.password })
      )
    } else {
      localStorage.removeItem('remembered')
    }
    const userStore = useUserStore()
    userStore.setLogin(res.token, { id: res.id, username: res.username })
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    // 错误已由 request 拦截器统一提示
  }
}

async function registerTo() {
  const { username, password, confirmPassword } = userData.value
  if (!username || !password) {
    ElMessage.error('用户名或密码不能为空')
    return
  }
  if (password !== confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (password.length < 6) {
    ElMessage.error('密码至少 6 位')
    return
  }
  try {
    await register({ username, password })
    ElMessage.success('注册成功，请登录')
    userData.value.password = ''
    userData.value.confirmPassword = ''
    // 切回登录并带入刚注册的用户名
    toggleMode()
  } catch (error) {
    // 错误已由 request 拦截器统一提示
  }
}

function toggleMode() {
  isLogin.value = !isLogin.value
  peelAnim = {
    from: clipC,
    to: isLogin.value ? 300 : -50,
    start: performance.now(),
    dur: PEEL_DUR
  }
}

// ============ 背景：登录=本地星空图，注册=本地冬天场景图（无落雪） ============
const loginBgStyle = computed(() => ({
  backgroundImage: `linear-gradient(rgba(8, 8, 30, 0.35), rgba(8, 8, 30, 0.35)), url("${bgImg}")`
}))

const registerBgStyle = computed(() => ({
  // 本地冬天图 + 轻冷蓝滤镜（让 canvas 的雪花更明显）
  backgroundImage: `linear-gradient(rgba(28, 50, 90, 0.35), rgba(12, 28, 60, 0.6)), url("${registerBg}")`
}))

// ============ Canvas：登录页星星 / 注册页雪花（原生，无需任何包） ============
const starCanvas = ref(null)
const snowCanvas = ref(null)
const cursorCanvas = ref(null)
let ctx = null
let snowCtx = null
let cursorCtx = null
let mouseX = 0
let mouseY = 0
let mouseInited = false
const wandSparks = []   // 魔法棒移动时留下的小火星
let width = 0
let height = 0
let rafId = 0
let running = true
let lastT = 0
const DPR = Math.min(window.devicePixelRatio || 1, 2)

// 登录页：星星
const stars = []
const shooters = []
const bursts = []
const AMBIENT_COUNT = 130

// 注册页：雪花
const snowflakes = []
const SNOW_COUNT = 130

function rand(a, b) { return a + Math.random() * (b - a) }

function resize() {
  width = window.innerWidth
  height = window.innerHeight
  const sc = starCanvas.value
  sc.width = width * DPR
  sc.height = height * DPR
  ctx.setTransform(DPR, 0, 0, DPR, 0, 0)
  const nc = snowCanvas.value
  nc.width = width * DPR
  nc.height = height * DPR
  snowCtx.setTransform(DPR, 0, 0, DPR, 0, 0)
  const cc = cursorCanvas.value
  cc.width = width * DPR
  cc.height = height * DPR
  cursorCtx.setTransform(DPR, 0, 0, DPR, 0, 0)
}

/* ---------- 星星（登录页） ---------- */
function spawnStar() {
  const fromTop = Math.random() < 0.5
  return {
    x: fromTop ? rand(-5, width) : -5,
    y: fromTop ? -5 : rand(-5, height),
    r: rand(0.6, 2.2),
    vx: rand(0.25, 0.85),
    vy: rand(0.45, 1.2),
    tw: rand(0.015, 0.05),
    ph: rand(0, Math.PI * 2),
    a: rand(0.4, 0.95)
  }
}

function spawnShooter() {
  const ang = rand(Math.PI / 6, Math.PI / 3)
  const sp = rand(7, 11)
  shooters.push({
    x: rand(0, width * 0.5),
    y: rand(0, height * 0.3),
    vx: Math.cos(ang) * sp,
    vy: Math.sin(ang) * sp,
    len: rand(130, 230),
    life: 1
  })
}

function spawnBurst(x, y) {
  const n = Math.floor(rand(24, 36))
  const colors = ['#ffffff', '#fff7d6', '#ffe9a8', '#ff9ed6', '#c4a6ff', '#9ef0ff']
  const parts = []
  for (let i = 0; i < n; i++) {
    const ang = rand(0, Math.PI * 2)
    const sp = rand(1.5, 6.5)
    parts.push({
      x, y,
      vx: Math.cos(ang) * sp,
      vy: Math.sin(ang) * sp,
      r: rand(1, 2.8),
      color: colors[(Math.random() * colors.length) | 0],
      life: 1,
      decay: rand(0.014, 0.03)
    })
  }
  bursts.push({ x, y, life: 1, r: 2, parts })
}

function onWindowClick(e) {
  spawnBurst(e.clientX, e.clientY)
}

/* ---------- 雪花（注册页） ---------- */
function spawnSnow() {
  return {
    x: rand(-5, width + 5),
    y: rand(-height, 0),        // 从屏幕上方（含屏幕外）落下
    r: rand(0.8, 3.2),
    vy: rand(0.4, 1.4),          // 下落速度
    sway: rand(0.4, 1.6),        // 左右摆动幅度
    ph: rand(0, Math.PI * 2),
    sp: rand(0.005, 0.02),       // 摆动频率
    a: rand(0.35, 0.85)
  }
}

/* ---------- 撕开裁剪 ---------- */
// 可见区域 = 斜线 x+y=c 的左上侧
function applyClip(c) {
  const layer = loginLayer.value
  if (!layer) return
  let poly
  if (c <= 0) {
    poly = 'polygon(0% 0%, 0% 0%, 0% 0%, 0% 0%)'                    // 全隐藏
  } else if (c >= 200) {
    poly = 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)'            // 全显示
  } else if (c < 100) {
    poly = `polygon(0% 0%, ${c}% 0%, 0% ${c}%)`                     // 三角形：切上边+左边
  } else {
    poly = `polygon(0% 0%, 100% 0%, 100% ${c - 100}%, ${c - 100}% 100%, 0% 100%)` // 五边形：切右边+下边
  }
  layer.style.clipPath = poly
}

// 柔和缓动：easeOutCubic（先快后慢）
// 开头立刻有动作（没有"点了半天不动"的起始段），收尾缓慢落定，柔和
function easeOutCubic(p) {
  return 1 - Math.pow(1 - p, 3)
}

/* ---------- 魔法棒鼠标（原生光标隐藏，canvas 画一根魔法棒跟着鼠标走） ---------- */
// 阻止右键菜单弹出，避免原生光标跟着右键一起冒出来
function onContextMenu(e) {
  e.preventDefault()
}

function onMouseMove(e) {
  mouseX = e.clientX
  mouseY = e.clientY
  mouseInited = true
  // 移动时随机抛几颗小火星
  if (wandSparks.length < 60 && Math.random() < 0.6) {
    wandSparks.push({
      x: mouseX + rand(-3, 3),
      y: mouseY + rand(-3, 3),
      vx: rand(-0.9, 0.9),
      vy: rand(-0.9, 0.9),
      life: 1,
      decay: rand(0.03, 0.06),
      r: rand(1, 2.2)
    })
  }
}

// 五角星（杖头）
function drawStar(cx, cy, outer, inner, color) {
  cursorCtx.beginPath()
  for (let i = 0; i < 10; i++) {
    const r = i % 2 === 0 ? outer : inner
    const a = (Math.PI / 5) * i - Math.PI / 2
    const px = cx + Math.cos(a) * r
    const py = cy + Math.sin(a) * r
    if (i === 0) cursorCtx.moveTo(px, py)
    else cursorCtx.lineTo(px, py)
  }
  cursorCtx.closePath()
  cursorCtx.fillStyle = color
  cursorCtx.fill()
}

// 每帧在鼠标位置画魔法棒（杖尖星星对准鼠标点）
function drawWand(t) {
  cursorCtx.clearRect(0, 0, width, height)
  if (!mouseInited) return

  const sway = Math.sin(t / 500) * 6   // 轻微摆动
  const tipX = mouseX
  const tipY = mouseY

  cursorCtx.globalCompositeOperation = 'lighter'
  // 杖身（金色）：从杖尖往右下延伸，魔法棒指向左上角
  cursorCtx.strokeStyle = '#e8b64c'
  cursorCtx.lineWidth = 4
  cursorCtx.lineCap = 'round'
  cursorCtx.beginPath()
  cursorCtx.moveTo(tipX + 26 + sway, tipY + 24)
  cursorCtx.lineTo(tipX + 2, tipY + 2)
  cursorCtx.stroke()
  // 杖身暗描边
  cursorCtx.strokeStyle = 'rgba(80, 40, 10, 0.45)'
  cursorCtx.lineWidth = 1
  cursorCtx.beginPath()
  cursorCtx.moveTo(tipX + 26 + sway, tipY + 24)
  cursorCtx.lineTo(tipX + 2, tipY + 2)
  cursorCtx.stroke()
  // 杖头星星光晕
  cursorCtx.beginPath()
  cursorCtx.arc(tipX, tipY, 15, 0, Math.PI * 2)
  cursorCtx.fillStyle = 'rgba(255, 215, 130, 0.25)'
  cursorCtx.fill()
  // 杖头星星
  drawStar(tipX, tipY, 9, 4, '#ffd86b')

  // 移动留下的火星
  for (let i = wandSparks.length - 1; i >= 0; i--) {
    const s = wandSparks[i]
    s.x += s.vx
    s.y += s.vy
    s.life -= s.decay
    if (s.life <= 0) { wandSparks.splice(i, 1); continue }
    cursorCtx.beginPath()
    cursorCtx.arc(s.x, s.y, s.r * s.life, 0, Math.PI * 2)
    cursorCtx.fillStyle = `rgba(255, 230, 160, ${Math.max(0, s.life)})`
    cursorCtx.fill()
  }
  cursorCtx.globalCompositeOperation = 'source-over'
}

function loop(t) {
  if (!running) return
  const dt = lastT ? Math.min((t - lastT) / 16.7, 3) : 1
  lastT = t

  // ---- 撕开切换（时间驱动，柔和慢速） ----
  if (peelAnim) {
    const p = Math.min((t - peelAnim.start) / peelAnim.dur, 1)
    clipC = peelAnim.from + (peelAnim.to - peelAnim.from) * easeOutCubic(p)
    applyClip(clipC)
    if (p >= 1) peelAnim = null
  }

  // ---- 登录页：星星 ----
  ctx.clearRect(0, 0, width, height)

  while (stars.length < AMBIENT_COUNT) stars.push(spawnStar())
  for (const s of stars) {
    s.x += s.vx * dt
    s.y += s.vy * dt
    s.ph += s.tw * dt
    if (s.x > width + 12 || s.y > height + 12) Object.assign(s, spawnStar())
  }

  if (Math.random() < 0.012 * dt) spawnShooter()
  for (let i = shooters.length - 1; i >= 0; i--) {
    const sh = shooters[i]
    sh.x += sh.vx * dt
    sh.y += sh.vy * dt
    sh.life -= 0.02 * dt
    if (sh.life <= 0 || sh.x > width + 300 || sh.y > height + 300) shooters.splice(i, 1)
  }

  for (let i = bursts.length - 1; i >= 0; i--) {
    const b = bursts[i]
    b.life -= 0.02 * dt
    b.r += 1.2 * dt
    for (const p of b.parts) {
      p.x += p.vx * dt
      p.y += p.vy * dt
      p.vx *= 0.96
      p.vy *= 0.96
      p.life -= p.decay * dt
    }
    if (b.life <= 0) bursts.splice(i, 1)
  }

  ctx.globalCompositeOperation = 'lighter'
  for (const s of stars) {
    const alpha = Math.max(0, s.a * (0.5 + 0.5 * Math.sin(s.ph)))
    ctx.beginPath()
    ctx.arc(s.x, s.y, s.r * 3, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(180, 205, 255, ${alpha * 0.22})`
    ctx.fill()
    ctx.beginPath()
    ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255, 255, 255, ${alpha})`
    ctx.fill()
  }
  for (const sh of shooters) {
    const tail = Math.hypot(sh.vx, sh.vy)
    const nx = sh.vx / tail
    const ny = sh.vy / tail
    const g = ctx.createLinearGradient(sh.x, sh.y, sh.x - nx * sh.len, sh.y - ny * sh.len)
    g.addColorStop(0, `rgba(255, 255, 255, ${Math.max(0, sh.life)})`)
    g.addColorStop(1, 'rgba(255, 255, 255, 0)')
    ctx.strokeStyle = g
    ctx.lineWidth = 2
    ctx.lineCap = 'round'
    ctx.beginPath()
    ctx.moveTo(sh.x, sh.y)
    ctx.lineTo(sh.x - nx * sh.len, sh.y - ny * sh.len)
    ctx.stroke()
  }
  for (const b of bursts) {
    ctx.beginPath()
    ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2)
    ctx.strokeStyle = `rgba(255, 230, 200, ${Math.max(0, b.life) * 0.6})`
    ctx.lineWidth = 1.5
    ctx.stroke()
    for (const p of b.parts) {
      if (p.life <= 0) continue
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r * p.life, 0, Math.PI * 2)
      ctx.fillStyle = p.color
      ctx.globalAlpha = Math.max(0, p.life)
      ctx.fill()
    }
    ctx.globalAlpha = 1
  }
  ctx.globalCompositeOperation = 'source-over'

  // ---- 注册页：雪花 ----
  snowCtx.clearRect(0, 0, width, height)
  for (const f of snowflakes) {
    f.y += f.vy * dt
    f.x += Math.cos(f.ph) * f.sway * 0.5 * dt
    f.ph += f.sp * dt
    if (f.y > height + 12) Object.assign(f, spawnSnow())
  }

  snowCtx.globalCompositeOperation = 'lighter'
  for (const f of snowflakes) {
    snowCtx.beginPath()
    snowCtx.arc(f.x, f.y, f.r * 2.4, 0, Math.PI * 2)
    snowCtx.fillStyle = `rgba(190, 225, 255, ${f.a * 0.16})`
    snowCtx.fill()
    snowCtx.beginPath()
    snowCtx.arc(f.x, f.y, f.r, 0, Math.PI * 2)
    snowCtx.fillStyle = `rgba(255, 255, 255, ${f.a})`
    snowCtx.fill()
  }
  snowCtx.globalCompositeOperation = 'source-over'

  // ---- 魔法棒鼠标 ----
  drawWand(t)

  rafId = requestAnimationFrame(loop)
}

onMounted(() => {
  // 记住我：回填上次保存的用户名密码
  const remembered = localStorage.getItem('remembered')
  if (remembered) {
    try {
      const { username, password } = JSON.parse(remembered)
      userData.value.username = username
      userData.value.password = password
      userData.value.remember = true
    } catch {}
  }

  ctx = starCanvas.value.getContext('2d')
  snowCtx = snowCanvas.value.getContext('2d')
  cursorCtx = cursorCanvas.value.getContext('2d')
  resize()
  for (let i = 0; i < SNOW_COUNT; i++) snowflakes.push(spawnSnow())
  window.addEventListener('resize', resize)
  window.addEventListener('click', onWindowClick, true)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('contextmenu', onContextMenu)
  lastT = 0
  clipC = 300
  applyClip(clipC)
  rafId = requestAnimationFrame(loop)
})

onUnmounted(() => {
  running = false
  cancelAnimationFrame(rafId)
  window.removeEventListener('resize', resize)
  window.removeEventListener('click', onWindowClick, true)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('contextmenu', onContextMenu)
})
</script>

<template>
  <div class="page" :class="isLogin ? 'page--login' : 'page--register'">
    <!-- 内层：注册页（垫底）—— 动漫冬天场景 + 雪花 -->
    <section class="layer layer--register">
      <div class="bg bg--register" :style="registerBgStyle"></div>
      <canvas ref="snowCanvas" class="layer-canvas"></canvas>
      <AuthCard mode="register" :user-data="userData" @submit="registerTo" @toggle="toggleMode" />
    </section>

    <!-- 外层：登录页（顶层，被斜线撕开）—— 星空 + 星星 -->
    <section ref="loginLayer" class="layer layer--login">
      <div class="bg bg--login" :style="loginBgStyle"></div>
      <canvas ref="starCanvas" class="layer-canvas"></canvas>
      <AuthCard mode="login" :user-data="userData" @submit="loginTo" @toggle="toggleMode" />
    </section>

    <!-- 魔法棒鼠标（最顶层） -->
    <canvas ref="cursorCanvas" class="cursor-canvas"></canvas>
  </div>
</template>

<style scoped>
.page {
  width: 100%;
  height: 100vh;
  background-color: #0b0b2e;
  position: relative;
  overflow: hidden;
  font-family: 'Quicksand', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===== 两层：登录层（星空冷紫）在上，注册层（冬天冰蓝）在下 ===== */
.layer {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 登录层主题变量（星空冷紫） */
.layer--login {
  --accent: #ab6fff;
  --accent-soft: rgba(168, 130, 255, 0.55);
  --glow-top: rgba(170, 120, 255, 0.3);
  --card-bg: linear-gradient(160deg, rgba(32, 26, 72, 0.6), rgba(16, 12, 44, 0.74));
  --icon-bg: radial-gradient(circle at 30% 25%, rgba(168, 130, 255, 0.5), rgba(70, 42, 150, 0.4));
  --icon-glow: rgba(168, 130, 255, 0.55);
  --title-grad: linear-gradient(90deg, #d4bfff, #ffb3e6, #ffe9a8, #d4bfff);
  --btn-grad: linear-gradient(135deg, #ab6fff, #ff7eb3);
  --btn-glow: rgba(171, 111, 255, 0.45);

  z-index: 5;
  /* 撕开裁剪由 JS（applyClip）驱动：一条斜线从右下往左上扫过 */
}

/* 注册层主题变量（冬天冰蓝） */
.layer--register {
  --accent: #8fd3ff;
  --accent-soft: rgba(143, 211, 255, 0.55);
  --glow-top: rgba(170, 225, 255, 0.32);
  --card-bg: linear-gradient(160deg, rgba(30, 55, 95, 0.62), rgba(14, 30, 60, 0.76));
  --icon-bg: radial-gradient(circle at 30% 25%, rgba(150, 210, 255, 0.5), rgba(60, 110, 170, 0.4));
  --icon-glow: rgba(143, 211, 255, 0.55);
  --title-grad: linear-gradient(90deg, #d6f0ff, #a8d8ff, #eaf6ff, #d6f0ff);
  --btn-grad: linear-gradient(135deg, #7cc8ff, #c9e6ff);
  --btn-glow: rgba(124, 200, 255, 0.5);

  z-index: 4;
}

/* ===== 每层的背景 ===== */
.bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  pointer-events: none;
}

.bg--login { background-color: #0b0b2e; }
.bg--register { background-color: #1f3a5f; }

/* ===== 每层自己的特效画布（在背景之上、卡片之下，不挡交互） ===== */
.layer-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  display: block;
}

/* ===== 魔法棒鼠标 ===== */
/* 隐藏原生光标，改由 canvas 画魔法棒 */
.page,
.page * {
  cursor: none !important;
}

.cursor-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 20;
  display: block;
}
</style>
