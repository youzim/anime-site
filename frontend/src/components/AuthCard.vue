<script setup>
import { ElButton } from 'element-plus'

defineProps({
  mode: { type: String, required: true },   // 'login' | 'register'
  userData: { type: Object, required: true }
})

defineEmits(['submit', 'toggle'])
</script>

<template>
  <div class="card" :class="`card--${mode}`">
    <div class="card__spark card__spark--tl">✦</div>
    <div class="card__spark card__spark--br">✦</div>

    <div class="card__icon">
      <svg viewBox="0 0 24 24" width="44" height="44" aria-hidden="true">
        <path d="M12 1.8l2.6 6.6 7 .4-5.4 4.6 1.7 6.8L12 16.9 6.1 20.2l1.7-6.8L2.4 8.8l7-.4z" fill="#fff"/>
      </svg>
    </div>

    <h2 class="card__title">{{ mode === 'login' ? '星空物语' : '加入星空' }}</h2>
    <p class="card__subtitle">{{ mode === 'login' ? '登录你的奇幻世界' : '注册开启你的星空之旅' }}</p>

    <div class="field">
      <label class="field__label">用户名</label>
      <div class="input-wrap">
        <svg class="input-icon" viewBox="0 0 24 24" fill="none" width="18" height="18" aria-hidden="true">
          <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.8"/>
          <path d="M4 20c1.6-3.8 4.8-5.8 8-5.8s6.4 2 8 5.8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        <input type="text" placeholder="请输入用户名" v-model="userData.username" class="input-field" />
      </div>
    </div>

    <div class="field">
      <label class="field__label">密码</label>
      <div class="input-wrap">
        <svg class="input-icon" viewBox="0 0 24 24" fill="none" width="18" height="18" aria-hidden="true">
          <rect x="5" y="10.5" width="14" height="10" rx="3" stroke="currentColor" stroke-width="1.8"/>
          <path d="M8 10.5V7.5a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        <input type="password" placeholder="请输入密码" v-model="userData.password" class="input-field" />
      </div>
    </div>

    <!-- 注册模式：确认密码 -->
    <div class="field" v-if="mode === 'register'">
      <label class="field__label">确认密码</label>
      <div class="input-wrap">
        <svg class="input-icon" viewBox="0 0 24 24" fill="none" width="18" height="18" aria-hidden="true">
          <path d="M12 3l7 4v5c0 4.5-3 8-7 9-4-1-7-4.5-7-9V7l7-4z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
          <path d="M9.5 12l2 2 3.5-3.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <input type="password" placeholder="请再次输入密码" v-model="userData.confirmPassword" class="input-field" />
      </div>
    </div>

    <!-- 登录模式：记住我 -->
    <label class="remember" v-if="mode === 'login'">
      <input type="checkbox" v-model="userData.remember" />
      <span>记住我</span>
    </label>

    <div class="actions">
      <ElButton class="btn-primary" @click="$emit('submit')">
        {{ mode === 'login' ? '登 录' : '注 册' }}
      </ElButton>
    </div>

    <p class="switch-link" @click="$emit('toggle')">
      {{ mode === 'login' ? '还没有账号？' : '已有账号？' }}
      <span class="switch-link__action">{{ mode === 'login' ? '去注册' : '去登录' }}</span>
    </p>
  </div>
</template>

<style scoped>
/* ===== 卡片 =====
   颜色全部走 var(--xxx)，主题由外层 layer 提供：
   .layer--login 冷紫主题，.layer--register 暖粉主题 */
.card {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 38px 38px 34px;
  border-radius: 24px;
  text-align: center;
  color: #fff;
  background: var(--card-bg);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
  box-shadow:
    0 24px 48px rgba(0, 0, 0, 0.5),
    0 0 80px var(--accent-soft),
    inset 0 1px 0 rgba(255, 255, 255, 0.12);
  animation: cardIn 0.8s cubic-bezier(0.22, 0.61, 0.36, 1);
  overflow: hidden;
}

