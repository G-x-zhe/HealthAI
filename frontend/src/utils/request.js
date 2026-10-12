import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一请求实例：baseURL /api 走 vite 代理；AI 问答较慢，超时给到 120 秒
const request = axios.create({
  baseURL: '/api',
  timeout: 120000
})

// 请求拦截：自动携带 JWT（后端 Filter 兼容裸 token 与 Bearer 两种形式）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('healthai_token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

// 响应拦截：后端统一返回 { code, message, data }，code=200 成功
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Business Error'))
  },
  async (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem('healthai_token')
      ElMessage.error('登录已过期，请重新登录')
      const { default: router } = await import('../router')
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
