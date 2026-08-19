import axios from 'axios'
import { ElMessage } from 'element-plus'
import { mockAdapter } from '@/mock'

// 后端地址：部署时通过 VITE_API_BASE_URL 环境变量覆盖（如 https://你的后端域名/api）
const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// 本地 mock 开关：默认关闭（连真实后端 http://localhost:8080）。
// 需要脱离后端独立演示时，在 .env 里设 VITE_USE_MOCK=true 即可。
const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

const request = axios.create({
  baseURL: baseUrl,
  timeout: 10000,
  ...(USE_MOCK ? { adapter: mockAdapter } : {})
})

// 请求拦截器：自动携带 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一处理返回体与错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res.data
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.msg
    if (status === 401) {
      // 未登录 / 过期：清除本地登录态并回登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.error(msg || '未登录或登录已过期，请重新登录')
      import('@/router').then(({ default: router }) => {
        if (router.currentRoute.value.path !== '/login') router.push('/login')
      })
    } else {
      ElMessage.error(msg || error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
