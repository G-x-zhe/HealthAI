import request from '../utils/request'

// 创建学习提醒 { title, content, remindTime, remindType }
export function createRemind(data) {
  return request.post('/remind/create', data)
}

// 我的提醒列表
export function getMyReminds() {
  return request.get('/remind/my')
}

// 取消提醒
export function cancelRemind(remindId) {
  return request.put(`/remind/cancel/${remindId}`)
}
