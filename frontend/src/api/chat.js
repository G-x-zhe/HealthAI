import request from '../utils/request'

// AI 智能问答 { question, sessionId }
export function askQuestion(data) {
  return request.post('/chat/ask', data)
}

// 分页查询当前用户全部对话历史
export function getHistory(params) {
  return request.get('/chat/history', { params })
}

// 按会话查询对话记录
export function getSessionHistory(sessionId, params) {
  return request.get(`/chat/history/session/${sessionId}`, { params })
}

// 查询单条对话详情
export function getChatDetail(chatId) {
  return request.get(`/chat/history/detail/${chatId}`)
}

// 删除整个会话
export function deleteSession(sessionId) {
  return request.delete(`/chat/history/session/${sessionId}`)
}
