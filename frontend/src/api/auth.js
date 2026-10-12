import request from '../utils/request'

// 用户注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 用户登录，data: { username, password }，返回 data 为 token 字符串
export function login(data) {
  return request.post('/auth/login', data)
}

// 用户登出（token 加入黑名单）
export function logout() {
  return request.post('/auth/logout')
}

// 获取当前登录用户信息
export function getUserInfo() {
  return request.get('/auth/info')
}