/* 顶部柔光 */
.card::after {
  content: '';
  position: absolute;
  top: -55%;
  left: 50%;
  transform: translateX(-50%);
  width: 220%;
  height: 150%;
  background: radial-gradient(closest-side, var(--glow-top), transparent 70%);
  pointer-events: none;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 角落装饰星 */
.card__spark {
  position: absolute;
  z-index: 3;
  color: rgba(255, 255, 255, 0.4);
  font-size: 16px;
  pointer-events: none;
  animation: sparkPulse 3s ease-in-out infinite;
}

.card__spark--tl { top: 14px; left: 18px; }
.card__spark--br { bottom: 14px; right: 18px; animation-delay: 1.5s; }

@keyframes sparkPulse {
  0%, 100% { opacity: 0.2; transform: scale(0.85) rotate(0deg); }
  50% { opacity: 0.7; transform: scale(1.15) rotate(90deg); }
}

/* 顶部星星图标 */
.card__icon {
  position: relative;
  z-index: 1;
  width: 78px;
  height: 78px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--icon-bg);
  box-shadow: 0 0 26px var(--icon-glow), inset 0 1px 0 rgba(255, 255, 255, 0.25);
  animation: iconFloat 4s ease-in-out infinite;
}

.card__icon svg {
  filter: drop-shadow(0 0 6px rgba(255, 255, 255, 0.85));
  animation: iconSpin 14s linear infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes iconSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 标题（渐变流动字） */
.card__title {
  position: relative;
  z-index: 1;
  font-size: 30px;
  font-weight: 700;
  margin: 0 0 6px;
  letter-spacing: 4px;
  background: var(--title-grad);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: titleShimmer 5s linear infinite;
}

@keyframes titleShimmer {
  to { background-position: 200% center; }
}

.card__subtitle {
  position: relative;
  z-index: 1;
  font-size: 14px;
  opacity: 0.75;
  margin: 0 0 30px;
  font-weight: 300;
  letter-spacing: 1px;
}

/* 输入区 */
.field {
  position: relative;
  z-index: 1;
  text-align: left;
  margin-bottom: 20px;
}

.field__label {
  display: block;
  font-size: 13px;
  margin: 0 0 8px 4px;
  opacity: 0.85;
  letter-spacing: 1px;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: rgba(255, 255, 255, 0.45);
  pointer-events: none;
  transition: color 0.3s;
}

.input-wrap:focus-within .input-icon {
  color: var(--accent);
}

.input-field {
  width: 100%;
  height: 48px;
  padding: 0 18px 0 42px;
  font-size: 15px;
  color: #fff;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 14px;
  outline: none;
  transition: 0.3s;
  box-sizing: border-box;
}

.input-field::placeholder {
  color: rgba(255, 255, 255, 0.32);
}

.input-wrap:focus-within .input-field {
  background: rgba(255, 255, 255, 0.12);
  border-color: transparent;
  box-shadow: 0 0 0 1.5px var(--accent), 0 0 18px var(--accent-soft);
}

/* 记住我 */
.remember {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0 4px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  cursor: pointer;
  user-select: none;
}
.remember input {
  width: 15px;
  height: 15px;
  accent-color: var(--accent);
  cursor: pointer;
}
.remember:hover {
  color: #fff;
}

/* 按钮（通栏） */
.actions {
  position: relative;
  z-index: 1;
  margin-top: 30px;
}

:deep(.btn-primary) {
  position: relative;
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 40px;
  border: none;
  overflow: hidden;
  letter-spacing: 4px;
  background: var(--btn-grad);
  color: #fff;
  box-shadow: 0 8px 24px var(--btn-glow);
  transition: transform 0.3s, box-shadow 0.3s;
}

:deep(.btn-primary:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px var(--btn-glow);
}

:deep(.btn-primary::after) {
  content: '';
  position: absolute;
  top: 0;
  left: -80%;
  width: 50%;
  height: 100%;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.45), transparent);
  transform: skewX(-20deg);
  transition: left 0.6s ease;
}

:deep(.btn-primary:hover::after) {
  left: 130%;
}

:deep(.btn-primary:active) {
  transform: translateY(1px) scale(0.98);
}

/* 切换链接：整行可点击，热区向下扩展；"去登录/去注册"恢复主题色加粗 */
.switch-link {
  position: relative;
  z-index: 1;
  margin: 12px 0 0;
  padding: 10px 0 22px;        /* 上 10px、下 22px 的隐形点击区，往下多留一些 */
  font-size: 14px;
  color: #fff;
  letter-spacing: 1px;
  cursor: pointer;
  user-select: none;
  transition: opacity 0.3s;
}

.switch-link:hover {
  opacity: 0.85;
}

.switch-link__action {
  color: var(--accent);
  font-weight: 600;
  margin-left: 4px;
  transition: text-shadow 0.3s;
}

.switch-link:hover .switch-link__action {
  text-shadow: 0 0 12px var(--accent);
}
</style>
