import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 路由切换滚动行为：默认回顶部；播放页内切集保持位置；浏览器前进/后退恢复原位置
  scrollBehavior(to, from, savedPosition) {
    if (to.name === 'player' && from.name === 'player') return {}
    if (savedPosition) return savedPosition
    return { top: 0 }
  },
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    {
      path: '/',
      component: () => import('@/components/LayoutView.vue'),
      children: [
        { path: '', redirect: '/home' },
        {
          path: '/home',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/profile',
          name: 'profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/rank',
          name: 'rank',
          component: () => import('@/views/RankingView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/history',
          name: 'history',
          component: () => import('@/views/HistoryView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/detail/:id',
          name: 'detail',
          component: () => import('@/views/DetailView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/player/:id',
          name: 'player',
          component: () => import('@/views/PlayerView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/community',
          name: 'community',
          component: () => import('@/views/CommunityView.vue'),
          meta: { requiresAuth: true }
        }
      ]
    },
    { path:'/next',name:'next',component:()=>import('@/views/Next.vue')},
    // 404 兜底
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue') }
  ]
})

// 导航守卫
router.beforeEach((to) => {
  const loggedIn = !!localStorage.getItem('token')
  // 未登录访问受保护页面 → 回登录页
  if (to.meta.requiresAuth && !loggedIn) return '/login'
  // 已登录访问登录页 → 直接进首页
  if (to.name === 'login' && loggedIn) return '/home'
})

export default router
